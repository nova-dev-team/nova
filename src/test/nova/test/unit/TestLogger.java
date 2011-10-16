package nova.test.unit;

import org.apache.log4j.Logger;
import org.junit.Test;

public class TestLogger {

    static Logger logger = Logger.getLogger(TestLogger.class.getName());

    @Test
    public void test() {
        logger.debug("This is a debug message");
        logger.error("This is an error message");
        logger.fatal("This is a fatal message");
        logger.info("This is an info");
        logger.trace("This is a trace");
    }

}
