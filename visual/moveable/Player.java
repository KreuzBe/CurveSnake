package visual.moveable;

import java.awt.*;
import java.awt.event.*;

import util.ImageLoader;
import visual.Moveable;
import visual.Display;

public class Player extends Moveable {


    public Player(float x, float y, float vx, float vy, float speed, Display display, int drawByte) {
        super(x, y, vx, vy, speed, display, drawByte);
        setDrawByte(Display.BYTE_PLAYER_MIN << 1);
        addEnemyByte(Display.BYTE_PLAYER);
        addEnemyByte(Display.BYTE_NPC);
        addEnemyByte(Display.BYTE_WALL);
        setTraceColor(Color.CYAN);
        setDrawByte(drawByte);
        setRocketImage(ImageLoader.images[5]);

    }

    @Override
    public void update(int tick) throws Exception {
        if (getDisplay().isKey(KeyEvent.VK_LEFT))
            turn(-5);
        if (getDisplay().isKey(KeyEvent.VK_RIGHT))
            turn(5);
        if (getDisplay().isKeyPressed(KeyEvent.VK_SPACE))
            getDisplay().addMoveable(new Bullet(getX() + getSpeed() * getVX(), getY() + getSpeed() * getVY(), getVX(), getVY(), 2 * getSpeed(), getDisplay()));


        super.update(tick);

    }
}
