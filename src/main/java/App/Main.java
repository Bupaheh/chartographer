package App;

import ChartasHttpHandler.ChartasHttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;


public class Main {

    private static final int port = 8080;
    private static final String requestPath = "/chartas";
    private static final int maxImagePartWidth = 20000;
    private static final int maxImagePartHeight = 5000;
    private static final String imageExtension = "bmp";


    public static void main(String[] args) {
        try {
            if (args.length <= 0) {
                System.out.println("The working directory was not passed.");
                return;
            }

            String workingDirectory = args[0];
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext(requestPath, new ChartasHttpHandler(workingDirectory,
                    maxImagePartWidth, maxImagePartHeight, imageExtension));
            server.setExecutor(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()));
            server.start();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

}
