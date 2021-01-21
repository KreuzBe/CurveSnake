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
		//setDrawByte(0b10);
		//addEnemyByte(0b10);
	}
	
	public void setTarget(Moveable mo) {
		target = mo;
	}
	
	private void aStarTest() {
		ArrayList<Node> openNodes = new ArrayList();
		int[][] map = getDisplay().getMap();
		boolean found = false;
		
		int tx = (int) (getX() + (getSpeed() + 3) * getVX());
		int ty = (int) (getY() + (getSpeed()+ 3)* getVY());
		int x = (int) (target.getX() + 50 * target.getVX());
		int y = (int) (target.getY() + 50 * target.getVY());
		
		System.out.println("START: " + x +" " + y);
	
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
				System.exit(0);
			}
			if(node.x == tx && node.y == ty) {
				System.out.println("FOUND: " + openNodes.size());
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
					if( (map[node.x+ix][node.y+iy] & 0b10) != 0b10) {
						if(surroundingNodes[ix+1][iy+1] == null) {
							Node nn = new Node(node.x + ix, node.y + iy, node.way + 1,Math.sqrt((node.x + ix - tx)*(node.x + ix - tx) + (node.y + iy-ty) * (node.y + iy-ty)), node);
							node.nextNode = nn;
							openNodes.add(nn);
						} else if(!surroundingNodes[ix+1][iy+1].isClosed()){
							//if(node.way + 1 < surroundingNodes[ix+1][iy+1].way){
							//	surroundingNodes[ix+1][iy+1].way = node.way + 1;
							//	surroundingNodes[ix+1][iy+1].prevNode = node;
							//}
						}
					}
				}
			}
			
			node.close();
			lastWay.add(node);
			getDisplay().fg.setColor(Color.GREEN);
			getDisplay().fg.drawRect(node.x, node.y, 1,1);
			getDisplay().repaint();
		}
		
		getDisplay().fg.setColor(Color.ORANGE);
		
		Node oNode = node;
		int c = 0;
		while(node.prevNode != null) {
			c++;
			getDisplay().fg.drawRect(node.x, node.y, 1,1);
			lastWay.remove(node);
			node = node.prevNode;
		}
		node = oNode;
		
		for(int i = 0; i < getSpeed() && node.prevNode != null; i++){
			node = node.prevNode;
		}
			
		getDisplay().fg.setColor(Color.BLUE);
		getDisplay().fg.drawRect(node.x-1, node.y-1, 3,3);
		
		int cW = 0;
		double nx = 0;
		double ny = 0;
		for(int ix = -500; ix <= 500; ix++){
			for(int iy = -500; iy <= 500; iy++) {
				if((ix*ix+iy*iy) < 1000 && ((map[(int) getX() + ix][(int) getY() + iy] & 0b10 ) != 0)) {	
					cW++;
					nx += ix;
					ny += iy;
					getDisplay().fg.setColor(Color.ORANGE);
					getDisplay().fg.drawRect((int)getX()+ix,(int)getY()+iy, 1,1);
				}
			}
		}
		
		if(cW != 0) {
			nx/=cW;
			ny/=cW;
			//System.out.println(x + " " + y);
			
			double atan = Math.atan(ny/nx);
			double f = 180;
			if(Math.abs(atan) > Math.toRadians(f))
				atan = Math.signum(atan) * Math.toRadians(f);
			
				
			turnRadians((float) atan);
			
			return;
		}
		
		
		double dx = node.x - this.getX();
		double dy = node.y - this.getY();
		double angleTarget = Math.atan( dy / dx );
		double angleMove = Math.atan(this.getVY() / this.getVX());
		double deltaAngle;
		
		if(Math.signum(dx) != Math.signum(getVX())) // Wrong direction lol
			deltaAngle = angleMove;
		else
			deltaAngle = angleTarget - angleMove;
		
			
		//if(Math.abs(deltaAngle) > Math.toRadians(3)) // TODO Maximal angle as var (here 3)
		//	deltaAngle = Math.signum(deltaAngle) * Math.toRadians(3);
			
		turnRadians((float) deltaAngle);
	}
	
	private void myWay() {
		simplifyVector();
		int[][] map = getDisplay().getMap();
		
		int cW = 0;
		double x = 0;
		double y = 0;
		for(int ix = -500; ix <= 500; ix++){
			for(int iy = -500; iy <= 500; iy++) {
				if((ix*ix+iy*iy) < 1000 && ((map[(int) getX() + ix][(int) getY() + iy] & 0b10 ) != 0)) {	
					cW++;
					x += ix;
					y += iy;
					getDisplay().fg.setColor(Color.ORANGE);
					getDisplay().fg.drawRect((int)getX()+ix,(int)getY()+iy, 1,1);
				}
			}
		}
		
		if(cW != 0) {
			x/=cW;
			y/=cW;
			//System.out.println(x + " " + y);
			
			double atan = Math.atan(y/x);
			double f = 7;
			if(Math.abs(atan) > Math.toRadians(f))
				atan = Math.signum(atan) * Math.toRadians(f);
			
			setVX((float)-Math.sin(atan));
			setVY((float)-Math.cos(atan));
			
			return;
		}
		
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
	}
	
	@Override
	public void update(int tick) {
		target.simplifyVector();
		
		//myWay();
		
		aStarTest();
		
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

