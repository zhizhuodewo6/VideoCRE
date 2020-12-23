package com.github.piasy.videocre;

import java.util.Arrays;
import java.util.List;
import org.webrtc.Logging;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoFrame;
import org.webrtc.VideoRenderer;

/**
 * Created by Piasy{github.com/Piasy} on 20/07/2017.
 */

public class VideoSink implements VideoCapturer.CapturerObserver {

    private static final String TAG = "VideoSink";

    private final List<VideoRenderer.Callbacks> mCallbacks;
    private final MatrixHelper mMatrixHelper;

    private volatile boolean mFlipHorizontal;
    private volatile boolean mFlipVertical;
    private volatile float mRotateDegree;

    public VideoSink(final VideoRenderer.Callbacks... callbacks) {
        mCallbacks = Arrays.asList(callbacks);
        mMatrixHelper = new MatrixHelper();
    }

    public void flipHorizontal(boolean flip) {
        mFlipHorizontal = flip;
    }

    public void flipVertical(boolean flip) {
        mFlipVertical = flip;
    }

    public void rotate(float rotateDegree) {
        mRotateDegree = rotateDegree;
    }

    @Override
    public void onCapturerStarted(final boolean success) {
        Logging.d(TAG, "onCapturerStarted " + success);
    }

    @Override
    public void onCapturerStopped() {
        Logging.d(TAG, "onCapturerStopped");
    }

    @Override
    public void onByteBufferFrameCaptured(final byte[] data, final int width, final int height,
            final int rotation, final long timestamp) {
    }

    @Override
    public void onTextureFrameCaptured(final int width, final int height, final int oesTextureId,
            final float[] transformMatrix, final int rotation, final long timestamp) {
        mMatrixHelper.flip(transformMatrix, mFlipHorizontal, mFlipVertical);//对矩阵进行修改
        mMatrixHelper.rotate(transformMatrix, mRotateDegree);

        VideoRenderer.I420Frame frame = new VideoRenderer.I420Frame(width, height, rotation,
                oesTextureId, transformMatrix, 0, timestamp);
        for (int i = 0, n = mCallbacks.size(); i < n; i++) {
            mCallbacks.get(i).renderFrame(frame);//为每个需要接收Frame的sink回传数据
        }
    }

    @Override
    public void onFrameCaptured(final VideoFrame frame) {
    }
}
