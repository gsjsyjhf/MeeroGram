package uz.unnarsx.cherrygram.camera.roundvideo;

import android.graphics.SurfaceTexture;
import android.util.Range;

import org.telegram.ui.Components.InstantCameraView;
import org.telegram.ui.Components.InstantCameraView2;

import uz.unnarsx.cherrygram.camera.CameraXController;

/**
 * MeeroGram reconstruction of the unpublished Cherrygram VideoMessagesHelper.
 * The real implementation drives a CameraX capture session; that module is not
 * published, so CameraXUtils forces the proven legacy camera engine and these
 * methods remain safe no-ops. Static fps helpers keep working for Camera2Session.
 */
public class VideoMessagesHelper {

    public CameraXController cameraXController;

    public void createSingleCameraX(InstantCameraView view, SurfaceTexture surfaceTexture) {
    }

    public void createDualCameraX(InstantCameraView view, SurfaceTexture[] surfaceTextures) {
    }

    public void destroyCameraX(InstantCameraView view) {
    }

    public void switchCameraX(InstantCameraView view) {
    }

    public void prepareCameraSwitch(InstantCameraView2 view) {
    }

    public void checkFlash(InstantCameraView view) {
    }

    public void checkFlash(InstantCameraView2 view) {
    }

    public void updateCameraXFlash(InstantCameraView view) {
    }

    public boolean createFlashConfigurator(InstantCameraView view) {
        return false;
    }

    public boolean createFlashConfigurator(InstantCameraView2 view) {
        return false;
    }

    public void showExposureControls(InstantCameraView view, boolean show) {
    }

    public void showExposureControls(InstantCameraView2 view, boolean show) {
    }

    public static boolean isCamera60Fps() {
        return false;
    }

    public static Range<Integer> getCameraXFpsRange() {
        return new Range<>(30, 30);
    }
}
