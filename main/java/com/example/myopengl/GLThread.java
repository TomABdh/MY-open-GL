package com.example.myopengl;

import android.app.Activity;
import android.content.Context;
import android.opengl.GLU;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGL11;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import javax.microedition.khronos.opengles.GL10;

public class GLThread extends Thread{
    private final GLView view;
    private boolean done = false;
    private final  GLCube cube = new GLCube();
    private long startTime;

    GLThread(GLView view) {
        this.view = view;
    }
    @Override
    public void run(){
        //Initialize OPenGL
        EGL10 egl = (EGL10) EGLContext.getEGL();
        EGLDisplay display = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);

        int[] version = new int[2];
        egl.eglInitialize(display,version);

        int[] configSpec = {EGL10.EGL_ALPHA_SIZE,5,EGL10.EGL_GREEN_SIZE,6,EGL10.EGL_BLUE_SIZE,5,EGL10.EGL_DEPTH_SIZE,16,EGL10.EGL_NONE};

        EGLConfig[] configs = new EGLConfig[1];
        int[] numConfig = new int[1];
        egl.eglChooseConfig(display,configSpec,configs,1,numConfig);
        EGLConfig config = configs[0];
        EGLContext glc = egl.eglCreateContext(display,config,EGL10.EGL_NO_CONTEXT,null);
        EGLSurface surface = egl.eglCreateWindowSurface(display,config,view.getHolder(),null);
        egl.eglMakeCurrent(display,surface,surface,glc);

        GL10 gl = (GL10) (glc.getGL());
        init(gl);
        //Loop until asked to quit
        while(!done){
            //Draw a single frame here....
            drawFrame(gl);
            egl.eglSwapBuffers(display,surface);

            //Error handling
            if(egl.eglGetError() == EGL11.EGL_CONTEXT_LOST){
                Context c = view.getContext();
                if(c instanceof Activity){
                    ((Activity)c).finish();
                }
            }
        }

        //Free OpenGL resources
        egl.eglMakeCurrent(display,EGL10.EGL_NO_SURFACE,EGL10.EGL_NO_SURFACE,EGL10.EGL_NO_CONTEXT);
        egl.eglDestroySurface(display,surface);
        egl.eglDestroyContext(display,glc);
        egl.eglTerminate(display);
    }

    public void requestExitAndWait(){
        //Tell the thread to quit
        done = true;
        try{
            join();
        }catch (InterruptedException ex){
            //Ignore
        }
    }
    private void init(GL10 gl){
        startTime = System.currentTimeMillis();
        //Define the view frustrum
        gl.glViewport(0,0,view.getWidth(),view.getHeight());
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();
        float ratio = (float)view.getWidth() / view.getHeight();
        GLU.gluPerspective(gl,45.0f,ratio,1,100f);

        //Set up any other options we need
        gl.glEnable(GL10.GL_DEPTH_TEST);
        gl.glDepthFunc(GL10.GL_LEQUAL);
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        //Optional : disable dither to boost performance
        //gl.glDisable(GL10.GL_DITHER);

        // Define the lighting
        float lightAmbient[] = new float[]{0.2f,0.2f,0.2f,1};
        float lightDiffuse[] = new float[]{1,1,1,1};
        float[] lightPos = new float[]{1,1,1,1};
        gl.glEnable(GL10.GL_LIGHTING);
        gl.glEnable(GL10.GL_LIGHT0);
        gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_AMBIENT,lightAmbient,0);
        gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_DIFFUSE,lightDiffuse,0);
        gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_POSITION,lightPos,0);

        // What is the cube made of ?
        float matAmbient[] = new float[]{ 1,1,1,1};
        float matDiffuse[] = new float[]{ 1,1,1,1};
        gl.glMaterialfv(GL10.GL_FRONT_AND_BACK,GL10.GL_AMBIENT,matAmbient,0);
        gl.glMaterialfv(GL10.GL_FRONT_AND_BACK,GL10.GL_DIFFUSE,matDiffuse,0);

        gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
        gl.glEnable(GL10.GL_TEXTURE_2D);
        GLCube.loadTexture(gl,view.getContext(),R.drawable.hhh);

        boolean SEE_THRU = true;
        //....
        if(SEE_THRU){
            gl.glDisable(GL10.GL_DEPTH_TEST);
            gl.glEnable(GL10.GL_BLEND);
            gl.glBlendFunc(GL10.GL_SRC_ALPHA,GL10.GL_ONE);
        }


    }
    private void drawFrame(GL10 gl){
        //Clear the screen to black
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
        //Position model so we can see it
        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();
        gl.glTranslatef(0,0,-3.0f);

        // Other drawing commands go here

        //...Draw the model
        cube.draw(gl);
        //...Set rotation angle based on the time
        long elapsed = System.currentTimeMillis() - startTime;
        gl.glRotatef(elapsed * (30f / 1000f),0,1,1);
        gl.glRotatef(elapsed * (15f / 1000f),1,0,1);

    }
}
