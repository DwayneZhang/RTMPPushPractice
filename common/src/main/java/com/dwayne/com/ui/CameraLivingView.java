package com.dwayne.com.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.os.Build;
import android.os.PowerManager;
import android.util.AttributeSet;

import com.dwayne.com.audio.AudioUtils;
import com.dwayne.com.camera.CameraData;
import com.dwayne.com.camera.CameraHolder;
import com.dwayne.com.camera.CameraListener;
import com.dwayne.com.capture.CameraCapture;
import com.dwayne.com.configuration.AudioConfiguration;
import com.dwayne.com.configuration.CameraConfiguration;
import com.dwayne.com.configuration.VideoConfiguration;
import com.dwayne.com.constant.SopCastConstant;
import com.dwayne.com.controller.StreamController;
import com.dwayne.com.controller.audio.NormalAudioController;
import com.dwayne.com.controller.video.CameraVideoController;
import com.dwayne.com.entity.Watermark;
import com.dwayne.com.mediacodec.AudioMediaCodec;
import com.dwayne.com.mediacodec.MediaCodecHelper;
import com.dwayne.com.mediacodec.VideoMediaCodec;
import com.dwayne.com.stream.packer.Packer;
import com.dwayne.com.stream.sender.Sender;
import com.dwayne.com.stream.sender.nativertmp.RtmpNativeSendr;
import com.dwayne.com.utils.SopCastLog;
import com.dwayne.com.utils.SopCastUtils;
import com.dwayne.com.utils.WeakHandler;
import com.dwayne.com.video.effect.Effect;


public class CameraLivingView extends CameraView {
    public static final int NO_ERROR = 0;
    public static final int VIDEO_TYPE_ERROR = 1;
    public static final int AUDIO_TYPE_ERROR = 2;
    public static final int VIDEO_CONFIGURATION_ERROR = 3;
    public static final int AUDIO_CONFIGURATION_ERROR = 4;
    public static final int CAMERA_ERROR = 5;
    public static final int AUDIO_ERROR = 6;
    public static final int AUDIO_AEC_ERROR = 7;
    public static final int SDK_VERSION_ERROR = 8;

    private static final String TAG = SopCastConstant.TAG;
    private StreamController mStreamController;
    private Context mContext;
    private PowerManager.WakeLock mWakeLock;
    private VideoConfiguration mVideoConfiguration = VideoConfiguration.createDefault();
    private AudioConfiguration mAudioConfiguration = AudioConfiguration.createDefault();
    private CameraListener mOutCameraOpenListener;
    private LivingStartListener mLivingStartListener;
    private WeakHandler mHandler = new WeakHandler();

    public interface LivingStartListener {
        void startError(int error);

        void startSuccess();
    }

