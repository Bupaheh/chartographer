package App;

import ChartasHttpHandler.ChartasHttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;


public class Main {

    private static final int port = 8085;
    private static final String hostname = "localhost";
    private static final String requestPath = "/chartas";
    private static final String workingDirectory = "Images";
    private static final int maxImagePartWidth = 20000;
    private static final int maxImagePartHeight = 5000;
    private static final String imageExtension = "bmp";


    public static void main(String[] args) {
        try {
            /* delete images from previous session */
            FileUtils.deleteDirectory(new File(workingDirectory));

            if (!new File(workingDirectory).mkdirs()) {
                System.out.println("Unable to create a working directory.");
                return;
            }

            HttpServer server = HttpServer.create(new InetSocketAddress(hostname, port), 0);
            server.createContext(requestPath, new ChartasHttpHandler(workingDirectory,
                    maxImagePartWidth, maxImagePartHeight, imageExtension));
            server.setExecutor(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()));
            server.start();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

}
