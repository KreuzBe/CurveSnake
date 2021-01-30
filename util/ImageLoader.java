package util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageLoader {

    public static int IMG_COIN = 0;
    public static int IMG_CLEAR = 1;
    public static int IMG_BOMB = 2;
    public static BufferedImage[] images = new BufferedImage[6];

    public static void init() {
        try {
            images[0] = ImageIO.read(new File("./res/item_coin.png"));
            images[1] = ImageIO.read(new File("./res/item_clear_line.png"));
            images[2] = ImageIO.read(new File("./res/item_clear.png"));
            images[3] = ImageIO.read(new File("./res/item_vanish.png"));
            images[4] = ImageIO.read(new File("./res/rocket.png"));
            images[5] = ImageIO.read(new File("./res/rocket_player.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void drawRotatedImage(BufferedImage img, int x, int y, int width, int height, float angle, Graphics2D g) {
        AffineTransform tr = new AffineTransform();
        tr.translate(x, y);
        tr.rotate(angle, width / 2f, height / 2f);
        g.drawImage(img.getScaledInstance(width, height, Image.SCALE_DEFAULT), tr, null);
    }
}
