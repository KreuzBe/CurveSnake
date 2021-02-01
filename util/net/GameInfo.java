package util.net;

import visual.Moveable;
import visual.PowerUp;

import java.io.Serializable;
import java.util.ArrayList;

public class GameInfo implements Serializable {

    public ArrayList<Moveable> moveables = new ArrayList<Moveable>();
    public ArrayList<PowerUp> powerups = new ArrayList<PowerUp>();
    public boolean stop = false;

    // public void addMoveable(float x, float y, float vx, float vy, float speed, int drawByte, int enemyByte, boolean isVisible) {
    // moveables.add(new ObjectContainer(x, y, vx, vy, speed, drawByte, enemyByte, isVisible, 1));
    //}

    // public void addPowerUp(float x, float y, int radius, int drawByte) {
    // powerups.add(new ObjectContainer(x, y, 0, 0, 0, drawByte, 0, true, radius));
    //}

    public void addMoveable(Moveable mo) {
        moveables.add(mo);
    }

    public void addPowerUp(PowerUp p) {
        powerups.add(p);
    }

    public static class ObjectContainer implements Serializable {
        public float x, y, vx, vy, speed;
        public int drawByte, enemyByte;
        public boolean isVisible;
        public int radius;

        public ObjectContainer(float x, float y, float vx, float vy, float speed, int drawByte, int enemyByte, boolean isVisible, int radius) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.speed = speed;
            this.drawByte = drawByte;
            this.enemyByte = enemyByte;
            this.isVisible = isVisible;
            this.radius = radius;
        }
    }
}
