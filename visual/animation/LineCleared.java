package visual.animation;

import visual.Display;
import visual.moveable.Animation;

import java.awt.*;
import java.util.ArrayList;

public class LineCleared extends Animation {


    int radius;
    ArrayList<Spark> sparks = new ArrayList<>();
    private Color color;

    public LineCleared(Display display, float x, float y, int drawByte, int lifeTime, int radius, Color color) {
        super(display, x, y, drawByte, lifeTime);
        this.radius = radius;
        this.color = color;
        for (int i = 0; i < 10; i++) {
            float s = (float) ((2 * Math.random()));
            sparks.add(new Spark(getX(), getY(), (float) (2 * Math.random() - 1), (float) (2 * Math.random() - 1), s, 3, this.getLife()));
        }

    }


    @Override
    public void update(int tick) throws Exception {
        clear();
        Graphics2D g = getGraphics();
        g.setColor(Math.random() < 0.5 ? color.darker() : color.brighter());
        for (Spark spark : sparks) {
            g.fillOval((int) (spark.getX() - spark.getRadius()), (int) (spark.getY() - spark.getRadius()), 2 * spark.getRadius(), 2 * spark.getRadius());
            spark.update(tick);
        }
        super.update(tick);
    }
}
