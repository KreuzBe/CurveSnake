package visual;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.HashMap;

public class VisualObject implements Serializable {

    private static final HashMap<Integer, VisualObject> INSTANCES = new HashMap<>();

    private Display display;
    private float x, y;
    private int drawByte; // also for identification
    private int life = -1;
    private int maxLife = 0;
    private boolean decay = false;

    private BufferedImage image;
    private Graphics2D graphics;

    public VisualObject(Display display, float x, float y, int drawByte) {
        INSTANCES.put(drawByte, this);
        this.display = display;
        this.x = x;
        this.y = y;
        this.drawByte = drawByte;
        clear();
    }

    public void kill() {
        INSTANCES.remove(drawByte);
    }

    public void update(int tick) throws Exception {
        checkLife();
    }

    public void checkLife() {
        if (getLife() > 0)
            setLife(getLife() - 1);
        else if (getLife() == 0)
            decay();
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public Display getDisplay() {
        return display;
    }

    public void setDisplay(Display display) {
        this.display = display;
    }

    public int getDrawByte() {
        return drawByte;
    }

    public void setDrawByte(int drawByte) {
        INSTANCES.remove(getDrawByte());
        this.drawByte = drawByte;
        INSTANCES.put(drawByte, this);
    }

    public static VisualObject getByDrawByte(int drawByte) {
        return INSTANCES.get(drawByte);
    }

    public int getLife() {
        return life;
    }

    public void setLife(int life) {
        this.life = life;
        if (maxLife < this.life)
            maxLife = life;
    }

    public int getMaxLife() {
        return maxLife;
    }

    public boolean shouldDecay() {
        return decay;
    }

    public void decay() {
        decay = true;
    }

    public void clear() {
        //  if (image == null || graphics == null) {
        image = new BufferedImage(Display.WIDTH, Display.HEIGHT, BufferedImage.TYPE_INT_ARGB);
        graphics = image.createGraphics();
//        } else {
//            Composite defaultComposite = graphics.getComposite();
//            graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR, 0.001f));
//            graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
//            graphics.setComposite(defaultComposite);
//        }
    }

    public Graphics2D getGraphics() {
        return graphics;
    }

    public BufferedImage getImage() {
        return image;
    }

    public VisualObject copy() throws CloneNotSupportedException {
        return (VisualObject) this.clone();
    }

}
