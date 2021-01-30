package visual;

import util.GameCreator;
import util.Loop;
import util.net.Client;
import util.net.GameInfo;
import util.net.Server;
import visual.moveable.Animation;
import visual.moveable.Enemy;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

public class Display extends JPanel implements KeyListener, WindowListener {

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
    public static final int BYTE_PLAYER_MAX = 0b1 << 13;
    public static final int BYTE_ANIMATION = 0b00000000000000;
    public static final int BYTE_SHIFT_ANIMATION = 14;
    public static final int BYTE_ANIMATION_MIN = 0b1 << BYTE_SHIFT_ANIMATION;
    public static final int BYTE_ANIMATION_MAX = 0b1 << 19;
    public static final int DEFAULT_PORT = 4444;

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
    private ArrayList<PowerUp> removedPowerUps;
    private ArrayList<Animation> animations;
    private ArrayList<Animation> removedAnimations;

    private boolean[] keys = new boolean[256];
    private boolean[] keysOld = new boolean[256];

    private final int scale = 10;
    private final int border = 10;
    private int[][] map; // maps the lines
    private int[][] scaledMap;

    private BufferedImage foreground;
    public Graphics fg;

    private boolean isMultiplayer;
    private Server server;
    private Client client;
    private boolean isServer;
    private boolean isGameOver = false;

    private GameCreator gc;

    public Display(GameCreator gc) {
        GameCreator.createGame(gc, this::initGame);
    }

    private void initGame(GameCreator gc) {
        this.gc = gc;
        this.isMultiplayer = gc.isMultiplayer();
        if (isMultiplayer) {
            isServer = gc.isServer();
            if (isServer) {
                this.server = gc.getServer();
                server.setInputConsumer(this::handleInput);
            } else {
                this.client = gc.getClient();
                client.setInputConsumer(this::handleInput);
            }
            System.out.println(server + "(" + isServer + ") / " + client);
        }
        moveables = new ArrayList<Moveable>();
        addedMoveables = new ArrayList<Moveable>();
        removedMoveables = new ArrayList<Moveable>();
        powerUps = new ArrayList<PowerUp>();
        removedPowerUps = new ArrayList<PowerUp>();
        animations = new ArrayList<Animation>();
        removedAnimations = new ArrayList<Animation>();

        loop = new Loop(60, this::update);

        frame = new JFrame();
        frame.setBounds(0, 0, 2 * WIDTH, 2 * HEIGHT);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.addWindowListener(this);
        this.addKeyListener(this);
        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        frame.add(this);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setLocationRelativeTo(null);
        frame.setUndecorated(false);
        frame.setVisible(true);
        frame.setAlwaysOnTop(true);

        this.grabFocus();

        keys = new boolean[256];
        keysOld = new boolean[256];

        map = new int[WIDTH][HEIGHT];
        scaledMap = new int[WIDTH / scale][HEIGHT / scale];

        clear();


        if (isServer)
            try {
                GameInfo gi = new GameInfo();
                server.send(gi);
            } catch (IOException e) {
                e.printStackTrace();
            }

        onGameReady();
    }


    public void onGameReady() {
    }

    public ArrayList<PowerUp> getPowerUps() {
        return powerUps;
    }

    private void update(int tick) {
        moveables.addAll(addedMoveables);
        addedMoveables.clear();


        frame.setTitle(loop.getLastUps() + " " + moveables.size());
        onUpdate(tick);


        for (Moveable mo : moveables) {
            try {
                mo.update(tick);
            } catch (Exception e) {
                // e.printStackTrace();
            }
            if (mo.shouldDecay())
                removedMoveables.add(mo);
        }

        moveables.removeAll(removedMoveables);
        removedMoveables.clear();


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
        paintMe(tick);

        System.arraycopy(keys, 0, keysOld, 0, keys.length);

    }

