package com.dwayne.com.video.effect;

import android.content.Context;

import com.dwayne.com.video.GLSLFileUtils;


public class NullEffect extends Effect{
    private static final String NULL_EFFECT_VERTEX = "null/vertexshader.glsl";
    private static final String NULL_EFFECT_FRAGMENT = "null/fragmentshader.glsl";

    public NullEffect(Context context) {
        super();
        String vertexShader = GLSLFileUtils.getFileContextFromAssets(context, NULL_EFFECT_VERTEX);
        String fragmentShader = GLSLFileUtils.getFileContextFromAssets(context, NULL_EFFECT_FRAGMENT);
        super.setShader(vertexShader, fragmentShader);
    }
}
