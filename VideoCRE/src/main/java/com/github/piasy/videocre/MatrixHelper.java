package com.github.piasy.videocre;

import android.opengl.Matrix;
import org.webrtc.RendererCommon;

/**
 * Created by Piasy{github.com/Piasy} on 16/07/2017.
 *
 * Not thread safe!
 */

public class MatrixHelper {//图像矩阵变换器
    private final float[] mFlipHorizontal;
    private final float[] mFlipVertical;
    private final float[] mTemp;

    public MatrixHelper() {
        mFlipHorizontal = RendererCommon.horizontalFlipMatrix();
        mFlipVertical = RendererCommon.verticalFlipMatrix();
        mTemp = new float[32];
    }

    public void flip(float[] matrix, boolean flipHorizontal, boolean flipVertical) {//左右、上下翻转
        if (matrix.length < 16) {
            throw new IllegalArgumentException("bad matrix length: " + matrix.length);
        }
        if (flipHorizontal && flipVertical) {
            Matrix.multiplyMM(mTemp, 0, mFlipHorizontal, 0, matrix, 0);
            Matrix.multiplyMM(matrix, 0, mFlipVertical, 0, mTemp, 0);
        } else if (flipHorizontal) {
            Matrix.multiplyMM(mTemp, 0, mFlipHorizontal, 0, matrix, 0);
            System.arraycopy(mTemp, 0, matrix, 0, 16);
        } else if (flipVertical) {
            Matrix.multiplyMM(mTemp, 0, mFlipVertical, 0, matrix, 0);
            System.arraycopy(mTemp, 0, matrix, 0, 16);
        }
    }

    public void rotate(float[] matrix, float degree) {//随意角度旋转
        Matrix.setRotateM(mTemp, 0, degree, 0, 0, 1);
        RendererCommon.adjustOrigin(mTemp);
        Matrix.multiplyMM(mTemp, 16, mTemp, 0, matrix, 0);
        System.arraycopy(mTemp, 16, matrix, 0, 16);
    }
}
