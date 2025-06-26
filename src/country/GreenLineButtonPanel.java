package country;

import common.CommonCountry;

import javax.swing.*;
import java.awt.*;

// 하단 초록 라인 국가 패널
public class GreenLineButtonPanel extends JPanel {
	public GreenLineButtonPanel() {
		setLayout(new BorderLayout());
		add(CommonCountry.setCountryButtonImage(
				/*panelW*/860,
				/*panelH*/154,
				/*buttonW*/92,
				/*buttonH*/154,
				/*buttonIdx*/8,
				/*start*/0));
	}
}
