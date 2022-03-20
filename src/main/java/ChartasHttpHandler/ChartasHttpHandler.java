package ChartasHttpHandler;

import ImageHandler.Exceptions.IncorrectImageIdException;
import ImageHandler.Exceptions.IncorrectImageRegionException;
import ImageHandler.ImageHandler;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ChartasHttpHandler implements HttpHandler {

    private final ImageHandler imageHandler;

    public ChartasHttpHandler(String workingDirectory, int maxImagePartWidth,
                              int maxImagePartHeight, String imageExtension) throws IOException {
        imageHandler = new ImageHandler(workingDirectory, maxImagePartWidth, maxImagePartHeight, imageExtension);
    }

    private static void sendBadRequest(HttpExchange httpExchange) throws IOException {
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

    private static Map<String, Integer> getParams(HttpExchange httpExchange) {
        URI uri = httpExchange.getRequestURI();
        List<NameValuePair> params = URLEncodedUtils.parse(uri, StandardCharsets.UTF_8);

        return params.stream().collect(
                Collectors.toMap(NameValuePair::getName, (NameValuePair el) -> {
                    String value = el.getValue();
                    return Integer.parseInt(value);
                }));
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
                sendBadRequest(httpExchange);
        }
    }

    private void handleGetRequest(HttpExchange httpExchange) throws IOException {
        try {
            int imageId = getImageId(httpExchange);
            Map<String, Integer> params = getParams(httpExchange);

            if (!(params.containsKey("x") && params.containsKey("y") &&
                    params.containsKey("width") && params.containsKey("height"))) {
                sendBadRequest(httpExchange);
                return;
            }

            byte[] subImage = imageHandler.getSubImage(imageId, params.get("x"),
                    params.get("y"), params.get("width"), params.get("height"));
            OutputStream outputStream = httpExchange.getResponseBody();

            httpExchange.sendResponseHeaders(200, subImage.length);
            outputStream.write(subImage);
            outputStream.close();
        } catch (IncorrectImageIdException e) {
            sendNotFound(httpExchange);
        } catch (IncorrectImageRegionException e) {
            sendBadRequest(httpExchange);
        }
    }

    private void handlePostRequest(HttpExchange httpExchange) {
        // TODO
    }

    private void handleDeleteRequest(HttpExchange httpExchange) throws IOException {
        try {
            int imageId = getImageId(httpExchange);
            imageHandler.deleteImage(imageId);
        } catch (NumberFormatException e) {
            sendBadRequest(httpExchange);
        } catch (IncorrectImageIdException e) {
            sendNotFound(httpExchange);
        }
    }
}
