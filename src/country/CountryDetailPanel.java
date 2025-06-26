package country;

import common.CommonImage;
import dice.DicePanel;
import list.BuildingList;
import list.CountryButtonList;
import list.DiceNumberList;
import list.PlayerList;
import player.PlayerPanel;
import player.PlayerView;
import sound.SoundManager;

import javax.swing.*;

public class CountryDetailPanel extends JPanel {
	private DicePanel dicePanel; // 주사위 패널
	private Timer turnTimer; // 20초 타이머
	private static final int TURN_TIMEOUT = 20000; // 20초

	// 버튼 이미지 경로 설정
	private ImageIcon btnUncheckedIcon = new ImageIcon("./images/btn_unchecked.png");
	private ImageIcon btnCheckedIcon = new ImageIcon("./images/btn_checked.png");

	public static JButton diceClickBtn; // 주사위 돌리기 버튼
	public static int playerNumber; // 서버에서 받은 플레이어 번호

	public CountryDetailPanel(int playerNumber) {
		CountryDetailPanel.playerNumber = playerNumber;
		setLayout(null); // 절대 레이아웃으로 설정

		// 게임 보드 이미지 패널
		CommonImage img = new CommonImage(new ImageIcon("./images/main.png").getImage());
		img.setBounds(0, 0, 552, 552);
		add(img);

		// 주사위 돌리기 버튼
		diceClickBtn = new JButton(btnUncheckedIcon);
		diceClickBtn.setBorderPainted(false); // 버튼 테두리 제거
		diceClickBtn.setContentAreaFilled(false); // 버튼 배경 제거
		diceClickBtn.setFocusPainted(false); // 포커스 테두리 제거
		diceClickBtn.setBounds(210, 400, 120, 110);

		img.add(diceClickBtn);

		// 버튼 클릭 이벤트
		diceClickBtn.addActionListener(e -> handleDiceRoll());

		// 타이머 초기화
		initializeTurnTimer();
	}

	private void initializeTurnTimer() {
		turnTimer = new Timer(TURN_TIMEOUT, e -> handleTurnTimeout());
		turnTimer.setRepeats(false); // 한 번만 실행되도록 설정
	}

	private void handleDiceRoll() {
		// 타이머 초기화
		turnTimer.stop();

		// 현재 턴의 플레이어만 주사위를 굴릴 수 있도록 제한
		if (PlayerPanel.playerOrder != playerNumber) {
			PlayerView.appendMessage("현재 순서가 아닙니다. 기다려주세요.");
			return;
		}

		diceClickBtn.setIcon(btnCheckedIcon);

		if (dicePanel == null) {
			dicePanel = new DicePanel();
			dicePanel.setOpaque(false);
			dicePanel.setBounds(100, 210, 400, 200);
			add(dicePanel);
		}

		// 주사위 굴리기
		DicePanel.startDiceRoll();
		int diceNum = DiceNumberList.getDiceNum1() + DiceNumberList.getDiceNum2(); // 주사위 숫자 합계

		// 더블 체크
		if (DiceNumberList.getDiceNum1() == DiceNumberList.getDiceNum2()) {
			DicePanel.doubleDiceCount++;
			SoundManager.playSound("./sound/더블.wav");

			// 3번 더블이면 무인도로 이동
			if (DicePanel.doubleDiceCount == 3) {
				SwingUtilities.invokeLater(() -> {
					CountryButtonList.getCountryButton(PlayerList.getPlayerPosition(PlayerPanel.playerOrder))
							.remove(PlayerList.getPlayer(PlayerPanel.playerOrder));
					PlayerPanel.movePlayer(PlayerPanel.playerOrder, 7);
					PlayerView.appendMessage("3번 연속 더블! " + (PlayerPanel.playerOrder) + "이(가) 무인도로 이동합니다.");
				});
				DicePanel.doubleDiceCount = 0; // 초기화
				return;
			} else {
				PlayerView.appendMessage("더블! 한 번 더 주사위를 굴립니다.");
				PlayerPanel.sendMessage("PLAYER_MOVE " + PlayerPanel.playerOrder + " " + diceNum);
				return; // 더블일 경우 현재 플레이어가 한 번 더 주사위를 굴림
			}
		}

		PlayerPanel.sendMessage("PLAYER_MOVE " + PlayerPanel.playerOrder + " " + diceNum);
		System.out.println("플레이어 " + PlayerPanel.playerOrder + "이 " + diceNum + "만큼 이동했습니다.");
		// 차례 변경
		DicePanel.doubleDiceCount = 0;
		++PlayerPanel.playerOrder;
		if (PlayerPanel.playerOrder > PlayerPanel.connectedPlayers)
			PlayerPanel.playerOrder = 1;

		updateDiceButtonState(PlayerPanel.playerOrder); // 차례 변경 후 버튼 상태 업데이트
		System.out.println("현재 차례 : " + PlayerPanel.playerOrder);

		// 차례 변경 요청
		PlayerPanel.sendMessage("NEXT_TURN " + PlayerPanel.playerOrder);

		// 다음 턴 타이머 시작
		turnTimer.restart();
	}

