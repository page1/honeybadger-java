
package honeybadger;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;

public class HoneybadgerAppender extends AppenderSkeleton {

  private Honeybadger honeybadger;
  private String honeybadgerAPIKey;
  private String javaEnv;

  public String getJavaEnv() {
    return javaEnv;
  }

  public void setJavaEnv(String javaEnv) {
    this.javaEnv = javaEnv;
  }


  public String getHoneybadgerAPIKey() {
    return honeybadgerAPIKey;
  }

  public void setHoneybadgerAPIKey(String key) {
    honeybadgerAPIKey = key;
  }

  @Override
  protected void append(LoggingEvent event) {
    ThrowableInformation errorInfo = event.getThrowableInformation();
    honeybadger.reportErrorToHoneyBadger(errorInfo == null ?
        new Exception(event.getRenderedMessage()) :
        errorInfo.getThrowable());
  }

  @Override
  public void close() {
    honeybadger.close();
  }

  @Override
  public boolean requiresLayout() {
    return false;
  }

  public void activateOptions() {
    honeybadger = new Honeybadger(honeybadgerAPIKey, javaEnv, false);
  }
}