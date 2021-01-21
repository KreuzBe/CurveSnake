package visual.moveable;

import java.awt.event.*;
import visual.Moveable;
import visual.Display;

public class Player extends Moveable {
	
	private int gap = 0;
	
	public Player(int x, int y, int vx, int vy, int speed, Display display) {
		super(x, y, vx, vy, speed, display,Moveable.TYPE_PLAYER);
		setDrawByte(0b10);
		addEnemyByte(0b10);
		addEnemyByte(0b1);
		addEnemyByte(0b100);
	}
	
	@Override
	public void update(int tick) {
		super.update(tick);
		if(getDisplay().isKey(KeyEvent.VK_LEFT))
			turn(-7);
		if(getDisplay().isKey(KeyEvent.VK_RIGHT))
			turn(7);
		
		if(!isVisible()) {
			if(gap == 0) {
				setVisible(true);
			} else {
				gap--;
			}
		}else if(Math.random() < 0.01) {
			setVisible(false);
			gap = 10;
		}
		
	}
	
	
}
