package visual;

import visual.moveable.Player;

import java.awt.*;

public class PowerUp extends Moveable {

    private int radius;
    private int drawByte = 0;
    private int imageNumber;

    public PowerUp(float x, float y, int radius, int drawByte, Display display) {
        super(x, y, 0f, 0f, 0f, display, TYPE_BULLET);
        this.radius = radius;
        this.drawByte = drawByte;
        this.imageNumber = ((int) (Math.log(drawByte) / Math.log(2)) - 1); //
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
        System.out.println("PowerUp activated: 0b" + Integer.toBinaryString(code));
        if ((code & Display.BYTE_POWERUP) == 0)
            return;

        if ((code & (Display.BYTE_POWERUP_MIN)) != 0) { // clear field
            moveable.getDisplay().removePowerUp(code & Display.BYTE_POWERUP);
            if (moveable instanceof Player)
                ((Player) moveable).addToScore(1);
        }

        if ((code & (Display.BYTE_POWERUP_MIN << 1)) != 0) { // clear field
            moveable.getDisplay().removePowerUp(code & Display.BYTE_POWERUP);
            moveable.getDisplay().clear();
        }

        if ((code & (Display.BYTE_POWERUP_MIN << 2)) != 0) { // Bomb
            int radius = 200 + (int) (Math.random() * 200);
            moveable.getDisplay().fg.setColor(Color.WHITE);
            moveable.getDisplay().fg.fillOval((int) moveable.getX() - radius, (int) moveable.getY() - radius, 2 * radius, 2 * radius);
            for (int rx = -radius; rx <= radius; rx++) {
                for (int ry = -radius; ry <= radius; ry++) {
                    try {
                        moveable.getDisplay().unsetMapByte((int) (moveable.getX() + rx), (int) (moveable.getY() + ry), Display.BYTE_NPC | Display.BYTE_PLAYER);
                    } catch (Exception ignore) {
                    }
                }
            }
            for (Moveable mo : moveable.getDisplay().getMoveables()) {
                mo.getGraphics().setColor(Display.bgColor);
                mo.getGraphics().setPaint(new Color(0f, 0f, 1f, 0.0f));
                mo.getGraphics().setXORMode(mo.getTraceColor());
                mo.getGraphics().fillOval((int) moveable.getX() - radius, (int) moveable.getY() - radius, 2 * radius, 2 * radius);
                mo.getGraphics().setPaintMode();
            }

            moveable.getDisplay().removePowerUp(code & Display.BYTE_POWERUP);
        }
    }
}
