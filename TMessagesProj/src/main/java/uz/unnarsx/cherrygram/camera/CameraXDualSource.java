/**
 * MeeroGram reconstruction of the unpublished Cherrygram CameraXDualSource
 * (dual-camera capture source). Callers only activate it when the CameraX
 * engine is selected, which CameraXUtils disables, so open() always returns
 * null and every path falls back to the legacy camera session.
 */
package uz.unnarsx.cherrygram.camera;

import android.content.Context;
import android.graphics.SurfaceTexture;

import org.telegram.messenger.camera.CameraSessionWrapper;
import org.telegram.messenger.camera.Size;

public class CameraXDualSource {

    public static boolean isDualSupported(Context context) {
        return false;
    }

    public static org.telegram.messenger.camera.Size estimateDisplaySize() {
        return new org.telegram.messenger.camera.Size(720, 1280);
    }

    public CameraXDualSource(Context context, boolean frontface) {
    }

    public Session open(boolean frontface, SurfaceTexture texture, SessionReadyCallback callback) {
        return null;
    }

    public void release() {
    }

    public void setMainFront(boolean frontface) {
    }

    public float getMainExposureValue() {
        return 0f;
    }

    public void setOnMainCameraChanged(Runnable listener) {
    }

    public void setOnError(Runnable listener) {
    }

    public CameraXController getController() {
        return null;
    }

    public interface SessionReadyCallback {
        void onSessionReady(Session session);
    }

    public static class Session extends CameraSessionWrapper {

        public Session() {
        }

        public org.telegram.messenger.camera.Size getDisplaySize() {
            return new org.telegram.messenger.camera.Size(720, 1280);
        }

        public android.util.Size getResolution() {
            return new android.util.Size(720, 1280);
        }

        @Override
        public boolean isInitiated() {
            return false;
        }
    }
}
