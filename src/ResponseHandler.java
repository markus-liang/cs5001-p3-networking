import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * ResponseHandler Class.
 */
public class ResponseHandler {
    static final String PROTOCOL = "HTTP/1.1";
    static final String HTTP_STATUS_OK = "200 OK";
    static final String HTTP_STATUS_NOT_FOUND = "404 Not Found";
    static final String HTTP_STATUS_NOT_IMPLEMENTED = "501 Not Implemented";

    private String root;
    private String method;
    private String query;
    private String headerStatus;
    private byte[] content;
    private String contentType;

    /**
     * ResponseHandler constructor.
     * @param root : root directory of the web server.
     * @param method : request method.
     * @param query : the page requested.
     */
    public ResponseHandler(String root, String method, String query) {
        this.root = root;
        this.method = method;
        this.query = this.root + "/" + query;
        this.contentType = "text/html"; // default type
    }

    /**
     * getResponse method, to get the response that will be delivered to user.
     * @return the array of bytes response, ready to be delivered to user.
     * @throws IOException - IO exception.
     */
    public byte[] getResponse() throws IOException {
        System.out.println("Inside getResponse");

        File f = new File(this.query);
        if (!f.isFile()) { // invalid path
            this.headerStatus = this.PROTOCOL + " " + this.HTTP_STATUS_NOT_FOUND;
            this.content = this.headerStatus.getBytes();
        } else if (!this.isValidMethod()) { // unimplemented method
            this.headerStatus = this.PROTOCOL + " " + this.HTTP_STATUS_NOT_IMPLEMENTED;
            this.content = this.headerStatus.getBytes();
        } else { // valid request
            // cek image extentions, other than GIF, JPEG, PNG would be handled as text/html
            String[] splittedQuery = this.query.split("/");
            String filename = splittedQuery[splittedQuery.length - 1];
            String[] splittedFilename = filename.split("\\.");

            this.headerStatus = this.PROTOCOL + " " + this.HTTP_STATUS_OK;

            if (
                splittedFilename.length > 1
                && (
                    splittedFilename[splittedFilename.length - 1].compareToIgnoreCase("JPEG") == 0
                    || splittedFilename[splittedFilename.length - 1].compareToIgnoreCase("JPG") == 0
                    || splittedFilename[splittedFilename.length - 1].compareToIgnoreCase("GIF") == 0
                    || splittedFilename[splittedFilename.length - 1].compareToIgnoreCase("PNG") == 0
                )
            ) { // treat as binary image
                this.contentType = "image/" + splittedFilename[splittedFilename.length - 1].toLowerCase();
            }

            // load file content
            System.out.println("Read content from  : " + this.query);
            System.out.println("Read path  : " + Paths.get(this.query).toString());
            this.content = Files.readAllBytes(Paths.get(this.query));
        }

        return this.formatResponse();
    }

    /**
     * getResponseStatus method to be called from server class, for logging purpose.
     * @return response header status.
     */
    public String getResponseStatus() { // this is just for logging purpose
        return this.headerStatus;
    }

    private byte[] formatResponse() {
        System.out.println("Inside formatResponse");
        System.out.println("header statues: " + this.headerStatus);
        System.out.println("content : " + this.content);
        System.out.println("contentType : " + this.contentType);

        byte[] header =
            (this.headerStatus + "\n"
            + "Content-Length: " + this.content.length + "\n"
            + "Content-Type: " + this.contentType + "\n\n").getBytes();

        if (this.method.equals("HEAD")) {
            return header;
        } else {
            return joinByteArray(header, this.content);
        }
    }

    private byte[] joinByteArray(byte[] arr1, byte[] arr2) {
        byte[] result = new byte[arr1.length + arr2.length];

        for (int i = 0; i < arr1.length; i++) {
            result[i] = arr1[i];
        }

        for (int i = 0; i < arr2.length; i++) {
            result[arr1.length + i] = arr2[i];
        }

        return result;
    }

    private boolean isValidMethod() {
        return this.method.equals("HEAD")
        || this.method.equals("GET")
        || this.method.equals("POST");
    }
}
