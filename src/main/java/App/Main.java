package App;

import ImageHandler.ImageHandler;

import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        try {
            ImageHandler imageHandler = new ImageHandler("Images");
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

}
