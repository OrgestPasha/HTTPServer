import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

public class Client {
    public static void main(String[] args) {
        try {
            // Connect to the server
            Socket socket = new Socket("localhost", 4221);

            // Send an HTTP GET request
            OutputStream output = socket.getOutputStream();
            output.write("GET / HTTP/1.1\r\nHost: localhost\r\n\r\n".getBytes());
            output.flush();

            // Read the response
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            // Close the connection
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}