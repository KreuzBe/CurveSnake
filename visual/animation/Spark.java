package visual.animation;

public class Spark {
    private int radius;
    private float initSpeed, speed;
    private float vx, vy;
    private float x, y;
    private int life, maxLife;

    public Spark(float x, float y, float vx, float vy, float speed, int radius, int life) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.radius = radius;
        this.speed = speed;
        this.initSpeed = speed;
        setLife(life);
        setMaxLife(life);
        simplifyVector();
    }


    public void update(int tick) throws Exception {
        setLife(getLife() - 1);
        setSpeed(initSpeed * (1f * getLife() / getMaxLife()));
        setX(getX() + getVX() * getSpeed());
        setY(getY() + getVY() * getSpeed());
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
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

    public int getLife() {
        return life;
    }

    public void setLife(int life) {
        this.life = life;
    }

    public int getMaxLife() {
        return maxLife;
    }

    public void setMaxLife(int maxLife) {
        this.maxLife = maxLife;
    }

    public void simplifyVector() {
        double l = Math.sqrt(vx * vx + vy * vy);
        if (l == 0)
            return;
        vx /= l;
        vy /= l;
    }
}
