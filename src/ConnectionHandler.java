import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.ArrayList;

/**
 * ConnectionHandler Class : Class to handle the connection.
 */
public class ConnectionHandler extends Thread {
    private Socket conn; // socket representing TCP/IP connection to Client
    private InputStream is; // get data from client on this input stream
    private OutputStream os; // can send data back to the client on this output stream
    private Logger logger;
    private String root;
    private BufferedReader br; // use buffered reader to read client data

    /**
     * ConnectionHandler constructor.
     * @param conn : the connection object.
     * @param root : root directory of the web server.
     * @param logger : logger object.
     */
    public ConnectionHandler(Socket conn, String root, Logger logger) {
        this.conn = conn;
        try {
            this.is = conn.getInputStream();
            this.os = conn.getOutputStream();
            this.br = new BufferedReader(new InputStreamReader(this.is));
            this.logger = logger;
            this.root = root;
        } catch (IOException ioe) {
            System.out.println("ConnectionHandler: " + ioe.getMessage());
        }
    }

    /**
     * Override run method.
     */
    public void run() {
        System.out.println("Thread started .... ");
        try {
            processRequest();
        } catch (Exception e) {
            System.out.println("Error : " + e.getMessage());
            cleanup();
        }
    }

    /**
     * ProcessRquest method : to process each incoming request.
     */
    private void processRequest() throws IOException {
        HashMap<String, String> reqParams = new HashMap<String, String>();

        String line = br.readLine(); // read first line to read request method
        if (line != null) {
            String[] requests = line.split(" "); // split result : [0: METHOD] [1: QUERY] [2: PROTOCOL]
            String method = requests[0];
            String query = requests[1];
            query = query.equals("/") ? "." : query.substring(1); // remove first slash character

            // to parse query such as : index.html?param1=xxx&param2=yyy
            if (query.contains("?")) {
                String[] arrQuery = query.split("\\?"); // index.html ? key1=val1&key2=val2
                query = arrQuery[0].trim(); // index.html

                if (arrQuery.length > 1) {
                    String[] arrParams = arrQuery[1].trim().split("&");
                    for (String temp: arrParams) {
                        String[] keyPair = temp.split("=");
                        if (keyPair.length > 1) {
                            reqParams.put(keyPair[0].trim(), keyPair[1].trim());
                        } else {
                            reqParams.put(keyPair[0].trim(), "");
                        }
                    }
                }

            }
            // here we have got the query and query params

            int contentLength = 0;
            // need to read post data
            if (method.equals("POST")) {
                while ((line = br.readLine()) != null) {
                    System.out.println(line);
                    if (line.contains("Content-Length")) {
                        contentLength = Integer.parseInt((line.split(":"))[1].trim());
                        line = br.readLine(); // get Content-Type after content length, not used,
                        break;
                    }
                }

                int currentLength = 0;
                boolean isProcStarted = false;
                String filename = null;
                ArrayList<String> content = new ArrayList<String>();
                // we got content length, start processing content here

                while ((line = br.readLine()) != null) {
                    System.out.println(line);

                    currentLength += line.getBytes().length + 2; // add 2 for new line

                    if (line.contains("---------")) {
                        if (isProcStarted) { // save previous content
                            System.out.println("save here");
                            FileHandler fw = new FileHandler(filename, content);
                            fw.write();
                        }
                        isProcStarted = false;
                    } else if (line.contains("Content-Disposition")) {
                        // get filename
                        Pattern pattern = Pattern.compile("filename=\"(.*?)\"");
                        Matcher matcher = pattern.matcher(line); // found the param key
                        if (matcher.find()) {
                            filename = matcher.group(1);
                            filename = root + "/" + filename;
                        }
                        content = new ArrayList<String>(); // list of new content
                    } else if (line.contains("Content-Type")) {
                        line = br.readLine(); // blank line after Content-Type
                        currentLength += line.getBytes().length + 2;
                        isProcStarted = true;
                    } else {
                        content.add(line);
                    }

                    if (currentLength >= contentLength) {
                        if (isProcStarted) {
                            FileHandler fw = new FileHandler(filename, content);
                            fw.write();
                        }
                        break;
                    }
                }
            }

            ResponseHandler rh = new ResponseHandler(root, method, query);
            byte[] bytes = rh.getResponse();
            this.logger.log("Rootdir = " + root + " | Request = [" + method + "] " + query + " => " + rh.getResponseStatus());
            this.logger.log("Request Parameters : " + reqParams.size());
            int i = 0;
            for (Map.Entry<String, String> entry : reqParams.entrySet()) {
                this.logger.log("Param-" + i + " : " + entry.getKey() + " | Value-" + i + " : " + entry.getValue());
                i++;
            }
            this.os.write(bytes, 0, bytes.length);
        }
    }

    /**
     * cleanup.
     */
    private void cleanup() {
        System.out.println("Clean up and exit.");
        try {
            this.br.close();
            this.is.close();
            this.conn.close();
        } catch (IOException ioe) {
            System.out.println("Error clean up" + ioe.getMessage());
        }
    }
}
