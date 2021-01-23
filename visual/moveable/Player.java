package visual.moveable;

import java.awt.*;
import java.awt.event.*;

import visual.Moveable;
import visual.Display;

public class Player extends Moveable {

    private int gap = 0;
    private int score = 0;

    public Player(int x, int y, int vx, int vy, int speed, Display display, int drawByte) {
        super(x, y, vx, vy, speed, display, Display.BYTE_PLAYER_MIN);
        setDrawByte(Display.BYTE_PLAYER_MIN << 1);
        addEnemyByte(Display.BYTE_PLAYER);
        addEnemyByte(Display.BYTE_NPC);
        addEnemyByte(Display.BYTE_WALL);
        setTraceColor(Color.CYAN);
        setDrawByte(drawByte);
    }

    @Override
    public void update(int tick) throws Exception {
        super.update(tick);
        if (getDisplay().isKey(KeyEvent.VK_LEFT))
            turn(-5);
        if (getDisplay().isKey(KeyEvent.VK_RIGHT))
            turn(5);

        if (!isVisible()) {
            if (gap == 0) {
                setVisible(true);
            } else {
                gap--;
            }
        } else if (Math.random() < 0.01) {
            setVisible(false);
            gap = 15;
        }
    }

    public void addToScore(int value) {
        score += value;
    }

    public int getScore() {
        return score;
    }

    @Override
    public void onCrash(int code) {
        getDisplay().stop();
        System.out.println("You Lose!!!");
        System.out.print("You were killed by ");
        if ((code & Display.BYTE_PLAYER) != 0) {
            System.out.println("player " + ((int) (Math.log(code & Display.BYTE_PLAYER) / Math.log(2)) - Display.BYTE_SHIFT_PLAYER + 1));
        } else if ((code & Display.BYTE_NPC) != 0) {
            System.out.println("NPC " + ((int) (Math.log(code & Display.BYTE_NPC) / Math.log(2)) - Display.BYTE_SHIFT_NPC + 1));
        } else if ((code & Display.BYTE_WALL) != 0) {
            System.out.println("a Wall");
        } else {
            System.out.println("something I dont know... what could that be? Are these... aliens?");
        }
        System.out.println("You have " + getScore() + " Points!!!");
        getDisplay().gameOver(this, code);
        System.exit(0);
    }
}
