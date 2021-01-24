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
        if (isServer())
            player = new Player(50, 50, 1, 1, 5, this, BYTE_PLAYER_MIN);
        else
            player = new Player(500, 500, -1, 1, 5, this, BYTE_PLAYER_MIN << 1);
        addMoveable(player);
        // addMoveable(new Enemy(500, 500, -1, -1, 3, this, player));
        start();
    }

    @Override
    public void onUpdate(int tick) {
        if (isServer()) {
            if (Math.random() < 0.1) {
                createPowerUp(new PowerUp((float) (Math.random() * (WIDTH - 60) + 30), (float) (Math.random() * (HEIGHT - 60) + 30), 30, BYTE_POWERUP_MIN, this, false));
            } else if (Math.random() < 0.1) {
                createPowerUp(new PowerUp((float) (Math.random() * (WIDTH - 60) + 30), (float) (Math.random() * (HEIGHT - 60) + 30), 30, BYTE_POWERUP_MIN << 1, this, false));
            } else if (Math.random() < 0.1) {
                createPowerUp(new PowerUp((float) (Math.random() * (WIDTH - 60) + 30), (float) (Math.random() * (HEIGHT - 60) + 30), 30, BYTE_POWERUP_MIN << 2, this, false));
            }
        }
    }


    public static void main(String[] args) {
        new Main();
    }
}
