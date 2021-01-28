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
    }

    public void addToScore(int value) {
        score += value;
    }

    public int getScore() {
        return score;
    }

    @Override
    public void onCrash(int code) {
        super.onCrash(code);
    }
}
