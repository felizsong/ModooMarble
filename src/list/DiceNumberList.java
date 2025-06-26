package list;


public class DiceNumberList {
	private static int diceNum1;
	private static int diceNum2;

	// 첫번째 주사위 값 알아내기
	public static int getDiceNum1() {
		return diceNum1;
	}

	// 두번째 주사위 값 알아내기
	public static int getDiceNum2() {
		return diceNum2;
	}

	// 첫번째 주사위 저장
	public static void setDiceNum1(int diecNum1) {
		DiceNumberList.diceNum1 = diecNum1;
	}

	// 두번째 주사위 저장
	public static void setDiceNum2(int diceNum2) {
		DiceNumberList.diceNum2 = diceNum2;
	}
}
