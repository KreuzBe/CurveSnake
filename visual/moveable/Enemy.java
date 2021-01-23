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
    private boolean[][] lastAvoidedDeaths;

    public Enemy(int x, int y, int vx, int vy, int speed, Display display, Moveable target) {
        super(x, y, vx, vy, speed, display, TYPE_NPC);
        this.target = this.mainTarget = target;
        setDrawByte(Display.BYTE_NPC_MIN);
        addEnemyByte(Display.BYTE_PLAYER);
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
            pathFind();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void pathFind() throws Exception {
        int closedNodes = 0;
        ArrayList<Node> openNodes = new ArrayList<Node>();
        int[][] map = getDisplay().getScaledMap();
        boolean hasObstacle = false;
        Node[][] surroundingNodes;
        int tx = (int) (getX() + (getSpeed() + scale) * getVX()) / scale;
        int ty = (int) (getY() + (getSpeed() + scale) * getVY()) / scale;
        int x = (int) (target.getX() + scale * target.getVX()) / scale;
        int y = (int) (target.getY() + scale * target.getVY()) / scale;

        Node node = new Node(x, y, 0, Math.sqrt(((x - tx) * (x - tx) + (y - ty) * (y - ty))), null);
        openNodes.add(node);

        for (Node n : avoidNext) {
            n.closed = true; // dont go here again... was bad last time!!!
            n.way = 0; // I dont care about the way
            n.distance = Math.sqrt(((n.x - tx) * (n.x - tx) + (n.y - ty) * (n.y - ty)));
            n.prevNode = null;
            //   openNodes.add(n);
        }
        avoidNext.clear();

        while (node.x != tx || node.y != ty) {

            if (closedNodes > map.length * map[0].length / 5) { // It is unlikely that there is a solution... just ignore it
                // System.out.println("Many closed nodes: " + avoidDeath());
                return;
            }

            if (openNodes.size() > map.length * map[0].length) {
                //  System.out.println("TOO MANY NODES");
                return;
            }
            node = null;
            for (Node n : openNodes) {
                if (!n.isClosed() && (node == null || (n.way + n.distance <= node.way + node.distance)))
                    node = n;
            }
            if (node == null) {
                System.out.println("NO OPEN NODES: " + avoidDeath());
                avoidDeath();
                //System.exit(0);
                return;
            }

            int surr = 1;
            surroundingNodes = new Node[2 * surr + 1][2 * surr + 1];

            for (int ix = -surr; ix <= surr; ix++) {
                for (int iy = -surr; iy <= surr; iy++) {
                    for (Node n : openNodes) {
                        if (n.x == node.x + ix && n.y == node.y + iy) {
                            surroundingNodes[ix + surr][iy + surr] = n;
                        }
                    }
                }
            }

            for (int ix = -surr; ix <= surr; ix++) {
                for (int iy = -surr; iy <= surr; iy++) {
                    if (ix == 0 && iy == 0)
                        continue;
                    try {
                        if ((map[node.x + ix][node.y + iy] & getEnemyBytes()) == 0) {
                            if (surroundingNodes[ix + surr][iy + surr] == null) {
                                Node nn = new Node(node.x + ix, node.y + iy, node.way + 1, Math.sqrt(((node.x + ix - tx) * (node.x + ix - tx) + (node.y + iy - ty) * (node.y + iy - ty))), node);
                                openNodes.add(nn);
                            } else if (!surroundingNodes[ix + surr][iy + surr].isClosed()) {
                                if (node.way + 1 < surroundingNodes[ix + surr][iy + surr].way) {
                                    surroundingNodes[ix + surr][iy + surr].way = node.way + 1;
                                    surroundingNodes[ix + surr][iy + surr].prevNode = node;
                                }
                            }
                        } else {
                            node.hasBadNeighbours = true;
                            if (surroundingNodes[ix + surr][iy + surr] != null) {
                                surroundingNodes[ix + surr][iy + surr].close();
                                avoidNext.add(surroundingNodes[ix + surr][iy + surr]);
                            }
                        }
                    } catch (Exception ignored) {
                    }
                }
            }

            node.close();
            avoidNext.add(node);
            //  getDisplay().fg.setColor(Color.BLUE);
            // getDisplay().fg.drawRect(node.x * scale, node.y * scale, scale, scale);
            //getDisplay().repaint();
            closedNodes++;
        }
        int c = 0;
        Node faceNode = null;
        while (node.prevNode != null) {
            c++;
            //   getDisplay().fg.setColor(Color.ORANGE);
            //  getDisplay().fg.drawRect(node.x * scale, node.y * scale, scale, scale);
            avoidNext.remove(node);
            node = node.prevNode;
            if (!hasObstacle && node.hasBadNeighbours) {
                hasObstacle = true;
                faceNode = node;
                //  getDisplay().fg.setColor(Color.RED);
                //   getDisplay().fg.drawRect(node.x * scale, node.y * scale, scale, scale);
            }
        }

        if (!hasObstacle) {
            lookAt(target.getX(), target.getY(), 3);
        } else {
            lookAt(faceNode.x * scale + scale / 2f, faceNode.y * scale + scale / 2f, 7);
        }
    }

    private boolean avoidDeath() {
        removeEnemyByte(getDrawByte());

        int[][] map = getDisplay().getMap();
        int radius = (int) (6 * getSpeed());
        if (lastAvoidedDeaths == null)
            lastAvoidedDeaths = new boolean[radius][radius];
        boolean ret = false;
        int cW = 0;
        double dx = 0;
        double dy = 0;
        for (int ix = -radius; ix <= radius; ix++) {
            for (int iy = -radius; iy <= radius; iy++) {
                try {
                    if ((ix * ix + iy * iy) < radius * radius && ((map[((int) (getX() + ix))][((int) (getY() + iy))] & getEnemyBytes()) != 0)) {
                        //  if (!lastAvoidedDeaths[ix + radius][iy + radius]) {
                        ret = true;
                        cW++;
                        dx += ix;
                        dy += iy;
                        //}
                        lastAvoidedDeaths[ix + radius][iy + radius] = true;
                    } else {
                        lastAvoidedDeaths[ix + radius][iy + radius] = false;
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
                dx = .001 * getVX();
            if (dy == 0)
                dy = .001 * getVY();

            lookAway((float) (getX() + dx), (float) (getY() + dy), 7);
        }
        return ret;
    }

    @Override
    public void update(int tick) {
        target.simplifyVector();

        if (getDisplay().getPowerUps().size() > 0) {
            setTarget(getDisplay().getPowerUps().get(0));
        } else {
            setTarget(mainTarget);
        }

        try {
            //pathFind();
            avoidDeath();
        } catch (Exception e) {
            System.out.println("ERR WHILE CALCULATING SHORTEST PATH: " + e.getMessage());
        }

        // gaps:
        if (!isVisible()) {
            if (gap == 0) {
                setVisible(true);
            } else {
                gap--;
            }
        } else if (Math.random() < 0.000) {
            setVisible(false);
            gap = 15;
        }

        int[][] map = getDisplay().getMap();
        try {
            if ((map[((int) (getX() + getVX() * getSpeed()))][(int) (getY() + getVY() * getSpeed())] & getEnemyBytes()) != 0) {
                setVisible(false);
                gap = 15;
            }
            super.update(tick);
        } catch (Exception e) {
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
        decay();
        for (int x = 0; x < Display.WIDTH; x++) {
            for (int y = 0; y < Display.HEIGHT; y++) {
                getDisplay().unsetMapByte(x, y, getDrawByte());
            }
        }
        getDisplay().addMoveable(new Enemy((int) (Display.WIDTH * Math.random()), (int) (Display.HEIGHT * Math.random()), (int) (20 * Math.random() - 10), (int) (20 * Math.random() - 10), 3, getDisplay(), target));
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

        private boolean isClosed() {
            return closed;
        }

        private void close() {
            closed = true;
        }
    }
}

