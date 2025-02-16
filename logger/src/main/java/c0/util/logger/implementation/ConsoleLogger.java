package c0.util.logger.implementation;

import c0.util.logger.Log;

/**
 * Console logging implementation
 */
public final class ConsoleLogger implements Log.Implementation {

    @Override
    public void info(String message, String time) {
        System.out.println(time + " | " + "(INFO) " + message);
    }

    @Override
    public void debug(String message, String time) {
        System.out.println(time + " | " + "(DEBUG) " + message);
    }

    @Override
    public void error(String message, String time) {
        System.out.println(time + " | " + "(ERROR) " + message);
    }

    @Override
    public void warn(String message, String time) {
        System.out.println(time + " | " + "(WARN) " + message);
    }

    @Override
    public void close() {

    }

}
