package visual;

import visual.Display;
import java.util.ArrayList;

public class Moveable {
	public static final int TYPE_PLAYER = 1;
	public static final int TYPE_NPC = 2;
	public static final int TYPE_BULLET = 3;
	
	
	private float x, y;
	private float vx, vy;
	private float speed;
	private boolean decay = false;
	private int life = -1;
	private int type;
	private boolean isVisible = true;
	
	private Display display;
	
	private int drawByte = 0b0;
	private ArrayList<Integer> enemyBytes = new ArrayList();
	
	
	
	public Moveable(float x, float y, float vx, float vy, float speed, Display display ,int type) {
		this.x = x;
		this.y = y;
		this.vx = vx;
		this.vy = vy;
		this.speed = speed;
		this.type = type;
		
		this.display = display;
		
		simplifyVector();
	}
	
	public void update(int tick) {
		x += vx * speed;
		y += vy * speed;
		
		if(life > 0)
			life--;
		else if(life == 0)
			decay = true;
		
		int[][] map = display.getMap();
		
		if(!isVisible())
				return;
			
		for(int i : enemyBytes) {
			if((map[(int)x][(int)y] & i) == i) {
				System.out.println("STOP: " + this);
				System.exit(0);
			}
		}
		
		int radius = 10;
		
		for(int i = -radius; i <= radius; i++) {
			for(int j = -radius; j <= radius; j++) {
				if(i*i + j*j < radius * radius)
					map[((int)(x - getVX() * radius)) + i][((int)(y - getVY() * radius)) + j] |= drawByte;
			}
		}
		
		
		
	}
	
	public void turn(float angle) {
		double atan = Math.atan(vy/vx) + Math.toRadians(angle);
		if(vx < 0.0) {
			vx = (float) -Math.cos(atan);
			vy = (float) -Math.sin(atan);
		} else {
			vx = (float) Math.cos(atan);
			vy = (float) Math.sin(atan);
		}
	}
	
	public void turnRadians(float radians) {
		double atan = Math.atan(vy/vx) + radians;
		if(vx < 0.0) {
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
	
	public int getType() {
		return type;
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
	
	public void setDrawByte(int b){
		drawByte = b;
	}
	
	public int getDrawByte() {
		return drawByte;
	}
	
	public void addEnemyByte(int b) {
		enemyBytes.add(b);
	}
	
	public ArrayList<Integer> getEnemyBytes() {
		return enemyBytes;
	}
	
	public void removeEnemyByte(Integer b) {
		enemyBytes.remove(b);
	}
	
	public void simplifyVector() {
		double l = Math.sqrt(vx * vx + vy * vy);
		if(l == 0)
			return;
		vx /= l;
		vy /= l;
	}
}

