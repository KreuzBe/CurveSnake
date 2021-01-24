import visual.Display;
import visual.ImageLoader;
import visual.PowerUp;
import visual.moveable.Enemy;
import visual.moveable.Player;

public class Main extends Display {

    private Player player;
    private Enemy enemy;

    public Main() {
        super(true);
        ImageLoader.init();
        player = new Player(50, 50, 1, 1, 5, this, BYTE_PLAYER_MIN);
        addMoveable(player);
        // addMoveable(new Enemy(500, 500, -1, -1, 3, this, player));
        start();
    }

    @Override
    public void onUpdate(int tick) {

        if (Math.random() < 0.001) {
            createPowerUp(new PowerUp((float) (Math.random() * (WIDTH - 60) + 30), (float) (Math.random() * (HEIGHT - 60) + 30), 30, BYTE_POWERUP_MIN, this));
        } else if (Math.random() < 0.0005) {
            createPowerUp(new PowerUp((float) (Math.random() * (WIDTH - 60) + 30), (float) (Math.random() * (HEIGHT - 60) + 30), 30, BYTE_POWERUP_MIN << 1, this));
        } else if (Math.random() < 0.001) {
            createPowerUp(new PowerUp((float) (Math.random() * (WIDTH - 60) + 30), (float) (Math.random() * (HEIGHT - 60) + 30), 30, BYTE_POWERUP_MIN << 2, this));
        }


    }

    public static void main(String[] args) {
        new Main();
    }
}