    public CameraLivingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
        mContext = context;
    }

    public CameraLivingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
        mContext = context;
    }

    public CameraLivingView(Context context) {
        super(context);
        initView();
        mContext = context;
    }

    private void initView() {
        CameraVideoController videoController = new CameraVideoController(mRenderer);
        NormalAudioController audioController = new NormalAudioController();
        mStreamController = new StreamController(videoController, audioController);
        mRenderer.setCameraOpenListener(mCameraOpenListener);
    }

    @SuppressLint("InvalidWakeLockTag")
    public void init() {
        SopCastLog.d(TAG, "Version : " + SopCastConstant.VERSION);
        SopCastLog.d(TAG, "Branch : " + SopCastConstant.BRANCH);

        PowerManager mPowerManager = ((PowerManager) mContext.getSystemService(getContext().POWER_SERVICE));
        mWakeLock = mPowerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK |
                PowerManager.ON_AFTER_RELEASE, TAG);
    }

    public void setLivingStartListener(LivingStartListener listener) {
        mLivingStartListener = listener;
    }

    public void setPacker(Packer packer) {
        mStreamController.setPacker(packer);
    }

    public void setSender(Sender sender) {
        mStreamController.setSender(sender);
        if (sender instanceof RtmpNativeSendr) {
            RtmpNativeSendr rtmpNativeSendr = (RtmpNativeSendr) sender;
            rtmpNativeSendr.setMediaCodec(true);
        }

    }

    public void setVideoConfiguration(VideoConfiguration videoConfiguration) {
        mVideoConfiguration = videoConfiguration;
        mStreamController.setVideoConfiguration(videoConfiguration);
    }

    public void setCameraConfiguration(CameraConfiguration cameraConfiguration) {
        CameraHolder.instance().setConfiguration(cameraConfiguration);
    }

    public void setAudioConfiguration(AudioConfiguration audioConfiguration) {
        mAudioConfiguration = audioConfiguration;
        mStreamController.setAudioConfiguration(audioConfiguration);
    }

    private int check() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
            SopCastLog.w(TAG, "Android sdk version error");
            return SDK_VERSION_ERROR;
        }
        if (!checkAec()) {
            SopCastLog.w(TAG, "Doesn't support audio aec");
            return AUDIO_AEC_ERROR;
        }
        if (!isCameraOpen()) {
            SopCastLog.w(TAG, "The camera have not open");
            return CAMERA_ERROR;
        }
        MediaCodecInfo videoMediaCodecInfo = MediaCodecHelper.selectCodec(mVideoConfiguration.mime);
        if (videoMediaCodecInfo == null) {
            SopCastLog.w(TAG, "Video type error");
            return VIDEO_TYPE_ERROR;
        }
        MediaCodecInfo audioMediaCodecInfo = MediaCodecHelper.selectCodec(mAudioConfiguration.mime);
        if (audioMediaCodecInfo == null) {
            SopCastLog.w(TAG, "Audio type error");
            return AUDIO_TYPE_ERROR;
        }
        MediaCodec videoMediaCodec = VideoMediaCodec.getVideoMediaCodec(mVideoConfiguration);
        if (videoMediaCodec == null) {
            SopCastLog.w(TAG, "Video mediacodec configuration error");
            return VIDEO_CONFIGURATION_ERROR;
        }
        MediaCodec audioMediaCodec = AudioMediaCodec.getAudioMediaCodec(mAudioConfiguration);
        if (audioMediaCodec == null) {
            SopCastLog.w(TAG, "Audio mediacodec configuration error");
            return AUDIO_CONFIGURATION_ERROR;
        }
        if (!AudioUtils.checkMicSupport(mAudioConfiguration)) {
            SopCastLog.w(TAG, "Can not record the audio");
            return AUDIO_ERROR;
        }
        return NO_ERROR;
    }

    private boolean checkAec() {
        if (mAudioConfiguration.aec) {
            if (mAudioConfiguration.frequency == 8000 ||
                    mAudioConfiguration.frequency == 16000) {
                if (mAudioConfiguration.channelCount == 1) {
                    return true;
                }
            }
            return false;
        } else {
            return true;
        }
    }

    public void start() {
        SopCastUtils.processNotUI(new SopCastUtils.INotUIProcessor() {
            @Override
            public void process() {
                final int result = check();
                if (result == NO_ERROR) {
                    if (mLivingStartListener != null) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mLivingStartListener.startSuccess();
                            }
                        });
                    }
                    chooseVoiceMode();
                    screenOn();
                    mStreamController.start();
                } else {
                    if (mLivingStartListener != null) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mLivingStartListener.startError(result);
                            }
                        });
                    }
                }
            }
        });
    }

    private void chooseVoiceMode() {
        AudioManager audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        if (mAudioConfiguration.aec) {
            audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
            audioManager.setSpeakerphoneOn(true);
        } else {
            audioManager.setMode(AudioManager.MODE_NORMAL);
            audioManager.setSpeakerphoneOn(false);
        }
    }

    public void stop() {
        screenOff();
        mStreamController.stop();
        setAudioNormal();
    }

    private void screenOn() {
        if (mWakeLock != null) {
            if (!mWakeLock.isHeld()) {
                mWakeLock.acquire();
            }
        }
    }

    private void screenOff() {
        if (mWakeLock != null) {
            if (mWakeLock.isHeld()) {
                mWakeLock.release();
            }
        }
    }

    public void pause() {
        mStreamController.pause();
    }

    public void resume() {
        mStreamController.resume();
    }

    public void mute(boolean mute) {
        mStreamController.mute(mute);
    }

    public int getSessionId() {
        return mStreamController.getSessionId();
    }

    public void setEffect(Effect effect) {
        mRenderSurfaceView.setEffect(effect);
    }

    public void setWatermark(Watermark watermark) {
        mRenderer.setWatermark(watermark);
    }

    public boolean setVideoBps(int bps) {
        return mStreamController.setVideoBps(bps);
    }

    private boolean isCameraOpen() {
        return mRenderer.isCameraOpen();
    }

    public void setCameraOpenListener(CameraListener cameraOpenListener) {
        mOutCameraOpenListener = cameraOpenListener;
    }

    public void switchCamera() {
        boolean change = CameraHolder.instance().switchCamera();
        if (change) {
            changeFocusModeUI();
            if (mOutCameraOpenListener != null) {
                mOutCameraOpenListener.onCameraChange();
            }
        }
    }

    public CameraData getCameraData() {
        return CameraHolder.instance().getCameraData();
    }

    public void switchFocusMode() {
        CameraHolder.instance().switchFocusMode();
        changeFocusModeUI();
    }

    public void switchTorch() {
        CameraHolder.instance().switchLight();
    }

    public void release() {
        screenOff();
        mWakeLock = null;
        CameraHolder.instance().releaseCamera();
        CameraHolder.instance().release();
        setAudioNormal();
    }

    private CameraListener mCameraOpenListener = new CameraListener() {
        @Override
        public void onOpenSuccess() {
            changeFocusModeUI();
            if (mOutCameraOpenListener != null) {
                mOutCameraOpenListener.onOpenSuccess();
            }
        }

        @Override
        public void onOpenFail(int error) {
            if (mOutCameraOpenListener != null) {
                mOutCameraOpenListener.onOpenFail(error);
            }
        }

        @Override
        public void onCameraChange() {
            // Won't Happen
        }
    };

    private void setAudioNormal() {
        AudioManager audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setMode(AudioManager.MODE_NORMAL);
        audioManager.setSpeakerphoneOn(false);
    }


    /**
     * 设置当屏幕发生改变需要设置的监听
     *
     * @param listener
     */
    public void setOnChangedSizeListener(CameraCapture.OnChangedSizeListener listener) {
        CameraHolder.instance().setOnChangedSizeListener(listener);
    }

    /**
     * 设置预览回调，用于软编
     *
     * @param previewCallback
     */
    public void setPreviewCallback(Camera.PreviewCallback previewCallback) {
        CameraHolder.instance().setPreviewCallback(previewCallback);

    }

    /**
     * 设置预览方向
     */
    public void setPreviewOrientation(int rotation) {
        CameraHolder.instance().setPreviewOrientation(rotation);
    }
}
