import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.util.*;

public class Server {
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(4221)) {
            serverSocket.setReuseAddress(true);
            System.out.println("Server started on port 4221");

            while (true) {
                try (Socket clientSocket = serverSocket.accept()) {
                    System.out.println("Accepted new connection");

                    BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    OutputStream output = clientSocket.getOutputStream();

                    // Read the request line
                    String line = reader.readLine();
                    if (line == null || line.isEmpty()) {
                        continue;
                    }

                    // Parse the request line
                    String[] requestParts = line.split(" ");
                    String method = requestParts[0];
                    String path = requestParts[1];
                    String version = requestParts[2];

                    System.out.println("Method: " + method);
                    System.out.println("Path: " + path);
                    System.out.println("Version: " + version);

                    // Read headers
                    Map<String, String> headers = new HashMap<>();
                    while (!(line = reader.readLine()).isEmpty()) {
                        String[] headerParts = line.split(": ");
                        headers.put(headerParts[0], headerParts[1]);
                    }

                    // Handle the request
                    if (method.equals("GET")) {
                        handleGetRequest(path, output);
                    } else {
                        // Handle other methods
                        output.write("HTTP/1.1 501 Not Implemented\r\n\r\n".getBytes());
                    }

                    // Close the connection
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleGetRequest(String path, OutputStream output) throws IOException {
        File file = new File("www" + path);
        if (file.exists() && !file.isDirectory()) {
            byte[] fileBytes = Files.readAllBytes(file.toPath());
            String contentType = Files.probeContentType(file.toPath());

            output.write("HTTP/1.1 200 OK\r\n".getBytes());
            output.write(("Content-Type: " + contentType + "\r\n").getBytes());
            output.write(("Content-Length: " + fileBytes.length + "\r\n").getBytes());
            output.write("\r\n".getBytes());
            output.write(fileBytes);
        } else {
            output.write("HTTP/1.1 404 Not Found\r\n\r\n".getBytes());
        }
    }
}
