import util.net.Client;
import util.net.Server;
import visual.Display;
import visual.ImageLoader;
import visual.PowerUp;
import visual.moveable.Enemy;
import visual.moveable.Player;

import java.awt.*;

public class Main extends Display {

    private Player player;
    private Enemy enemy;

    public Main() {
        ImageLoader.init();
        player = new Player(800, 200, 1, 1, 7, this);
        this.addMoveable(player);
        for (int i = 0; i < 1; i++) {
            enemy = new Enemy((int) (Math.random() * WIDTH), (int) (Math.random() * HEIGHT), 1, 1, 5, this, player);
            enemy.setDrawByte(Display.BYTE_NPC_MIN << i);
            this.addMoveable(enemy);
        }

        start();
    }

    @Override
    public void onUpdate(int tick) {
        fg.setColor(Color.RED);
        for (int i = 0; i < player.getScore(); i++) {
            fg.drawImage(ImageLoader.images[ImageLoader.IMG_COIN][0], 100 + 50 * i, 25, 25, 25, null);
        }

        if (Math.random() < 0.01)
            createPowerUp(new PowerUp((int) ((WIDTH - 120) * Math.random()) + 60, (int) ((HEIGHT - 60) * Math.random()) + 30, 30, 0b1 << 1, this));
        else if (Math.random() < 0.001)
            createPowerUp(new PowerUp((int) ((WIDTH - 120) * Math.random()) + 60, (int) ((HEIGHT - 100) * Math.random()) + 50, 30, 0b1 << 2, this));
        else if (Math.random() < 0.005)
            createPowerUp(new PowerUp((int) ((WIDTH - 120) * Math.random()) + 60, (int) ((HEIGHT - 100) * Math.random()) + 50, 30, 0b1 << 3, this));
    }

    public static void main(String[] args) {
        Client cl = new Client("192.168.2.121", 4444);

        new Main();
    }
}
