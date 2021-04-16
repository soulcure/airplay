package swaiotos.sensor.touch;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import swaiotos.sensor.R;
import swaiotos.sensor.utils.TouchPointAnimator;

public class TouchPoint {
    public int alpha;
    public Drawable drawable;
    public float x, y, scale;
    public static final int endAlpha = 255;
    public static final float startScale = 0.8f;
    public static final float endScale = 1f;
    //反馈图标尺寸
    private static final int IMAGE_WIDTH = 306;
    private Rect rect;
    private TouchPointAnimator touchPointAnimator;

    public TouchPoint(Context context, InputTouchView inputTouchView, int id) {
        touchPointAnimator = new TouchPointAnimator(inputTouchView, id);
        rect = new Rect();
        drawable = context.getResources().getDrawable(R.drawable.touch_respond);
    }

    public void draw(Canvas canvas) {
        drawable.setBounds(getBounds(rect, x, y, scale));
        drawable.setAlpha(alpha);
        drawable.draw(canvas);
    }

    public void updatePosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void addDrawableAnim() {
        touchPointAnimator.startDownAnim();
    }

    public void removeDrawableAnim() {
        touchPointAnimator.startUpAnim();
    }

    public void setValue(float value) {
        scale = value;
        alpha = (int) ((value - startScale) / (endScale - startScale) * endAlpha);
    }

    private Rect getBounds(Rect rect, float x, float y, float scale) {
        rect.set((int) (x - IMAGE_WIDTH * scale / 2), (int) (y - IMAGE_WIDTH * scale / 2),
                (int) (x + IMAGE_WIDTH * scale / 2), (int) (y + IMAGE_WIDTH * scale / 2));

        return rect;
    }
}
