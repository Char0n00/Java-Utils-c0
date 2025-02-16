package c0.util.logger.implementation;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import c0.util.logger.Log;

/**
 * Implementation of a file logger. It writes log files to a specified directory
 */
public final class FileLogger implements Log.Implementation {

    private String LOG_FILE_FOLDER = "./data/logs/";
    private String LOG_FILE;

    private FileWriter writer;
    private RandomAccessFile randomAccessFile;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private String lastMessage = " ";
    private boolean collapse = true;
    private int sameCount = 1;

    public FileLogger(String fileName) {
        if(fileName == null){
            throw new IllegalArgumentException("Log file name cannot be null.");
        }
        this.LOG_FILE = fileName;
        try{
            initializeFileWriter(LOG_FILE_FOLDER + LOG_FILE);
        } catch(IOException e){
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void close(){
        Log.awaitUntilWriteComplete();
        try{
            writer.close();
            randomAccessFile.close();
        } catch (IOException e){

        }
        executor.shutdownNow();
        try{
            executor.awaitTermination(3000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e){
            e.printStackTrace();
        }
        
    }

    private void initializeFileWriter(String fileName) throws IOException {
        File file = new File(fileName);
        if(file.exists()){
            this.writer = new FileWriter(fileName, true);
            this.randomAccessFile = new RandomAccessFile(fileName, "rw");
        }
        else{
            this.writer = new FileWriter(fileName);
            this.randomAccessFile = new RandomAccessFile(fileName, "rw");
        }
        log("------------------------------- SESSION START -------------------------------");
    }

    private String handleSameMessage(String message) {
        final String messageToThread = message;
        if (lastMessage.equals(message)) {
            sameCount++;
            Future<?> future = executor.submit(() -> incrementLastLogMessage(messageToThread));
            try {
                message = (String) future.get();
            } catch (InterruptedException | ExecutionException e) {
                Thread.currentThread().interrupt();
            }
        } else {
            sameCount = 1;
            lastMessage = message;
        }
        return message;
    }

    /**
     * Helper function to "collapse" log messages in log files. Reads the last log
     * message written to file and updates the time to the last messages, and
     * increments a count of logged messages. Called if the last log message was the
     * same as the incoming one.
     * @param String message to add the increment to
     * @return 
     */
    private String incrementLastLogMessage(String message) {
        try {
            long fileLength = randomAccessFile.length();
            long pointer = fileLength - 1;
            
            // Read backwards to find the beginning of the last line
            while (pointer >= 0) {
                randomAccessFile.seek(pointer); // Keep setting pointer to the last character which is not a new line character
                char c = (char) randomAccessFile.read();
            
                if (c == '\n' || c == '\r') {
                    break;
                }
                
                pointer--;
            }
            
            // Remove last line
            randomAccessFile.setLength(pointer >= 0 ? pointer : 0);
            
        } catch (IOException e) {
            e.printStackTrace();
        }
        return message + " (x" + sameCount + ")";
    }

    private void log(String message){
        try{
            this.writer.write("\n" + message);
            this.writer.flush();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public String getLogFilePath(){
        return LOG_FILE_FOLDER + LOG_FILE;
    }

    public void updateLogFilePath(String path){
        File oldFile = new File(getLogFilePath());
        oldFile.delete();
        this.LOG_FILE_FOLDER = path;
        try{
            initializeFileWriter(getLogFilePath());
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public void updateLogFileName(String newName){
        File oldFile = new File(getLogFilePath());
        oldFile.delete();
        this.LOG_FILE = newName + ".log";
        try {
            initializeFileWriter(getLogFilePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void compress(List<String> files){
        
    }

    @Override
    public void warn(String message, String time) {
        message = "(WARNING) " + message;
        if (collapse) {
            message = handleSameMessage(message);
        }
        //message = addDateTime(message);
        log(time + " | " + message);
    }

    @Override
    public void error(String message, String time) {
        message = "(ERROR) " + message;
        if (collapse) {
            message = handleSameMessage(message);
        }
        //message = addDateTime(message);
        log(time + " | " + message);
    }

    @Override
    public void info(String message, String time) {
        message = "(INFO) " + message;
        if (collapse) {
            message = handleSameMessage(message);
        }
        //message = addDateTime(message);
        log(time + " | " + message);
    }

    @Override
    public void debug(String message, String time) {
        message = "(DEBUG) " + message;
        if (collapse) {
            message = handleSameMessage(message);
        }
        //message = addDateTime(message);
        log(time + " | " + message);
    }

}
