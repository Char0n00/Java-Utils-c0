package c0.util.logger;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import c0.util.logger.implementation.ConsoleLogger;
import c0.util.logger.implementation.FileLogger;

/**
 * Logger service
 */
public final class Log {
    
    // Singleton
    private static Log instance;
    
    public static Log Init(Type type){
        if(instance == null){
            instance = new Log(type);
        }
        return instance;
    }


    /**
     * Implementation requirements for a concrete logger
     */
    public interface Implementation {

        void info(String message, String time);

        void debug(String message, String time);

        void error(String message, String time);

        void warn(String message, String time);

        void close();
    }

    /**
     * Available logger types
     */
    public enum Type {
        CONSOLE,
        FILE;
    }

    private Log.Implementation logger;
    private Type type;
    private Collection<String> enabledLogs;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final String TIME_PATTERN = "HH:mm:ss dd-MM-yyyy";
    
    private Log(Type type) {
        this.type = type;
        switch(type){
            case CONSOLE -> {
                this.logger = new ConsoleLogger();
            }
            case FILE -> {
                LocalDate now = LocalDate.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                String formattedTimestamp = now.format(formatter);
                logger = new FileLogger(formattedTimestamp + ".log");
            }
        }
        this.enabledLogs = new HashSet<>();
        this.enabledLogs.add("info");
    }

    static public void compressFile(String path){
        if (instance.type.equals(Log.Type.FILE) && instance.logger instanceof FileLogger) {
            ((FileLogger) instance.logger).compress(List.of(path));
        }
    }

    static public void updateLogsDirectory(String path){
        if(instance.type.equals(Log.Type.FILE) && instance.logger instanceof FileLogger){
            ((FileLogger)instance.logger).updateLogFilePath(path);
        }
    }

    static public void updateLogFileName(String name) {
        if (instance.type.equals(Log.Type.FILE) && instance.logger instanceof FileLogger) {
            ((FileLogger) instance.logger).updateLogFileName(name);
        }
    }

    static public void awaitUntilWriteComplete() {
        if (instance.type.equals(Log.Type.FILE) && instance.logger instanceof FileLogger) {
            try{
                instance.lastFuture.get();
            } catch (InterruptedException e){
                e.printStackTrace();
            } catch (ExecutionException e){
                e.printStackTrace();
            }
        }
    }

    private Future<?> lastFuture;

    // Main used methods
    public static void info(String message) {
        if (instance.enabledLogs.contains("info")) {
            String time = Log.Utils.captureTime(TIME_PATTERN);
            instance.lastFuture = instance.executor.submit(() -> {
                instance.logger.info(message, time);
            });
        }
    }

    public static void debug(String message) {
        if (instance.enabledLogs.contains("debug")) {
            String time = Log.Utils.captureTime(TIME_PATTERN);
            instance.lastFuture = instance.executor.submit(() -> {
                instance.logger.debug(message, time);
            });
        }
    }

    public static void error(String message) {
        if (instance.enabledLogs.contains("error")) {
            String time = Log.Utils.captureTime(TIME_PATTERN);
            instance.lastFuture = instance.executor.submit(() -> {
                instance.logger.error(message, time);
            });
        }
    }

    public static void warn(String message) {
        if (instance.enabledLogs.contains("warn")) {
            String time = Log.Utils.captureTime(TIME_PATTERN);
            instance.lastFuture = instance.executor.submit(() -> {
                instance.logger.warn(message, time);
            });
        }
    }

    // Utility methods
    static public void updateLogLevel(String... enabledLogs) {
        if (instance.enabledLogs == null) {
            instance.enabledLogs = new HashSet<>();
        
        }
        instance.enabledLogs.clear();
        if(enabledLogs == null){
            return;
        }
        instance.enabledLogs.addAll(Arrays.stream(enabledLogs).map(String::toLowerCase).collect(Collectors.toSet()));
    }

    static public void close() {
        instance.logger.close();
        instance.logger = null;
        instance.enabledLogs = null;
        instance.type = null;
        instance = null;
    }

    public static class Utils{
        
        static private final String DEFAULT_TIME_PATTERN = "HH:mm:ss dd-MM-yyyy";

        /**
         * Util function to capture and get the current time at time of call. Formats the returned time string to the passed time regex pattern
         * @param pattern String pattern to format time to
         * @return String formatted time
         */
        static public String captureTime(String pattern){
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter;

            if(pattern != null)
                formatter = DateTimeFormatter.ofPattern(pattern);
            else 
                formatter = DateTimeFormatter.ofPattern(DEFAULT_TIME_PATTERN);

            return now.format(formatter);
        }

    }

}
