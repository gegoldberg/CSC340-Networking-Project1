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
    private static final int portNumber = 12345;
    private static final int numberOfClients = 2;
    private static int totalWordCount = 0;
    private static long startTime;

    public static void main(String[] args) {
        System.out.println("Enter the file path:");
        Scanner scanner = new Scanner(System.in);
        String originalFileName = scanner.nextLine();
        scanner.close();

        List<Socket> clientSockets = new ArrayList<>();
        List<Thread> threads = new ArrayList<>();

        try (ServerSocket serverSocket = new ServerSocket(portNumber)) {
            System.out.println("Server started.");
            System.out.println("Waiting for clients...");

            byte[] originalFileContent = Files.readAllBytes(Paths.get(originalFileName));
            List<byte[]> segments = divideFileIntoSegments(originalFileContent, numberOfClients);

            // Accept all clients before processing
            for (int i = 0; i < numberOfClients; i++) {
                Socket clientSocket = serverSocket.accept();
                clientSockets.add(clientSocket);
                System.out.println("Client " + (i + 1) + " connected.");
            }

            startTime = System.currentTimeMillis(); // Start timing after all clients are connected

            // Distribute segments and process clients in parallel
            for (int i = 0; i < numberOfClients; i++) {
                final Socket clientSocket = clientSockets.get(i);
                final byte[] segment = segments.get(i);
                Thread thread = new Thread(() -> processClient(clientSocket, segment));
                threads.add(thread);
                thread.start();
            }

            // Wait for all threads to finish
            for (Thread thread : threads) {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    System.out.println("A processing thread was interrupted.");
                }
            }

            long endTime = System.currentTimeMillis();
            System.out.println("Total word count from all clients: " + totalWordCount);
            System.out.println("Total Time: " + (endTime - startTime) + " milliseconds");
        } catch (IOException e) {
            System.out.println("Server exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

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

    private static void processClient(Socket clientSocket, byte[] segment) {
        try {
            DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream());
            dos.writeInt(segment.length);
            dos.write(segment);
            dos.flush();

            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            synchronized (Server.class) {
                totalWordCount += Integer.parseInt(in.readLine());
            }
            clientSocket.close();
        } catch (IOException e) {
            System.out.println("Error processing client: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
