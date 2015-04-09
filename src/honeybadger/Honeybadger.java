/*
  @Author: Scott Page
  @Company: StyleSeek
  @Brief: Catches any uncaught exceptions and sends them to honeybadger.
  You must set the following Env Vars
  HONEYBADGER_API_KEY ... Your honeybadger api key found in the settings page
  JAVA_ENV ... probably development or production
*/
package honeybadger;

import java.io.*;
import com.google.gson.*;
import org.apache.commons.lang3.StringUtils;

import java.net.*;
import javax.net.ssl.HttpsURLConnection;

public class Honeybadger implements Thread.UncaughtExceptionHandler {
  private static final String HONEY_BADGER_URL = "https://api.honeybadger.io/v1/notices";
  private final String apiKey;
  private final String envName;

  public Honeybadger() {
    this(System.getenv("HONEYBADGER_API_KEY"), System.getenv("JAVA_ENV"), true);
  }

  /**
   * Construct a new HoneyBadger reporter
   *
   * @param apiKey      Your HoneyBadger API Key
   * @param envName     The environment to log to
   * @param exitIfEmpty Kill the application if the apiKey or envName aren't present
   */
  public Honeybadger(final String apiKey, final String envName, final boolean exitIfEmpty) {
    this.apiKey = apiKey;
    this.envName = envName;

    if (StringUtils.isBlank(this.apiKey) || StringUtils.isBlank(this.envName)) {
      System.out.println("ERROR: API Key and Environment Name must be non-empty.");
      if (exitIfEmpty) {
        System.exit(1);
      }
    } else {
      Thread.setDefaultUncaughtExceptionHandler(this);
    }
  }

  /**
   * Unregister the exception handler.
   */
  public void close() {
    Thread.setDefaultUncaughtExceptionHandler(null);
  }

  public void uncaughtException(Thread thread, Throwable error) {
    reportErrorToHoneyBadger(error);
  }

  /**
   * Send an error to the HoneyBadger API
   *
   * @param error Error to send
   */
  public void reportErrorToHoneyBadger(Throwable error) {
    Gson myGson = new Gson();
    JsonObject jsonError = new JsonObject();
    jsonError.add("notifier", makeNotifier());
    jsonError.add("error", makeError(error));
    /*
      If you need to add more information to your errors add it here
    */
    jsonError.add("server", makeServer());
    for (int retries = 0; retries < 3; retries++) {
      try {
        int responseCode = sendToHoneyBadger(myGson.toJson(jsonError));
        if (responseCode != 201)
          System.err.println("ERROR: Honeybadger did not respond with the correct code. Response was = " + responseCode + " retry=" + retries);
        else {
          System.err.println("Honeybadger logged error correctly:  " + error);
          break;
        }
      } catch (IOException e) {
        System.out.println("ERROR: Honeybadger got an ioexception when trying to send the error retry=" + retries);
      }
    }
  }

  /*
    Identify the notifier
  */
  private JsonObject makeNotifier() {
    JsonObject notifier = new JsonObject();
    notifier.addProperty("name", "Honeybadger-java Notifier");
    notifier.addProperty("version", "1.3.0");
    return notifier;
  }

  /*
    Format the throwable into a json object
  */
  private JsonObject makeError(Throwable error) {
    JsonObject jsonError = new JsonObject();
    jsonError.addProperty("class", error.toString());

    JsonArray backTrace = new JsonArray();
    for (StackTraceElement trace : error.getStackTrace()) {
      JsonObject jsonTraceElement = new JsonObject();
      jsonTraceElement.addProperty("number", trace.getLineNumber());
      jsonTraceElement.addProperty("file", trace.getFileName());
      jsonTraceElement.addProperty("method", trace.getMethodName());
      backTrace.add(jsonTraceElement);
    }
    jsonError.add("backtrace", backTrace);

    return jsonError;
  }

  /*
    Establish the environment
  */
  private JsonObject makeServer() {
    JsonObject jsonServer = new JsonObject();
    jsonServer.addProperty("environment_name", envName);
    return jsonServer;
  }

  /*
    Send the json string error to honeybadger
  */
  private int sendToHoneyBadger(String jsonError) throws IOException {
    URL obj;
    HttpsURLConnection con;
    DataOutputStream wr = null;
    BufferedReader in = null;
    int responseCode = -1;
    try {
      obj = new URL(HONEY_BADGER_URL);
      con = (HttpsURLConnection) obj.openConnection();
      //add request header
      con.setRequestMethod("POST");
      con.setRequestProperty("X-API-Key", apiKey);
      con.setRequestProperty("Content-Type", "application/json");
      con.setRequestProperty("Accept", "application/json");

      // Send post request
      con.setDoOutput(true);
      wr = new DataOutputStream(con.getOutputStream());
      wr.writeBytes(jsonError);
      wr.flush();

      responseCode = con.getResponseCode();

      in = new BufferedReader(
          new InputStreamReader(con.getInputStream()));
      String inputLine;
      StringBuilder response = new StringBuilder();

      while ((inputLine = in.readLine()) != null) {
        response.append(inputLine);
      }
    } catch (MalformedURLException e) {
      System.err.println("ERROR: Bad url " + HONEY_BADGER_URL + " " + e);
      System.exit(1);
    } finally {
      try {
        if (in != null)
          in.close();
        if (wr != null)
          wr.close();
      } catch (Exception e) {
        System.err.println("WARNING: Failure to close honey badger " + e);
      }
    }
    return responseCode;
  }
}