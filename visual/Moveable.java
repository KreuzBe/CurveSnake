package visual;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.ArrayList;

public class Moveable {

    private BufferedImage trace;
    private Graphics2D traceGraphics;
    private Color traceColor = Color.GREEN;
    private float x, y;
    private float vx, vy;
    private float speed;
    private boolean decay = false;
    private int life = -1;
    private boolean isVisible = true;
    private boolean remoteControlled = false;
    private float lastX, lastY;

    private Display display;

    public boolean isRemoteControlled() {
        return remoteControlled;
    }

    public void setRemoteControlled(boolean remoteControlled) {
        this.remoteControlled = remoteControlled;
    }

    private int drawByte = 0b0;
    private int enemyBytes = Display.BYTE_WALL;

    public Moveable(float x, float y, float vx, float vy, float speed, Display display, int drawByte) {
        this.x = this.lastX = x;
        this.y = this.lastY = y;
        this.vx = vx;
        this.vy = vy;
        this.speed = speed;
        this.drawByte = drawByte;

        this.display = display;
        clear();
        simplifyVector();
    }

    public void clear() {
        trace = new BufferedImage(Display.WIDTH, Display.HEIGHT, BufferedImage.TYPE_INT_ARGB);
        traceGraphics = trace.createGraphics();
    }

    public void lookAt(float lx, float ly, int maxAngle) {
        double dx = lx - x;
        double dy = ly - y;
        double angleTarget = Math.atan(dy / dx);
        double angleMove = Math.atan(this.getVY() / this.getVX());
        double deltaAngle = deltaAngle = angleTarget - angleMove;

        if (Math.signum(dx) != Math.signum(getVX())) // Wrong direction lol
            deltaAngle = angleMove;
        else
            deltaAngle = angleTarget - angleMove;

        if (Math.abs(deltaAngle) > Math.toRadians(maxAngle))
            deltaAngle = Math.signum(deltaAngle) * Math.toRadians(maxAngle);

        turnRadians((float) deltaAngle);
    }

    public void lookAway(float lx, float ly, int maxAngle) {
        double dx = lx - x;
        double dy = ly - y;
        double angleTarget = Math.atan(dy / dx);
        double angleMove = Math.atan(this.getVY() / this.getVX());
        double deltaAngle = deltaAngle = angleTarget - angleMove;

        if (Math.signum(dx) != Math.signum(getVX())) // Wrong direction lol
            deltaAngle = angleMove;
        else
            deltaAngle = angleTarget - angleMove;

        if (deltaAngle == 0) deltaAngle++;

        if (Math.abs(deltaAngle) > Math.toRadians(maxAngle))
            deltaAngle = Math.signum(deltaAngle) * Math.toRadians(maxAngle);

        turnRadians((float) -deltaAngle);
    }

    public void update(int tick) throws Exception {
        if (!isRemoteControlled()) {
            x += vx * speed;
            y += vy * speed;
        }

        if (life > 0)
            life--;
        else if (life == 0)
            decay = true;

        if (x < 0 || y < 0 || x > Display.WIDTH || y > Display.HEIGHT) {
            onCrash(0);
        }

        int[][] map = display.getMap();

        if ((map[(int) x][(int) y] & Display.BYTE_POWERUP) != 0) {
            PowerUp.activatePowerUp(this, map[(int) x][(int) y]);
        }
        int radius = 10;
        getDisplay().fg.setColor(traceColor);
        getDisplay().fg.fillOval((int) (getX() - radius), (int) (getY() - radius), 2 * radius, 2 * radius);

        if (!isVisible())
            return;
        try {
            if ((map[(int) x][(int) y] & enemyBytes) != 0) {
                onCrash(map[(int) x][(int) y]);
            }
        } catch (Exception e) {
            e.printStackTrace();
            //  System.exit(0);
        }

        for (int i = -radius; i <= radius; i++) {
            for (int j = -radius; j <= radius; j++) {
                if (i * i + j * j < radius * radius)
                    try {
                        getDisplay().setMapByte(((int) (x - getVX() * radius)) + i, ((int) (y - getVY() * radius)) + j, drawByte);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
            }
        }

        if (!isVisible) {

            return;
        }
        traceGraphics.setColor(traceColor);
        for (int i = 0; i < getSpeed(); i++) {
            traceGraphics.fillOval((int) (getX() - i * getVX()) - 5, (int) (getY() - i * getVY()) - 5, radius, radius);
        }
        if (isRemoteControlled()) {
            traceGraphics.setStroke(new BasicStroke(radius, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, radius));
            traceGraphics.drawLine((int) x, (int) y, (int) lastX, (int) lastY);
            lastX = x;
            lastY = y;
        }
    }

    public void onCrash(int code) {
        System.out.println("CRASH!!!");
    }

    public void turn(float angle) {
        double atan = Math.atan(vy / vx) + Math.toRadians(angle);
        if (vx < 0.0) {
            vx = (float) -Math.cos(atan);
            vy = (float) -Math.sin(atan);
        } else {
            vx = (float) Math.cos(atan);
            vy = (float) Math.sin(atan);
        }
    }

    public void turnRadians(float radians) {
        double atan = Math.atan(vy / vx) + radians;
        if (vx < 0.0) {
            vx = (float) -Math.cos(atan);
            vy = (float) -Math.sin(atan);
        } else {
            vx = (float) Math.cos(atan);
            vy = (float) Math.sin(atan);
        }
    }

    public boolean shouldDecay() {
        return decay;
    }

    public void decay() {
        decay = true;
    }

    public void setlife(int life) {
        this.life = life;
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

    public float getVX() {
        return vx;
    }

    public void setVX(float vx) {
        this.vx = vx;
    }

    public float getVY() {
        return vy;
    }

    public void setVY(float vy) {
        this.vy = vy;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setVisible(boolean visible) {
        this.isVisible = visible;
    }

    public Display getDisplay() {
        return display;
    }

    public void setDisplay(Display display) {
        this.display = display;
    }

    public void setDrawByte(int b) {
        drawByte = b;
    }

    public int getDrawByte() {
        return drawByte;
    }

    public void addEnemyByte(int b) {
        enemyBytes |= b;
    }

    public int getEnemyBytes() {
        return enemyBytes;
    }

    public void removeEnemyByte(int b) {
        enemyBytes &= ~b;
    }

    public BufferedImage getImage() {
        return trace;
    }

    public Graphics2D getGraphics() {
        return traceGraphics;
    }

    public void setTraceColor(Color color) {
        traceColor = color;
    }

    public Color getTraceColor() {
        return traceColor;
    }

    public void simplifyVector() {
        double l = Math.sqrt(vx * vx + vy * vy);
        if (l == 0)
            return;
        vx /= l;
        vy /= l;
    }
}

