package visual;

import visual.moveable.Player;

import java.awt.*;

public class PowerUp extends Moveable {

    private int radius;
    private int drawByte = 0;
    private int imageNumber;
    private boolean isRemoteControlled;

    public PowerUp(float x, float y, int radius, int drawByte, Display display, boolean isRemoteControlled) {
        super(x, y, 0f, 0f, 0f, display, drawByte);
        this.radius = radius;
        this.drawByte = drawByte;
        this.imageNumber = ((int) (Math.log(drawByte) / Math.log(2)) - 1); //
        this.isRemoteControlled = isRemoteControlled;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public int getDrawByte() {
        return drawByte;
    }

    public void setDrawByte(int drawByte) {
        this.drawByte = drawByte;
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
            if (moveable instanceof Player)
                ((Player) moveable).addToScore(5);
        }

        if ((code & (Display.BYTE_POWERUP_MIN << 1)) != 0) { // clear field
            moveable.getDisplay().removePowerUp(code & Display.BYTE_POWERUP);
            if (moveable instanceof Player)
                ((Player) moveable).addToScore(1);
            moveable.getDisplay().clear();
        }

        if ((code & (Display.BYTE_POWERUP_MIN << 2)) != 0) { // clear me
            moveable.clear();
            if (moveable instanceof Player)
                ((Player) moveable).addToScore(3);
            for (int x = 0; x < Display.WIDTH; x++) {
                for (int y = 0; y < Display.HEIGHT; y++) {
                    moveable.getDisplay().unsetMapByte(x, y, moveable.getDrawByte());
                }
            }

            moveable.getDisplay().removePowerUp(code & Display.BYTE_POWERUP);
        }
        if ((code & (Display.BYTE_POWERUP_MIN << 3)) != 0) { // clear walls
            moveable.setInvisibleTicks(100);
            moveable.getDisplay().removePowerUp(code & Display.BYTE_POWERUP);
        }
    }
}
