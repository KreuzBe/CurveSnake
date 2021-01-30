package visual.animation;

import visual.Display;
import visual.moveable.Animation;

import java.awt.*;
import java.util.ArrayList;

public class Explosion extends Animation {

    int radius;
    ArrayList<Spark> sparks = new ArrayList<>();

    public Explosion(Display display, float x, float y, int drawByte, int lifeTime, int radius) {
        super(display, x, y, drawByte, lifeTime);
        this.radius = radius;

        for (int i = 0; i < 10; i++) {
            float s = (float) (2 * Math.random());
            sparks.add(new Spark(getX(), getY(), (float) (2 * Math.random() - 1), (float) (2 * Math.random() - 1), s, 3, this.getLife()));
        }

    }


    @Override
    public void update(int tick) throws Exception {
        clear();
        Graphics2D g = getGraphics();
        g.setColor(new Color(.5f, .4f, 0f, .3f * getLife() / getMaxLife()));
        g.fillOval((int) (getX() - radius), (int) (getY() - radius), 2 * radius, 2 * radius);

        g.setColor(Math.random() < 0.5 ? Color.ORANGE : Color.RED);
        for (Spark spark : sparks) {
            g.fillOval((int) (spark.getX() - spark.getRadius()), (int) (spark.getY() - spark.getRadius()), 2 * spark.getRadius(), 2 * spark.getRadius());
            spark.update(tick);
        }
        super.update(tick);
    }
}
