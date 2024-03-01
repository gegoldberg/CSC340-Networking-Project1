import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Server {
    private static final int portNumber = 12345;  // Port number for communication
    private static final int numberOfClients = 5; // Number of clients server will handle
    private static int totalWordCount = 0; // Total word count from all clients
    private static long startTime; // Starting time of server operation

    public static void main(String[] args) {
        System.out.println("Enter the file path:"); // Prompt user for file path
        Scanner scanner = new Scanner(System.in);
        String originalFileName = scanner.nextLine(); // Read file path from user input
        scanner.close();

        List<Socket> clientSockets = new ArrayList<>();
        List<Thread> threads = new ArrayList<>();

        try (ServerSocket serverSocket = new ServerSocket(portNumber)) {
            System.out.println("Server started."); // Server started message
            System.out.println("Waiting for clients..."); // Waiting for clients message

            byte[] originalFileContent = Files.readAllBytes(Paths.get(originalFileName)); // Read file content
            List<byte[]> segments = divideFileIntoSegments(originalFileContent, numberOfClients); // Divide file into segments

            // Accept all clients before processing
            for (int i = 0; i < numberOfClients; i++) {
                Socket clientSocket = serverSocket.accept(); // Accept client connection
                clientSockets.add(clientSocket); // Add client socket to list
                System.out.println("Client " + (i + 1) + " connected."); // Client connection message
            }

            startTime = System.currentTimeMillis(); // Start timing after all clients are connected

            // Distribute segments and process clients in parallel
            for (int i = 0; i < numberOfClients; i++) {
                final Socket clientSocket = clientSockets.get(i);
                final byte[] segment = segments.get(i);
                Thread thread = new Thread(() -> processClient(clientSocket, segment)); // Create thread for client processing
                threads.add(thread); // Add thread to list
                thread.start(); // Start thread for client
            }

            // Wait for all threads to finish
            for (Thread thread : threads) {
                try {
                    thread.join(); // Wait for thread to finish execution
                } catch (InterruptedException e) {
                    System.out.println("A processing thread was interrupted."); // Handling interrupted threads
                }
            }

            long endTime = System.currentTimeMillis(); // Record end time of server operation
            System.out.println("Total word count from all clients: " + (totalWordCount-1)); // Display total word count
            System.out.println("Total Time: " + (endTime - startTime) + " milliseconds"); // Display total time taken
        } catch (IOException e) {
            System.out.println("Server exception: " + e.getMessage()); // Server exception handling
            e.printStackTrace(); // Print stack trace for exception
        }
    }

    // Method to divide file content into segments
    private static List<byte[]> divideFileIntoSegments(byte[] fileContent, int numberOfSegments) {
        List<byte[]> segments = new ArrayList<>();
        int segmentSize = fileContent.length / numberOfSegments;
        int remainder = fileContent.length % numberOfSegments;
        int start = 0;
        for (int i = 0; i < numberOfSegments; i++) {
            int end = start + segmentSize + (remainder-- > 0 ? 1 : 0);
            byte[] segment = new byte[end - start];
            System.arraycopy(fileContent, start, segment, 0, segment.length);
            segments.add(segment);
            start = end;
        }
        return segments;
    }

    // Method to process client requests
    private static void processClient(Socket clientSocket, byte[] segment) {
        try {
            DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream());
            dos.writeInt(segment.length); // Send segment length to client
            dos.write(segment); // Send segment data to client
            dos.flush(); // Flush output stream

            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            synchronized (Server.class) {
                totalWordCount += Integer.parseInt(in.readLine()); // Receive word count from client and update total
            }
            clientSocket.close(); // Close client socket
        } catch (IOException e) {
            System.out.println("Error processing client: " + e.getMessage()); // Error handling during client processing
            e.printStackTrace(); // Print stack trace for exception
        }
    }
}
