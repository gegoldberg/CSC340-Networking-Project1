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
    private static final int PORT_NUMBER = 12345;
    private static final int NUMBER_OF_CLIENTS = 5;
    private static int totalWordCount = 0; // Counter for total word count from all clients
    private static long startTime; // Variable to track start time of server

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Enter the file path:"); // Prompt user to enter file path
        Scanner scanner = new Scanner(System.in);
        String originalFileName = scanner.nextLine(); // Read file path from user input
        scanner.close();

        try (ServerSocket serverSocket = new ServerSocket(PORT_NUMBER)) {
            System.out.println("Server started. Waiting for clients...");
            byte[] originalFileContent = Files.readAllBytes(Paths.get(originalFileName)); // Read content of the file
            List<byte[]> segments = divideFileIntoSegments(originalFileContent, NUMBER_OF_CLIENTS); // Divide file into segments
            List<Thread> threads = new ArrayList<>(); // List to hold client processing threads
            startTime = System.currentTimeMillis(); // Record start time of server operation

            for (int i = 0; i < NUMBER_OF_CLIENTS; i++) {
                Socket clientSocket = serverSocket.accept(); // Accept client connection
                System.out.println("Client " + (i + 1) + " connected.");
                byte[] segment = segments.get(i); // Get corresponding segment for client
                Thread thread = new Thread(() -> processClient(clientSocket, segment)); // Create a thread to process client
                threads.add(thread); // Add the thread to list
                thread.start(); // Start the thread
            }

            for (Thread thread : threads) {
                thread.join(); // Wait for all client threads to finish
            }

            long endTime = System.currentTimeMillis(); // Record end time of server operation
            System.out.println("Total word count from all clients: " + totalWordCount); // Display total word count
            System.out.println("Total Time: " + (endTime - startTime) + " milliseconds"); // Display total time taken
        }
    }

    // Method to divide the file content into segments
    private static List<byte[]> divideFileIntoSegments(byte[] fileContent, int numberOfSegments) {
        List<byte[]> segments = new ArrayList<>(); // List to hold file segments
        int segmentSize = fileContent.length / numberOfSegments; // Calculate segment size
        int remainder = fileContent.length % numberOfSegments; // Calculate remainder
        int start = 0;

        for (int i = 0; i < numberOfSegments; i++) {
            int end = start + segmentSize + (remainder-- > 0 ? 1 : 0); // Calculate end index for each segment
            byte[] segment = new byte[end - start]; // Create segment array
            System.arraycopy(fileContent, start, segment, 0, segment.length); // Copy data to segment array
            segments.add(segment); // Add segment to the list
            start = end; // Update start index for next segment
        }

        return segments; // Return list of segments
    }

    // Method to process client requests
    private static void processClient(Socket clientSocket, byte[] segment) {
        try {
            DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream());
            dos.writeInt(segment.length); // Send segment length to client
            dos.write(segment); // Send segment data to client
            dos.flush();
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            totalWordCount += Integer.parseInt(in.readLine()); // Receive word count from client and update total
            clientSocket.close(); // Close client connection
        } catch (IOException e) {
            e.printStackTrace(); // Handle any I/O errors
        }
    }
}
