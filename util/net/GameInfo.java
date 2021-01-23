package util.net;

import java.io.Serializable;
import java.util.ArrayList;

public class GameInfo implements Serializable {

    public ArrayList<ObjectContainer> moveables = new ArrayList<ObjectContainer>();
    public ArrayList<ObjectContainer> powerups = new ArrayList<ObjectContainer>();

    public void addMoveable(float x, float y, float vx, float vy, float speed, int drawByte) {
        moveables.add(new ObjectContainer(x, y, vx, vy, speed, drawByte));
    }

    private static class ObjectContainer {
        float x, y, vx, vy, speed;
        int drawByte;

        public ObjectContainer(float x, float y, float vx, float vy, float speed, int drawByte) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.speed = speed;
            this.drawByte = drawByte;
        }
    }
}