package country;

import common.CommonCountry;

import javax.swing.*;
import java.awt.*;

// 상단 보라 라인 국가 패널
public class PurpleLineButtonPanel extends JPanel {
	public PurpleLineButtonPanel() {
		setLayout(new BorderLayout());
		add(CommonCountry.setCountryButtonImage(
				/* panelW */860,
				/* panelH */154,
				/*buttonW*/92,
				/*buttonH*/154,
				/* buttonIdx */8,
				/* start */14));
	}
}
