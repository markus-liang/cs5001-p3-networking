/**
 * WebServerMain Class : the main class.
 */
public class WebServerMain {
    /**
     * main method.
     * @param args [root directory] [port].
     */
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java WebServerMain <document_root> <port>");
        } else {
            Server s = new Server(args[0], Integer.parseInt(args[1]));
        }
    }
}
