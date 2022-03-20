package ChartasHttpHandler;

import ImageHandler.Exceptions.IncorrectImageIdException;
import ImageHandler.ImageHandler;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;

public class ChartasHttpHandler implements HttpHandler {

    private final ImageHandler imageHandler;

    public ChartasHttpHandler(String workingDirectory, int maxImagePartWidth,
                              int maxImagePartHeight, String imageExtension) throws IOException {
        imageHandler = new ImageHandler(workingDirectory, maxImagePartWidth, maxImagePartHeight, imageExtension);
    }

    private static void sendRadRequest(HttpExchange httpExchange) throws IOException {
        httpExchange.sendResponseHeaders(HttpStatus.SC_BAD_REQUEST, 0);
    }

    private static void sendNotFound(HttpExchange httpExchange) throws IOException {
        httpExchange.sendResponseHeaders(HttpStatus.SC_NOT_FOUND, 0);
    }

    private static int getImageId(HttpExchange httpExchange) {
        return Integer.parseInt(httpExchange
                .getRequestURI()
                .toString()
                .split("/")[2]);
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        switch (httpExchange.getRequestMethod()) {
            case "GET":
                handleGetRequest(httpExchange);
                break;
            case "POST":
                handlePostRequest(httpExchange);
                break;
            case "DELETE":
                handleDeleteRequest(httpExchange);
                break;
            default:
                sendRadRequest(httpExchange);
        }
    }

    private void handleGetRequest(HttpExchange httpExchange){
        // TODO
    }

    private void handlePostRequest(HttpExchange httpExchange) {
        // TODO
    }

    private void handleDeleteRequest(HttpExchange httpExchange) throws IOException {
        try {
            int imageId = getImageId(httpExchange);
            imageHandler.deleteImage(imageId);
        } catch (NumberFormatException e) {
            sendRadRequest(httpExchange);
        } catch (IncorrectImageIdException e) {
            sendNotFound(httpExchange);
        }
    }
}
