package list;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class PlayerList {
	private static ArrayList<JLabel> playerList = new ArrayList<>();
	private static ArrayList<Integer> playerPositionList = new ArrayList<>(List.of(0, 0, 0, 0));

	// playerlist 반환
	public static ArrayList<JLabel> getPlayerList() {
		return playerList;
	}

	// playerlist 초기화
	public static void setPlayerList(ArrayList<JLabel> playerList) {
		PlayerList.playerList = playerList;
	}

	// player 추가
	public static void insertPlayer(JLabel player) {
		playerList.add(player);
	}

	// 해당하는 player 반환
	public static JLabel getPlayer(int index) {
		return playerList.get(index);
	}

	// player 위치 반환
	public static ArrayList<Integer> getPlayerPositionList() {
		return playerPositionList;
	}

	// player 위치 리스트 설정
	public static void setPlayerPositionList(ArrayList<Integer> playerPositionList) {
		PlayerList.playerPositionList = playerPositionList;
	}

	// 현재 플레이어 수 반환
	public static int getPlayerCount() {
		return playerList.size();
	}

	// player 위치 설정
	public static void setPlayerPosition(int index, int position) {
		PlayerList.playerPositionList.set(index, position);
	}

	// index에 해당하는 player 위치 반환
	public static int getPlayerPosition(int index) {
		return playerPositionList.get(index);
	}
}
