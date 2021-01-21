 
import java.awt.event.*;
import visual.Display;
import visual.Moveable;
import visual.moveable.Enemy;
import visual.moveable.Player;

public class Main extends Display {
	
	private Player player;
	private Enemy enemy;
	
	public Main() {
		player = new Player(800, 200, 1, 1, 7, this);
		this.addMoveable(player);
		
		enemy = new Enemy((int)	(800), 100, 1, 1,  5 ,this, player);
		this.addMoveable(enemy);
		
		start();
	}
	
	public static void main(String[] args) {
		new Main();
	} 
}
