package common;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;

import javax.swing.JPanel;

public class CommonImage extends JPanel {
    private Image img;

    public CommonImage(Image img) {
        this.img = img;
        setSize(new Dimension(img.getWidth(null), img.getHeight(null)));
        setPreferredSize(new Dimension(img.getWidth(null), img.getHeight(null)));
        setLayout(null);
    }

    public void paintComponent(Graphics g) {

        g.drawImage(img, 0, 0, getWidth(), getHeight(), this); // 이미지를 패널 크기에 맞게 그리기
    }
}