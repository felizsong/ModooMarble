package main;

import country.*;
import player.PlayerPanel;
import player.PlayerView;
import sound.SoundManager;

import javax.swing.*;
import java.awt.*;
import java.net.Socket;

public class MainFrame extends JFrame {
    private static CountryDetailPanel detailPanel;

    public MainFrame() {
        super("모두의 마블");
        setSize(860, 860);
        setResizable(false);

        // 각각의 라인 나라 panel을 생성
        add(new GreenLineButtonPanel(), "South");
        add(new BlueLineButtonPanel(), "West");
        add(new PurpleLineButtonPanel(), "North");
        add(new RedLineButtonPanel(), "East");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);

        // PlayerView 및 PlayerPanel 생성
        SwingUtilities.invokeLater(() -> {
            String playerName = JOptionPane.showInputDialog(null, "이름을 입력하세요:", "게임 입장", JOptionPane.PLAIN_MESSAGE);
            if (playerName == null || playerName.trim().isEmpty()) {
                playerName = "Player1"; // 기본 이름
            }

            new PlayerView(this, playerName).setVisible(true); // 이름 전달
        });
    }

    public void initializePanels(Socket socket, int playerNumber) {
        // CountryDetailPanel에 현재 접속 중인 playerNumber 전달
        detailPanel = new CountryDetailPanel(playerNumber);
        add(detailPanel, BorderLayout.CENTER);

        // PlayerPanel 생성
        new PlayerPanel(socket);

        // 레이아웃을 업데이트하여 새 패널을 반영
        revalidate();
        repaint();
    }

    public static void main(String[] args) {
        new MainFrame();
    }
}
