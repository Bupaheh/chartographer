import ImageHandler.Exceptions.IncorrectImageIdException;
import ImageHandler.Exceptions.IncorrectImageRegionException;
import ImageHandler.ImageHandler;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public class TestImageHandler {

    private final String workingDir = "testData";
    private final String imageExtension = "bmp";
    private final String smallImagePath = "src/test/resources/small-one.bmp";
    private final String largeImagePath = "src/test/resources/lena_512.bmp";
    private final String testAnswersDir = "src/test/resources/TestAnswers";

    private String getImagePartPath(int imageId, int imagePart) {
        return workingDir + "/" + imageId + "/" + imagePart + "." + imageExtension;
    }

    private String getAnswerPath(int ansId) {
        return testAnswersDir + "/" + ansId + "." + imageExtension;
    }

    @AfterEach
    public void clean() throws IOException {
        FileUtils.deleteDirectory(new File(workingDir));
    }

    @BeforeEach
    public void createWorkingDir() {
        new File(workingDir).mkdirs();
    }

    @Test
    public void singlePartCreateTest() throws IOException {
        ImageHandler imageHandler = new ImageHandler(workingDir, 10, 10, imageExtension);
        int imageId = imageHandler.createImage(10, 10);
        File imageDir = new File(workingDir + "/" + imageId);
        assertEquals(1, Objects.requireNonNull(imageDir.listFiles()).length);
    }

    @Test
    public void multiplePartsCreateTest() throws IOException {
        ImageHandler imageHandler = new ImageHandler(workingDir, 10, 10, imageExtension);
        int imageId = imageHandler.createImage(10, 64);
        File imageDir = new File(workingDir + "/" + imageId);
        assertEquals(7, Objects.requireNonNull(imageDir.listFiles()).length);
    }

    @Test
    public void deleteTest() throws IOException {
        ImageHandler imageHandler = new ImageHandler(workingDir, 10, 10, imageExtension);
        int imageId = imageHandler.createImage(20, 20);
        imageHandler.deleteImage(imageId);
        assertFalse(new File(workingDir + "/" + imageId).exists());
    }

    @Test
    public void incorrectIdDeleteTest() throws IOException {
        ImageHandler imageHandler = new ImageHandler(workingDir, 10, 10, imageExtension);
        int imageId = imageHandler.createImage(20, 20);
        assertThrowsExactly(IncorrectImageIdException.class, () -> imageHandler.deleteImage(imageId + 1));
    }

    @Test
    public void inSinglePartDrawImageTest() throws IOException {
        ImageHandler imageHandler = new ImageHandler(workingDir, 100, 100, imageExtension);
        int imageId = imageHandler.createImage(7, 7);
        File imageFile = new File(smallImagePath);
        InputStream inputStream = new FileInputStream(imageFile);

        imageHandler.drawImage(imageId, 1, 2, 3, 2, inputStream);

        File ansFile = new File(getAnswerPath(0));
        File resFile = new File(getImagePartPath(imageId, 0));

        assertTrue(FileUtils.contentEquals(ansFile, resFile));
    }

    @Test
    public void inMultiplePartsDrawImageTest() throws IOException {
        ImageHandler imageHandler = new ImageHandler(workingDir, 10, 10, imageExtension);
        int imageId = imageHandler.createImage(7, 20);
        File imageFile = new File(smallImagePath);
        InputStream inputStream = new FileInputStream(imageFile);

        imageHandler.drawImage(imageId, 1, 9, 3, 2, inputStream);

        File ansFile1 = new File(getAnswerPath(1));
        File ansFile2 = new File(getAnswerPath(2));
        File resFile1 = new File(getImagePartPath(imageId, 0));
        File resFile2 = new File(getImagePartPath(imageId, 1));

        assertAll(() -> {
            assertTrue(FileUtils.contentEquals(ansFile1, resFile1));
            assertTrue(FileUtils.contentEquals(ansFile2, resFile2));
        });
    }

    @Test
    public void notWholeDrawImageTest1() throws IOException {
        ImageHandler imageHandler = new ImageHandler(workingDir, 10, 10, imageExtension);
        int imageId = imageHandler.createImage(7, 20);
        File imageFile = new File(smallImagePath);
        InputStream inputStream = new FileInputStream(imageFile);

        imageHandler.drawImage(imageId, -1, 9, 3, 2, inputStream);

        File ansFile1 = new File(getAnswerPath(3));
        File ansFile2 = new File(getAnswerPath(4));
        File resFile1 = new File(getImagePartPath(imageId, 0));
        File resFile2 = new File(getImagePartPath(imageId, 1));

        assertAll(() -> {
            assertTrue(FileUtils.contentEquals(ansFile1, resFile1));
            assertTrue(FileUtils.contentEquals(ansFile2, resFile2));
        });
    }

    @Test
    public void notWholeDrawImageTest2() throws IOException {
        ImageHandler imageHandler = new ImageHandler(workingDir, 10, 10, imageExtension);
        int imageId = imageHandler.createImage(7, 20);
        File imageFile = new File(smallImagePath);
        InputStream inputStream = new FileInputStream(imageFile);

        imageHandler.drawImage(imageId, 6, 9, 3, 2, inputStream);

        File ansFile1 = new File(getAnswerPath(5));
        File ansFile2 = new File(getAnswerPath(6));
        File resFile1 = new File(getImagePartPath(imageId, 0));
        File resFile2 = new File(getImagePartPath(imageId, 1));

        assertAll(() -> {
            assertTrue(FileUtils.contentEquals(ansFile1, resFile1));
            assertTrue(FileUtils.contentEquals(ansFile2, resFile2));
        });
    }

    @Test
    public void notWholeDrawImageTest3() throws IOException {
        ImageHandler imageHandler = new ImageHandler(workingDir, 10, 10, imageExtension);
        int imageId = imageHandler.createImage(7, 20);
        File imageFile = new File(smallImagePath);
        InputStream inputStream = new FileInputStream(imageFile);

        imageHandler.drawImage(imageId, -1, -1, 3, 2, inputStream);

        File ansFile1 = new File(getAnswerPath(7));
        File ansFile2 = new File(getAnswerPath(8));
        File resFile1 = new File(getImagePartPath(imageId, 0));
        File resFile2 = new File(getImagePartPath(imageId, 1));

        assertAll(() -> {
            assertTrue(FileUtils.contentEquals(ansFile1, resFile1));
            assertTrue(FileUtils.contentEquals(ansFile2, resFile2));
        });
    }

    @Test
    public void notWholeDrawImageTest4() throws IOException {
        ImageHandler imageHandler = new ImageHandler(workingDir, 10, 10, imageExtension);
        int imageId = imageHandler.createImage(7, 20);
        File imageFile = new File(smallImagePath);
        InputStream inputStream = new FileInputStream(imageFile);

        imageHandler.drawImage(imageId, 5, 19, 3, 2, inputStream);

        File ansFile1 = new File(getAnswerPath(9));
        File ansFile2 = new File(getAnswerPath(10));
        File resFile1 = new File(getImagePartPath(imageId, 0));
        File resFile2 = new File(getImagePartPath(imageId, 1));

        assertAll(() -> {
            assertTrue(FileUtils.contentEquals(ansFile1, resFile1));
            assertTrue(FileUtils.contentEquals(ansFile2, resFile2));
        });
    }

    @Test
    public void incorrectIdDrawImageTest() throws IOException {
        ImageHandler imageHandler = new ImageHandler(workingDir, 10, 10, imageExtension);
        int imageId = imageHandler.createImage(7, 20);
        File imageFile = new File(smallImagePath);
        InputStream inputStream = new FileInputStream(imageFile);

        assertThrowsExactly(IncorrectImageIdException.class, () ->
                imageHandler.drawImage(imageId + 1, 5, 19, 3, 2, inputStream));
    }

    @Test
    public void incorrectRegionDrawImageTest() throws IOException {
        ImageHandler imageHandler = new ImageHandler(workingDir, 10, 10, imageExtension);
        int imageId = imageHandler.createImage(7, 20);
        File imageFile = new File(smallImagePath);
        InputStream inputStream = new FileInputStream(imageFile);

        assertThrowsExactly(IncorrectImageRegionException.class, () ->
                imageHandler.drawImage(imageId, 100, 100, 3, 2, inputStream));
    }

    @Test
    public void inSinglePartGetSubImageTest() throws IOException {
        ImageHandler imageHandler = new ImageHandler(workingDir, 100, 100, imageExtension);
        int imageId = imageHandler.createImage(7, 7);
        File imageFile = new File(smallImagePath);
        InputStream inputStream = new FileInputStream(imageFile);

        imageHandler.drawImage(imageId, 1, 2, 3, 2, inputStream);

        byte[] res = imageHandler.getSubImage(imageId, 0, 1, 5, 4);
        File ansFile = new File(getAnswerPath(11));

        assertArrayEquals(FileUtils.readFileToByteArray(ansFile), res);
    }

    @Test
    public void inMultiplePartsGetSubImageTest() throws IOException {
        ImageHandler imageHandler = new ImageHandler(workingDir, 10, 10, imageExtension);
        int imageId = imageHandler.createImage(7, 20);
        File imageFile = new File(smallImagePath);
        InputStream inputStream = new FileInputStream(imageFile);

        imageHandler.drawImage(imageId, 1, 9, 3, 2, inputStream);

        byte[] res = imageHandler.getSubImage(imageId, 0, 8, 5, 4);
        File ansFile = new File(getAnswerPath(12));

        assertArrayEquals(FileUtils.readFileToByteArray(ansFile), res);
    }

    @Test
    public void notWholeSubImageTest1() throws IOException {
        ImageHandler imageHandler = new ImageHandler(workingDir, 10, 10, imageExtension);
        int imageId = imageHandler.createImage(7, 20);
        File imageFile = new File(smallImagePath);
        InputStream inputStream = new FileInputStream(imageFile);

        imageHandler.drawImage(imageId, -1, 9, 3, 2, inputStream);

        byte[] res = imageHandler.getSubImage(imageId, -2, 8, 5, 4);
        File ansFile = new File(getAnswerPath(13));

        assertArrayEquals(FileUtils.readFileToByteArray(ansFile), res);
    }

    @Test
    public void notWholeSubImageTest2() throws IOException {
        ImageHandler imageHandler = new ImageHandler(workingDir, 10, 10, imageExtension);
        int imageId = imageHandler.createImage(7, 20);
        File imageFile = new File(smallImagePath);
        InputStream inputStream = new FileInputStream(imageFile);

        imageHandler.drawImage(imageId, 6, 9, 3, 2, inputStream);

        byte[] res = imageHandler.getSubImage(imageId, 5, 8, 5, 4);
        File ansFile = new File(getAnswerPath(14));

        assertArrayEquals(FileUtils.readFileToByteArray(ansFile), res);
    }

    @Test
    public void notWholeSubImageTest3() throws IOException {
        ImageHandler imageHandler = new ImageHandler(workingDir, 10, 10, imageExtension);
        int imageId = imageHandler.createImage(7, 20);
        File imageFile = new File(smallImagePath);
        InputStream inputStream = new FileInputStream(imageFile);

        imageHandler.drawImage(imageId, -1, -1, 3, 2, inputStream);

        byte[] res = imageHandler.getSubImage(imageId, -2, -2, 5, 4);
        File ansFile = new File(getAnswerPath(15));

        assertArrayEquals(FileUtils.readFileToByteArray(ansFile), res);
    }

    @Test
    public void notWholeSubImageTest4() throws IOException {
        ImageHandler imageHandler = new ImageHandler(workingDir, 10, 10, imageExtension);
        int imageId = imageHandler.createImage(7, 20);
        File imageFile = new File(smallImagePath);
        InputStream inputStream = new FileInputStream(imageFile);

        imageHandler.drawImage(imageId, 5, 19, 3, 2, inputStream);

        byte[] res = imageHandler.getSubImage(imageId, 4, 18, 5, 4);
        File ansFile = new File(getAnswerPath(16));

        assertArrayEquals(FileUtils.readFileToByteArray(ansFile), res);
    }

    @Test
    public void largeImageSubImageTest() throws IOException {
        ImageHandler imageHandler = new ImageHandler(workingDir, 2000, 10, imageExtension);
        int imageId = imageHandler.createImage(540, 540);
        File imageFile = new File(largeImagePath);
        InputStream inputStream = new FileInputStream(imageFile);

        imageHandler.drawImage(imageId, 10, 10, 512, 512, inputStream);

        byte[] res = imageHandler.getSubImage(imageId, 10, 10, 512, 512);
        File ansFile = new File(largeImagePath);

        assertArrayEquals(FileUtils.readFileToByteArray(ansFile), res);
    }

    @Test
    public void incorrectIdSubImageTest() throws IOException {
        ImageHandler imageHandler = new ImageHandler(workingDir, 10, 10, imageExtension);
        int imageId = imageHandler.createImage(7, 20);

        assertThrowsExactly(IncorrectImageIdException.class, () ->
                imageHandler.getSubImage(imageId + 1, 5, 19, 3, 2));
    }

    @Test
    public void incorrectRegionSubImageTest() throws IOException {
        ImageHandler imageHandler = new ImageHandler(workingDir, 10, 10, imageExtension);
        int imageId = imageHandler.createImage(7, 20);

        assertThrowsExactly(IncorrectImageRegionException.class, () ->
                imageHandler.getSubImage(imageId, -100, 19, 3, 2));
    }

}
