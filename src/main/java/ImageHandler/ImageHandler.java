package ImageHandler;

import ImageHandler.Exceptions.IncorrectImageIdException;
import ImageHandler.Exceptions.IncorrectImageRegionException;
import org.apache.commons.io.FileUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class ImageHandler {

    private final int maxImagePartWidth;
    private final int maxImagePartHeight;
    private final String imageExtension;
    final private String workingDirectory;
    final private AtomicInteger imageCount = new AtomicInteger();
    final private List<LargeImage> imageList = Collections.synchronizedList(new ArrayList<>());
    final private List<ReadWriteLock> imageLocks = Collections.synchronizedList(new ArrayList<>());

    private String getImageDirectoryPath(int imageId) {
        return workingDirectory + "/" + imageId;
    }

    private String getImagePartPath(int imageId, int imagePart) {
        return workingDirectory + "/" + imageId + "/" + imagePart + "." + imageExtension;
    }

    public ImageHandler(String workingDirectory, int maxImagePartWidth,
                        int maxImagePartHeight, String imageExtension) {
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
        if (imageId < 0 || imageId >= imageCount.get() || imageList.get(imageId) == null) {
            throw new IncorrectImageIdException();
        }

        int regionX = max(x, 0);
        int regionY = max(y, 0);
        int regionWidth = min(width, width + x);
        int regionHeight = min(height, height + y);

        LargeImage image = imageList.get(imageId);
        int sourceSubImageWidth = min(image.getImageWidth() - regionX, regionWidth);
        int sourceSubImageHeight = min(image.getImageHeight() - regionY, regionHeight);
        int subImageX = 0;
        int subImageY = 0;
        if (x < 0) {
            subImageX = -x;
        }
        if (y < 0) {
            subImageY = -y;
        }

        if (sourceSubImageWidth <= 0 || sourceSubImageHeight <= 0) {
            throw new IncorrectImageRegionException();
        }

        BufferedImage subImage = new BufferedImage(width,
                height, BufferedImage.TYPE_INT_RGB);
        Graphics subImageGraphics = subImage.createGraphics();

        int firstPartIndex = regionY / maxImagePartHeight;
        int lastPartIndex = (regionY + sourceSubImageHeight - 1) / maxImagePartHeight;

        imageLocks.get(imageId).readLock().lock();

        for (int i = firstPartIndex; i <= lastPartIndex; i++) {
            BufferedImage imagePart = ImageIO.read(new File(getImagePartPath(imageId, i)));

            int partSubImageX = regionX;
            int partSubImageY = 0;
            if (regionY > maxImagePartHeight * i) {
                partSubImageY = regionY - maxImagePartHeight * i;
            }

            int partSubImageWidth = sourceSubImageWidth;
            int partSubImageHeight = maxImagePartHeight - partSubImageY;
            if (regionY + sourceSubImageHeight - 1 < maxImagePartHeight * (i + 1) - 1) {
                partSubImageHeight = regionY + sourceSubImageHeight - maxImagePartHeight * i;
            }

            BufferedImage sourceSubImage = imagePart.getSubimage(partSubImageX,
                    partSubImageY, partSubImageWidth, partSubImageHeight);

            subImageGraphics.drawImage(sourceSubImage, subImageX, subImageY, null);

            subImageY += partSubImageHeight;
        }

        imageLocks.get(imageId).readLock().unlock();

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(subImage, imageExtension, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    public void drawImage(int imageId, int x, int y, int width, int height, InputStream inputStream) throws IOException {
        if (imageId < 0 || imageId >= imageCount.get() || imageList.get(imageId) == null) {
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

        imageLocks.get(imageId).writeLock().lock();

        for (int i = 0; i < targetImage.getNumberOfParts(); i++) {
            String imagePartPath = getImagePartPath(imageId, i);
            BufferedImage targetImagePart = ImageIO.read(new File(imagePartPath));
            Graphics targetImagePartGraphics = targetImagePart.createGraphics();
            int sourceImageX = x;
            int sourceImageY = y - maxImagePartHeight * i;

            targetImagePartGraphics.drawImage(sourceImage, sourceImageX, sourceImageY, null);
            ImageIO.write(targetImagePart, imageExtension, new File(imagePartPath));
        }
        imageLocks.get(imageId).writeLock().unlock();
    }

    public int createImage(int width, int height) throws IOException {
        if (width <= 0 || height <= 0) {
            throw new IncorrectImageRegionException();
        }

        int imageId = imageCount.getAndIncrement();

        imageLocks.add(imageId, new ReentrantReadWriteLock());
        imageLocks.get(imageId).writeLock().lock();

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
        imageLocks.get(imageId).writeLock().unlock();
        return imageId;
    }

    public void deleteImage(int imageId) throws IOException {
        if (imageId < 0 || imageId >= imageCount.get() || imageList.get(imageId) == null) {
            throw new IncorrectImageIdException();
        }

        imageLocks.get(imageId).writeLock().lock();
        FileUtils.deleteDirectory(new File(getImageDirectoryPath(imageId)));
        imageList.set(imageId, null);
        imageLocks.get(imageId).writeLock().unlock();
    }

}
