package com.dwayne.com.pusher;


public abstract class BaseChannel {



    public abstract void startLive();

    public  abstract void stopLive();

    public  abstract void release();

    public abstract void onRestart();
}