	private void handleTurnTimeout() {
		SwingUtilities.invokeLater(() -> {
			PlayerView.appendMessage("플레이어 " + PlayerPanel.playerOrder + "이(가) 20초 동안 주사위를 굴리지 않아 턴이 넘어갑니다.");
			++PlayerPanel.playerOrder;
			if (PlayerPanel.playerOrder > PlayerPanel.connectedPlayers)
				PlayerPanel.playerOrder = 1;

			updateDiceButtonState(PlayerPanel.playerOrder); // 차례 변경 후 버튼 상태 업데이트
			PlayerPanel.sendMessage("NEXT_TURN " + PlayerPanel.playerOrder);
			turnTimer.restart(); // 새로운 턴 타이머 시작
		});
	}
	// 현재 턴이 접속한 플레이어 번호와 일치하면 버튼 활성화
	private static void updateDiceButtonState(int number) {
		SwingUtilities.invokeLater(() -> {
			diceClickBtn.setEnabled(PlayerPanel.playerOrder == number);
		});
	}

	public static void addBuildingImage(int position, int playerNum, int level) {
		JButton countryButton = CountryButtonList.getCountryButton(position);
		BuildingList.BuildingState state = BuildingList.getBuildingState(position);

		// 플레이어별 건물 이미지 설정
		String[][] buildingImages = {
				{"노란 빌라.png", "노란 빌딩.png", "노란 호텔.png"}, // 1번 플레이어
				{"갈색 빌라.png", "갈색 빌딩.png", "갈색 호텔.png"}, // 2번 플레이어
				{"파란 빌라.png", "파란 빌딩.png", "파란 호텔.png"}, // 3번 플레이어
				{"빨간 빌라.png", "빨간 빌딩.png", "빨간 호텔.png"}  // 4번 플레이어
		};

		String[] specialImages = {
				"붉은 깃발.png", "오두막.png", "방갈로.png"
		};

		String imagePath = "";
		// 소유한 플레이어의 색상에 따른 이미지 가져오기
		if (position == 3 || position == 8 || position == 12 || position == 16 || position == 22) {
			imagePath = "./images/" + specialImages[level - 1];
		} else {
			// 플레이어별 건물 이미지 가져오기
			imagePath = "./images/" + buildingImages[playerNum][level - 1];
		}
		// 건물 이미지 추가
		ImageIcon icon = new ImageIcon(new ImageIcon(imagePath).getImage());
		JLabel buildingLabel = new JLabel(icon);
		countryButton.setLayout(null);

		// 레벨에 따라 다른 위치 설정 (겹치지 않도록 조정)
		int xOffset = 0;
		int yOffset = 0;

		// position에 따라 다른 위치 지정
		if (position >= 1 && position <= 6) {
			switch (level) {
				case 1: xOffset = 5; yOffset = 8; break;
				case 2: xOffset = 30; yOffset = 8; break;
				case 3: xOffset = 55; yOffset = 8; break;
			}
		} else if (position >= 8 && position <= 13) {
			switch (level) {
				case 1: xOffset = 118; yOffset = 1; break;
				case 2: xOffset = 118; yOffset = 30; break;
				case 3: xOffset = 118; yOffset = 58; break;
			}
		} else if (position >= 15 && position <= 20) {
			switch (level) {
				case 1: xOffset = 5;  yOffset = 118; break;
				case 2: xOffset = 30; yOffset = 118; break;
				case 3: xOffset = 55; yOffset = 118; break;
			}
		} else {
			switch (level) {
				case 1: xOffset = 5; yOffset = 1; break;
				case 2: xOffset = 5; yOffset = 30; break;
				case 3: xOffset = 5; yOffset = 58; break;
			}
		}

		buildingLabel.setBounds(xOffset, yOffset, 30, 30);

		// 상태 업데이트
		if (level == 1) state.setVilla();
		if (level == 2) state.setBuilding();
		if (level == 3) state.setHotel();
		BuildingList.setBuildingState(position, state);

		// 건물 레이블 추가
		countryButton.add(buildingLabel);
		countryButton.revalidate();
		countryButton.repaint();
	}
}
