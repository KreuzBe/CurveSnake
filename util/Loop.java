package util;

import java.util.function.Consumer;
import java.lang.Thread;

public class Loop {

    private Thread loopThread;
    private int ups; // updates per second
    private Consumer<Integer> action; // action to be executed every update
    private boolean isRunning;
    private int lastUps = 0;
    private int tick = 0;

    public Loop(int ups, Consumer<Integer> action) {
        this.ups = ups;
        this.action = action;

        isRunning = false;
        loopThread = new Thread(this::run);
    }

    public void start() {
        isRunning = true;
        loopThread.start();
    }

    public void stop() {
        isRunning = false;
        loopThread.interrupt();
    }

    public boolean isRunning() {
        return isRunning;
    }

    public int getLastUps() {
        return lastUps;
    }

    private void run() {
        long nanoFirst = System.nanoTime();
        double nanosPerUpdate = 1E9 / ups;
        long dTime;
        long secondTimer = System.currentTimeMillis();
        int nextUps = 0;

        while (isRunning) {
            dTime = System.nanoTime() - nanoFirst;
            if (dTime > nanosPerUpdate) {
                tick++;
                nextUps++;
                action.accept(tick);
                nanoFirst = System.nanoTime();
            }

            if (System.currentTimeMillis() - secondTimer > 1000) { // Naja
                lastUps = nextUps;
                nextUps = 0;
                secondTimer = System.currentTimeMillis();
            }
        }
    }
}

