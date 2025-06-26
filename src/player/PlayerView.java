package player;

import country.CountryDetailPanel;
import font.FontLoader;
import main.MainFrame;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;

public class PlayerView extends JFrame {
	private MainFrame mainFrame;
	private String playerName; // 플레이어 이름
	public static JLabel[] playerIconLabel = new JLabel[4];
	public static JLabel[] playerGoldLabel = new JLabel[4];
	public static JLabel[] playerNameLabel = new JLabel[4]; // 플레이어 이름 레이블
	public static int[] playerGold = {2000000, 2000000, 2000000, 2000000};
	private Font boldFont = FontLoader.loadCustomFont("src/font/GodoB.otf", 18f); // 굵은 폰트
	private Font regularFont = FontLoader.loadCustomFont("src/font/GodoM.otf", 14f); // 일반 폰트

	// 채팅 UI 컴포넌트
	private static JTextArea chatArea;
	private JTextField chatInput;
	private PrintWriter out;
	private Socket socket;
	private int playerNumber; // 서버로부터 받은 플레이어 번호
	private int currentTurnPlayer = 1; // 초기값은 1번 플레이어

	public PlayerView(MainFrame mainFrame, String playerName) { // 이름 전달받음
		super("플레이어 목록");
		setSize(400, 860);
		this.mainFrame = mainFrame;
		this.playerName = playerName; // 이름 저장
		setLocation(mainFrame.getLocation().x + mainFrame.getWidth(), mainFrame.getLocation().y);
		setLayout(new BorderLayout());

		// 플레이어 목록과 채팅창을 JSplitPane으로 나눔
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, playerListView(), chatPanelSetup());
		splitPane.setResizeWeight(0.6); // 위쪽 60%, 아래쪽 40% 비율로 나눔
		splitPane.setDividerSize(5); // 구분선의 두께 설정
		splitPane.setDividerLocation(500); // 초기 구분선 위치 설정
		splitPane.setEnabled(false); // 구분선 이동 비활성화

