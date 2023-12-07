import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;

public class HTTPClient {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 8080;

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            // Sending 200 messages to the server
            for (int i = 1; i <= 200; i++) {
                String response = sendAndReceiveMessage("Client ID: " + i);
                System.out.println("Client " + i + " received: " + response);
            }

            System.out.print("Would you like to play the guessing game? (yes/no): ");
            String playGame = scanner.nextLine();
            if ("yes".equalsIgnoreCase(playGame)) {
                playGuessingGame(scanner);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void playGuessingGame(Scanner scanner) {
        System.out.print("Enter your client ID for the game: ");
        int clientId = scanner.nextInt();
        scanner.nextLine(); // Consume the newline

        boolean continuePlaying = true;
        while (continuePlaying) {
            System.out.print("Enter your guess (1-100): ");
            int guess = scanner.nextInt();
            scanner.nextLine(); // Consume the newline

            String serverResponse = sendAndReceiveMessage("Client " + clientId + " guesses " + guess);
            System.out.println("Server: " + serverResponse);

            if (serverResponse.contains("Correct")) {
                System.out.print("Do you want to play again? (yes/no): ");
                String playAgain = scanner.nextLine();
                continuePlaying = "yes".equalsIgnoreCase(playAgain);
            }
        }
    }

    private static String sendAndReceiveMessage(String message) {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             OutputStream out = socket.getOutputStream();
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.write((message + "\n").getBytes());
            out.flush();

            return in.readLine();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return "Error: " + e.getMessage();
        }
    }
}