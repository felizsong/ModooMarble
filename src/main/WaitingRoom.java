package main;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class WaitingRoom extends JFrame {
    private ArrayList<PlayerSlot> playerSlots; // 플레이어 슬롯 저장
    private JButton startButton;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private int playerIndex = -1; // 플레이어의 인덱스 (서버에서 할당)

    public WaitingRoom() {
        super("대기실");
        setLayout(new BorderLayout());
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        playerSlots = new ArrayList<>();
        JPanel playerPanel = new JPanel(new GridLayout(4, 1));
        JPanel chatPanel = new JPanel(new BorderLayout());

        // 플레이어 슬롯 초기화
        for (int i = 0; i < 4; i++) {
            PlayerSlot slot = new PlayerSlot("대기 중...", false);
            playerSlots.add(slot);
            playerPanel.add(slot);
        }

        // 채팅 패널
        JTextArea chatArea = new JTextArea();
        chatArea.setEditable(false);
        JTextField chatInput = new JTextField();
        JButton sendButton = new JButton("전송");

        chatPanel.add(new JScrollPane(chatArea), BorderLayout.CENTER);
        chatPanel.add(chatInput, BorderLayout.SOUTH);
        chatPanel.add(sendButton, BorderLayout.EAST);

        // 게임 시작 버튼
        startButton = new JButton("게임시작");
        startButton.setEnabled(false);
        startButton.addActionListener(e -> out.println("START_GAME"));

        // UI 배치
        add(playerPanel, BorderLayout.WEST);
        add(chatPanel, BorderLayout.CENTER);
        add(startButton, BorderLayout.SOUTH);

        setVisible(true);

        // 서버 연결
        connectToServer(chatArea, chatInput);
    }

    private void connectToServer(JTextArea chatArea, JTextField chatInput) {
        try {
            socket = new Socket("localhost", 12345);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // 서버에서 메시지 수신 처리
            new Thread(() -> {
                try {
                    while (true) {
                        String message = in.readLine();
                        if (message.startsWith("ASSIGN_INDEX")) {
                            playerIndex = Integer.parseInt(message.split(" ")[1]);
                            playerSlots.get(playerIndex).setEditable(true);
                            if (playerIndex == 0) {
                                startButton.setEnabled(true); // 첫 번째 입장자만 시작 버튼 활성화
                            }
                        } else if (message.startsWith("PLAYER_JOINED")) {
                            String[] parts = message.split(" ", 3);
                            int index = Integer.parseInt(parts[1]);
                            playerSlots.get(index).setName(parts[2]);
                        } else if (message.startsWith("CHAT")) {
                            chatArea.append(message.substring(5) + "\n");
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            // 채팅 메시지 전송
            chatInput.addActionListener(e -> {
                out.println("CHAT " + chatInput.getText());
                chatInput.setText("");
            });
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "서버에 연결할 수 없습니다.", "오류", JOptionPane.ERROR_MESSAGE);
        }
    }

    // 플레이어 슬롯 클래스
    class PlayerSlot extends JPanel {
        private JTextField nameField;
        private JLabel avatarLabel;

        public PlayerSlot(String defaultName, boolean isEditable) {
            setLayout(new BorderLayout());
            nameField = new JTextField(defaultName);
            nameField.setEditable(isEditable);
            avatarLabel = new JLabel("아바타", SwingConstants.CENTER);
            avatarLabel.setPreferredSize(new Dimension(80, 80));
            avatarLabel.setOpaque(true);
            avatarLabel.setBackground(Color.RED);

            add(avatarLabel, BorderLayout.WEST);
            add(nameField, BorderLayout.CENTER);
        }

        public void setName(String name) {
            nameField.setText(name);
        }

        public void setEditable(boolean editable) {
            nameField.setEditable(editable);
        }
    }
}
