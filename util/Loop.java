package util;

import java.util.function.Consumer;
import java.lang.Thread;

public class Loop {
	
	private Thread loopThread;
	private int ups; // updates per second
	private Consumer<Integer> action; // action to be executed every update
	private boolean isRunning;
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
	}
	
	
	public boolean isRunning() {
		return isRunning;
	}

	private void run() {
		long nanoFirst = System.nanoTime();
		double nanosPerUpdate = 1E9 / ups;
		long dTime;
		
		int fps = 0;
		
		System.out.println(nanosPerUpdate);
		while(isRunning) {
			dTime = System.nanoTime() - nanoFirst;
			if(dTime > nanosPerUpdate){
				tick++;
				fps++;
				action.accept(tick);
				nanoFirst = System.nanoTime();
			}
			
			if(System.nanoTime() % 1E9 == 0){
				System.out.println("fps: " + fps);
				fps = 0;
			}
			
			
		}
		
	}
	
}

