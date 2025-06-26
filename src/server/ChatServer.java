package server;

import player.PlayerPanel;
import sound.SoundManager;

import java.io.*;
import java.net.*;
import java.util.*;

import static server.ChatServer.broadcastMessage;

public class ChatServer {
    private static final int PORT = 12345;
    private static final int MAX_PLAYERS = 4;
    private static Vector<ClientHandler> clients = new Vector<>();
    static Map<Integer, String> playerNames = new HashMap<>(); // 플레이어 이름 저장
    static Map<Integer, Integer> playerPositions = new HashMap<>(); // 플레이어 위치
    static Map<Integer, Integer> playerGold = new HashMap<>();  // 플레이어 골드
    private static Queue<Integer> availablePlayerNumbers = new PriorityQueue<>(); // 빈 번호 우선적으로 사용

    public static void main(String[] args) {
        initializeAvailablePlayerNumbers(); // 번호 초기화

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("서버가 시작되었습니다.");
            while (true) {
                Socket clientSocket = serverSocket.accept();
                SoundManager.playSoundBackground("./sound/GamePlay.wav");

                // 플레이어 번호 할당
                if (availablePlayerNumbers.isEmpty()) {
                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                    out.println("플레이어가 다 찼습니다.");
                    clientSocket.close();
                    continue;
                }

                int playerNumber = availablePlayerNumbers.poll(); // 빈 번호를 우선 사용
                ClientHandler clientHandler = new ClientHandler(clientSocket, playerNumber);
                clients.add(clientHandler);
                clientHandler.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void initializeAvailablePlayerNumbers() {
        availablePlayerNumbers.clear();
        for (int i = 1; i <= MAX_PLAYERS; i++) {
            availablePlayerNumbers.add(i);
        }
    }

    static void broadcastMessage(String message, ClientHandler sender) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    static void broadcastPlayerNames() {
        StringBuilder namesMessage = new StringBuilder("PLAYER_NAMES ");
        for (int i = 1; i <= MAX_PLAYERS; i++) {
            namesMessage.append(playerNames.getOrDefault(i, "NONE")).append(",");
        }
        String message = namesMessage.toString();
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    static void removeClient(ClientHandler clientHandler) {
        clients.remove(clientHandler);
        int playerNumber = clientHandler.getPlayerNumber();
        playerNames.remove(playerNumber);
        playerPositions.remove(playerNumber);
        availablePlayerNumbers.add(playerNumber); // 빈 번호를 다시 사용 가능하게 추가
        System.out.println("클라이언트가 끊어졌습니다: " + clientHandler.getClientName());
        broadcastPlayerNames();
    }
    // 골드 초기화
    static {
        for (int i = 1; i <= MAX_PLAYERS; i++) {
            playerGold.put(i, 2000000);
        }
    }

    // 골드 업데이트
    static void updateGold(int playerNumber, int amount) {
        playerGold.put(playerNumber, playerGold.get(playerNumber) + amount);

        // 모든 클라이언트에게 변경 사항 전송
        broadcastMessage("UPDATE_GOLD " + playerNumber + " " + amount, null);
    }
}

class ClientHandler extends Thread {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String clientName;
    private int playerNumber;

    public ClientHandler(Socket socket, int playerNumber) {
        this.socket = socket;
        this.playerNumber = playerNumber;
        ChatServer.playerPositions.putIfAbsent(playerNumber, 0); // 초기 위치
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // 클라이언트로부터 이름 입력 받기
            clientName = in.readLine();
            ChatServer.playerNames.put(playerNumber, clientName);

            // 플레이어 번호 전송
            sendMessage("PLAYER_NUMBER " + playerNumber);

            // 접속 알림 브로드캐스트 및 이름 업데이트
            broadcastMessage("📢 " + clientName + "님이 입장하셨습니다.", this);
            ChatServer.broadcastPlayerNames();

            // 메시지 수신
            String message;
            while ((message = in.readLine()) != null) {
                if (message.startsWith("PLAYER_MOVE")) {
                    String[] parts = message.split(" ");
                    int movedPlayer = Integer.parseInt(parts[1]);
                    int diceResult = Integer.parseInt(parts[2]);

                    // 서버에서 받은 데이터로 플레이어 이동
                    broadcastMessage("PLAYER_MOVE " + movedPlayer + " " + diceResult, null);
                } else if (message.startsWith("NEXT_TURN")) {
                    String[] parts = message.split(" ");
                    int nextTurnPlayer = Integer.parseInt(parts[1]);

                    // PlayerPanel의 playerOrder 업데이트
                    PlayerPanel.playerOrder = nextTurnPlayer;
                    broadcastMessage("NEXT_TURN " + nextTurnPlayer, null);
                }
                else if (message.startsWith("UPDATE_GOLD")) {
                    String[] parts = message.split(" ");
                    int playerNumber = Integer.parseInt(parts[1]);
                    int amount = Integer.parseInt(parts[2]);

                    ChatServer.updateGold(playerNumber, amount); // 서버 골드 업데이트 및 브로드캐스트
                } else {
                    broadcastMessage(clientName + ": " + message, this);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ChatServer.removeClient(this);
            broadcastMessage("📢 " + clientName + "님이 떠났습니다.", null);
        }
    }

    public void sendMessage(String message) {
        out.println(message);
    }

    public String getClientName() {
        return clientName;
    }

    public int getPlayerNumber() {
        return playerNumber;
    }
}
