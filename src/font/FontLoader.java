package font;

import java.awt.*;
import java.io.File;
import java.io.IOException;

public class FontLoader {
    public static Font loadCustomFont(String filePath, float size) {
        try {
            Font customFont = Font.createFont(Font.TRUETYPE_FONT, new File(filePath));
            return customFont.deriveFont(size); // 폰트 크기 설정
        } catch (FontFormatException | IOException e) {
            e.printStackTrace();
            return new Font("Arial", Font.PLAIN, (int) size); // 실패 시 기본 폰트 반환
        }
    }
}
