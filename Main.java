import visual.Display;
import visual.ImageLoader;
import visual.moveable.Enemy;
import visual.moveable.Player;

public class Main extends Display {

    private Player player;
    private Enemy enemy;

    public Main() {
        super(false);
        ImageLoader.init();

        System.out.println("I AM A PLAYER!!!!!!!!!!!!!!");
        player = new Player(50, 50, 1, 1, 3, this, BYTE_PLAYER_MIN);
        addMoveable(player);

        addMoveable(new Enemy(500, 500, -1, -1, 3, this, player));
        
        start();
    }

    @Override
    public void onUpdate(int tick) {

    }

    public static void main(String[] args) {
        new Main();
    }
}
