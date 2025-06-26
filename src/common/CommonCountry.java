package common;

import list.CountryButtonList;

import javax.swing.*;
import java.awt.*;

public class CommonCountry {

	// panel에 국가 설정
	public static JPanel setCountryButtonImage(int panelW, int panelH, int buttonW, int buttonH, int buttonIdx, int start) {
		// panel 생성
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		panel.setPreferredSize(new Dimension(panelW, panelH));

		// button 생성
		var buttons = new JButton[buttonIdx];
		int buttonWCopy = buttonW;
		int buttonHCopy = buttonH;

		for (var btn : buttons) {
			buttonW = buttonWCopy;
			buttonH = buttonHCopy;

			// 가장 사이드에 있는 출발지, 무인도, 올림픽, 세계여행
			if (start == 0 || start == 7 || start == 14 || start == 21) {
				buttonW = 154;
				buttonH = 154;
			}

			Image img = new ImageIcon("./images/" + (start++) + ".png").getImage();

			// 각 버튼 생성 후 list에 해당 버튼 추가
			btn = new JButton(new ImageIcon(img));
			CountryButtonList.insertCountryButton(btn);

			btn.setPreferredSize(new Dimension(buttonW, buttonH));

			if(start <= 14) panel.add(btn, 0);
			else panel.add(btn);
		}

		return panel;
	}
}
