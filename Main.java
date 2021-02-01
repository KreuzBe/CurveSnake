import util.GameCreator;
import visual.Display;
import util.ImageLoader;
import visual.Moveable;
import visual.PowerUp;
import visual.moveable.Enemy;
import visual.moveable.Player;

public class Main extends Display {


    public Main(GameCreator gc) {
        super(gc);
        ImageLoader.init();
    }

    @Override
    public void onGameReady() {
        Moveable player;
        if (isMultiplayer()) {
            if (isServer()) {
                player = new Enemy(50, 50, 1, 1, 4, this, null, 0);
                createPowerUp(new PowerUp((float) (Math.random() * (WIDTH - 60) + 30), (float) (Math.random() * (HEIGHT - 60) + 30), 30, BYTE_POWERUP_MIN, this, false));
            } else
                player = new Player(500, 500, -1, 1, 4, this, BYTE_PLAYER_MIN << 1);
        } else {
            player = new Player((int) (WIDTH * Math.random()), (int) (HEIGHT * Math.random()), -1, 1, 5.5f, this, BYTE_PLAYER_MIN);
            for (int i = 0; i < 1; i++)
                addMoveable(new Enemy((int) (WIDTH * Math.random()), (int) (HEIGHT * Math.random()), -1, 1, 5, this, player, i));
        }
        player.lookAt(WIDTH / 2f, HEIGHT / 2f, 360);
        player.lookAt(WIDTH / 2f, HEIGHT / 2f, 360);
        addMoveable(player);

        start();
    }

    @Override
    public void onUpdate(int tick) {
        if (!isMultiplayer() || isServer()) {
            if (Math.random() < 0.0005) {
                createPowerUp(new PowerUp((float) (Math.random() * (WIDTH - 60) + 30), (float) (Math.random() * (HEIGHT - 60) + 30), 30, BYTE_POWERUP_MIN, this, false));
            } else if (Math.random() < 0.0005) {
                createPowerUp(new PowerUp((float) (Math.random() * (WIDTH - 60) + 30), (float) (Math.random() * (HEIGHT - 60) + 30), 30, BYTE_POWERUP_MIN << 1, this, false));
            } else if (Math.random() < 0.001) {
                createPowerUp(new PowerUp((float) (Math.random() * (WIDTH - 60) + 30), (float) (Math.random() * (HEIGHT - 60) + 30), 30, BYTE_POWERUP_MIN << 2, this, false));
            } else if (Math.random() < 0.0005) {
                createPowerUp(new PowerUp((float) (Math.random() * (WIDTH - 60) + 30), (float) (Math.random() * (HEIGHT - 60) + 30), 30, BYTE_POWERUP_MIN << 3, this, false));
            } else if (Math.random() < 0.0005) {
                createPowerUp(new PowerUp((float) (Math.random() * (WIDTH - 60) + 30), (float) (Math.random() * (HEIGHT - 60) + 30), 30, BYTE_POWERUP_MIN << 4, this, false));
            }
        }
    }

    @Override
    public void onGameOver() {
        if (isRunning())
            new Main(getGameCreator());
    }

    public static void main(String[] args) {
        new Main(null);
    }
}
