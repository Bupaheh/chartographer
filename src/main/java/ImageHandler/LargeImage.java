package ImageHandler;

public class LargeImage {

    private final int imageWidth;
    private final int imageHeight;
    private final int numberOfParts;

    public LargeImage(int imageWidth, int imageHeight, int numberOfParts) {
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        this.numberOfParts = numberOfParts;
    }

    public int getImageHeight() { return imageHeight; }
    public int getImageWidth() { return imageWidth; }
    public int getNumberOfParts() { return numberOfParts; }

}
