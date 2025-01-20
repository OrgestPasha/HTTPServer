import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONObject;

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

                    if (method.equals("GET")) {
                        handleGetRequest(path, output);
                    } else if (method.equals("POST")) {
                        handlePostRequest(headers.get("Content-Length"), output, reader);
                    } else if (method.equals("DELETE")) {
                        handleDeleteRequest(path, output);
                    } else if (method.equals("PUT")) {
                        handlePutRequest(headers.get("Content-Length"), path, output, reader);
                    } else {
                        // Handle other methods
                        output.write("HTTP/1.1 501 Not Implemented\r\n\r\n".getBytes());
                        output.flush();
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

    private static void handlePostRequest(String contentLengthHeader, OutputStream output, BufferedReader reader)
            throws IOException {
        System.out.println("Handling POST request");
        if (contentLengthHeader != null) {
            int contentLength = Integer.parseInt(contentLengthHeader);
            char[] body = new char[contentLength];
            reader.read(body, 0, contentLength);
            String requestBody = new String(body);

            System.out.println("Request Body: " + requestBody);

            JSONObject json = new JSONObject(requestBody);
            String filename = json.getString("filename");
            String content = json.getString("content");

            // Ensure the www directory exists
            File directory = new File("www");
            if (!directory.exists()) {
                directory.mkdir();
            }

            // Create a new HTML file in the www directory
            String filepath = "www/" + filename;
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filepath))) {
                writer.write(content);
                System.out.println("New HTML file created: " + filepath);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Generate a response
            String httpResponse = "HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\n\r\nNew HTML file created: "
                    + filepath;
            output.write(httpResponse.getBytes("UTF-8"));
            output.flush(); // Ensure all data is sent before closing the connection
        } else {
            System.out.println("Content-Length header is missing");
            output.write("HTTP/1.1 411 Length Required\r\n\r\n".getBytes());
            output.flush();
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
            output.flush();
        } else {
            output.write("HTTP/1.1 404 Not Found\r\n\r\n".getBytes());
            output.flush();
        }
    }

    private static void handleDeleteRequest(String path, OutputStream output) throws IOException {
        System.out.println("Handling DELETE request");
        File file = new File("www" + path);
        if (file.exists() && !file.isDirectory()) {
            if (file.delete()) {
                System.out.println("File deleted: " + file.getPath());
                String httpResponse = "HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\n\r\nFile deleted: "
                        + file.getPath();
                output.write(httpResponse.getBytes("UTF-8"));
            } else {
                System.out.println("Failed to delete file: " + file.getPath());
                String httpResponse = "HTTP/1.1 500 Internal Server Error\r\nContent-Type: text/plain\r\n\r\nFailed to delete file: "
                        + file.getPath();
                output.write(httpResponse.getBytes("UTF-8"));
            }
        } else {
            System.out.println("File not found: " + file.getPath());
            String httpResponse = "HTTP/1.1 404 Not Found\r\nContent-Type: text/plain\r\n\r\nFile not found: "
                    + file.getPath();
            output.write(httpResponse.getBytes("UTF-8"));
        }
        output.flush();
    }

    private static void handlePutRequest(String contentLengthHeader, String path, OutputStream output,
            BufferedReader reader)
            throws IOException {
        System.out.println("Handling PUT request");
        if (contentLengthHeader != null) {
            int contentLength = Integer.parseInt(contentLengthHeader);
            char[] body = new char[contentLength];
            reader.read(body, 0, contentLength);
            String requestBody = new String(body);

            System.out.println("Request Body: " + requestBody);

            JSONObject json = new JSONObject(requestBody);
            String content = json.getString("content");

            // Update the HTML file in the www directory
            String filepath = "www" + path;
            File file = new File(filepath);
            if (file.exists() && !file.isDirectory()) {
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(filepath))) {
                    writer.write(content);
                    System.out.println("HTML file updated: " + filepath);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // Generate a response
                String httpResponse = "HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\n\r\nHTML file updated: "
                        + filepath;
                output.write(httpResponse.getBytes("UTF-8"));
                output.flush(); // Ensure all data is sent before closing the connection
            } else {
                System.out.println("File not found: " + filepath);
                String httpResponse = "HTTP/1.1 404 Not Found\r\nContent-Type: text/plain\r\n\r\nFile not found: "
                        + filepath;
                output.write(httpResponse.getBytes("UTF-8"));
                output.flush();
            }
        } else {
            System.out.println("Content-Length header is missing");
            output.write("HTTP/1.1 411 Length Required\r\n\r\n".getBytes());
            output.flush();
        }
    }
}