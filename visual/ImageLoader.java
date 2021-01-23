package visual;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageLoader {

    public static int IMG_COIN = 0;
    public static int IMG_CLEAR = 1;
    public static int IMG_BOMB = 2;
    public static BufferedImage[][] images = new BufferedImage[3][5];

    public static void init() {
        try {
            for (int i = 0; i < 5; i++) {
                images[0][i] = ImageIO.read(new File("./res/MonedaD.png")).getSubimage(i * 16, 0, 16, 16);
                images[1][i] = ImageIO.read(new File("./res/MonedaR.png")).getSubimage(i * 16, 0, 16, 16);
                images[2][i] = ImageIO.read(new File("./res/MonedaP.png")).getSubimage(i * 16, 0, 16, 16);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
