package ImageHandler;

import ImageHandler.Exceptions.IncorrectImageIdException;
import ImageHandler.Exceptions.IncorrectImageRegionException;
import org.apache.commons.io.FileUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static java.lang.Math.min;

public class ImageHandler {

    private final int maxImagePartWidth;
    private final int maxImagePartHeight;
    private final String imageExtension;
    final private String workingDirectory;
    final private ArrayList<LargeImage> imageList = new ArrayList<>();

    private String getImageDirectoryPath(int imageId) {
        return workingDirectory + "/" + imageId;
    }

    private String getImagePartPath(int imageId, int imagePart) {
        return workingDirectory + "/" + imageId + "/" + imagePart + "." + imageExtension;
    }

    public ImageHandler(String workingDirectory, int maxImagePartWidth,
                        int maxImagePartHeight, String imageExtension) throws IOException {
        File directory = new File(workingDirectory);

        if (!directory.isDirectory())
            throw new IllegalArgumentException("The passed path is not a directory.");

        if (maxImagePartWidth <= 0 || maxImagePartHeight <= 0)
            throw new IllegalArgumentException("Invalid image part size.");

        this.workingDirectory = directory.getPath();
        this.maxImagePartWidth = maxImagePartWidth;
        this.maxImagePartHeight = maxImagePartHeight;
        this.imageExtension = imageExtension;
    }

    public byte[] getSubImage(int imageId, int x, int y, int width, int height) throws IOException {
        if (imageId >= imageList.size() || imageList.get(imageId) == null)
            throw new IncorrectImageIdException();

        LargeImage image = imageList.get(imageId);
        int subImageWidth = min(image.getImageWidth() - x, width);
        int subImageHeight = min(image.getImageHeight() - y, height);
        int subImageX = 0;
        int subImageY = 0;

        if (subImageWidth <= 0 || subImageHeight <= 0)
            throw new IncorrectImageRegionException();

        BufferedImage subImage = new BufferedImage(subImageWidth,
                subImageHeight, BufferedImage.TYPE_INT_RGB);
        Graphics subImageGraphics = subImage.createGraphics();

        int firstPartIndex = y / maxImagePartHeight;
        int lastPartIndex = (y + subImageHeight - 1) / maxImagePartHeight;

        for (int i = firstPartIndex; i <= lastPartIndex; i++) {
            BufferedImage imagePart = ImageIO.read(new File(getImagePartPath(imageId, i)));

            int sourceSubImageX = x;
            int sourceSubImageY = 0;
            if (y > maxImagePartHeight * i) {
                sourceSubImageY = y - maxImagePartHeight * i;
            }

            int sourceSubImageWidth = subImageWidth;
            int sourceSubImageHeight = maxImagePartHeight;
            if (y + subImageHeight - 1 < maxImagePartHeight * (i + 1) - 1) {
                sourceSubImageHeight = y + subImageHeight - maxImagePartHeight * i;
            }

            BufferedImage sourceSubImage = imagePart.getSubimage(sourceSubImageX,
                    sourceSubImageY, sourceSubImageWidth, sourceSubImageHeight);

            subImageGraphics.drawImage(sourceSubImage, subImageX, subImageY, null);

            subImageY += sourceSubImageHeight;
        }

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(subImage, imageExtension, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
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

    public void deleteImage(int imageId) throws IOException {
        if (imageId >= imageList.size() || imageList.get(imageId) == null)
            throw new IncorrectImageIdException();

        FileUtils.deleteDirectory(new File(getImageDirectoryPath(imageId)));
        imageList.set(imageId, null);
    }

}
