package visual.moveable;

import visual.Display;
import visual.Moveable;
import visual.animation.Explosion;

import java.awt.*;

public class Bullet extends Moveable {

    public Bullet(float x, float y, float vx, float vy, float speed, Display display) {
        super(x, y, vx, vy, speed, display, 0b0);
        setLife(1000);
        setTraceColor(new Color(0, 0, 0, 0));
        setVisible(true);
    }

    @Override
    public void update(int tick) throws Exception {

        move();

        if (getX() < 0 || getX() > Display.WIDTH) {
            setVX(-getVX());
            move();
        }
        if (getY() < 0 || getY() > Display.HEIGHT) {
            setVY(-getVY());
            move();
        }

        if ((getDisplay().getMap()[(int) getX()][(int) getY()] & (Display.BYTE_PLAYER | Display.BYTE_NPC | Display.BYTE_WALL)) != 0) {
            int radius = (int) (20 + Math.random() * 30);
            for (Moveable mo : getDisplay().getMoveables()) {
                Composite defaultComposite = mo.getGraphics().getComposite();
                mo.getGraphics().setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR, 0.001f));
                mo.getGraphics().fillOval((int) getX() - radius, (int) getY() - radius, 2 * radius, 2 * radius);
                mo.getGraphics().setComposite(defaultComposite);
            }
            for (int x = -radius; x <= radius; x++) {
                for (int y = -radius; y <= radius; y++) {
                    if (x * x + y * y <= radius * radius) {
                        try {
                            getDisplay().unsetMapByte((int) getX() + x, (int) getY() + y, Display.BYTE_PLAYER | Display.BYTE_NPC);
                        } catch (Exception ignored) {
                        }
                    }
                }
            }
            getDisplay().addAnimation(new Explosion(getDisplay(), getX(), getY(), 0, 10, 2 * radius));
            // decay();
        }


        if (getLife() > 0)
            setLife(getLife() - 1);
        else if (getLife() == 0)
            decay();
    }

    @Override
    public void onCrash(int code) {

    }
}
