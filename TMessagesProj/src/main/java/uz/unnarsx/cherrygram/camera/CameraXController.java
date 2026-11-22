/**
 * MeeroGram reconstruction of the unpublished Cherrygram CameraXController.
 * The real implementation drives androidx.camera capture sessions. With
 * CameraXUtils forcing the legacy engine this controller is never activated;
 * it exists to satisfy every call site in CameraXView, InstantCameraView(2),
 * CameraView, StoryRecorder and EffectSelectorView.
 */
package uz.unnarsx.cherrygram.camera;

import android.content.Context;
import android.util.Range;

import androidx.camera.core.MeteringPointFactory;

import org.telegram.messenger.camera.Size;

import java.io.File;

public class CameraXController {

    public static final int CAMERA_NONE = 0;
    public static final int CAMERA_AUTO = 1;
    public static final int CAMERA_HDR = 2;
    public static final int CAMERA_NIGHT = 3;
    public static final int CAMERA_WIDE = 4;
    public static final int CAMERA_ASPECT_RATIO_SELECTOR = 5;

    @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.SOURCE)
    public @interface EffectFacing {
    }

    public interface LensSwitchListener {
        void onLensSwitchStarted(boolean ultraWide);

        void onLensSettled();
    }

    public static class CameraLifecycle implements androidx.lifecycle.LifecycleOwner {
        private final androidx.lifecycle.LifecycleRegistry registry = new androidx.lifecycle.LifecycleRegistry(this);

        @Override
        public androidx.lifecycle.Lifecycle getLifecycle() {
            return registry;
        }

        public void start() {
            registry.handleLifecycleEvent(androidx.lifecycle.Lifecycle.Event.ON_START);
        }

        public void pause() {
            registry.handleLifecycleEvent(androidx.lifecycle.Lifecycle.Event.ON_STOP);
        }

        public void resume() {
            registry.handleLifecycleEvent(androidx.lifecycle.Lifecycle.Event.ON_START);
        }

        public void stop() {
            registry.handleLifecycleEvent(androidx.lifecycle.Lifecycle.Event.ON_DESTROY);
        }

        public boolean isDestroyed() {
            return false;
        }
    }

    public CameraXController(CameraLifecycle lifecycle, MeteringPointFactory meteringPointFactory, Object surfaceProvider) {
    }

    public boolean isInitiated() {
        return false;
    }

    public boolean isCameraReady() {
        return false;
    }

    public static boolean hasGoodCamera(Context context) {
        return false;
    }

    public boolean isFrontface() {
        return false;
    }

    public boolean hasFrontFaceCamera() {
        return false;
    }

    public void setFrontFace(boolean frontFace) {
    }

    public boolean isAvailableAutoMode() {
        return false;
    }

    public boolean isAvailableHdrMode() {
        return false;
    }

    public boolean isAvailableNightMode() {
        return false;
    }

    public boolean isAvailableWideMode() {
        return false;
    }

    public boolean isFlashAvailable() {
        return false;
    }

    public int getCurrentFlashMode() {
        return 0;
    }

    public int setNextFlashMode() {
        return 0;
    }

    public boolean isExposureCompensationSupported() {
        return false;
    }

    public void setExposureCompensation(float value) {
    }

    public float getMainExposureValue() {
        return 0f;
    }

    public int getDisplayOrientation() {
        return 0;
    }

    public void focusToPoint(int x, int y, boolean smooth) {
    }

    public float getZoomRatio() {
        return 1f;
    }

    public float getMaxZoomRatio() {
        return 1f;
    }

    public float resetZoom() {
        return 1f;
    }

    public void setUltraWideStartAllowed(boolean allowed) {
    }

    public void setZoomRatio(float ratio) {
    }

    public void setZoom(float zoom) {
    }

    public static Range<Float> getZoomRatioRange(androidx.camera.core.CameraInfo info) {
        return new Range<>(1f, 1f);
    }

    public Size getPreviewSize() {
        return new Size(720, 1280);
    }

    public int getCameraEffect() {
        return CAMERA_NONE;
    }

    public void setCameraEffect(int effect) {
    }

    public void setCaptureRotation(int rotation) {
    }

    public void setPreviewRotation(int rotation) {
    }

    public void bindUseCasesKeepingZoom() {
    }

    public void switchCamera() {
    }

    public void closeCamera() {
    }

    public void release() {
    }

    public void setTorchEnabled(boolean enabled) {
    }

    public void stopVideoRecording(boolean abandon) {
    }

    public void takePicture(File file, Runnable onDone) {
    }

    public void recordVideo(File file, boolean frontFace, CameraXView.VideoSavedCallback callback) {
    }

    public void addLensSwitchListener(LensSwitchListener listener) {
    }

    public void setOnLensSwitchStarted(Runnable listener) {
    }

    public void setOnLensSettled(Runnable listener) {
    }

    public void setFrontFaceListener(Runnable listener) {
    }

    public void initCamera(Context context, boolean frontFace, Runnable onStreamReady) {
    }
}
