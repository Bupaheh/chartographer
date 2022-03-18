package ImageHandler;

public class LargeBMP {
    private final int imageHeight;
    private final int imageWidth;
    private final int numberOfParts;

    public LargeBMP(int imageHeight, int imageWidth, int numberOfParts) {
        this.imageHeight = imageHeight;
        this.imageWidth = imageWidth;
        this.numberOfParts = numberOfParts;
    }

    public int getImageHeight() { return imageHeight; }
    public int getImageWidth() { return imageWidth; }
    public int getNumberOfParts() { return numberOfParts; }
}
