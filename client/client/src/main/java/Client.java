import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

public class Client {
    private static final int DEFAULT_PORT = 5000;
    private static final int BUFFER_SIZE = 16;
    private static final int TIMEOUT_MS = 5000;  // 5 seconds timeout

    public static void main(String[] args) {
        // Validate command-line arguments
        if (args.length < 2 || args.length > 3) {
            printUsage();
            System.exit(1);
        }

        String serverIP = args[0];
        int serverPort = DEFAULT_PORT;
        String message;

        // Parse arguments based on count
        if (args.length == 2) {
            // Format: java Client <ip> <message>
            message = args[1];
        } else {
            // Format: java Client <ip> <port> <message>
            try {
                serverPort = Integer.parseInt(args[1]);
                validatePort(serverPort);
            } catch (IllegalArgumentException e) {
                System.err.println("Error: Invalid port number - " + e.getMessage());
                System.exit(1);
            }
            message = args[2];
        }

        // Validate string length
        if (message.length() >= BUFFER_SIZE) {
            System.err.println("Error: String exceeds " + (BUFFER_SIZE - 1) + " characters");
            System.exit(1);
        }

        try {
            // Create UDP socket
            DatagramSocket socket = new DatagramSocket();
            socket.setSoTimeout(TIMEOUT_MS);

            // Convert message to bytes
            byte[] sendBuffer = message.getBytes();

            // Get server address
            InetAddress serverAddress = InetAddress.getByName(serverIP);

            // Create and send packet
            DatagramPacket sendPacket = new DatagramPacket(
                    sendBuffer,
                    sendBuffer.length,
                    serverAddress,
                    serverPort
            );
            socket.send(sendPacket);
            System.out.println("Sent '" + message + "' to " + serverIP + ":" + serverPort);

            // Prepare and receive response
            byte[] receiveBuffer = new byte[BUFFER_SIZE];
            DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
            socket.receive(receivePacket);

            // Extract response
            String response = new String(
                    receivePacket.getData(),
                    0,
                    receivePacket.getLength()
            );
            System.out.println("Server response: Character count = " + response);

            // Close socket
            socket.close();

        } catch (UnknownHostException e) {
            System.err.println("Error: Unknown host - " + serverIP);
            System.exit(1);
        } catch (SocketTimeoutException e) {
            System.err.println("Error: No response from server (timeout)");
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Network error: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void printUsage() {
        System.err.println("Usage:");
        System.err.println("  java Client <server_ip> <message>");
        System.err.println("  java Client <server_ip> <port> <message>");
        System.err.println("Examples:");
        System.err.println("  java Client 192.168.1.100 \"Hello Java\"");
        System.err.println("  java Client 192.168.1.100 5000 \"Hello Java\"");
        System.err.println("  java Client 127.0.0.1 9999 \"Local test\"");
    }

    private static void validatePort(int port) {
        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("Port must be between 1 and 65535");
        }
    }
}