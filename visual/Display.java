package visual;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.event.*;
import javax.swing.*;
import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;

import util.Loop;
import util.net.Client;
import util.net.GameInfo;
import util.net.Server;
import visual.Moveable;
import visual.moveable.Enemy;
import visual.moveable.Player;

public class Display extends JPanel implements KeyListener {

    public static final int BYTE_WALL = 0b1;
    public static final int BYTE_POWERUP = 0b111110;
    public static final int BYTE_POWERUP_MIN = 0b1 << 1;
    public static final int BYTE_POWERUP_MAX = 0b1 << 5;
    public static final int BYTE_NPC = 0b11111000000;
    public static final int BYTE_SHIFT_NPC = 6;
    public static final int BYTE_NPC_MIN = 0b1 << BYTE_SHIFT_NPC;
    public static final int BYTE_NPC_MAX = 0b1 << 10;
    public static final int BYTE_PLAYER = 0b1111100000000000;
    public static final int BYTE_SHIFT_PLAYER = 11;
    public static final int BYTE_PLAYER_MIN = 0b1 << BYTE_SHIFT_PLAYER;
    public static final int BYTE_PLAYER_MAX = 0b1 << 15;
    private static final int DEFAULT_PORT = 12975;

    private static final Toolkit TOOLKIT = Toolkit.getDefaultToolkit();
    public static final int WIDTH = 1600;
    public static final int HEIGHT = 900;

    protected static final Color bgColor = new Color(0x220033);

    private Loop loop;
    private JFrame frame;
    private ArrayList<Moveable> moveables;
    private ArrayList<Moveable> addedMoveables;
    private ArrayList<Moveable> removedMoveables;
    private ArrayList<PowerUp> powerUps;

    private boolean[] keys = new boolean[256];
    private boolean[] keysOld = new boolean[256];

    private final int scale = 10;
    private final int border = 10;
    private int[][] map; // maps the lines
    private int[][] scaledMap;

    private BufferedImage visualMap;
    private BufferedImage foreground;
    public Graphics g;
    public Graphics fg;

    private boolean isMultiplayer;
    private Server server;
    private Client client;
    private boolean isServer;
    private boolean isGameOver = false;

    public Display(boolean isMultiplayer) {
        this.isMultiplayer = isMultiplayer;
        if (isMultiplayer) {
            Scanner sc = new Scanner(System.in);
            System.out.println("If you want to create a Server, enter nothing.\nElse enter your hosts IP.\nYour input:");
            String input = sc.nextLine();
            if (input.isBlank()) {
                try {
                    System.out.println("Creating server...");
                    System.out.println("Your IP is: " + InetAddress.getLocalHost().getHostAddress());
                    isServer = true;
                    server = new Server(DEFAULT_PORT);
                    server.setInputConsumer(this::handleInput);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.exit(-1);
                }
            } else {
                System.out.println("Setting up client...");
                isServer = false;
                try {
                    client = new Client(input, DEFAULT_PORT);
                    client.setInputConsumer(this::handleInput);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("Exiting program...");
                    System.exit(-1);
                }
            }
        }
        moveables = new ArrayList<Moveable>();
        addedMoveables = new ArrayList<Moveable>();
        removedMoveables = new ArrayList<Moveable>();
        powerUps = new ArrayList<PowerUp>();
        loop = new Loop(60, this::update);

        frame = new JFrame();
        //frame.setUndecorated(true);
        frame.setBounds(0, 0, 2 * WIDTH, 2 * HEIGHT);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.addKeyListener(this);
        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        frame.add(this);

        frame.pack();
        frame.setLocationRelativeTo(null);
        //frame.setAlwaysOnTop(true);
        frame.setVisible(true);
        frame.setAlwaysOnTop(true);
        frame.requestFocus();
        repaint(); // causes JPanel to resize
        map = new int[WIDTH][HEIGHT];
        scaledMap = new int[WIDTH / scale][HEIGHT / scale];

        clear();

        visualMap = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        g = visualMap.createGraphics();


        if (isServer)
            try {
                GameInfo gi = new GameInfo();
                //System.out.println(gi);
                server.send(gi);
            } catch (IOException e) {
                e.printStackTrace();
            }
    }


    public ArrayList<PowerUp> getPowerUps() {
        return powerUps;
    }

