// ChatClient.java
// Este é o cliente que se conecta ao servidor e permite enviar/receber mensagens.
// Inclui usabilidade com nome de usuário e saída formatada.
import java.io.*;
import java.net.*;
import java.util.Scanner;

public class ChatClient {
    public static void main(String[] args) {
        String serverAddress = "locahost"; // Endereço do servidor (ajuste para teste remoto)
        int serverPort = 5000; // Porta do servidor

        try (Socket socket = new Socket(serverAddress, serverPort);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             Scanner scanner = new Scanner(System.in)) {

            System.out.println("Conectado ao servidor de chat!");

            // Thread para receber mensagens
            new Thread(() -> {
                try {
                    String message;
                    while ((message = in.readLine()) != null) {
                        System.out.println(message); // Exibe mensagens recebidas
                    }
                } catch (IOException e) {
                    System.err.println("Erro ao receber mensagens: " + e.getMessage());
                }
            }).start();

            // Lê e envia nome de usuário
            String username = in.readLine(); // Aguarda prompt do servidor
            System.out.print(username); // Exibe prompt
            out.println(scanner.nextLine()); // Envia nome digitado

            // Loop para enviar mensagens
            while (true) {
                String message = scanner.nextLine();
                if (message.equalsIgnoreCase("sair")) {
                    out.println("sair");
                    break;
                }
                out.println(message); // Envia mensagem ao servidor
            }

        } catch (IOException e) {
            System.err.println("Erro ao conectar ao servidor: " + e.getMessage());
        }
    }
}