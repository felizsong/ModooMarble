package dice;

import list.DiceNumberList;
import sound.SoundManager;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;

public class DicePanel extends JPanel {
    private static JButton[] diceBtn = new JButton[2];
    private final static int IMAGESIZE = 190;
    public static int doubleDiceCount;
    public static int diceNum;

    public DicePanel() {
        setLayout(new FlowLayout(FlowLayout.CENTER, 48, 30));
        initDiceButton();   // 주사위 생성
        randomDiceNumber(); // 랜덤 주사위 수 결정
        randomThreadDice(); // 랜덤 주사위 돌리기
    }

    // 주사위 굴림
    public static void startDiceRoll() {
        randomDiceNumber();
        randomThreadDice();
    }

    // 주사위 생성 및 초기화
    private void initDiceButton() {
        for (int i = 0; i < 2; i++) {
            diceBtn[i] = new JButton();
            diceBtn[i].setOpaque(true);
            diceBtn[i].setContentAreaFilled(true);
            diceBtn[i].setBorderPainted(false);
            diceBtn[i].setFocusPainted(false);
            diceBtn[i].setPreferredSize(new Dimension(90, 90));
            add(diceBtn[i]);
        }
    }

    private static void randomDiceNumber() {
        int diceNum1 = (int) (Math.random() * 6 + 1);
        int diceNum2 = (int) (Math.random() * 6 + 1);

        DiceNumberList.setDiceNum1(diceNum1);
        DiceNumberList.setDiceNum2(diceNum2);
    }

    // 주사위를 랜덤으로 돌리는 메소드
    public static void randomThreadDice() {
        Timer timer = new Timer();
        int delay = 100; // 이미지 변경 간격 (0.1초)
        int duration = 1100; // 전체 동작 시간 (1.1초)
        int steps = duration / delay; // 이미지 변경 횟수

        TimerTask task = new TimerTask() {
            int stepCount = 0;

            @Override
            public void run() {
                if (stepCount < steps) {
                    // 첫 번째 주사위 이미지 무작위로 선택 후 버튼에 적용
                    int randomImageIndex1 = (int) (Math.random() * 6 + 1);
                    int randomImageIndex2 = (int) (Math.random() * 6 + 1);
                    Image img1 = new ImageIcon("./images/dice" + randomImageIndex1 + ".png").getImage()
                            .getScaledInstance(IMAGESIZE, IMAGESIZE, java.awt.Image.SCALE_SMOOTH);
                    Image img2 = new ImageIcon("./images/dice" + randomImageIndex2 + ".png").getImage()
                            .getScaledInstance(IMAGESIZE, IMAGESIZE, java.awt.Image.SCALE_SMOOTH);
                    diceBtn[0].setIcon(new ImageIcon(img1));
                    diceBtn[1].setIcon(new ImageIcon(img2));
                    stepCount++;
                } else {
                    setFinalButtonImage(); // 최종 숫자의 주사위 보여주기
                    cancel();
                }
            }
        };

        // 이미지 변경 간격(delay)마다 TimerTask 실행
        timer.schedule(task, delay, delay);
    }

    // 랜덤으로 굴려진 주사위의 결과를 보여줌
    private static void setFinalButtonImage() {
        Image[] diceFinalImages = {
                new ImageIcon("./images/dice" + DiceNumberList.getDiceNum1() + ".png").getImage()
                        .getScaledInstance(IMAGESIZE, IMAGESIZE, java.awt.Image.SCALE_SMOOTH),

                new ImageIcon("./images/dice" + DiceNumberList.getDiceNum2() + ".png").getImage()
                        .getScaledInstance(IMAGESIZE, IMAGESIZE, java.awt.Image.SCALE_SMOOTH) };

        // 이미지를 버튼에 적용
        diceBtn[0].setIcon(new ImageIcon(diceFinalImages[0]));
        diceBtn[1].setIcon(new ImageIcon(diceFinalImages[1]));
        diceNum = DiceNumberList.getDiceNum1() + DiceNumberList.getDiceNum2();
        SoundManager.playSound("./sound/주사위 " + diceNum + ".wav");
    }
}
