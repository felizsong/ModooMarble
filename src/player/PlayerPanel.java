package player;

import country.CountryDetailPanel;
import list.CountryButtonList;
import list.DiceNumberList;
import list.PlayerList;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class PlayerPanel extends JPanel {
	private static JLabel[] label = new JLabel[4];
	private static int[] blockedTurns = {0, 0, 0, 0}; // 각 플레이어의 차단된 턴 수
	public static int playerOrder = 1;
	private static int totalMovePosition;
	private static HashMap<Integer, CountryState> countryStates = new HashMap<>();
	public static int connectedPlayers = 0; // 서버에 접속한 인원 수
	private static PrintWriter out;

	private static int lapCount = 0;
	private static int[] playerGolds = {0, 0, 0, 0};  // 각 플레이어의 금액 추적
	private static boolean isOlympicActive = false; // 올림픽 활성화 여부
	private static int olympicLocation = 0; // 올림픽 개최 위치

	public PlayerPanel(Socket socket) {
		try {
			// 서버와 연결된 소켓을 사용하여 PrintWriter 생성
			setLayout(null);
			setOpaque(false);
			createPlayer();
			this.out = new PrintWriter(socket.getOutputStream(), true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void setConnectedPlayers(int playerCount) {
		connectedPlayers = playerCount;
		updateDiceState();
	}

	private static void updateDiceState() {
		SwingUtilities.invokeLater(() -> {
			// 첫 번째 플레이어가 첫 번째 턴을 시작하도록 버튼 상태를 설정
			if (connectedPlayers >= 2) {
				CountryDetailPanel.diceClickBtn.setEnabled(playerOrder == 1);
			} else {
				CountryDetailPanel.diceClickBtn.setEnabled(false); // 1인일 경우 비활성화
			}
		});
	}

	public static void createPlayer() {
		int length = label.length;

		JButton countryButton = CountryButtonList.getCountryButton(0);
		countryButton.setLayout(null);

		int[][] positions = {
				{20, 10}, {80, 10}, {20, 80}, {80, 80}
		};

		for (int i = 0; i < length; i++) {
			Image img = new ImageIcon("./images/player" + (i + 1) + ".png").getImage();
			label[i] = new JLabel(new ImageIcon(img));
			label[i].setBounds(positions[i][0], positions[i][1], 51, 59);

			countryButton.add(label[i]);
			PlayerList.insertPlayer(label[i]);
		}
		countryButton.revalidate();
		countryButton.repaint();
	}

	public static void movePlayer(int playerIndex, int diceNum) {
		if (blockedTurns[playerIndex] > 0) {
			if (DiceNumberList.getDiceNum1() == DiceNumberList.getDiceNum2()) { // 더블이면 탈출
				blockedTurns[playerIndex] = 0; // 무인도 탈출 처리
				PlayerView.appendMessage("플레이어 " + (playerIndex + 1) + "이(가) 무인도를 탈출했습니다!");
			} else {
				blockedTurns[playerIndex]--; // 차단된 턴 감소
				PlayerView.appendMessage("플레이어 " + (playerIndex + 1) + "이(가) 무인도에 갇혀 있습니다. 남은 차단 턴: " + blockedTurns[playerIndex]);
				return; // 무인도에 갇혀 이동하지 않음
			}
		}
		int position = PlayerList.getPlayerPosition(playerIndex);
		totalMovePosition = position + diceNum;
		boolean lapCompleted = totalMovePosition >= 28;

		if (lapCompleted) totalMovePosition -= 28;

		Timer playerMoveTimer = new Timer(130, new ActionListener() {
			private int currentPosition = position;

			@Override
			public void actionPerformed(ActionEvent e) {
				currentPosition++;
				if (currentPosition == 28) currentPosition = 0; // 맵 순환 처리

				PlayerList.setPlayerPosition(playerIndex, currentPosition);
				SwingUtilities.invokeLater(() -> {
					// 현재 위치에서 플레이어 제거
					int prevIndex = (currentPosition == 0) ? 27 : currentPosition - 1;
					JButton prevButton = getSafeCountryButton(prevIndex);
					if (prevButton != null) {
						prevButton.remove(PlayerList.getPlayer(playerIndex));
						repaintComponent(prevIndex);
					}

					// 새 위치에 플레이어 추가
					JButton currentButton = getSafeCountryButton(currentPosition);
					if (currentButton != null) {
						currentButton.add(PlayerList.getPlayer(playerIndex));
						repaintComponent(currentPosition);

						if (currentPosition >= 22 && currentPosition < 28) {
							JLabel player = PlayerList.getPlayer(playerIndex);
							adjustPlayerPosition(currentPosition, playerIndex); // 위치 조정
							currentButton.add(player);
							repaintComponent(currentPosition);
						}
					}
				});

				// 플레이어가 목표 위치에 도달한 경우
				if (currentPosition == totalMovePosition) {
					((Timer) e.getSource()).stop(); // 이동 타이머 정지

					// 랩 완료 시 보너스 금액
					if (lapCompleted) {
						sendMessage("UPDATE_GOLD " + (playerIndex+ 1 ) + " 300000");
						lapCount++; // 바퀴 수 증가
						if (lapCount >= 10) {
							endGame();  // 10바퀴 돌았으면 게임 종료
						}
					}
					if (currentPosition == 7) {
						blockedTurns[playerIndex] = 3; // 무인도 위치 도달 시 3턴 차단
						JOptionPane.showMessageDialog(null, "플레이어 " + (playerIndex+ 1) + "이(가) 무인도에 갇혔습니다!");
					} else if (totalMovePosition == 10 || totalMovePosition == 18 || totalMovePosition == 25) {
						showSpecialPanel((playerIndex + 1));
					} else if (totalMovePosition == 14) startOlympic(playerIndex);
					else {
						handleCountryPurchase(playerIndex, currentPosition); // 건물 구매 처리
					}

					// 마지막 플레이어라면 주사위 버튼 활성화
					if (playerIndex == connectedPlayers) {
						CountryDetailPanel.diceClickBtn.setVisible(true);
					}
				}
			}
		});

		// 이동 지연 타이머
		Timer delayTimer = new Timer(1500, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(() -> {
					// 이전 위치의 플레이어 이미지 제거
					JButton prevButton = getSafeCountryButton(position);
					if (prevButton != null) {
						prevButton.remove(PlayerList.getPlayer(playerIndex));
						repaintComponent(position);
					}
				});

				playerMoveTimer.start(); // 이동 타이머 시작
			}
		});
		delayTimer.setRepeats(false); // 한 번만 실행되도록 설정
		delayTimer.start(); // 지연 타이머 시작
	}

	// PlayerPanel 클래스에서 movePlayerTo 메서드 수정
	public static void movePlayerTo(int playerNumber, int newPosition) {
		int currentPosition = PlayerList.getPlayerPosition(playerNumber - 1); // 기존 위치

		SwingUtilities.invokeLater(() -> {
			// 기존 위치에서 플레이어 제거
			JButton oldButton = CountryButtonList.getCountryButton(currentPosition);
			oldButton.remove(PlayerList.getPlayer(playerNumber - 1));
			oldButton.revalidate();
			oldButton.repaint();

			// 새 위치에 플레이어 추가
			JButton newButton = CountryButtonList.getCountryButton(newPosition);
			JLabel playerLabel = PlayerList.getPlayer(playerNumber - 1);

			adjustPlayerPosition(newPosition, playerNumber - 1); // 새 위치 조정
			newButton.add(playerLabel);
			newButton.revalidate();
			newButton.repaint();

			// 위치 정보 업데이트
			PlayerList.setPlayerPosition(playerNumber - 1, newPosition);
			PlayerView.appendMessage("플레이어 " + playerNumber + "이(가) " + newPosition + "으로 이동했습니다.");
		});
	}

	// 통행료 처리 로직 수정
	private static void handleCountryPurchase(int playerIndex, int position) {
		if (position == 0 || position == 7 || position == 14 || position == 10 || position == 18 || position == 25) return;

		if (position == 21) { // 세계여행 위치
			int playerGold = PlayerView.getPlayerGold(playerIndex);

			// 골드가 10만 원 이상인지 확인
			if (playerGold < 100000) {
				JOptionPane.showMessageDialog(null, "골드가 부족하여 세계여행을 할 수 없습니다.");
				return;
			}

			// 세계여행 창 띄우기
			JFrame travelFrame = new JFrame("세계여행");
			travelFrame.setSize(400, 300);
			travelFrame.setLayout(new BorderLayout());
			travelFrame.setLocationRelativeTo(null); // 화면 중앙에 위치

			JLabel messageLabel = new JLabel("이동할 위치를 선택하세요 (0 ~ 27):", SwingConstants.CENTER);
			travelFrame.add(messageLabel, BorderLayout.NORTH);

			JTextField positionField = new JTextField();
			travelFrame.add(positionField, BorderLayout.CENTER);

			JButton confirmButton = new JButton("확인");
			confirmButton.addActionListener(e -> {
				try {
					int newPosition = Integer.parseInt(positionField.getText());

					if (newPosition < 0 || newPosition > 27) {
						JOptionPane.showMessageDialog(travelFrame, "유효한 위치를 입력하세요 (0 ~ 27).");
					} else {
						// 골드 차감
						sendMessage("UPDATE_GOLD " + (playerIndex + 1) + " -100000");

						// 새로운 위치로 이동
						movePlayer(playerIndex, newPosition);

						travelFrame.dispose(); // 창 닫기
					}
				} catch (NumberFormatException ex) {
					JOptionPane.showMessageDialog(travelFrame, "숫자를 입력하세요.");
				}
			});

			travelFrame.add(confirmButton, BorderLayout.SOUTH);
			travelFrame.setVisible(true);
			return; // 세계여행 처리 후 함수 종료
		}

		int playerGold = PlayerView.getPlayerGold(playerIndex);
		CountryState countryState = countryStates.getOrDefault(position, new CountryState());

		if (countryState.owner == -1) { // 국가가 비어있는 경우
			if (countryState.level == 0 && playerGold >= 60000) {
				countryState.owner = playerIndex;
				countryState.level = 1; // 빌라 구매
				CountryDetailPanel.addBuildingImage(position, playerIndex, 1); // 빌라 이미지 추가
				sendMessage("UPDATE_GOLD " + (playerIndex + 1) + " -500000");
			}
		} else if (countryState.owner == playerIndex) {
			if (countryState.level == 1 && playerGold >= 121000) {
				countryState.level = 2; // 빌딩 업그레이드
				CountryDetailPanel.addBuildingImage(position, playerIndex, 2); // 빌딩 이미지 추가
				sendMessage("UPDATE_GOLD " + (playerIndex + 1) + " -121000");
			} else if (countryState.level == 2 && playerGold >= 205000) {
				countryState.level = 3; // 호텔 업그레이드
				CountryDetailPanel.addBuildingImage(position, playerIndex, 3); // 호텔 이미지 추가
				sendMessage("UPDATE_GOLD " + (playerIndex + 1) + " -205000");
			}
		} else {
			// 다른 플레이어가 소유한 경우 페널티 적용
			int penalty = (countryState.level == 1) ? 248000 : (countryState.level == 2) ? 505000 : 1000000;

			// 올림픽 활성화 중이고 위치가 올림픽 개최 위치라면 통행료 두 배
			if (isOlympicActive && position == olympicLocation) {
				penalty *= 2;
				PlayerView.appendMessage("플레이어 " + playerIndex + "이(가) 올림픽 지역에 도착했습니다! 통행료 두 배!");
			}

			int ownerIndex = countryState.owner; // 소유주 인덱스
			if (playerGold >= penalty) {
				sendMessage("UPDATE_GOLD " + (playerIndex + 1) + " " + -penalty); // 현재 플레이어 돈 감소
				sendMessage("UPDATE_GOLD " + (ownerIndex + 1) + " " + penalty);  // 소유주 돈 증가
			} else {
				sendMessage("UPDATE_GOLD " + (playerIndex + 1) + " " + -playerGold); // 남은 돈 전부 차감
				sendMessage("UPDATE_GOLD " + (ownerIndex + 1) + " " + playerGold);  // 소유주에게 남은 돈 지급
				endGameDueToBankruptcy(playerIndex); // 파산 처리
			}
		}
		countryStates.put(position, countryState);
		checkGameEnd();
	}

	private static void repaintComponent(int buttonIndex) {
		CountryButtonList.getCountryButton(buttonIndex).revalidate();
		CountryButtonList.getCountryButton(buttonIndex).repaint();
	}

	private static void adjustPlayerPosition(int buttonIndex, int playerIndex) {
		JLabel player = PlayerList.getPlayer(playerIndex);

		// 기본 위치 설정
		int x = 0;
		int y = 0;

		// 특정 위치에서는 오른쪽에 배치
		if (buttonIndex >= 22 && buttonIndex < 28) {
			x = 80; // 버튼의 오른쪽
			y = 20; // 세로 위치는 동일
		}

		// 위치 조정 적용
		player.setBounds(x, y, 51, 59); // 크기는 기존 설정 유지
	}

	static class CountryState {
		int owner = -1; // 소유주 (-1은 비어있음을 의미)
		int level = 0;  // 0: 없음, 1: 빌라, 2: 빌딩, 3: 호텔
	}
	// 서버로 메시지 전송
	public static void sendMessage(String message) {
		if (out != null) {
			out.println(message);
		}
	}
	// 안전하게 CountryButton 가져오기
	private static JButton getSafeCountryButton(int index) {
		if (index >= 0 && index < CountryButtonList.getCountryButtonList().size()) {
			return CountryButtonList.getCountryButton(index);
		}
		return null;
	}


	private static void endGame() {
		// 게임 종료 로직
		int winner = getWinner(); // 가장 돈이 많은 플레이어를 판별
		JOptionPane.showMessageDialog(null, "게임 종료! " + winner + "번 플레이어가 이겼습니다.");
		System.exit(0);
		// new EndGamePanel(winner);
	}
	// 파산으로 게임 종료
	private static void endGameDueToBankruptcy(int bankruptPlayer) {
		JOptionPane.showMessageDialog(null, "플레이어 " + bankruptPlayer + "이(가) 파산했습니다! 게임 종료!");
		System.exit(0);
	}

	private static int getWinner() {
		// 가장 많은 돈을 가진 플레이어 찾기
		int maxGold = -1;
		int winner = -1;
		for (int i = 0; i < playerGolds.length; i++) {
			if (playerGolds[i] > maxGold) {
				maxGold = playerGolds[i];
				winner = i + 1; // 플레이어 번호는 1부터 시작
			}
		}
		return winner;
	}

	// 플레이어 돈을 확인하여 게임 종료 조건을 만족하는지 검사
	private static void checkGameEnd() {
		for (int i = 0; i < playerGolds.length; i++) {
			if (PlayerView.playerGold[i] <= 0) { // 돈이 0 이하인 플레이어가 있으면
				endGameDueToBankruptcy(i + 1); // 해당 플레이어 번호로 게임 종료
				return;
			}
		}
	}
	private static void showSpecialPanel(int playerIndex) {
		Random rand = new Random();
		int cardIndex = rand.nextInt(4);  // 0부터 3까지의 숫자 랜덤 선택
		String cardImagePath = "./images/chance" + cardIndex + ".png";

		// 카드 이미지를 JLabel로 설정
		ImageIcon cardImage = new ImageIcon(cardImagePath);
		JLabel cardLabel = new JLabel(cardImage);

		// 새로운 패널 생성
		JPanel specialPanel = new JPanel();
		specialPanel.setLayout(new BorderLayout());
		specialPanel.add(cardLabel, BorderLayout.CENTER);

		// 닫기 버튼 만들기
		JButton closeButton = new JButton("닫기");
		closeButton.setBackground(Color.blue);
		closeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// 닫기 버튼 클릭 시 패널을 닫기
				closePanel(specialPanel);
			}
		});

		specialPanel.add(closeButton, BorderLayout.SOUTH);

		// 새 패널을 JFrame에 추가하여 화면에 표시
		JFrame frame = new JFrame("찬스 카드");
		frame.setSize(200, 280);
		frame.setLocationRelativeTo(null); // 화면 중앙에 위치
		frame.add(specialPanel);
		frame.setVisible(true);
		System.out.println("showSpecialPanel 호출 - playerIndex: " + playerIndex + ", playerOrder: " + playerOrder);
	}

	private static void closePanel(JPanel panel) {
		// 패널을 닫는 메소드
		JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(panel);
		if (parentFrame != null) {
			parentFrame.dispose(); // 창 닫기
		}
	}

	// 올림픽을 개최
	private static void startOlympic(int playerIndex) {
		// 플레이어가 소유한 국가 리스트 생성
		java.util.List<Integer> ownedCountries = new ArrayList<>();
		for (int countryPosition : countryStates.keySet()) {
			CountryState state = countryStates.get(countryPosition);
			if (state.owner == playerIndex) {
				ownedCountries.add(countryPosition);
			}
		}

		// 소유 국가가 없는 경우 메시지 출력 후 반환
		if (ownedCountries.isEmpty()) {
			JOptionPane.showMessageDialog(null, "플레이어가 소유한 국가가 없습니다. 올림픽을 개최할 수 없습니다.");
			return;
		}

		// 올림픽 개최를 위한 JFrame 생성
		JFrame olympicFrame = new JFrame("올림픽 개최");
		olympicFrame.setSize(400, 300);
		olympicFrame.setLayout(new BorderLayout());
		olympicFrame.setLocationRelativeTo(null); // 화면 중앙에 위치

		// 라벨 설정
		JLabel messageLabel = new JLabel("올림픽을 개최할 국가를 선택하세요:", SwingConstants.CENTER);
		olympicFrame.add(messageLabel, BorderLayout.NORTH);

		// 라디오 버튼 그룹 생성
		ButtonGroup buttonGroup = new ButtonGroup();
		JPanel radioPanel = new JPanel();
		radioPanel.setLayout(new BoxLayout(radioPanel, BoxLayout.Y_AXIS));

		java.util.List<JRadioButton> radioButtons = new ArrayList<>();
		for (int ownedCountry : ownedCountries) {
			JRadioButton radioButton = new JRadioButton("국가 " + ownedCountry);
			radioButtons.add(radioButton);
			buttonGroup.add(radioButton);
			radioPanel.add(radioButton);
		}

		olympicFrame.add(new JScrollPane(radioPanel), BorderLayout.CENTER);

		// 확인 버튼 생성
		JButton confirmButton = new JButton("확인");
		confirmButton.addActionListener(e -> {
			for (int i = 0; i < radioButtons.size(); i++) {
				if (radioButtons.get(i).isSelected()) {
					int selectedCountry = ownedCountries.get(i);
					olympicLocation = selectedCountry;
					isOlympicActive = true;

					// 올림픽 개최 메시지 출력
					PlayerView.appendMessage("플레이어 " + playerIndex +
							"이(가) 국가 " + selectedCountry + "에서 올림픽을 개최했습니다! 통행료가 두 배로 증가합니다.");

					olympicFrame.dispose(); // 창 닫기
					return;
				}
			}

			JOptionPane.showMessageDialog(null, "국가를 선택하세요!");
		});

		olympicFrame.add(confirmButton, BorderLayout.SOUTH);
		olympicFrame.setVisible(true);
	}
}
