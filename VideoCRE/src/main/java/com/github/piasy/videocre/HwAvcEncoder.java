package com.github.piasy.videocre;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Handler;
import android.os.HandlerThread;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.webrtc.EglBase;
import org.webrtc.MediaCodecVideoEncoder;
import org.webrtc.VideoRenderer;

/**
 * Created by Piasy{github.com/Piasy} on 21/07/2017.
 */

public class HwAvcEncoder implements VideoRenderer.Callbacks, MediaCodecCallback {//视频编码并保存到文件里

    private final HandlerThread mMediaCodecThread;
    private final Handler mMediaCodecHandler;
    private final MediaCodecVideoEncoder mVideoEncoder;
    private final VideoConfig mVideoConfig;
    private final List<MediaCodecCallback> mMediaCodecCallbacks;

    public HwAvcEncoder(final VideoConfig videoConfig, final MediaCodecCallback... callbacks) {
        mVideoConfig = videoConfig;
        mMediaCodecThread = new HandlerThread("HwAvcEncoderThread");
        mMediaCodecThread.start();
        mMediaCodecHandler = new Handler(mMediaCodecThread.getLooper());
        mVideoEncoder = new MediaCodecVideoEncoder();
        mMediaCodecCallbacks = Arrays.asList(callbacks);
    }

    public void start(final EglBase eglBase) {
        mMediaCodecHandler.post(new Runnable() {
            @Override
            public void run() {
                mVideoEncoder.initEncode(MediaCodecVideoEncoder.VideoCodecType.VIDEO_CODEC_H264,
                        MediaCodecVideoEncoder.H264Profile.CONSTRAINED_BASELINE.getValue(),
                        mVideoConfig.outputWidth(), mVideoConfig.outputHeight(),
                        mVideoConfig.outputBitrate(), mVideoConfig.fps(),
                        eglBase.getEglBaseContext(), HwAvcEncoder.this);
            }
        });
    }

    @Override
    public void renderFrame(final VideoRenderer.I420Frame frame) {
        mMediaCodecHandler.post(new Runnable() {
            @Override
            public void run() {
                mVideoEncoder.encodeTexture(false, frame.textureId, frame.samplingMatrix,
                        TimeUnit.NANOSECONDS.toMicros(frame.timestamp));
            }
        });
    }

    public void destroy() {
        mMediaCodecHandler.post(new Runnable() {
            @Override
            public void run() {
                mVideoEncoder.release();
                mMediaCodecThread.quit();
            }
        });
    }

    @Override
    public void onEncodedFrame(final MediaCodecVideoEncoder.OutputBufferInfo frame,
            final MediaCodec.BufferInfo bufferInfo) {//编码后的数据
        for (int i = 0, n = mMediaCodecCallbacks.size(); i < n; i++) {
            mMediaCodecCallbacks.get(i).onEncodedFrame(frame, bufferInfo);
        }
    }

    @Override
    public void onOutputFormatChanged(final MediaCodec codec, final MediaFormat format) {
        for (int i = 0, n = mMediaCodecCallbacks.size(); i < n; i++) {//编码格式有所变化
            mMediaCodecCallbacks.get(i).onOutputFormatChanged(codec, format);
        }
    }
}
