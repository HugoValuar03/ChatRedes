// ChatServer.java
// Este é o servidor que gerencia conexões de múltiplos clientes usando TCP.
// Ele retransmite mensagens de um cliente para todos os outros conectados.
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class ChatServer {
    protected static final List<ClientHandler> clients = new ArrayList<>(); // Lista de clientes conectados

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(5000); // Cria servidor na porta 5000
            System.out.println("Servidor de chat iniciado na porta 5000. Aguardando conexões...");

            while (true) {
                Socket clientSocket = serverSocket.accept(); // Aceita nova conexão
                System.out.println("Novo cliente conectado: " + clientSocket.getInetAddress().getHostAddress());

                // Cria e inicia uma thread para o novo cliente
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                synchronized (clients) {
                    clients.add(clientHandler); // Adiciona cliente à lista
                }
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            System.err.println("Erro ao iniciar o servidor: " + e.getMessage());
        }
    }
}

// Classe para gerenciar cada cliente conectado
class ClientHandler implements Runnable {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String username;

    public ClientHandler(Socket socket) {
        this.socket = socket;
        try {
            out = new PrintWriter(socket.getOutputStream(), true); // Stream de saída
            in = new BufferedReader(new InputStreamReader(socket.getInputStream())); // Stream de entrada
            // Solicita nome de usuário ao conectar
            out.println("Digite seu nome de usuário: ");
            username = in.readLine(); // Lê o nome enviado pelo cliente
            BroadcastMessage(username + " entrou no chat!");
        } catch (IOException e) {
            System.err.println("Erro na conexão com cliente: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        try {
            String message;
            while ((message = in.readLine()) != null) {
                if (!message.isEmpty()) {
                    String broadcastMsg = username + ": " + message; // Formata mensagem com nome
                    BroadcastMessage(broadcastMsg); // Envia para todos
                }
            }
        } catch (IOException e) {
            System.err.println("Cliente " + username + " desconectou: " + e.getMessage());
        } finally {
            if (username != null) {
                BroadcastMessage(username + " saiu do chat!");
            }
            synchronized (ChatServer.clients) {
                ChatServer.clients.remove(this); // Remove cliente da lista
            }
            try {
                socket.close(); // Fecha conexão
            } catch (IOException e) {
                System.err.println("Erro ao fechar socket: " + e.getMessage());
            }
        }
    }

    // Método para enviar mensagem a todos os clientes
    private void BroadcastMessage(String message) {
        synchronized (ChatServer.clients) {
            for (ClientHandler client : ChatServer.clients) {
                client.out.println(message); // Envia mensagem a cada cliente
            }
        }
    }
}