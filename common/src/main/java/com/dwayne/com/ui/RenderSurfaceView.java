package com.dwayne.com.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.util.AttributeSet;
import android.view.SurfaceHolder;

import com.dwayne.com.camera.CameraHolder;
import com.dwayne.com.constant.SopCastConstant;
import com.dwayne.com.utils.SopCastLog;
import com.dwayne.com.video.MyRenderer;
import com.dwayne.com.video.effect.Effect;


public class RenderSurfaceView extends GLSurfaceView {
    private MyRenderer mRenderer;

    public RenderSurfaceView(Context context) {
        super(context);
        init();
    }

    public RenderSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mRenderer = new MyRenderer(this);
        setEGLContextClientVersion(2);
        setRenderer(mRenderer);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        SurfaceHolder surfaceHolder = getHolder();
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceHolder.addCallback(mSurfaceHolderCallback);
    }

    public MyRenderer getRenderer() {
        return mRenderer;
    }

    private SurfaceHolder.Callback mSurfaceHolderCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            SopCastLog.d(SopCastConstant.TAG, "SurfaceView destroy");
            CameraHolder.instance().stopPreview();
            CameraHolder.instance().releaseCamera();
        }

        @TargetApi(Build.VERSION_CODES.GINGERBREAD)
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            SopCastLog.d(SopCastConstant.TAG, "SurfaceView created");
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            SopCastLog.d(SopCastConstant.TAG, "SurfaceView width:" + width + " height:" + height);
        }
    };

    public void setEffect(final Effect effect) {
        this.queueEvent(new Runnable() {
            @Override
            public void run() {
                if (null != mRenderer) {
                    mRenderer.setEffect(effect);
                }
            }
        });
    }
}
