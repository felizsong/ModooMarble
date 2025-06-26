package country;

import common.CommonCountry;

import javax.swing.*;
import java.awt.*;

// 우측 빨간 라인 국가 패널
public class RedLineButtonPanel extends JPanel {
	public RedLineButtonPanel() {
		setLayout(new BorderLayout());
		add(CommonCountry.setCountryButtonImage(
				/*panelW*/154,
				/*panelH*/552,
				/*buttonW*/154,
				/*buttonH*/88,
				/*buttonIdx*/6,
				/*start*/22));
	}
}
