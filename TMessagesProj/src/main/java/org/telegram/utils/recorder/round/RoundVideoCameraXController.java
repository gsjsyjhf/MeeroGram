/**
 * MeeroGram reconstruction of the unpublished Cherrygram
 * RoundVideoCameraXController. RoundVideoSession only constructs it when
 * Builder.setUseCameraX(true) is set, which InstantCameraView2 derives from
 * CameraXUtils.isCurrentCameraCameraX() (forced false), so this controller is
 * never instantiated. Implements the abstract RoundVideoCameraSource contract
 * with inert defaults.
 */
package org.telegram.utils.recorder.round;

import android.content.Context;
import android.view.TextureView;

import uz.unnarsx.cherrygram.camera.CameraXController;
import uz.unnarsx.cherrygram.camera.CherryZoomSliderView;

public class RoundVideoCameraXController extends RoundVideoCameraSource {

    public RoundVideoCameraXController(
            Context context,
            TextureView previewView,
            RoundVideoSession.OutputResolution outputResolution,
            int videoBitrate,
            RoundVideoSession.CameraResolution cameraResolution,
            RoundVideoSession.FrameRate frameRate,
            boolean compositionEnabled,
            RoundVideoDiagnostics diagnostics,
            RoundVideoCameraController.Callback callback
    ) {
    }

    @Override
    void startSegment(RoundVideoMp4Writer writer, long timelineOffsetUs, RoundVideoSession.CameraFacing facing) {
    }

    @Override
    boolean setCameraFacing(RoundVideoSession.CameraFacing facing) {
        return false;
    }

    @Override
    boolean setZoom(float progress) {
        return false;
    }

    @Override
    boolean setTorchEnabled(boolean enabled) {
        return false;
    }

    @Override
    boolean stopSegment() {
        return false;
    }

    @Override
    void release() {
    }

    public CherryZoomSliderView.ZoomController getZoomController() {
        return null;
    }

    public CameraXController getCameraXController() {
        return null;
    }

    public void focusToPoint(float x, float y) {
    }
}
