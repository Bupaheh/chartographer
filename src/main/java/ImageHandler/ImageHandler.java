package ImageHandler;

import org.apache.commons.io.FileUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static java.lang.Math.min;

public class ImageHandler {

    final private int maxImagePartWidth = 20000;
    final private int maxImagePartHeight = 5000;
    final private String imageExtension = "bmp";

    final private String workingDirectory;
    final private ArrayList<LargeImage> imageList = new ArrayList<>();

    private String getImageDirectoryPath(int imageId) {
        return workingDirectory + "/" + imageId;
    }

    private String getImagePartPath(int imageId, int imagePart) {
        return workingDirectory + "/" + imageId + "/" + imagePart + "." + imageExtension;
    }

    public ImageHandler(String workingDirectory) throws IOException {
        File directory = new File(workingDirectory);
        if (directory.isDirectory()) {
            this.workingDirectory = directory.getPath();
        } else {
            throw new IllegalArgumentException("The passed path is not a directory.");
        }
    }

    public int createImage(int width, int height) throws IOException {
        int imageId = imageList.size();
        int numberOfParts = (height + maxImagePartHeight - 1) / maxImagePartHeight;

        for (int i = 0; i < numberOfParts; i++) {
            int imagePartHeight = min(height - i * maxImagePartHeight, maxImagePartHeight);
            int imagePartWidth = min(width, maxImagePartWidth);
            BufferedImage imagePart = new BufferedImage(imagePartWidth, imagePartHeight, BufferedImage.TYPE_INT_RGB);
            File imagePartFile = new File(getImagePartPath(imageId, i));
            ImageIO.write(imagePart, imageExtension, imagePartFile);
        }

        imageList.set(imageId, new LargeImage(width, height, numberOfParts));
        return imageId;
    }

    public boolean deleteImage(int id) throws IOException {
        if (id < imageList.size()) {
            FileUtils.deleteDirectory(new File(getImageDirectoryPath(id)));
            imageList.set(id, null);
            return true;
        } else {
            return false;
        }
    }

}
