import util.net.Client;
import util.net.Server;
import visual.Display;
import visual.ImageLoader;
import visual.PowerUp;
import visual.moveable.Enemy;
import visual.moveable.Player;

import java.awt.*;
import java.io.IOException;

public class Main extends Display {

    private Player player;
    private Enemy enemy;

    public Main() {
        ImageLoader.init();
        start();
    }

    @Override
    public void onUpdate(int tick) {
        fg.setColor(Color.RED);
        for (int i = 0; i < player.getScore(); i++) {
            fg.drawImage(ImageLoader.images[ImageLoader.IMG_COIN][0], 100 + 50 * i, 25, 25, 25, null);
        }
    }

    public static void main(String[] args) {
        new Main();
    }
}