    private void paintMe(int tick) {
        foreground = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        fg = foreground.createGraphics();
        for (Moveable mo : moveables) {
            try {
                mo.paint(tick);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        for (Animation a : animations) {
            try {
                a.update(tick);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (a.shouldDecay())
                removeAnimation(a);
        }
        animations.removeAll(removedAnimations);
        removedAnimations.clear();

        for (PowerUp p : powerUps) {
            try {
                p.update(tick);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (p.shouldDecay())
                removePowerUp(p.getDrawByte());
        }
        powerUps.removeAll(removedPowerUps);
        removedPowerUps.clear();


        repaint();
    }


    public void addMoveable(Moveable mo) {
        if (mo.getDrawByte() != 0b0) {
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
        }
        mo.simplifyVector();
        addedMoveables.add(mo);
        if (mo instanceof Enemy)
            ((Enemy) mo).start();
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

        for (Moveable mo : moveables) {
            mo.clear();
        }

        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                unsetMapByte(x, y, ~(BYTE_WALL | BYTE_POWERUP));
            }
        }

        scaledMap = new int[WIDTH / scale][HEIGHT / scale];
        recreateWalls();

    }

    public void recreateWalls() {
        for (int x = 0; x < WIDTH; x++) {
            for (int i = 0; i <= border; i++) {
                setMapByte(x, i, BYTE_WALL);
                setMapByte(x, HEIGHT - 1 - i, BYTE_WALL);
            }
        }
        for (int y = 0; y < HEIGHT; y++) {
            for (int i = 0; i <= border; i++) {
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

        if ((map[(int) powerUp.getX()][(int) powerUp.getY()] & (BYTE_PLAYER | BYTE_NPC)) != 0)
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
        for (int rx = -powerUp.getRadius() - 3; rx < powerUp.getRadius() + 3; rx++) {
            for (int ry = -powerUp.getRadius() - 3; ry < powerUp.getRadius() + 3; ry++) {
                if (rx * rx + ry + ry <= powerUp.getRadius() * powerUp.getRadius() + 4) {
                    unsetMapByte((int) powerUp.getX() + rx, (int) powerUp.getY() + ry, powerUp.getDrawByte());
                }
            }
        }
        removedPowerUps.add(powerUp);
    }

    public GameInfo createGameInfo() {
        GameInfo gi = new GameInfo();
        for (Moveable mo : moveables) {
            if (!mo.isRemoteControlled()) {
                mo.simplifyVector();
                gi.addMoveable(mo.getX(), mo.getY(), mo.getVX(), mo.getVY(), mo.getSpeed(), mo.getDrawByte(), mo.getEnemyBytes(), mo.isVisible());
            }
        }

        for (PowerUp p : powerUps) {
            if (!p.isRemoteControlled()) {
                gi.addPowerUp(p.getX(), p.getY(), p.getRadius(), p.getDrawByte());
            }
        }
        gi.stop = isGameOver;
        return gi;
    }

    private void handleInput(Object obj) {
        if (obj instanceof GameInfo) {
            GameInfo gi = (GameInfo) obj;
            if (gi.stop) {
                System.out.println("RECEIVED STOP");
                gameOver(null, 0);
                return;
            }

            for (GameInfo.ObjectContainer pu : gi.powerups) {
                PowerUp powerUp = null;
                for (PowerUp p : powerUps) {
                    if (p.isRemoteControlled() && p.getDrawByte() == pu.drawByte) {
                        powerUp = p;
                        break;
                    }
                }
                if (powerUp == null) createPowerUp(new PowerUp(pu.x, pu.y, pu.radius, pu.drawByte, this, true));
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
                    moveable.addEnemyByte(oc.enemyByte);
                    moveable.setTraceColor(Color.RED);
                    addMoveable(moveable);
                } else {
                    moveable.setX(oc.x);
                    moveable.setY(oc.y);
                    moveable.setVX(oc.vx);
                    moveable.setVY(oc.vy);
                    moveable.setSpeed(oc.speed);
                    moveable.clearEnemyByte();
                    moveable.addEnemyByte(oc.enemyByte);
                    moveable.setVisible(oc.isVisible);
                }
            }
        } else {
            gameOver(null, 0);
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
        try {
            map[x][y] |= value;
            scaledMap[(x) / scale][(y) / scale] |= value;
        } catch (Exception e) {
            e.printStackTrace();
            // gameOver(null, 0);
        }
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

    public boolean isMultiplayer() {
        return isMultiplayer;
    }

    public void addAnimation(Animation animation) {
        animations.add(animation);
    }

    public void removeAnimation(Animation animation) {
        removedAnimations.add(animation);
        animation.kill();
    }

    @Override
    public void paint(Graphics graphics) {
        graphics.setColor(bgColor);
        graphics.fillRect(0, 0, this.getWidth(), this.getHeight());
        for (int i = 0; i < moveables.size(); i++) { // DO NOT USE FOREACH HERE!!! MULTITHREADING CAUSES LIST MODIFICATION !!!
            graphics.drawImage(moveables.get(i).getImage(), 0, 0, this.getWidth(), this.getHeight(), null);
        }
        for (int i = 0; i < powerUps.size(); i++) {
            graphics.drawImage(powerUps.get(i).getImage(), 0, 0, this.getWidth(), this.getHeight(), null);
        }
        for (int i = 0; i < animations.size(); i++) {
            graphics.drawImage(animations.get(i).getImage(), 0, 0, this.getWidth(), this.getHeight(), null);
        }
        graphics.drawImage(foreground, 0, 0, this.getWidth(), this.getHeight(), null);
        graphics.setColor(Color.WHITE);
        graphics.drawString("Updates per second: " + loop.getLastUps(), 0, this.getHeight());
    }

    public GameCreator getGameCreator() {
        return gc;
    }

    public void gameOver(Moveable player, int code) {
        if (isGameOver) return;
        isGameOver = true;
        onGameOver();
        long gt = System.currentTimeMillis() + 1000;
        int dsTime;
        do {
            paintMe(1);
            dsTime = (int) (gt - System.currentTimeMillis());
            fg.setColor(Color.ORANGE);
            fg.drawString("Meow in: " + (dsTime / 1000), 0, 30);
            repaint();
            try {
                Thread.sleep((long) (1E3 / 60));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while (dsTime > 0);

        frame.dispose();
        System.out.println("THIS GAME IS OVER");
        stop();
        if (isMultiplayer) {
            try {
                if (isServer) {
                    server.send(createGameInfo());
                    server.send(-1);
                    //    server.stop();
                } else {
                    client.send(createGameInfo());
                    client.send(-1);
                    //   client.stop();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void onGameOver() {

    }

    @Override
    public void windowOpened(WindowEvent e) {

    }

    @Override
    public void windowClosing(WindowEvent e) {

    }

    @Override
    public void windowClosed(WindowEvent e) {
        gameOver(null, 0);
    }

    @Override
    public void windowIconified(WindowEvent e) {

    }

    @Override
    public void windowDeiconified(WindowEvent e) {

    }

    @Override
    public void windowActivated(WindowEvent e) {

    }

    @Override
    public void windowDeactivated(WindowEvent e) {

    }
}

