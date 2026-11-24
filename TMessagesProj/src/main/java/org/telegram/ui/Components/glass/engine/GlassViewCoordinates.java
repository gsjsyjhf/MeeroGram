package org.telegram.ui.Components.glass.engine;

import android.graphics.Matrix;
import android.view.View;
import android.view.ViewParent;
/* loaded from: /home/z/my-project/cg/mrtelegram/dexpick/classes5.dex */
abstract class GlassViewCoordinates {
    /* JADX INFO: Access modifiers changed from: package-private */
    public static boolean positionInAncestor(View view, View view2, Matrix matrix, float[] fArr) {
        fArr[0] = 0.0f;
        fArr[1] = 0.0f;
        if (view == view2) {
            return true;
        }
        while (view != view2) {
            matrix.set(view.getMatrix());
            if (!matrix.isIdentity()) {
                matrix.mapPoints(fArr);
            }
            ViewParent parent = view.getParent();
            if (!(parent instanceof View)) {
                return false;
            }
            View view3 = (View) parent;
            fArr[0] = fArr[0] + (view.getLeft() - view3.getScrollX());
            fArr[1] = fArr[1] + (view.getTop() - view3.getScrollY());
            view = view3;
        }
        return true;
    }
}
