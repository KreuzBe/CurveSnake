package visual.moveable;

import visual.Moveable;
import visual.Display;

import java.awt.*;
import java.util.ArrayList;

public class Enemy extends Moveable {

	private Moveable target;
	private int gap = 0;
	
	private ArrayList<Node> lastWay = new ArrayList();
	
	public Enemy(int x, int y, int vx, int vy, int speed,Display display, Moveable target) {
		super(x, y, vx, vy, speed, display ,TYPE_NPC);
		this.target = target;
		setDrawByte(0b100);
		addEnemyByte(0b10);
		addEnemyByte(0b1);
	}
	
	public void setTarget(Moveable mo) {
		target = mo;
	}
	
	private void aStarTest() throws Exception {
		int closedNodes = 0;
		
		ArrayList<Node> openNodes = new ArrayList();
		int[][] pMap = getDisplay().getMap();
		int scale = 10;
		int[][] map = new int[(pMap.length)/scale][(pMap[0].length)/scale];
		
		// /////////////////////////////
		
		
		/////////////////////////////////
		simplifyVector();
		for(int xm = 0; xm < pMap.length; xm ++) {
			for(int ym = 0; ym < pMap[0].length; ym ++) {
				if(xm/scale >= map.length ||  ym/scale >= map[0].length)
					System.out.println(xm/scale + " " + ym/scale);
				map[xm/scale][ym/scale] |= pMap[xm][ym];
			}
		}
		//System.exit(0);
		boolean found = false;
		
		int tx = (int) (getX() + (getSpeed() + 10) * getVX())/scale;
		int ty = (int) (getY() + (getSpeed()+ 10)* getVY())/scale;
		int x = (int) (target.getX() + 10 * target.getVX())/scale;
		int y = (int) (target.getY() + 10 * target.getVY())/scale;
		
	//	System.out.println("START: " + x +" " + y);
	
		Node node = new Node(x, y, 0, Math.sqrt((x-tx)*(x-tx) + (y-ty) * (y-ty)), null);
		openNodes.add(node);
		
		for(Node n : lastWay) {
			n.closed = true;
			n.way = Integer.MAX_VALUE;
			n.distance =  Math.sqrt((n.x-tx)*(n.x-tx) + (n.y-ty)*(n.y-ty));
			n.prevNode = null;
			openNodes.add(n);
		}
		lastWay.clear();
		
		Node[][] surroundingNodes = new Node[3][3];
		
		
		
		
		
		main:while(!found) {
			if(closedNodes > map.length * map[0].length/5) {
				System.out.println("TOO MANY CLOSED NODES");
				return;
			}
			
			
			if(openNodes.size() > map.length * map[0].length) {
				System.out.println("TOO MANY NODES");
				System.exit(0);
			}
			node = null;
			for(Node n : openNodes) {
				if(!n.isClosed() && (node == null || (n.way + n.distance <= node.way + node.distance)))
					node = n;
			}
			if(node == null) {
				System.out.println("NO OPEN NODES");
				//System.exit(0);
				return;
			}
			if(node.x == tx && node.y == ty) {
			//	System.out.println("FOUND: " + openNodes.size());
				break;
			}
			
			
		
			
			surroundingNodes = new Node[3][3];
			
			
			for(int ix = -1; ix <=1; ix++){
				for(int iy = -1; iy <=1; iy++) {
					for(Node n : openNodes) {
						if(n.x == node.x + ix && n.y == node.y + iy){
							surroundingNodes[ix+1][iy+1] = n;
							continue;
						}
					}
				}
			}
			
			
			for(int ix = -1; ix <=1; ix++){
				for(int iy = -1; iy <=1; iy++) {
					if(ix == 0 && iy == 0)
						continue;
					try{
						boolean free = true;
						for(int i : getEnemyBytes()){
							if((map[node.x +ix][node.y+iy] & i) == i) free = false;
						}
						if(free) {
							if(surroundingNodes[ix+1][iy+1] == null) {
								Node nn = new Node(node.x + ix, node.y + iy, node.way + 1,Math.sqrt((node.x + ix - tx)*(node.x + ix - tx) + (node.y + iy-ty) * (node.y + iy-ty)), node);
								node.nextNode = nn;
								openNodes.add(nn);
							} else if(!surroundingNodes[ix+1][iy+1].isClosed()){
								if(node.way + 1 < surroundingNodes[ix+1][iy+1].way){
									surroundingNodes[ix+1][iy+1].way = node.way + 1;
									surroundingNodes[ix+1][iy+1].prevNode = node;
								}
							}
						}
					}catch(Exception e){}
				}
			}
			
			node.close();
			lastWay.add(node);
			closedNodes++;
			getDisplay().fg.setColor(Color.BLUE);
			getDisplay().fg.fillRect(node.x*scale, node.y*scale, 1,1);
			getDisplay().repaint();
		}
		
		getDisplay().fg.setColor(Color.GRAY);
		
		Node oNode = node;
		int c = 0;
		while(node.prevNode != null) {
			c++;
			getDisplay().fg.drawLine(node.x*scale, node.y*scale, node.prevNode.x*scale,node.prevNode.y*scale);
			lastWay.remove(node);
			node = node.prevNode;
		}
		node = oNode;
		
		for(int i = 0; i < 1.5*getSpeed() && node.prevNode != null; i++){
			node = node.prevNode;
		}
			
		
	
		
		double dx = node.x*scale - this.getX();
		double dy = node.y*scale - this.getY();
		double angleTarget = Math.atan( dy / dx );
		double angleMove = Math.atan(this.getVY() / this.getVX());
		double deltaAngle;
		
		if(Math.signum(dx) != Math.signum(getVX())) // Wrong direction lol
			deltaAngle = angleMove;
		else
			deltaAngle = angleTarget - angleMove;
		
			
		if(Math.abs(deltaAngle) > Math.toRadians(6)) // TODO Maximal angle as var (here 3)
			deltaAngle = Math.signum(deltaAngle) * Math.toRadians(6);
			
		turnRadians((float) deltaAngle);
	}
	
