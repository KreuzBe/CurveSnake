package visual;

import util.ImageLoader;
import visual.animation.Explosion;
import visual.animation.LineCleared;
import visual.moveable.Player;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.Serializable;

public class PowerUp extends VisualObject implements Serializable {

    private int radius;
    private int imageNumber;
    private boolean isRemoteControlled;

    public PowerUp(float x, float y, int radius, int drawByte, Display display, boolean isRemoteControlled) {
        super(display, x, y, drawByte);
        this.radius = radius;
        this.imageNumber = ((int) (Math.log(drawByte) / Math.log(2)) - 1); //
        if (imageNumber > 3) imageNumber = 3;
        this.isRemoteControlled = isRemoteControlled;
    }

    public boolean isRemoteControlled() {
        return isRemoteControlled;
    }

    public void setRemoteControlled(boolean remoteControlled) {
        isRemoteControlled = remoteControlled;
    }

    @Override
    public void update(int tick) throws Exception {
        super.update(tick);
        clear();
        BufferedImage img = ImageLoader.images[getImageNumber()];
        float v = (float) (5 * (Math.sin(Math.toRadians(5 * tick))));
        if (img != null) {
            getGraphics().drawImage(img, (int) (getX() - getRadius()), (int) (getY() - getRadius() - v), 2 * getRadius(), 2 * getRadius(), null);
        } else {
            getGraphics().setColor(Color.ORANGE);
            getGraphics().fillRect((int) (getX() - getRadius()), (int) (getY() - getRadius() - v), 2 * getRadius(), 2 * getRadius());
        }
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }


    public int getImageNumber() {
        return imageNumber;
    }

    public static void activatePowerUp(Moveable moveable, int code) {
        //     System.out.println("PowerUp activated: 0b" + Integer.toBinaryString(code));
        if ((code & Display.BYTE_POWERUP) == 0)
            return;

        if ((code & (Display.BYTE_POWERUP_MIN)) != 0) { // clear field
            moveable.getDisplay().removePowerUp(code & Display.BYTE_POWERUP);
            moveable.addToScore(5);
            moveable.getDisplay().getMoveables().forEach(mo -> mo.setSpeed(mo.getSpeed() + 0.5f));
        }

        if ((code & (Display.BYTE_POWERUP_MIN << 1)) != 0) { // clear field
            moveable.getDisplay().removePowerUp(code & Display.BYTE_POWERUP);
            moveable.addToScore(1);
            moveable.getDisplay().getMoveables().forEach(mo -> mo.setSpeed(mo.getSpeed() + 0.1f));
            moveable.getDisplay().clear();
        }

        if ((code & (Display.BYTE_POWERUP_MIN << 2)) != 0) { // clear me
            moveable.clear();
            if (moveable instanceof Player)
                moveable.addToScore(3);
            for (int x = 0; x < Display.WIDTH; x++) {
                for (int y = 0; y < Display.HEIGHT; y++) {
                    moveable.getDisplay().unsetMapByte(x, y, moveable.getDrawByte());
                }
            }
            moveable.getDisplay().addAnimation(new LineCleared(moveable.getDisplay(), moveable.getX(), moveable.getY(), 0b0, 100, 100, moveable.getTraceColor()));
            moveable.getDisplay().removePowerUp(code & Display.BYTE_POWERUP);
        }
        if ((code & ((Display.BYTE_POWERUP_MIN << 3) | (Display.BYTE_POWERUP << 4))) != 0) { // bomb
            int radius = (int) (100 + Math.random() * 200);
            for (Moveable mo : moveable.getDisplay().getMoveables()) {
                Composite defaultComposite = mo.getGraphics().getComposite();
                mo.getGraphics().setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR, 0.001f));
                mo.getGraphics().fillOval((int) moveable.getX() - radius, (int) moveable.getY() - radius, 2 * radius, 2 * radius);
                mo.getGraphics().setComposite(defaultComposite);
            }
            for (int x = -radius; x <= radius; x++) {
                for (int y = -radius; y <= radius; y++) {
                    if (x * x + y * y <= radius * radius) {
                        try {
                            moveable.getDisplay().unsetMapByte((int) moveable.getX() + x, (int) moveable.getY() + y, Display.BYTE_PLAYER | Display.BYTE_NPC);
                        } catch (Exception ignored) {
                        }
                    }
                }
            }
            moveable.getDisplay().addAnimation(new Explosion(moveable.getDisplay(), moveable.getX(), moveable.getY(), 0, 50, radius));
            moveable.setInvisibleTicks((int) (radius / (3f * moveable.getSpeed())));
            moveable.getDisplay().removePowerUp(code & Display.BYTE_POWERUP);
        }
    }

    public PowerUp copy() {
        return new PowerUp(this.getX(), this.getY(), this.getRadius(), this.getDrawByte(), null, this.isRemoteControlled());
    }
}
