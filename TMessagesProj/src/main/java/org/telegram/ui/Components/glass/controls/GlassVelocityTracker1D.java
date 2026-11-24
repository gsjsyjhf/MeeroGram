package org.telegram.ui.Components.glass.controls;

import java.lang.reflect.Array;
import java.util.Arrays;
/* loaded from: /home/z/my-project/cg/mrtelegram/dexpick/classes5.dex */
final class GlassVelocityTracker1D {
    private final float[][] a;
    private int index;
    private final float[][] q;
    private final float[][] r;
    private final DataPointAtTime[] samples = new DataPointAtTime[20];
    private final float[] reusableDataPoints = new float[20];
    private final float[] reusableTimes = new float[20];
    private final float[] reusableVelocityCoefficients = new float[3];

    /* loaded from: /home/z/my-project/cg/mrtelegram/dexpick/classes5.dex */
    private static final class DataPointAtTime {
        float dataPoint;
        long time;

        DataPointAtTime(long j, float f) {
            this.time = j;
            this.dataPoint = f;
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public GlassVelocityTracker1D() {
        Class cls = Float.TYPE;
        this.a = (float[][]) Array.newInstance(cls, 3, 20);
        this.q = (float[][]) Array.newInstance(cls, 3, 20);
        this.r = (float[][]) Array.newInstance(cls, 3, 3);
    }

    private float calculateLeastSquaresVelocity(int i) {
        int i2 = 0;
        while (true) {
            if (i2 >= i) {
                break;
            }
            this.a[0][i2] = 1.0f;
            for (int i3 = 1; i3 < 3; i3++) {
                float[][] fArr = this.a;
                fArr[i3][i2] = fArr[i3 - 1][i2] * this.reusableTimes[i2];
            }
            i2++;
        }
        int i4 = 0;
        while (i4 < 3) {
            float[] fArr2 = this.q[i4];
            System.arraycopy(this.a[i4], 0, fArr2, 0, i);
            for (int i5 = 0; i5 < i4; i5++) {
                float[] fArr3 = this.q[i5];
                float dot = dot(fArr2, fArr3, i);
                for (int i6 = 0; i6 < i; i6++) {
                    fArr2[i6] = fArr2[i6] - (fArr3[i6] * dot);
                }
            }
            float max = 1.0f / Math.max(norm(fArr2, i), 1.0E-6f);
            for (int i7 = 0; i7 < i; i7++) {
                fArr2[i7] = fArr2[i7] * max;
            }
            float[] fArr4 = this.r[i4];
            int i8 = 0;
            while (i8 < 3) {
                fArr4[i8] = i8 < i4 ? 0.0f : dot(fArr2, this.a[i8], i);
                i8++;
            }
            i4++;
        }
        Arrays.fill(this.reusableVelocityCoefficients, 0.0f);
        for (int i9 = 2; i9 >= 0; i9--) {
            float dot2 = dot(this.q[i9], this.reusableDataPoints, i);
            float[] fArr5 = this.r[i9];
            for (int i10 = 2; i10 > i9; i10--) {
                dot2 -= fArr5[i10] * this.reusableVelocityCoefficients[i10];
            }
            this.reusableVelocityCoefficients[i9] = dot2 / fArr5[i9];
        }
        return this.reusableVelocityCoefficients[1];
    }

    private static float dot(float[] fArr, float[] fArr2, int i) {
        float f = 0.0f;
        for (int i2 = 0; i2 < i; i2++) {
            f += fArr[i2] * fArr2[i2];
        }
        return f;
    }

    private static float norm(float[] fArr, int i) {
        return (float) Math.sqrt(dot(fArr, fArr, i));
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void addDataPoint(long j, float f) {
        int i = (this.index + 1) % 20;
        this.index = i;
        DataPointAtTime[] dataPointAtTimeArr = this.samples;
        DataPointAtTime dataPointAtTime = dataPointAtTimeArr[i];
        if (dataPointAtTime == null) {
            dataPointAtTimeArr[i] = new DataPointAtTime(j, f);
            return;
        }
        dataPointAtTime.time = j;
        dataPointAtTime.dataPoint = f;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public float calculateVelocity() {
        int i = this.index;
        DataPointAtTime dataPointAtTime = this.samples[i];
        if (dataPointAtTime == null) {
            return 0.0f;
        }
        int i2 = 0;
        DataPointAtTime dataPointAtTime2 = dataPointAtTime;
        while (true) {
            DataPointAtTime dataPointAtTime3 = this.samples[i];
            if (dataPointAtTime3 != null) {
                long j = dataPointAtTime.time;
                long j2 = dataPointAtTime3.time;
                float f = (float) (j - j2);
                float abs = (float) Math.abs(j2 - dataPointAtTime2.time);
                if (f > 100.0f || abs > 40.0f) {
                    break;
                }
                this.reusableDataPoints[i2] = dataPointAtTime3.dataPoint;
                this.reusableTimes[i2] = -f;
                if (i == 0) {
                    i = 20;
                }
                i--;
                i2++;
                if (i2 >= 20) {
                    break;
                }
                dataPointAtTime2 = dataPointAtTime3;
            } else {
                break;
            }
        }
        if (i2 < 3) {
            return 0.0f;
        }
        float calculateLeastSquaresVelocity = calculateLeastSquaresVelocity(i2) * 1000.0f;
        if (Float.isNaN(calculateLeastSquaresVelocity) || Float.isInfinite(calculateLeastSquaresVelocity)) {
            return 0.0f;
        }
        return calculateLeastSquaresVelocity;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void resetTracking() {
        Arrays.fill(this.samples, (Object) null);
        this.index = 0;
    }
}
