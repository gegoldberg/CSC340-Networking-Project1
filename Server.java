import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Server {
    public static void main(String[] args) {
        int portNumber = 12345;  
        int numberOfClients = 5; 
        String originalFileName = "C:\\Users\\ggold\\OneDrive\\Documents\\CSC340-Project1\\Job.txt";
        int totalWordCount = 0;  

        try (ServerSocket serverSocket = new ServerSocket(portNumber)) {
            // The server socket is automatically closed when try-with-resources block is exited
            System.out.println("Server started. Waiting for clients to connect...");

            // Read the content of the original file into a byte array
            byte[] originalFileContent = Files.readAllBytes(Paths.get(originalFileName));
            int segmentSize = originalFileContent.length / numberOfClients;
            // Calculate the size of each segment based on the number of clients
            int lastIndexUsed = 0;  // To keep track of the last index used for segmenting the original file

            // Loop through the expected number of clients
            for (int i = 0; i < numberOfClients; i++) {
                try (Socket clientSocket = serverSocket.accept()) {
                    // Accepts a connection from a client
                    System.out.println("Client connected: " + (i + 1) + "/" + numberOfClients);

                    // Determine the start and end index for the current segment
                    int startIndex = lastIndexUsed;
                    int endIndex = i < numberOfClients - 1 ? (i + 1) * segmentSize : originalFileContent.length;

                    // Adjust the end index to ensure words are not split
                    while (endIndex > startIndex && !isWhiteSpaceOrNewline(originalFileContent[endIndex - 1])) {
                        endIndex--;
                    }

                    lastIndexUsed = endIndex;

                    // Extract the segment from the original file content
                    byte[] segment = extractSegment(originalFileContent, startIndex, endIndex);

                    // Save the segment to a file
                    String segmentFileName = "segment_" + i + ".txt";
                    saveToFile(segmentFileName, segment);
                    // Send the file name to the client
                    sendFileNameToClient(clientSocket, segmentFileName);

                    // Receive word count from the client
                    int wordCount = receiveWordCountFromClient(clientSocket);

                    // Process the received word count
                    if (wordCount != -1) {
                        System.out.println("Client sent word count: " + wordCount);
                        totalWordCount += wordCount;
                    } else {
                        System.out.println("No word count received, client may have closed the connection.");
                    }

                    // Print a message indicating client disconnection
                    System.out.println("Client disconnected.");
                } catch (IOException e) {
                    // Handle any errors that may occur with a client connection
                    handleClientConnectionError(e);
                }
            }

            // Print the total word count received from all clients
            System.out.println("Total word count from all clients: " + totalWordCount);
        } catch (IOException e) {
            // Handle errors that may occur during server initialization
            handleServerError(e);
        }
    }

    // Helper method to extract a segment from a byte array
    private static byte[] extractSegment(byte[] fileContent, int startIndex, int endIndex) {
        int segmentSize = endIndex - startIndex;
        byte[] segment = new byte[segmentSize];
        System.arraycopy(fileContent, startIndex, segment, 0, segmentSize);
        return segment;
    }

    // Helper method to save a byte array to a file
    private static void saveToFile(String fileName, byte[] content) {
        try {
            Files.write(Paths.get(fileName), content);
        } catch (IOException e) {
            System.out.println("Error saving file: " + e.getMessage());
        }
    }

    // Helper method to send a file name to the client
    private static void sendFileNameToClient(Socket clientSocket, String fileName) {
        try (PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
            out.println(fileName);
        } catch (IOException e) {
            System.out.println("Error sending file name to client: " + e.getMessage());
        }
    }

    // Helper method to receive word count from the client
    private static int receiveWordCountFromClient(Socket clientSocket) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String line = reader.readLine();

            // Check for null or empty line
            if (line != null && !line.isEmpty()) {
                return Integer.parseInt(line);
            } else {
                System.out.println("Error reading word count from client.");
            }
        } catch (IOException e) {
            System.out.println("Error reading word count from client: " + e.getMessage());
        }

        // Default value or error indicator
        return -1;
    }

    // Helper method to handle client connection errors
    private static void handleClientConnectionError(IOException e) {
        System.out.println("An error occurred with a client connection.");
        e.printStackTrace();
    }

    // Helper method to handle server errors
    private static void handleServerError(IOException e) {
        System.out.println("An error occurred starting the server.");
        e.printStackTrace();
    }

    // Helper method to check if a byte represents whitespace or newline
    private static boolean isWhiteSpaceOrNewline(byte b) {
        return b == ' ' || b == '\n';
    }
}
