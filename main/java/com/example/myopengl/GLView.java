package com.example.myopengl;

import android.content.Context;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

public class GLView extends SurfaceView implements SurfaceHolder.Callback {
    private GLThread glThread;
    GLView(Context context){
        super(context);

        //Install a SurfaceHolder.Callback so we get notified when
        // the underlying surface is crated and destroyed
        getHolder().addCallback(this);

        //Use hardware acceleration if available
        getHolder().setType(SurfaceHolder.SURFACE_TYPE_GPU);
    }
    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        // The Surface has been created so start our drawing thread
        glThread = new GLThread(this);
        glThread.start();

    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        //TODO : handle window size changes

    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        // Stop our drawing thread . The Surface will be destroyed
        // when we return
        glThread.requestExitAndWait();
        glThread = null;

    }
}
