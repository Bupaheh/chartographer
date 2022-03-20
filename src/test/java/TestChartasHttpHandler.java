import ChartasHttpHandler.ChartasHttpHandler;
import com.squareup.okhttp.*;
import com.sun.net.httpserver.HttpServer;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

public class TestChartasHttpHandler {

    private static final int port = 8080;
    private static final String requestPath = "/chartas";
    private static final String workingDirectory = "testData";
    private static final int maxImagePartWidth = 540;
    private static final int maxImagePartHeight = 10;
    private static final String imageExtension = "bmp";

    private final String testAnswersDir = "src/test/resources/TestAnswers";
    private final String smallImagePath = "src/test/resources/small-one.bmp";
    private final String largeImagePath = "src/test/resources/lena_512.bmp";

    private HttpServer server;

    private String getAnswerPath(int ansId) {
        return testAnswersDir + "/" + ansId + "." + imageExtension;
    }

    @AfterEach
    public void clean() throws IOException {
        server.stop(0);
        FileUtils.deleteDirectory(new File(workingDirectory));
    }

    @BeforeEach
    public void startUp() throws IOException {
        new File(workingDirectory).mkdirs();
        server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext(requestPath, new ChartasHttpHandler(workingDirectory,
                maxImagePartWidth, maxImagePartHeight, imageExtension));
        server.start();
    }

    private static class CreateResponse {
        public Integer imageId;
        public int responseCode;

        public CreateResponse(Integer imageId, int responseCode) {
            this.imageId = imageId;
            this.responseCode = responseCode;
        }
    }

    private static class SubImageResponse {
        public byte[] subImage;
        public int responseCode;

        public SubImageResponse(byte[] subImage, int responseCode) {
            this.subImage = subImage;
            this.responseCode = responseCode;
        }
    }

    private static CreateResponse createImage (int width, int height) throws IOException {
        OkHttpClient client = new OkHttpClient();
        RequestBody reqBody = RequestBody.create(null, new byte[0]);
        Request request = new Request.Builder()
                .url("http://localhost:" + port + "/chartas/?width=" + width + "&height=" + height)
                .post(reqBody)
                .build();
        Response response = client.newCall(request).execute();

        int responseCode = response.code();

        if (response.isSuccessful()) {
            String imageId = response.body().string();
            return new CreateResponse(Integer.parseInt(imageId), responseCode);
        } else {
            return new CreateResponse(null, responseCode);
        }
    }

    private static int deleteImage(int imageId) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://localhost:" + port + "/chartas/" + imageId)
                .delete()
                .build();
        Response response = client.newCall(request).execute();

