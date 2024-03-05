import java.io.*;
import java.net.Socket;

public class Client {
    public static void main(String[] args) {
        // Server details
        String serverIP = "10.111.104.35";  // IP address of the server
        int serverPort = 12345;         // Port number the server is listening on

        try (Socket socket = new Socket(serverIP, serverPort)) {
            System.out.println("Connected to server.");

            // Receive a segment of data from the server
            byte[] segmentData = receiveSegment(socket);
            String tempFileName = "received_segment.txt";
            // Save the received data to a temporary file
            saveToFile(tempFileName, segmentData);

            // Calculate the word count of the saved file
            int wordCount = WordCount.wordCount(tempFileName); // Assuming there's a method to count words
            System.out.println("Number of words in the file: " + wordCount);

            // Send the calculated word count back to the server
            sendWordCount(socket, wordCount);
        } catch (IOException e) {
            // Handle any errors that occur during the process
            e.printStackTrace();
        }
    }

    // Method to receive a segment of data from the server
    private static byte[] receiveSegment(Socket socket) throws IOException {
        DataInputStream dis = new DataInputStream(socket.getInputStream());
        int length = dis.readInt();
        byte[] data = new byte[length];
        dis.readFully(data);
        return data;
    }

    // Method to save data to a file
    private static void saveToFile(String fileName, byte[] data) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(fileName)) {
            fos.write(data);
        }
    }

    // Method to send the word count back to the server
    private static void sendWordCount(Socket socket, int wordCount) throws IOException {
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        out.println(wordCount);
    }
}
