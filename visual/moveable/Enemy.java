package visual.moveable;

import util.Loop;
import visual.Moveable;
import visual.Display;

import java.awt.*;
import java.util.ArrayList;

public class Enemy extends Moveable {

    private Moveable target;
    private Moveable mainTarget;
    private int gap = 0;
    private final int scale;
    private ArrayList<Node> avoidNext = new ArrayList<Node>();
    private Loop pathFindLoop;

    public Enemy(int x, int y, int vx, int vy, int speed, Display display, Moveable target) {
        super(x, y, vx, vy, speed, display, Display.BYTE_NPC_MIN);
        this.target = this.mainTarget = target;
        setDrawByte(Display.BYTE_NPC_MIN);
        addEnemyByte(Display.BYTE_PLAYER); // You like nobody
        addEnemyByte(Display.BYTE_NPC);
        addEnemyByte(Display.BYTE_WALL);
        setTraceColor(Color.RED);
        scale = display.getScale();
        pathFindLoop = new Loop(60, this::loopAction);
        pathFindLoop.start();
    }

    public void setTarget(Moveable mo) {
        target = mo;
    }

    private void loopAction(int tick) {
        try {
            if (target != null)
                pathFind();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void pathFind() {
        if (!isVisible()) {
            lookAt(target.getX(), target.getY(), 5);
            return;
        }

        int closedNodes = 0;
        ArrayList<Node> openNodes = new ArrayList<Node>();
        int[][] map = getDisplay().getScaledMap();
        boolean hasObstacle = false;
        Node[][] surroundingNodes;
        int tx = (int) (getX() + (getSpeed() + scale) * getVX()) / scale; // caluculating path from target to enemy because it is then easier to track the next point to go
        int ty = (int) (getY() + (getSpeed() + scale) * getVY()) / scale;
        int x = (int) (target.getX() + scale * target.getVX()) / scale;
        int y = (int) (target.getY() + scale * target.getVY()) / scale;

        Node node = new Node(x, y, 0, Math.sqrt(((x - tx) * (x - tx) + (y - ty) * (y - ty))), null);
        openNodes.add(node);

        for (Node n : avoidNext) {
            n.closed = true; // dont go here again... was bad last time!!!
            n.way = 0; // I dont care about the way, could be Integer.MAX_VALUE
            n.distance = Math.sqrt(((n.x - tx) * (n.x - tx) + (n.y - ty) * (n.y - ty)));
            n.prevNode = null;
            //   openNodes.add(n);
        }
        avoidNext.clear();

        while (node.x != tx || node.y != ty) {

            if (closedNodes > map.length * map[0].length / 5) { // It is unlikely that there is a solution... just ignore it for the sake of performance
                return;
            }

            if (openNodes.size() > map.length * map[0].length) {
                return;
            }
            node = null;
            for (Node n : openNodes) {
                if (n.isOpen() && (node == null || (n.way + n.distance <= node.way + node.distance)))
                    node = n;
            }
            if (node == null) { // usually when either target or enemy is surrounded by bad nodes
                avoidDeath();
                return;
            }


            surroundingNodes = new Node[3][3];

            for (int ix = -1; ix <= 1; ix++) {
                for (int iy = -1; iy <= 1; iy++) {
                    for (Node n : openNodes) {
                        if (n.x == node.x + ix && n.y == node.y + iy) {
                            surroundingNodes[ix + 1][iy + 1] = n;
                        }
                    }
                }
            }

            for (int ix = -1; ix <= 1; ix++) {
                for (int iy = -1; iy <= 1; iy++) {
                    if (ix == 0 && iy == 0)
                        continue;
                    try {
                        if ((map[node.x + ix][node.y + iy] & getEnemyBytes()) == 0) {
                            if (surroundingNodes[ix + 1][iy + 1] == null) { // create new node
                                openNodes.add(new Node(node.x + ix, node.y + iy, node.way + 1, Math.sqrt(((node.x + ix - tx) * (node.x + ix - tx) + (node.y + iy - ty) * (node.y + iy - ty))), node));
                            } else if (surroundingNodes[ix + 1][iy + 1].isOpen()) { // use existing node
                                if (node.way + 1 < surroundingNodes[ix + 1][iy + 1].way) {
                                    surroundingNodes[ix + 1][iy + 1].way = node.way + 1;
                                    surroundingNodes[ix + 1][iy + 1].prevNode = node;
                                }
                            }
                        } else {
                            node.hasBadNeighbours = true;
                            if (surroundingNodes[ix + 1][iy + 1] != null) { // might be added with last nodes
                                surroundingNodes[ix + 1][iy + 1].close();
                                avoidNext.add(surroundingNodes[ix + 1][iy + 1]);
                            }
                        }
                    } catch (Exception ignored) {
                    }
                }
            }

            node.close();
            avoidNext.add(node);
            closedNodes++;
        }

        Node faceNode = null; // The node on wich the path touches a bad node, most likely a curve
        while (node.prevNode != null) {
            avoidNext.remove(node);
            node = node.prevNode;
            if (!hasObstacle && node.hasBadNeighbours) {
                hasObstacle = true;
                faceNode = node;
            }
        }


        if (!hasObstacle) {
            lookAt(target.getX(), target.getY(), 5);
        } else {
            lookAt(faceNode.x * scale + scale / 2f, faceNode.y * scale + scale / 2f, 5); // look at the center of this node to avoid crash
        }
    }

    private void avoidDeath() {
        if (!isVisible()) {
            return;
        }
        removeEnemyByte(getDrawByte()); // TODO find a good way to ignore enemies drawByte it just did, but not the ones it did longer ago. (are two bits per Enemy good?)

        int[][] map = getDisplay().getMap();
        int radius = (int) (6 * getSpeed());

        int cW = 0;
        double dx = 0;
        double dy = 0;
        for (int ix = -radius; ix <= radius; ix++) {
            for (int iy = -radius; iy <= radius; iy++) {
                try {
                    if ((ix * ix + iy * iy) < radius * radius && ((map[((int) (getX() + ix))][((int) (getY() + iy))] & getEnemyBytes()) != 0)) {
                        cW++;
                        dx += ix;
                        dy += iy;
                    }
                } catch (Exception ignored) {
                }
            }
        }
        addEnemyByte(getDrawByte());

        if (cW != 0) {
            dx /= cW;
            dy /= cW;

            if (dx == 0)
                dx = .01 * getVX();
            if (dy == 0)
                dy = .01 * getVY();

            lookAway((float) (getX() + dx), (float) (getY() + dy), 7);
        }
    }

    @Override
    public void update(int tick) {
        if (target != null)
            target.simplifyVector();

        if (getDisplay().getPowerUps().size() > 0) {
            setTarget(getDisplay().getPowerUps().get(0));
        } else {
            if (mainTarget == null) {
                Moveable newTarget = null;
                for (Moveable mo : getDisplay().getMoveables()) {
                    if (mo != this) {
                        newTarget = mo;
                        break;
                    }
                }
                target = newTarget;
                return;
            } else {
                setTarget(mainTarget);
            }
        }


        avoidDeath();
        int[][] map = getDisplay().getMap();
        try {
            if ((map[((int) (getX() + getVX() * getSpeed()))][(int) (getY() + getVY() * getSpeed())] & getEnemyBytes()) != 0) {
                setVisible(false);
                gap = 30;
            }
            super.update(tick);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            die();
        }

    }

    @Override
    public void onCrash(int code) {
        if ((code & Display.BYTE_WALL) == 0) {
            die();
        }
    }

    private void die() {
        stop();
        getDisplay().removeMoveable(this);
        Enemy newMe = new Enemy((int) (Display.WIDTH * Math.random()), (int) (Display.HEIGHT * Math.random()), 1, 1, (int) getSpeed(), getDisplay(), mainTarget);
        newMe.lookAt(target.getX(), target.getY(), 360);
        getDisplay().addMoveable(newMe);
    }

    public void stop() {
        pathFindLoop.stop();
    }

    private static class Node {
        private final int x;
        private final int y;
        private int way;
        private double distance;
        private Node prevNode;
        private boolean closed = false;
        private boolean hasBadNeighbours = false;

        private Node(int x, int y, int way, double distance, Node prevNode) {
            this.x = x;
            this.y = y;
            this.way = 0;
            this.distance = distance;
            this.prevNode = prevNode;
        }

        private boolean isOpen() {
            return !closed;
        }

        private void close() {
            closed = true;
        }
    }
}