	private void avoidDeath(){
		int[][] map = getDisplay().getMap();
		
		int cW = 0;
		double dx = 0;
		double dy = 0;
		for(int ix = -500; ix <= 500; ix++){
			for(int iy = -500; iy <= 500; iy++) {
				try{
					if((ix*ix+iy*iy) < 2000 && ((map[((int) (getX()  + ix))][((int)( getY() +  iy))] & 0b10 ) != 0)) {	
						cW++;
						dx += ix;
						dy += iy;
						getDisplay().fg.setColor(Color.GREEN);
						getDisplay().fg.fillRect((int)getX() + ix, (int)getY()+iy, 1,1);
					}
				}catch(Exception e){}
			}
		}
		
		
		
		if(cW != 0) {
			dx/=cW;
			dy/=cW;
			//System.out.println(x + " " + y);
			
			//double atan = Math.atan(ny/nx);
			
			double angleTarget = Math.atan( dy / dx );
			double angleMove = Math.atan(this.getVY() / this.getVX());
			double deltaAngle;
			
			if(Math.signum(dx) != Math.signum(getVX())) // Wrong direction lol
				deltaAngle = angleMove;
			else
				deltaAngle = angleTarget - angleMove;
			
			int f = 20;
			if(Math.abs(deltaAngle) > Math.toRadians(f)) // TODO Maximal angle as var (here 3)
				deltaAngle = Math.signum(deltaAngle) * Math.toRadians(f);
				
			turnRadians((float) -deltaAngle);
			
		}
	}

	@Override
	public void update(int tick) {
		target.simplifyVector();
		
		//myWay();
		
		try{
			aStarTest();
			avoidDeath();
		}catch(Exception e){
			
		}
	/*	
		double dx = (target.getX() + 50 * target.getVX()) - this.getX();
		double dy = (target.getY() + 50 * target.getVY()) - this.getY();
		double angleTarget = Math.atan( dy / dx );
		double angleMove = Math.atan(this.getVY() / this.getVX());
		double deltaAngle;
		
		if(Math.signum(dx) != Math.signum(getVX())) // Wrong direction lol
			deltaAngle = angleMove;
		else
			deltaAngle = angleTarget - angleMove;
			
		if(Math.abs(deltaAngle) > Math.toRadians(6)) // TODO Maximal angle as var (here 3)
			deltaAngle = Math.signum(deltaAngle) * Math.toRadians(6);
			
		turnRadians((float) deltaAngle);
		
		*/
		
		// gaps:
		if(!isVisible()) {
			if(gap == 0) {
				setVisible(true);
			} else {
				gap--;
			}
		}else if(Math.random() < 0.01) {
			setVisible(false);
			gap = 5;
		}
		
		
		super.update(tick);
	} 

	private class Node {
		private int x, y;
		private int way;
		private double distance;
		private Node prevNode;
		private Node nextNode;
		private boolean closed = false;
		
		private Node(int x, int y, int way, double distance, Node prevNode) {
			this.x = x;
			this.y = y;
			this.way = 0;
			this.distance = distance;
			this.prevNode = prevNode;
		}
		
		private boolean isClosed() {
			return closed;
		}
		
		private void close() {
			closed = true;
		}
	}
	
}

