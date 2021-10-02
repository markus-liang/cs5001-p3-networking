import java.util.Date;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;

/**
 * Logger Class : for handling logs.
 */
public class Logger {
    private String logfile;

    /**
     * Logger Constructor.
     * @param logfile the name of the log file.
     */
    public Logger(String logfile) {
        System.out.println("Create LOGGER");
        this.logfile = logfile;
    }

    /**
     * log method : method to be called for loggin.
     * @param msg the message to be logged.
     */
    public void log(String msg) {
        System.out.println("Inside logger : " + msg);
        try (
            BufferedWriter bw = new BufferedWriter(new FileWriter(this.logfile, true));
            PrintWriter pw = new PrintWriter(bw);
        ) {
            Date date = new Date();
            pw.println(date.toString() + " : " + msg);
        } catch (IOException ioe) {
            System.out.println("Error writing log : " + ioe.getMessage());
        }
    }
}
