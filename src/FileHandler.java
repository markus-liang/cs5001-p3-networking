import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * FileHandler Class : to handle posted file.
 */
public class FileHandler {
    private String filename;
    private ArrayList content;

    /**
     * FileHandler constructor.
     * @param filename : destination file name.
     * @param content : content of the file to be saved.
     */
    FileHandler(String filename, ArrayList content) {
        this.filename = filename;
        this.content = content;
    }

    /**
     * Write method.
     */
    public void write() {
        try (
            BufferedWriter bw = new BufferedWriter(new FileWriter(filename));
            PrintWriter pw = new PrintWriter(bw);
        ) {
            for (int i = 0; i < content.size(); i++) {
                pw.println(content.get(i));
            }
        } catch (IOException ioe) {
            System.err.println(ioe.getMessage());
        }
    }
}
