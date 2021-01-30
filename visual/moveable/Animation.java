package visual.moveable;

import visual.Display;
import visual.VisualObject;

public class Animation extends VisualObject {
    public Animation(Display display, float x, float y, int drawByte, int lifeTime) {
        super(display, x, y, drawByte);
        setLife(lifeTime);
    }

    @Override
    public void update(int tick) throws Exception {
        //clear();
        super.update(tick);
    }
}
