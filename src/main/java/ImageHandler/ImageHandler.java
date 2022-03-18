package ImageHandler;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class ImageHandler {

    final private String workingDir;
    final private ArrayList<LargeBMP> imageList = new ArrayList<>();

    public ImageHandler(String workingDir) throws IOException {
        this.workingDir = workingDir;
    }

    public boolean deleteImage(int id) throws IOException {
        if (id < imageList.size()) {
            FileUtils.deleteDirectory(new File(workingDir + "/" + id));
            imageList.set(id, null);
            return true;
        } else {
            return false;
        }
    }

}
