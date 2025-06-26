package country;

import common.CommonCountry;

import javax.swing.*;
import java.awt.*;

// 좌측 파랑 라인 국가 패널
public class BlueLineButtonPanel extends JPanel {
	public BlueLineButtonPanel() {
		setLayout(new BorderLayout());
		add(CommonCountry.setCountryButtonImage(
				/*panelW*/154,
				/*panelH*/552,
				/*buttonW*/154,
				/*buttonH*/88,
				/*buttonIdx*/6,
				/*start*/8));
	}
}
