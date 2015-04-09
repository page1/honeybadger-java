/**
 * Created by lbuckley on 4/8/15.
 */

package honeybadger;

import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;
import org.apache.log4j.varia.NullAppender;
import org.apache.log4j.Level;

public class HoneybadgerAppender extends NullAppender {

    private Honeybadger honeybadger;

    public HoneybadgerAppender() {
            honeybadger = new Honeybadger();
    }

    @Override
    public void doAppend(LoggingEvent event) {
        if (event.getLevel() == Level.ERROR) {
            Throwable error;
            ThrowableInformation errorInfo = event.getThrowableInformation();
            if (errorInfo != null) {
                error = errorInfo.getThrowable();
            }
            else {
                error = new Exception(event.getRenderedMessage());
            }
            if (error != null) {
                honeybadger.reportErrorToHoneyBadger(error);
            }
        }
    }
}