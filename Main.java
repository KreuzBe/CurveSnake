import visual.Display;
import visual.ImageLoader;
import visual.Moveable;
import visual.PowerUp;
import visual.moveable.Enemy;
import visual.moveable.Player;

public class Main extends Display {

    private Moveable player;


    public Main() {
        ImageLoader.init();
    }

    @Override
    public void onGameReady() {
        if (isMultiplayer()) {
            if (isServer())
                player = new Enemy(50, 50, 1, 1, 3, this, null);
            else
                player = new Player(500, 500, -1, 1, 5, this, BYTE_PLAYER_MIN << 1);
        } else {
            player = new Player(500, 500, -1, 1, 5, this, BYTE_PLAYER_MIN);
            addMoveable(new Enemy(50, 50, 1, 1, 3, this, player));
        }
        addMoveable(player);

        start();
    }

    @Override
    public void onUpdate(int tick) {
        if (!isMultiplayer() || isServer()) {
            if (Math.random() < 0.005) {
                createPowerUp(new PowerUp((float) (Math.random() * (WIDTH - 60) + 30), (float) (Math.random() * (HEIGHT - 60) + 30), 30, BYTE_POWERUP_MIN, this, false));
            } else if (Math.random() < 0.001) {
                createPowerUp(new PowerUp((float) (Math.random() * (WIDTH - 60) + 30), (float) (Math.random() * (HEIGHT - 60) + 30), 30, BYTE_POWERUP_MIN << 1, this, false));
            } else if (Math.random() < 0.005) {
                createPowerUp(new PowerUp((float) (Math.random() * (WIDTH - 60) + 30), (float) (Math.random() * (HEIGHT - 60) + 30), 30, BYTE_POWERUP_MIN << 2, this, false));
            }
        }
    }


    public static void main(String[] args) {
        new Main();
    }
}
