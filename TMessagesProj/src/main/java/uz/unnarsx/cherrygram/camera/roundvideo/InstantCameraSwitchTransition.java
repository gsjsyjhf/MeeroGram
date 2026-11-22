package uz.unnarsx.cherrygram.camera.roundvideo;

/**
 * MeeroGram reconstruction of the unpublished Cherrygram instant-camera switch
 * transition (a cosmetic GL crossfade). With the legacy camera engine enforced,
 * this is never constructed; the stub keeps the call sites compiling.
 */
public class InstantCameraSwitchTransition {

    public InstantCameraSwitchTransition(int videoWidth) {
    }

    public boolean isActive() {
        return false;
    }

    public boolean isWaitingForCamera() {
        return false;
    }

    public boolean begin(int frameTexture, boolean toFront) {
        return false;
    }

    public boolean render(int texture) {
        return true;
    }

    public boolean onCameraFrame(int texture) {
        return true;
    }

    public int getNextFrameDelayMs() {
        return 16;
    }

    public void cancel() {
    }

    public void destroy() {
    }
}