    private void update(int tick) {
        moveables.addAll(addedMoveables);
        addedMoveables.clear();

        g.setColor(bgColor);
        g.fillRect(0, 0, WIDTH, HEIGHT);

        foreground = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        fg = foreground.createGraphics();

        frame.setTitle(loop.getLastUps() + " " + moveables.size());
        onUpdate(tick);

        System.arraycopy(keys, 0, keysOld, 0, keys.length);


        for (Moveable mo : moveables) {
            try {
                mo.update(tick);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (mo.shouldDecay())
                removedMoveables.add(mo);
        }

        moveables.removeAll(removedMoveables);
        removedMoveables.clear();

        // Foreground painting (lasts one tick)

// multiplayer
        if (isMultiplayer) {
            try {
                if (isServer && server != null)
                    server.send(createGameInfo());
                else if (!isServer && client != null) {
                    client.send(createGameInfo());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
//paint
        for (Moveable mo : moveables) {
            try {
                mo.paint(tick);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        for (PowerUp p : powerUps) {
            fg.drawImage(ImageLoader.images[p.getImageNumber()][(tick / 10) % 5], (int) (p.getX() - p.getRadius()), (int) (p.getY() - p.getRadius() - (tick / 10) % 5), 2 * p.getRadius(), 2 * p.getRadius(), null);
        }

        repaint();
    }


    public void addMoveable(Moveable mo) {
        boolean onRemove = false;
        for (Moveable m : removedMoveables) {
            if (mo.getDrawByte() == m.getDrawByte()) {
                onRemove = true;
                break;
            }
        }
        if (!onRemove) {
            for (Moveable m : addedMoveables) {
                if (mo.getDrawByte() == m.getDrawByte())
                    return;
            }
            for (Moveable m : moveables) {
                if (mo.getDrawByte() == m.getDrawByte())
                    return;
            }
        }
        System.out.println("Added " + mo);
        addedMoveables.add(mo);
    }

    public void removeMoveable(Moveable mo) {
        for (int x = 0; x < Display.WIDTH; x++) {
            for (int y = 0; y < Display.HEIGHT; y++) {
                unsetMapByte(x, y, mo.getDrawByte());
            }
        }
        removedMoveables.add(mo);
    }

    public void onUpdate(int tick) {
    }

    public void clear() {
        visualMap = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        g = visualMap.createGraphics();

        for (Moveable mo : moveables) {
            mo.clear();
        }

        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                unsetMapByte(x, y, ~(BYTE_WALL | BYTE_POWERUP));
            }
        }

        scaledMap = new int[WIDTH / scale][HEIGHT / scale];

        for (int x = 0; x < WIDTH; x++) {
            for (int i = 0; i <= border; i++) {
                //map[x][i] = BYTE_WALL; // TODO WALL
                setMapByte(x, i, BYTE_WALL);
                setMapByte(x, HEIGHT - 1 - i, BYTE_WALL);
            }
        }
        for (int y = 0; y < HEIGHT; y++) {
            for (int i = 0; i <= border; i++) {
                // map[i][y] = BYTE_WALL; // TODO WALL
                setMapByte(i, y, BYTE_WALL);
                setMapByte(WIDTH - 1 - i, y, BYTE_WALL);
            }
        }
    }

    public void start() {
        loop.start();
    }

    public void stop() {
        for (Moveable mo : moveables) {
            if (mo instanceof Enemy) {
                ((Enemy) mo).stop();
            }
        }
        loop.stop();
    }

    public void createPowerUp(PowerUp powerUp) {
        for (PowerUp p : powerUps)
            if (p.getDrawByte() == powerUp.getDrawByte())
                return;

        powerUps.add(powerUp);
        for (int rx = -powerUp.getRadius(); rx < powerUp.getRadius(); rx++) {
            for (int ry = -powerUp.getRadius(); ry < powerUp.getRadius(); ry++) {
                if (rx * rx + ry + ry < powerUp.getRadius() * powerUp.getRadius()) {
                    //  map[powerUp.getX() + rx][powerUp.getY() + ry] |= powerUp.getDrawByte();
                    setMapByte((int) powerUp.getX() + rx, (int) powerUp.getY() + ry, powerUp.getDrawByte());
                }
            }
        }
    }

    public void removePowerUp(int power) {
        PowerUp powerUp = null;
        for (PowerUp p : powerUps) {
            if ((power & p.getDrawByte()) != 0) {
                powerUp = p;
                break;
            }
        }

        if (powerUp == null)
            return;
        //  System.out.println("Remove PowerUp " + Integer.toBinaryString(power));
        for (int rx = -powerUp.getRadius() - 3; rx < powerUp.getRadius() + 3; rx++) {
            for (int ry = -powerUp.getRadius() - 3; ry < powerUp.getRadius() + 3; ry++) {
                if (rx * rx + ry + ry <= powerUp.getRadius() * powerUp.getRadius() + 4) {
                    unsetMapByte((int) powerUp.getX() + rx, (int) powerUp.getY() + ry, powerUp.getDrawByte());
                }
            }
        }
        powerUps.remove(powerUp);
    }

    public GameInfo createGameInfo() {
        GameInfo gi = new GameInfo();
        for (Moveable mo : moveables) {
            if (!mo.isRemoteControlled()) {
                mo.simplifyVector();
                gi.addMoveable(mo.getX(), mo.getY(), mo.getVX(), mo.getVY(), mo.getSpeed(), mo.getDrawByte(), mo.isVisible());
            }
        }
        gi.stop = isGameOver;
        return gi;
    }

    private void handleInput(Object obj) {

        if (obj instanceof GameInfo) {
            GameInfo gi = (GameInfo) obj;
            if (gi.stop) {
                gameOver(null, 0);
            }

            for (GameInfo.ObjectContainer oc : gi.moveables) {
                Moveable moveable = null;
                for (Moveable mo : addedMoveables) {
                    if (mo.isRemoteControlled() && mo.getDrawByte() == oc.drawByte) {
                        moveable = mo;
                        break;
                    }
                }
                for (Moveable mo : moveables) {
                    if (mo.isRemoteControlled() && mo.getDrawByte() == oc.drawByte) {
                        moveable = mo;
                        break;
                    }
                }
                if (moveable == null) {
                    moveable = new Moveable(oc.x, oc.y, oc.vx, oc.vy, oc.speed, this, oc.drawByte);
                    moveable.setVisible(oc.isVisible);
                    moveable.setRemoteControlled(true);
                    moveable.setTraceColor(Color.RED);
                    addMoveable(moveable);
                } else {
                    moveable.setX(oc.x);
                    moveable.setY(oc.y);
                    moveable.setVX(oc.vx);
                    moveable.setSpeed(oc.speed);
                    moveable.setVisible(oc.isVisible);
                }
            }
        }
    }

    public boolean isRunning() {
        return loop.isRunning();
    }

    public int[][] getMap() {
        return map;
    }

    public int[][] getScaledMap() {
        return scaledMap;
    }

    public int getScale() {
        return scale;
    }

    public void setMapByte(int x, int y, int value) {
        map[x][y] |= value;
        scaledMap[(x) / scale][(y) / scale] |= value;
    }

    public void unsetMapByte(int x, int y, int value) {
        map[x][y] &= ~value;
        scaledMap[x / scale][y / scale] &= ~value;
    }

    public void keyPressed(KeyEvent e) {
        if (loop.isRunning() && e.getKeyCode() < keys.length)
            keys[e.getKeyCode()] = true;
    }

    public void keyReleased(KeyEvent e) {
        if (loop.isRunning() && e.getKeyCode() < keys.length)
            keys[e.getKeyCode()] = false;
    }

    public void keyTyped(KeyEvent e) {

    }

    public boolean isKey(int keyCode) {
        return keys[keyCode];
    }

    public boolean isKeyPressed(int keyCode) {
        return keys[keyCode] && !keysOld[keyCode];
    }

    public boolean isKeyReleased(int keyCode) {
        return !keys[keyCode] && keysOld[keyCode];
    }

    public ArrayList<Moveable> getMoveables() {
        return moveables;
    }

    public boolean isServer() {
        return isServer;
    }

    //public Graphics getGraphics(){
    //	return g;
    //}

    public void paint(Graphics graphics) {

        graphics.drawImage(visualMap, 0, 0, this.getWidth(), this.getHeight(), null);

        for (int i = 0; i < moveables.size(); i++) {
            graphics.drawImage(moveables.get(i).getImage(), 0, 0, this.getWidth(), this.getHeight(), null);
        }

        graphics.drawImage(foreground, 0, 0, this.getWidth(), this.getHeight(), null);
        //System.out.println("PAINT");
    }

    public void gameOver(Player player, int code) {
        System.out.println("THE GAME IS OVER");
        isGameOver = true;
        stop();
        if (isMultiplayer) {
            try {
                if (isServer) {
                    server.send(createGameInfo());
                } else {
                    client.send(createGameInfo());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

