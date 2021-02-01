package visual;

import util.ImageLoader;
import visual.animation.Explosion;
import visual.animation.LineCleared;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Moveable extends VisualObject {

    private int maxGap = 0;
    private int gap = 0;

    private Color traceColor = Color.GREEN;
    private BufferedImage rocketImage;
    private float vx, vy;
    private float speed;
    private boolean isVisible = true;
    private boolean remoteControlled = false;
    private float lastX, lastY;
    private int score = 0;

    private int enemyBytes = Display.BYTE_WALL;

    int radius = 10;


    public Moveable(float x, float y, float vx, float vy, float speed, Display display, int drawByte) {
        super(display, x, y, drawByte);
        this.lastX = x;
        this.lastY = y;
        this.vx = vx;
        this.vy = vy;
        this.speed = speed;
        this.rocketImage = ImageLoader.images[4];
        clear();
        setInvisibleTicks(50);
        simplifyVector();
    }


    public void lookAt(float lx, float ly, int maxAngle) {
        double dx = lx - getX();
        double dy = ly - getY();
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
        double dx = lx - getX();
        double dy = ly - getY();
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
        move();
        super.update(tick);
        // gaps:
        if (!isVisible()) {
            if (gap == 0) {
                setVisible(true);
                maxGap = 0;
            } else {
                gap--;
            }
        } else if (Math.random() < 0.01) {
            setInvisibleTicks(15);
        }


        if (getX() < 0 || getY() < 0 || getX() > Display.WIDTH || getY() > Display.HEIGHT) {
            onCrash(0);
        }

        int[][] map = getDisplay().getMap();

        if (((getDrawByte() & (Display.BYTE_PLAYER | Display.BYTE_NPC)) != 0) && (map[(int) getX()][(int) getY()] & Display.BYTE_POWERUP) != 0) {
            PowerUp.activatePowerUp(this, map[(int) getX()][(int) getY()]);
        }


        if (!isVisible) {
            lastX = getX();
            lastY = getY();
            return;
        }
        try {
            if ((map[(int) getX()][(int) getY()] & enemyBytes) != 0) {
                onCrash(map[(int) getX()][(int) getY()]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (getDrawByte() != 0b0)
            for (int i = -radius; i <= radius; i++) {
                for (int j = -radius; j <= radius; j++) {
                    if (i * i + j * j < radius * radius)
                        try {
                            getDisplay().setMapByte(((int) (getX() - getVX() * radius)) + i, ((int) (getY() - getVY() * radius)) + j, getDrawByte());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                }
            }


    }

    public void move() {
        setX(getX() + getVX() * getSpeed());
        setY(getY() + getVY() * getSpeed());
    }

    public void paint(int tick) {
        getDisplay().fg.setColor(traceColor);
        float angle = (float) (Math.atan(vy / vx) - Math.PI / 2 + (vx < 0 ? 0 : Math.PI));
        ImageLoader.drawRotatedImage(rocketImage, (int) (getX() - 2 * radius), (int) (getY() - 2 * radius), 4 * radius, 4 * radius, angle, (Graphics2D) getDisplay().fg);

        if (gap > 0 && maxGap > 15) {
            getDisplay().fg.setColor(Color.DARK_GRAY);
            getDisplay().fg.fillRect((int) getX() - 2 * radius, (int) getY() - 30, 4 * radius, 10);
            getDisplay().fg.setColor(Color.GREEN);
            getDisplay().fg.fillRect((int) (getX() - 2 * radius + (4 * radius * (1 - 1d * gap / maxGap))), (int) getY() - 30, (int) (4 * radius * (1d * gap / maxGap)), 10);
        }
        if (!isVisible) {
            return;
        }
        getGraphics().setColor(traceColor);
        if (isRemoteControlled()) {
            getGraphics().setStroke(new BasicStroke(radius, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, radius));
            getGraphics().drawLine((int) getX(), (int) getY(), (int) lastX, (int) lastY);
            lastX = getX();
            lastY = getY();
            return;
        }
        for (int i = 0; i < getSpeed(); i++) {
            getGraphics().setColor(traceColor);
            getGraphics().fillOval((int) (getX() - i * getVX()) - 5, (int) (getY() - i * getVY()) - 5, radius, radius);
        }
    }


    public void onCrash(int code) {
        if (isRemoteControlled()) return;
        System.out.print(this + " was killed by ");
        if ((code & Display.BYTE_PLAYER) != 0) {
            System.out.println("player " + ((int) (Math.log(code & Display.BYTE_PLAYER) / Math.log(2)) - Display.BYTE_SHIFT_PLAYER + 1));
        } else if ((code & Display.BYTE_NPC) != 0) {
            System.out.println("NPC " + ((int) (Math.log(code & Display.BYTE_NPC) / Math.log(2)) - Display.BYTE_SHIFT_NPC + 1));
        } else if ((code & Display.BYTE_WALL) != 0) {
            System.out.println("a Wall");
        } else {
            System.out.println("something I dont know... what could that be? Are these... aliens?");
        }
        getDisplay().addAnimation(new LineCleared(getDisplay(), getX(), getY(), 0b0, 30, 100, getTraceColor()));
        getDisplay().addAnimation(new LineCleared(getDisplay(), getX(), getY(), 0b0, 30, 100, getTraceColor()));
        getDisplay().addAnimation(new Explosion(getDisplay(), getX(), getY(), 0, 10, 200));
        getDisplay().addAnimation(new Explosion(getDisplay(), getX(), getY(), 0, 10, 200));
        getDisplay().gameOver(this, code);
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

    public int getGap() {
        return gap;
    }

    public int getMaxGap() {
        return maxGap;
    }

    public void setGap(int gap) {
        this.gap = gap;
    }

    public void setMaxGap(int maxGap) {
        this.maxGap = maxGap;
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
        lastX = getX();
        lastY = getY();
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

    protected void setInvisibleTicks(int gap) {
        setVisible(false);
        this.gap += gap;
        if (this.gap > maxGap)
            maxGap = gap;
    }

    public void addToScore(int value) {
        score += value;
    }

    public int getScore() {
        return score;
    }

    public BufferedImage getRocketImage() {
        return rocketImage;
    }

    public void setRocketImage(BufferedImage rocketImage) {
        this.rocketImage = rocketImage;
    }

    public boolean isRemoteControlled() {
        return remoteControlled;
    }

    public void setRemoteControlled(boolean remoteControlled) {
        this.remoteControlled = remoteControlled;
    }

    public void clearEnemyByte() {
        enemyBytes = 0b0;
    }

}

