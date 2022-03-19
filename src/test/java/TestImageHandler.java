import ImageHandler.Exceptions.IncorrectImageIdException;
import ImageHandler.ImageHandler;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

public class TestImageHandler {

    private final String workingDir = "testData";
    private final String imageExtension = "bmp";

    @AfterEach
    public void clean() throws IOException {
        FileUtils.deleteDirectory(new File(workingDir));
    }

    @BeforeEach
    public void createWorkingDir() throws IOException {
        new File(workingDir).mkdirs();
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

}
