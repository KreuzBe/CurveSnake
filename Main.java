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
        if (isServer())
            addMoveable(new Player(50, 50, 1, 1, 3, this));
        start();
    }

    @Override
    public void onUpdate(int tick) {

    }

    public static void main(String[] args) {
        new Main();
    }
}