        return response.code();
    }

    private static SubImageResponse getSubImage(int imageId, int x, int y, int width, int height) throws IOException {
        String query = "/?x=" + x + "&y=" + y + "&width=" + width + "&height=" + height;

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://localhost:" + port + "/chartas/" + imageId + query)
                .build();
        Response response = client.newCall(request).execute();

        int responseCode = response.code();

        if (response.isSuccessful()) {
            byte[] subImage = response.body().bytes();
            return new SubImageResponse(subImage, responseCode);
        } else {
            return new SubImageResponse(null, responseCode);
        }
    }

    private static int drawImage(int imageId, int x, int y, int width, int height, String filePath) throws IOException {
        String query = "/?x=" + x + "&y=" + y + "&width=" + width + "&height=" + height;
        File sourceFile = new File(filePath);
        byte[] imageBytes = Files.readAllBytes(sourceFile.toPath());
        RequestBody reqBody = RequestBody.create(null, imageBytes);

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://localhost:" + port + "/chartas/" + imageId + query)
                .post(reqBody)
                .build();
        Response response = client.newCall(request).execute();

        return response.code();
    }

    @Test
    public void createTest() throws IOException {
        CreateResponse response = createImage(10, 10);

        assertAll(() -> {
            assertEquals(HttpStatus.SC_CREATED, response.responseCode);
            assertEquals(0, response.imageId);
        });
    }

    @Test
    public void incorrectImageSizeCreateTest() throws IOException {
        CreateResponse response = createImage(0, 10);

        assertEquals(HttpStatus.SC_BAD_REQUEST, response.responseCode);
    }

    @Test
    public void deleteTest() throws IOException {
        int imageId = createImage(10, 20).imageId;
        int responseCode = deleteImage(imageId);
        assertEquals(HttpStatus.SC_OK, responseCode);
    }

    @Test
    public void incorrectIdDeleteTest() throws IOException {
        int responseCode = deleteImage(10);
        assertEquals(HttpStatus.SC_NOT_FOUND, responseCode);
    }

    @Test
    public void getEmptySubImageTest() throws IOException {
        int imageId = createImage(10, 20).imageId;
        SubImageResponse response = getSubImage(imageId, 0, 0, 10, 20);
        File ansFile = new File(getAnswerPath(17));
        byte[] ans = Files.readAllBytes(ansFile.toPath());

        assertAll(() -> {
            assertEquals(HttpStatus.SC_OK, response.responseCode);
            assertArrayEquals(ans, response.subImage);
        });
    }

    @Test
    public void incorrectIdSubImageTest() throws IOException {
        SubImageResponse response = getSubImage(10, 0, 0, 10, 20);

        assertEquals(HttpStatus.SC_NOT_FOUND, response.responseCode);
    }

    @Test
    public void deletedImageSubImageTest() throws IOException {
        int imageId = createImage(10, 10).imageId;
        int responseCode1 = getSubImage(imageId, 0, 0, 10, 10).responseCode;

        deleteImage(imageId);

        int responseCode2 = getSubImage(imageId, 0, 0, 10, 10).responseCode;

        assertAll(() -> {
            assertEquals(HttpStatus.SC_OK, responseCode1);
            assertEquals(HttpStatus.SC_NOT_FOUND, responseCode2);
        });
    }

    @Test
    public void drawImageTest() throws IOException {
        int imageId = createImage(7, 20).imageId;

        drawImage(imageId, -1, 9, 3, 2, smallImagePath);

        byte[] subImage = getSubImage(imageId, -3, 7, 10, 10).subImage;
        File ansFile = new File(getAnswerPath(18));
        byte[] ans = Files.readAllBytes(ansFile.toPath());

        assertArrayEquals(ans, subImage);
    }

    @Test
    public void consecutiveCallsDrawImageTest() throws IOException {
        int imageId = createImage(7, 20).imageId;

        drawImage(imageId, -1, 9, 3, 2, smallImagePath);
        drawImage(imageId, 1, 8, 3, 2, smallImagePath);
        drawImage(imageId, 0, 10, 3, 2, smallImagePath);
        drawImage(imageId, 5, 15, 3, 2, smallImagePath);

        byte[] subImage = getSubImage(imageId, -3, 7, 10, 10).subImage;
        File ansFile = new File(getAnswerPath(19));
        byte[] ans = Files.readAllBytes(ansFile.toPath());

        assertArrayEquals(ans, subImage);
    }

    @Test
    public void incorrectIdDrawImageTest() throws IOException {
        createImage(7, 20);
        int responseCode = drawImage(10, -1, 9, 3, 2, smallImagePath);

        assertEquals(HttpStatus.SC_NOT_FOUND, responseCode);
    }

    @Test
    public void incorrectRegionDrawImageTest() throws IOException {
        int imageId = createImage(7, 20).imageId;
        int responseCode = drawImage(imageId, 7, 9, 3, 2, smallImagePath);

        assertEquals(HttpStatus.SC_BAD_REQUEST, responseCode);
    }

    @Test
    public void largeSubImageDrawImageTest() throws IOException {
        int imageId = createImage(540, 540).imageId;

        drawImage(imageId, 3, 3, 512, 512, largeImagePath);
        byte[] subImage = getSubImage(imageId, 3, 3, 512, 512).subImage;

        File ansFile = new File(largeImagePath);
        byte[] ans = Files.readAllBytes(ansFile.toPath());

        assertArrayEquals(ans, subImage);
    }

}
