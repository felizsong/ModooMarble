package sound;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class SoundManager {
    // clip을 클래스의 정적 변수로 선언하여 재사용
    private static Clip clip;

    // 일반적인 한 번만 재생되는 소리
    public static void playSound(String filePath) {
        try {
            File soundFile = new File(filePath);
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundFile);
            clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    // 배경음악을 무한 반복 재생
    public static void playSoundBackground(String filePath) {
        try {
            // 이미 clip이 재생 중이면 새로운 재생을 시작하지 않도록 처리
            if (clip != null && clip.isRunning()) {
                return;  // 이미 재생 중인 경우 아무 작업도 하지 않음
            }

            // clip이 null이거나 열린 상태가 아니라면 새로운 파일을 로드
            if (clip == null || !clip.isOpen()) {
                File soundFile = new File(filePath);
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundFile);
                clip = AudioSystem.getClip();
                clip.open(audioInputStream);
            }

            // 무한 반복 설정
            clip.loop(Clip.LOOP_CONTINUOUSLY);
            clip.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    // 음악을 멈춤
    public static void stopSound() {
        if (clip != null && clip.isRunning()) {
            clip.stop();
        }
    }
}
