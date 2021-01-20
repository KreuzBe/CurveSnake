package visual;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.event.*;
import javax.swing.*;
import java.util.ArrayList;
import util.Loop;
import visual.Moveable;

public class Display extends JPanel implements KeyListener {

	private static final Toolkit TOOLKIT = Toolkit.getDefaultToolkit();
	private Loop loop;
	private JFrame frame;
	private ArrayList<Moveable> moveables;
	private ArrayList<Moveable> removedMoveables;
	
	private boolean[] keys = new boolean[256];
	private boolean[] keysOld = new boolean[256];
	
	private int[][] map; // maps the lines
	
	private BufferedImage visualMap;
	private BufferedImage foreground;
	public Graphics g;
	public Graphics fg;
	
	public Display() {
		moveables = new ArrayList();
		removedMoveables = new ArrayList();
		loop = new Loop(30, this::update);
		
		frame = new JFrame();
		frame.setBounds(0, 0, 1600, 900);
		frame.setLocationRelativeTo(null);
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);
		frame.addKeyListener(this);
		frame.add(this);
	
		frame.setVisible(true);
		repaint(); // causes JPanel to resize
		map = new int[(int)TOOLKIT.getScreenSize().getWidth()][(int)TOOLKIT.getScreenSize().getHeight()];
		
		visualMap = new BufferedImage((int)TOOLKIT.getScreenSize().getWidth(), (int)TOOLKIT.getScreenSize().getHeight(),BufferedImage.TYPE_INT_RGB);
		foreground = new BufferedImage((int)TOOLKIT.getScreenSize().getWidth(), (int)TOOLKIT.getScreenSize().getHeight(),BufferedImage.TYPE_INT_ARGB);
		g = visualMap.createGraphics();
		fg = foreground.createGraphics();
		System.out.println(this.getWidth());
	}
	
	private void update(int tick){
		
		paintMap();
		
		foreground = new BufferedImage((int)TOOLKIT.getScreenSize().getWidth(), (int)TOOLKIT.getScreenSize().getHeight(),BufferedImage.TYPE_INT_ARGB);
		fg = foreground.createGraphics();
		
		frame.setTitle(tick + " " + moveables.size());
		onUpdate(tick);
		

		for(int i = 0; i < keys.length; i++)
			keysOld[i] = keys[i];

		for(Moveable mo : moveables) {
			mo.update(tick);
			if(mo.shouldDecay())
				removedMoveables.add(mo);
		}
		
		for(Moveable mo : removedMoveables) {
			moveables.remove(mo);
		}
		removedMoveables.clear();
		
		repaint();
	}
	
	private void paintMap() {
		for(Moveable mo : moveables) {
			if(!mo.isVisible())
				continue;
			
			switch(mo.getType()){
				case Moveable.TYPE_PLAYER:
					g.setColor(Color.CYAN);
					break;
				case Moveable.TYPE_NPC:
					g.setColor(Color.RED);
					break;
				default:
					g.setColor(Color.GRAY);
				
			}
			
			for(int i = 0; i < mo.getSpeed(); i++){
				g.fillOval((int) (mo.getX() - i * mo.getVX()) - 5, (int) (mo.getY() - i * mo.getVY())- 5, 10, 10);
			}
		}
	}
	
	public void addMoveable(Moveable mo) {
		moveables.add(mo);
	}
	
	public void onUpdate(int tick){};
	
	public void start() {
		loop.start();
	}
	
	public void stop() {
		loop.stop();
	}
	
	public boolean isRunning() {
		return loop.isRunning();
	}
	
	public int[][] getMap() {
		return map;
	}
	
	public void keyPressed(KeyEvent e) {
		if(loop.isRunning() && e.getKeyCode() < keys.length)
			keys[e.getKeyCode()] = true;
	}
	public void keyReleased(KeyEvent e) {
		if(loop.isRunning() && e.getKeyCode() < keys.length)
			keys[e.getKeyCode()] = false;
	}
	
	public void keyTyped(KeyEvent e) {
		
	}
	
	public boolean isKey(int keyCode){
		return keys[keyCode];
	}
	public boolean isKeyPressed(int keyCode) {
		return keys[keyCode] && !keysOld[keyCode];
	}
	
	public boolean isKeyReleased(int keyCode) {
		return !keys[keyCode] && keysOld[keyCode];
	}
	
	//public Graphics getGraphics(){
	//	return g;
	//}
	
	public void paint(Graphics graphics) {
		graphics.drawImage(visualMap, 0, 0, null);
		graphics.drawImage(foreground, 0, 0, null);
		//System.out.println("PAINT");
		for(Moveable mo : moveables) {
			switch(mo.getType()){
				case Moveable.TYPE_PLAYER:
					graphics.setColor(Color.CYAN);
					break;
				case Moveable.TYPE_NPC:
					graphics.setColor(Color.RED);
					break;
				default:
					graphics.setColor(Color.GRAY);
			}
			graphics.fillOval((int) mo.getX() - 10, (int) mo.getY() - 10, 20, 20);
		}
	}
	
}

