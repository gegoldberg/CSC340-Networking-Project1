import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {
    public static void main(String[] args) {
        // Server information
        String serverIP = "127.0.0.1";  
        int serverPort = 12345;      

        try (Socket socket = new Socket(serverIP, serverPort)) {
            // Connect to the server using a Socket
            System.out.println("Connected to server.");

            // Read the file path sent by the server
            String filePath = new BufferedReader(new InputStreamReader(socket.getInputStream())).readLine();

            // Calculate the word count of the file using a helper method (not provided)
            int wordCount = WordCount.wordCount(filePath);
            System.out.println("Number of words in the file: " + wordCount);

            // Send the word count back to the server
            try (PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {
                writer.println(wordCount);
            }

            // Print a message indicating that the word count has been sent to the server
            System.out.println("Word count sent to the server.");

        } catch (IOException e) {
            // Handle any IOException that may occur during the client-server communication
            e.printStackTrace();
        }
    }
}
