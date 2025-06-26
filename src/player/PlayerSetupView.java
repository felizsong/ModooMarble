package player;

import main.MainFrame;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class PlayerSetupView extends JFrame {
    private ArrayList<PlayerConfig> playerConfigs; // 플레이어 설정 저장
    private JTextField[] nameFields = new JTextField[4];
    private JLabel[] imagePreviews = new JLabel[4];
    private String[] selectedImages = new String[4]; // 이미지 경로 저장
    private Socket socket; // 서버와 연결된 소켓
    private PrintWriter out;

    public PlayerSetupView() {
        super("플레이어 설정");
        setLayout(new GridLayout(6, 3));
        setSize(1000, 860);

        playerConfigs = new ArrayList<>();
        connectToServer(); // 서버에 연결

        // 플레이어 설정 UI
        for (int i = 0; i < 4; i++) {
            JLabel nameLabel = new JLabel("플레이어 " + (i + 1) + " 이름:");
            nameFields[i] = new JTextField("Player" + (i + 1));
            JButton imageButton = new JButton("캐릭터 선택");
            imagePreviews[i] = new JLabel("미리보기", SwingConstants.CENTER);
            imagePreviews[i].setPreferredSize(new Dimension(80, 80));

            int index = i;
            imageButton.addActionListener(e -> selectImage(index));

            add(nameLabel);
            add(nameFields[i]);
            add(imageButton);
            add(imagePreviews[i]);
        }

        // 완료 버튼
        JButton completeButton = new JButton("완료");
        completeButton.addActionListener(e -> savePlayerConfigs());
        add(completeButton);

        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    // 서버에 연결하는 메서드
    private void connectToServer() {
        try {
            socket = new Socket("localhost", 12345); // 서버 주소 및 포트
            out = new PrintWriter(socket.getOutputStream(), true);
            System.out.println("서버에 연결되었습니다.");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "서버에 연결할 수 없습니다.", "연결 실패", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    // 이미지 선택 메서드
    private void selectImage(int playerIndex) {
        JFileChooser fileChooser = new JFileChooser();
        int returnValue = fileChooser.showOpenDialog(this);

        if (returnValue == JFileChooser.APPROVE_OPTION) {
            selectedImages[playerIndex] = fileChooser.getSelectedFile().getAbsolutePath();
            ImageIcon icon = new ImageIcon(new ImageIcon(selectedImages[playerIndex])
                    .getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH));
            imagePreviews[playerIndex].setIcon(icon);
        }
    }

    // 설정 저장 및 전달
    private void savePlayerConfigs() {
        // 플레이어 설정 저장 및 서버에 이름 전송
        for (int i = 0; i < 4; i++) {
            String playerName = nameFields[i].getText();
            String imagePath = selectedImages[i] != null ? selectedImages[i] : "./images/default.png";
            playerConfigs.add(new PlayerConfig(playerName, imagePath));

            // 서버에 플레이어 이름 전송
            out.println(playerName);
        }

/*        // MainFrame 실행
        SwingUtilities.invokeLater(() -> {
            MainFrame mainFrame = new MainFrame();
            mainFrame.setVisible(true);
            new PlayerView(mainFrame, "하이").setVisible(true);
        });*/

        dispose(); // 현재 창 닫기
    }

    // 플레이어 설정 저장 클래스
    public static class PlayerConfig {
        String name;
        String imagePath;

        public PlayerConfig(String name, String imagePath) {
            this.name = name;
            this.imagePath = imagePath;
        }
    }
}
