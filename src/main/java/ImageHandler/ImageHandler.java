package ImageHandler;

import ImageHandler.Exceptions.IncorrectImageIdException;
import ImageHandler.Exceptions.IncorrectImageRegionException;
import org.apache.commons.io.FileUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;

import static java.lang.Math.max;
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

        if (!directory.isDirectory()) {
            throw new IllegalArgumentException("The passed path is not a directory.");
        }

        if (maxImagePartWidth <= 0 || maxImagePartHeight <= 0) {
            throw new IllegalArgumentException("Invalid image part size.");
        }

        this.workingDirectory = directory.getPath();
        this.maxImagePartWidth = maxImagePartWidth;
        this.maxImagePartHeight = maxImagePartHeight;
        this.imageExtension = imageExtension;
    }

    public byte[] getSubImage(int imageId, int x, int y, int width, int height) throws IOException {
        if (imageId >= imageList.size() || imageList.get(imageId) == null) {
            throw new IncorrectImageIdException();
        }

        int regionX = max(x, 0);
        int regionY = max(y, 0);
        int regionWidth = min(width, width + x);
        int regionHeight = min(height, height + y);

        LargeImage image = imageList.get(imageId);
        int subImageWidth = min(image.getImageWidth() - regionX, regionWidth);
        int subImageHeight = min(image.getImageHeight() - regionY, regionHeight);
        int subImageX = 0;
        int subImageY = 0;

        if (subImageWidth <= 0 || subImageHeight <= 0) {
            throw new IncorrectImageRegionException();
        }

        BufferedImage subImage = new BufferedImage(subImageWidth,
                subImageHeight, BufferedImage.TYPE_INT_RGB);
        Graphics subImageGraphics = subImage.createGraphics();

        int firstPartIndex = regionY / maxImagePartHeight;
        int lastPartIndex = (regionY + subImageHeight - 1) / maxImagePartHeight;

        for (int i = firstPartIndex; i <= lastPartIndex; i++) {
            BufferedImage imagePart = ImageIO.read(new File(getImagePartPath(imageId, i)));

            int sourceSubImageX = regionX;
            int sourceSubImageY = 0;
            if (regionY > maxImagePartHeight * i) {
                sourceSubImageY = regionY - maxImagePartHeight * i;
            }

            int sourceSubImageWidth = subImageWidth;
            int sourceSubImageHeight = maxImagePartHeight;
            if (regionY + subImageHeight - 1 < maxImagePartHeight * (i + 1) - 1) {
                sourceSubImageHeight = regionY + subImageHeight - maxImagePartHeight * i;
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

    public void drawImage(int imageId, int x, int y, int width, int height, InputStream inputStream) throws IOException {
        if (imageId >= imageList.size() || imageList.get(imageId) == null) {
            throw new IncorrectImageIdException();
        }

        LargeImage targetImage = imageList.get(imageId);
        BufferedImage sourceImage = ImageIO.read(inputStream);
        if (sourceImage.getWidth() != width || sourceImage.getHeight() != height) {
            throw new IncorrectImageRegionException();
        }

        if (x + width < 0 || y + height < 0 || y >= targetImage.getImageHeight() || x >= targetImage.getImageWidth()) {
            throw new IncorrectImageRegionException();
        }

        for (int i = 0; i < targetImage.getNumberOfParts(); i++) {
            String imagePartPath = getImagePartPath(imageId, i);
            BufferedImage targetImagePart = ImageIO.read(new File(imagePartPath));
            Graphics targetImagePartGraphics = targetImagePart.createGraphics();
            int sourceImageX = x;
            int sourceImageY = y - maxImagePartHeight * i;

            targetImagePartGraphics.drawImage(sourceImage, sourceImageX, sourceImageY, null);
            ImageIO.write(targetImagePart, imageExtension, new File(imagePartPath));
        }
    }

    public int createImage(int width, int height) throws IOException {
        if (width <= 0 || height <= 0) {
            throw new IncorrectImageRegionException();
        }

        int imageId = imageList.size();
        int numberOfParts = (height + maxImagePartHeight - 1) / maxImagePartHeight;
        new File(getImageDirectoryPath(imageId)).mkdirs();

        for (int i = 0; i < numberOfParts; i++) {
            int imagePartHeight = min(height - i * maxImagePartHeight, maxImagePartHeight);
            int imagePartWidth = min(width, maxImagePartWidth);
            BufferedImage imagePart = new BufferedImage(imagePartWidth, imagePartHeight, BufferedImage.TYPE_INT_RGB);
            File imagePartFile = new File(getImagePartPath(imageId, i));
            ImageIO.write(imagePart, imageExtension, imagePartFile);
        }

        imageList.add(imageId, new LargeImage(width, height, numberOfParts));
        return imageId;
    }

    public void deleteImage(int imageId) throws IOException {
        if (imageId >= imageList.size() || imageList.get(imageId) == null) {
            throw new IncorrectImageIdException();
        }

        FileUtils.deleteDirectory(new File(getImageDirectoryPath(imageId)));
        imageList.set(imageId, null);
    }

}
