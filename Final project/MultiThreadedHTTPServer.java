import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class MultiThreadedHTTPServer {
    public static final int PORT = 8080;
    private static final Map<Integer, Integer> clientGames = new ConcurrentHashMap<>(); // Maps client ID to secret number

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started and listening on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                new ClientHandler(clientSocket).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static class ClientHandler extends Thread {
        private final Socket clientSocket;

        ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 OutputStream out = clientSocket.getOutputStream()) {

                String clientMessage;
                while ((clientMessage = in.readLine()) != null) {
                    String response = generateResponse(clientMessage);
                    out.write((response + "\n").getBytes());
                    out.flush();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    clientSocket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        private String generateResponse(String clientMessage) {
            if (clientMessage.startsWith("Client ID:")) {
                int clientId = Integer.parseInt(clientMessage.substring(10).trim());
                int secretNumber = new Random().nextInt(100) + 1;
                clientGames.put(clientId, secretNumber);
                System.out.println("New game started for client " + clientId + " with number " + secretNumber);
                return "Thanks for your message: " + clientMessage;
            } else {
                try {
                    String[] parts = clientMessage.split(" ");
                    int clientId = Integer.parseInt(parts[1]);
                    int guess = Integer.parseInt(parts[3]);
                    int secretNumber = clientGames.getOrDefault(clientId, -1);

                    if (secretNumber == -1) {
                        return "Client " + clientId + ": No game found. Please send your client ID first.";
                    }

                    if (guess == secretNumber) {
                        int newSecretNumber = new Random().nextInt(100) + 1;
                        clientGames.put(clientId, newSecretNumber);
                        return "Client " + clientId + ": Correct! The number was " + secretNumber + ". New number generated.";
                    } else if (guess < secretNumber) {
                        return "Client " + clientId + ": Too low! Try a higher number.";
                    } else {
                        return "Client " + clientId + ": Too high! Try a lower number.";
                    }
                } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                    return "Invalid format. Please send a message in the format: 'Client [id] guesses [number]'.";
                }
            }
        }
    }
}