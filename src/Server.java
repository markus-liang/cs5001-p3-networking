import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Server Class : The server class.
 */
public class Server {
    private static String logfile = "logs.txt";
    private ServerSocket ss;
    private Logger logger;

    /**
     * Server constructor.
     * @param root : root directory of the web server.
     * @param port : port to listen.
     */
    public Server(String root, int port) {
        try {
            logger = new Logger(this.logfile);
            File dir = new File(root);
            if (!dir.isDirectory()) {
                throw new IOException("Invalid root folder");
            }

            ss = new ServerSocket(port);
            System.out.println("Server listening on port " + port + " & Root : " + root);

            while (true) {
                Socket conn = ss.accept();
                System.out.println("Server got new connection request from " + conn.getInetAddress());

                ConnectionHandler ch = new ConnectionHandler(conn, root, logger); 
                ch.start();
            }
        } catch (IOException ioe) {
            System.out.println("IO Exception : " + ioe.getMessage());
        } catch (Exception e) {
            System.out.println("General Exception : " + e.getMessage());
        }
    }
}
