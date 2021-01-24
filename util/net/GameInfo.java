package util.net;

import java.io.Serializable;
import java.util.ArrayList;

public class GameInfo implements Serializable {

    public ArrayList<ObjectContainer> moveables = new ArrayList<ObjectContainer>();
    public ArrayList<ObjectContainer> powerups = new ArrayList<ObjectContainer>();
    public boolean stop = false;

    public void addMoveable(float x, float y, float vx, float vy, float speed, int drawByte, boolean isVisible) {
        moveables.add(new ObjectContainer(x, y, vx, vy, speed, drawByte, isVisible, 1));
    }

    public void addPowerUp(float x, float y, int radius, int drawByte) {
        powerups.add(new ObjectContainer(x, y, 0, 0, 0, drawByte, true, radius));
    }

    public static class ObjectContainer implements Serializable {
        public float x, y, vx, vy, speed;
        public int drawByte;
        public boolean isVisible;
        public int radius;

        public ObjectContainer(float x, float y, float vx, float vy, float speed, int drawByte, boolean isVisible, int radius) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.speed = speed;
            this.drawByte = drawByte;
            this.isVisible = isVisible;
            this.radius = radius;
        }
    }
}
