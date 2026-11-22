/**
 * MeeroGram reconstruction of the unpublished Cherrygram CherryZoomSliderView
 * (camera zoom slider with presets). The visual slider implementation is not
 * published, so this renders nothing and reports neutral values; camera zoom
 * still works through the standard pinch/pager controls.
 */
package uz.unnarsx.cherrygram.camera;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.blur3.BlurredBackgroundDrawableViewFactory;
import org.telegram.ui.Components.blur3.drawable.color.BlurredBackgroundColorProvider;

public class CherryZoomSliderView extends FrameLayout {

    public interface ZoomDelegate {
        void onZoomChanged(float zoom);
    }

    public interface PresetSelectedListener {
        void onPresetSelected(float zoom);
    }

    public CherryZoomSliderView(Context context) {
        super(context);
    }

    public CherryZoomSliderView(Context context, Theme.ResourcesProvider resourcesProvider, boolean allowPresets) {
        super(context);
    }

    public CherryZoomSliderView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setDelegate(ZoomDelegate delegate) {
    }

    public void setOnPresetSelectedListener(PresetSelectedListener listener) {
    }

    public void setLiquidGlassBackground(BlurredBackgroundDrawableViewFactory factory, BlurredBackgroundColorProvider colorProvider) {
    }

    public void bindCamera(CameraXController controller) {
    }

    public void bindCamera(ZoomController controller) {
    }

    public void unbindCamera() {
    }

    public void prepareForCameraChange() {
    }

    public void hideImmediately() {
    }

    public void collapse() {
    }

    public void scaleZoom(float factor) {
    }

    public void setZoom(float zoom) {
    }

    public void setSliderValue(float value, boolean animated) {
    }

    public void animateToValue(float value) {
    }

    public boolean isTouch() {
        return false;
    }

    public void setMaxWidth(int maxWidth) {
    }

    public void setMaxHeight(int maxHeight) {
    }

    public static class ZoomController {
        public boolean isZoomSupported() {
            return false;
        }

        public void setZoom(float zoom) {
        }

        public float getZoom() {
            return 0f;
        }
    }
}