		add(splitPane, BorderLayout.CENTER);
		chatServerSetup();
	}

	// 서버와 연결 설정
	private void chatServerSetup() {
		try {
			this.socket = new Socket("localhost", 12345);
			out = new PrintWriter(socket.getOutputStream(), true);
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			out.println(playerName); // 서버에 이름 전송

			// 서버 메시지 수신 스레드
			new Thread(() -> {
				String serverMessage;
				try {
					while ((serverMessage = in.readLine()) != null) {
						if (serverMessage.startsWith("PLAYER_MOVE")) {
							System.out.println("SERVER_MESSAGE: " + serverMessage);

							String[] parts = serverMessage.split(" ");
							int movedPlayer = Integer.parseInt(parts[1]) - 1;
							int newPosition = Integer.parseInt(parts[2]);

							SwingUtilities.invokeLater(() -> {
								PlayerPanel.movePlayer(movedPlayer, newPosition);
							});
						} else if (serverMessage.startsWith("PLAYER_NAMES")) {
							String[] names = serverMessage.substring(13).split(",");
							int connectedCount = 0;

							for (String name : names) {
								if (!name.contains("NONE")) { // 연결된 플레이어만 카운트
									connectedCount++;
								}
							}
							SwingUtilities.invokeLater(() -> {
								for (int i = 0; i < names.length; i++) {
									String displayName = names[i].equals("NONE") ? "플레이어 " + (i + 1) : "플레이어 " + (i + 1) + ": " + names[i];
									playerNameLabel[i].setText(displayName); // 실시간 동기화
								}
							});
							PlayerPanel.setConnectedPlayers(connectedCount); // 연결된 플레이어 수 업데이트
						} else if (serverMessage.startsWith("PLAYER_NUMBER")) {
							playerNumber = Integer.parseInt(serverMessage.split(" ")[1]);
							SwingUtilities.invokeLater(() -> {
								playerNameLabel[playerNumber - 1].setText("플레이어 " + playerNumber + " : " + playerName);
								chatArea.append("당신은 플레이어 " + playerNumber + " 입니다.\n");

								mainFrame.initializePanels(socket, playerNumber);
							});
						} else if (serverMessage.startsWith("NEXT_TURN")) {
							int turnPlayer = Integer.parseInt(serverMessage.split(" ")[1]);
							PlayerPanel.playerOrder = turnPlayer;

							SwingUtilities.invokeLater(() -> {
								if (playerNumber == turnPlayer) {
									CountryDetailPanel.diceClickBtn.setEnabled(true);
									chatArea.append("당신의 턴입니다! 주사위를 굴리세요.\n");
								} else {
									CountryDetailPanel.diceClickBtn.setEnabled(false);
									chatArea.append("플레이어 " + playerNumber + "의 턴입니다.\n");
								}
							});
						} else if (serverMessage.startsWith("UPDATE_GOLD")) {
							String[] parts = serverMessage.split(" ");
							int playerNumber = Integer.parseInt(parts[1]);
							int amount = Integer.parseInt(parts[2]);

							// 골드 업데이트 및 UI 갱신
							SwingUtilities.invokeLater(() -> {
								PlayerView.deductGold(playerNumber, -amount);
							});
						} else {
							String finalServerMessage = serverMessage;
							SwingUtilities.invokeLater(() -> chatArea.append(finalServerMessage + "\n"));
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}).start();

		} catch (IOException e) {
			JOptionPane.showMessageDialog(this, "채팅 서버에 연결할 수 없습니다.", "Error", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}

	// 플레이어 목록 보여줌
	public JPanel playerListView() {
		JPanel playerPanel = new JPanel(new GridLayout(0, 1)); // 플레이어 리스트 전체를 담을 패널

		for (int i = 0; i < 4; i++) {
			// 개별 플레이어 정보 패널
			JPanel playerInfoPanel = new JPanel();
			playerInfoPanel.setLayout(new BoxLayout(playerInfoPanel, BoxLayout.X_AXIS));
			playerInfoPanel.setBorder(BorderFactory.createEmptyBorder(10, 30, 0, 10)); // 여백 추가

			// 플레이어 아이콘
			Image img = new ImageIcon("./images/player" + (i + 1) + ".png").getImage()
					.getScaledInstance(100, 100, Image.SCALE_SMOOTH); // 이미지 크기 조정
			playerIconLabel[i] = new JLabel(new ImageIcon(img)); // 배열에서 아이콘 레이블 설정

			// 플레이어 이름과 골드 정보
			JPanel playerDetailsPanel = new JPanel();
			playerDetailsPanel.setLayout(new BoxLayout(playerDetailsPanel, BoxLayout.Y_AXIS)); // 세로 정렬
			playerDetailsPanel.setAlignmentX(Component.LEFT_ALIGNMENT); // 전체적으로 LEFT 정렬

			// 플레이어 이름
			playerNameLabel[i] = new JLabel("플레이어 " + (i + 1));
			playerNameLabel[i].setFont(regularFont);
			playerNameLabel[i].setAlignmentX(Component.LEFT_ALIGNMENT); // 이름을 LEFT 정렬

			// 골드 정보 패널
			JPanel goldInfoPanel = new JPanel();
			goldInfoPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0)); // 컴포넌트 간격 최소화
			goldInfoPanel.setAlignmentX(Component.LEFT_ALIGNMENT); // 골드 정보 LEFT 정렬
			JLabel goldTextLabel = new JLabel("현 금: ");
			goldTextLabel.setFont(regularFont);

			JLabel goldValueLabel = new JLabel(formatGold(playerGold[i])); // 금액 부분
			goldValueLabel.setFont(boldFont);

			goldInfoPanel.add(goldTextLabel);
			goldInfoPanel.add(goldValueLabel);

			playerGoldLabel[i] = goldValueLabel; // 골드 값 레이블 저장

			// 이름과 골드 정보를 세로로 배치
			playerDetailsPanel.add(playerNameLabel[i]);
			playerDetailsPanel.add(Box.createVerticalStrut(30)); // 이름과 골드 사이 간격
			playerDetailsPanel.add(goldInfoPanel);

			// 아이콘과 세부 정보를 가로로 배치
			playerInfoPanel.add(playerIconLabel[i]); // 아이콘 추가
			playerInfoPanel.add(Box.createHorizontalStrut(10)); // 아이콘과 정보 사이 여백
			playerInfoPanel.add(playerDetailsPanel); // 세부 정보 추가

			playerPanel.add(playerInfoPanel); // 최종 패널에 추가
		}

		return playerPanel;
	}

	// 채팅 UI 설정
	private JPanel chatPanelSetup() {
		JPanel chatPanel = new JPanel(new BorderLayout());

		chatArea = new JTextArea();
		chatArea.setEditable(false);
		chatArea.setLineWrap(true);
		chatArea.setFont(new Font("Arial", Font.PLAIN, 14)); // 채팅 폰트 키움
		JScrollPane scrollPane = new JScrollPane(chatArea);

		chatInput = new JTextField();
		chatInput.setFont(new Font("Arial", Font.PLAIN, 12));
		chatInput.addActionListener(e -> {
			String message = chatInput.getText().trim();
			if (!message.isEmpty()) {
				out.println(message); // 서버로 메시지 전송
				chatInput.setText(""); // 입력창 초기화
			}
		});

		chatPanel.add(scrollPane, BorderLayout.CENTER);
		chatPanel.add(chatInput, BorderLayout.SOUTH);
		return chatPanel;
	}

	// 메시지를 chatArea에 추가
	public static void appendMessage(String message) {
		SwingUtilities.invokeLater(() -> {
			chatArea.append(message + "\n");
			chatArea.setCaretPosition(chatArea.getDocument().getLength()); // 스크롤을 최신 메시지로 이동
		});
	}

	// 골드 차감
	public static void deductGold(int playerIndex, int amount) {
		if (playerIndex >= 0 && playerIndex < playerGold.length) {
			playerGold[playerIndex] -= amount;

			// 골드 값 화면 업데이트
			playerGoldLabel[playerIndex - 1].setText(formatGold(playerGold[playerIndex]));
		}
	}

	public static int getPlayerGold(int playerIndex) {
		return playerGold[playerIndex]; // 특정 플레이어의 골드를 반환
	}

	// 숫자를 한글 단위로 포맷하는 메서드
	public static String formatGold(int amount) {
		if (amount <= 0) return "0원";

		int billion = amount / 100000000;
		int million = (amount % 100000000) / 10000;
		int remaining = amount % 10000;

		StringBuilder result = new StringBuilder();
		if (billion > 0) result.append(billion).append("억 ");
		if (million > 0) result.append(million).append("만 ");
		if (remaining > 0 || (billion == 0 && million == 0)) result.append(remaining).append("원");

		return result.toString().trim();
	}
}
