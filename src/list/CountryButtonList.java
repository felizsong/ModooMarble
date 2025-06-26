package list;

import javax.swing.*;
import java.util.ArrayList;

// 나라 리스트
public class CountryButtonList {
	private static ArrayList<JButton> countryButtonList = new ArrayList<>();

	// 나라별 button list 반환
	public static ArrayList<JButton> getCountryButtonList() {
		return countryButtonList;
	}

	// 나라 button list 초기화
	public static void setCountryButtonList(ArrayList<JButton> countryButtonList) {
		CountryButtonList.countryButtonList = countryButtonList;
	}

	// 나라 button list에 버튼 추가
	public static void insertCountryButton(JButton countryButton) {
		countryButtonList.add(countryButton);
	}

	// index에 해당하는 값 반환
	public static JButton getCountryButton(int index) {
		return countryButtonList.get(index);
	}
}
