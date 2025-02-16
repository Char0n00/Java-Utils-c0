package c0.util.logger;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class LoggerTest {
    
    @Before
    public void setup(){
        Log.Init(Log.Type.FILE);
        Log.updateLogLevel("infO", "DEBuG", "warn", "ERror");
        Log.updateLogsDirectory("./");
        Log.updateLogFileName("test");
        File file = new File("./test.log");
        assertTrue(file.exists());
    }

    private void confirmLastLineContainsMessage(String message){
        Log.awaitUntilWriteComplete();
        String lastLine = null;
        try{
            BufferedReader reader = new BufferedReader(new FileReader("./test.log"));
            String line;
            while ((line = reader.readLine()) != null) {
                lastLine = line;
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        assertTrue(lastLine.contains(message));
    }

    @Test
    public void testWritingToFile(){
        Log.info("Test info level enabled");
        confirmLastLineContainsMessage("(INFO) Test info level enabled");

        Log.debug("Test debug level enabled");
        confirmLastLineContainsMessage("(DEBUG) Test debug level enabled");

        Log.warn("Test warn level enabled");
        confirmLastLineContainsMessage("(WARNING) Test warn level enabled");

        Log.error("Test error level enabled");
        confirmLastLineContainsMessage("(ERROR) Test error level enabled");
    }

    @Test
    public void testMessageCollapsing(){
        Log.info("Collapsing test");
        Log.info("Collapsing test");
        confirmLastLineContainsMessage("Collapsing test (x2)");

        Log.info("Collapsing test 1");
        Log.debug("Collapsing test 1");
        Log.info("Collapsing test 1");
        confirmLastLineContainsMessage("Collapsing test 1");
    }

    @After
    public void deconstruct(){
        Log.close();
        File file = new File("./test.log");
        file.delete();
    }

}
