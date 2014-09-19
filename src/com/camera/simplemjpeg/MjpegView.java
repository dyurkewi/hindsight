package com.camera.simplemjpeg;

import java.io.IOException;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class MjpegView extends SurfaceView implements SurfaceHolder.Callback {
	public static final String TAG="MJPEG";
	
    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    ScaleGestureDetector mScaleDetector;
    
  //InteractionMode mode;
    private int mode;
    public final static int None = 0;
    public final static int Zoom   = 1; 
    public final static int Pan   = 2;
    
    
    Matrix mMatrix = new Matrix();
    static float mScaleFactor = 1.f;
    static float maxScaleFactor = 10.f;
    static float mTouchX;
    static float mTouchY;
    float mTouchBackupX;
    float mTouchBackupY;
    float mTouchDownX;
    float mTouchDownY;
    static Rect boundingBox = new Rect();

    
	public final static int POSITION_UPPER_LEFT  = 9;
    public final static int POSITION_UPPER_RIGHT = 3;
    public final static int POSITION_LOWER_LEFT  = 12;
    public final static int POSITION_LOWER_RIGHT = 6;

    public final static int SIZE_STANDARD   = 1; 
    public final static int SIZE_BEST_FIT   = 4;
    public final static int SIZE_FULLSCREEN = 8;
    public final static int SIZE_PANZOOM = 16;
    
    SurfaceHolder holder;
	Context saved_context;
    
    private MjpegViewThread thread;
    private MjpegInputStream mIn = null;    
    private boolean showFps = false;
    private boolean mRun = false;
    private boolean surfaceDone = false;    

    private Paint overlayPaint;
    private int overlayTextColor;
    private int overlayBackgroundColor;
    private int ovlPos;
    private int dispWidth;
    private int dispHeight;
    private static int displayMode;

	private boolean suspending = false;
	
	private Bitmap bmp = null;
	// hard-coded image size
	private static int imgWidth = 320;
	private static int imgHeight = 240;

    public class MjpegViewThread extends Thread {
        private SurfaceHolder mSurfaceHolder;
        private int frameCounter = 0;
        private long start;
        private String fps = "";

         
        public MjpegViewThread(SurfaceHolder surfaceHolder, Context context) { 
            mSurfaceHolder = surfaceHolder; 
        }
        
        public void setRunning(boolean b) {
        	mRun = b;
        }

        private Rect destRect(int bmw, int bmh) {
            int tempx;
            int tempy;
            MjpegView.setDisplayMode(SIZE_PANZOOM);
            if (displayMode == MjpegView.SIZE_PANZOOM) {

            	float bmasp = (float) bmw / (float) bmh;
                bmw = dispWidth;
                bmh = (int) (dispWidth / bmasp);
                if (bmh > dispHeight) {
                    bmh = dispHeight;
                    bmw = (int) (dispHeight * bmasp);
                }
                
            	bmw = (int)(bmw*mScaleFactor);
            	bmh = (int)(bmh*mScaleFactor);
            	tempx = (int)((dispWidth / 2) - (bmw / 2))+((int)mTouchX-dispWidth/2);
                tempy = (int)((dispHeight / 2) - (bmh / 2))+((int)mTouchY-dispHeight/2);
                return new Rect(tempx, tempy, bmw + tempx, bmh + tempy);
            }
            if (displayMode == MjpegView.SIZE_STANDARD) {
                tempx = (dispWidth / 2) - (bmw / 2);
                
                tempy = (dispHeight / 2) - (bmh / 2);
                
                return new Rect(tempx, tempy, bmw + tempx, bmh + tempy);
            }
            if (displayMode == MjpegView.SIZE_BEST_FIT) {
            	//increases to max while keeping proportions
            	//destRec = just fits around perimeter of image
                float bmasp = (float) bmw / (float) bmh;
                bmw = dispWidth;
                bmh = (int) (dispWidth / bmasp);
                if (bmh > dispHeight) {
                    bmh = dispHeight;
                    bmw = (int) (dispHeight * bmasp);
                }
                tempx = (dispWidth / 2) - (bmw / 2); //177
                tempy = (dispHeight / 2) - (bmh / 2);//0
                //Log.i(TAG,"BestFit");
                return new Rect(tempx, tempy, bmw + tempx, bmh + tempy);
            }
            if (displayMode == MjpegView.SIZE_FULLSCREEN){
            	//returns full screen regardless
                return new Rect(0, 0, dispWidth, dispHeight);
            }
            return null;
        }
         
        public void setSurfaceSize(int width, int height) {
            synchronized(mSurfaceHolder) {
                dispWidth = width;
                dispHeight = height;
            }
        }
        
        //Show the fps number using a new Canvas
        private Bitmap makeFpsOverlay(Paint p) {
            Rect b = new Rect();
            p.getTextBounds(fps, 0, fps.length(), b);

            // false indentation to fix forum layout             
            Bitmap bm = Bitmap.createBitmap(b.width(), b.height(), Bitmap.Config.ARGB_8888);

            Canvas c = new Canvas(bm);
            p.setColor(overlayBackgroundColor);
            c.drawRect(0, 0, b.width(), b.height(), p);
            p.setColor(overlayTextColor);
            c.drawText(fps, -b.left, b.bottom-b.top-p.descent(), p);
            return bm;        	 
        }

        public void run() {
            start = System.currentTimeMillis();
            PorterDuffXfermode mode = new PorterDuffXfermode(PorterDuff.Mode.DST_OVER);

            int width;
            int height;
            Paint p = new Paint();
            Bitmap ovl=null;
            
            while (mRun) {

                Rect destRect=null;
                Canvas c = null;

                if(surfaceDone) {   
                	try {
                		if(bmp==null){
                			//bmp = Bitmap.createBitmap(1440, 1080, Bitmap.Config.ARGB_8888);
                			bmp = Bitmap.createBitmap(imgWidth, imgHeight, Bitmap.Config.ARGB_8888);
                		}
                		mIn.readMjpegFrame(bmp);
                        destRect = destRect(bmp.getWidth(),bmp.getHeight());
                        
                        c = mSurfaceHolder.lockCanvas();
                        synchronized (mSurfaceHolder) {

                               	doDraw(c, destRect, p);
                               	
                                if(showFps) {
                                    p.setXfermode(mode);
                                    if(ovl != null) {

                                    	// false indentation to fix forum layout 	                                	 
                                    	height = ((ovlPos & 1) == 1) ? destRect.top : destRect.bottom-ovl.getHeight();
                                    	width  = ((ovlPos & 8) == 8) ? destRect.left : destRect.right -ovl.getWidth();

                                        c.drawBitmap(ovl, width, height, null);
                                    
                                    }
                                    p.setXfermode(null);
                                    frameCounter++;
                                    if((System.currentTimeMillis() - start) >= 1000) {
                                    	
                                    	fps = String.valueOf(frameCounter)+"fps";

                                        frameCounter = 0; 
                                        start = System.currentTimeMillis();
                                        if(ovl!=null) ovl.recycle();
                                    	
                                        ovl = makeFpsOverlay(overlayPaint);
                                    }
                                }
                                

                        }

                    }catch (IOException e){ 
                	
                }finally { 
                    	if (c != null) mSurfaceHolder.unlockCanvasAndPost(c); 
                    }
                }
            }
        }
    }
    
    private void doDraw(Canvas canvas, Rect destRect, Paint p) {
    	//Rect rect = new Rect(0,0,dispWidth,dispHeight);
    	canvas.drawColor(Color.BLACK);
    	//canvas.drawColor(Color.BLACK);
        //canvas.drawRect(rect, paint);

    	canvas.drawBitmap(bmp, null, destRect, p);
    }

    private void init(Context context) {
    	
        //SurfaceHolder holder = getHolder();
    	holder = getHolder();
    	saved_context = context;
        holder.addCallback(this);
        thread = new MjpegViewThread(holder, context);
        setFocusable(true);
        overlayPaint = new Paint();
        overlayPaint.setTextAlign(Paint.Align.LEFT);
        overlayPaint.setTextSize(12);
        overlayPaint.setTypeface(Typeface.DEFAULT);
        overlayTextColor = Color.WHITE;
        overlayBackgroundColor = Color.BLACK;
        ovlPos = MjpegView.POSITION_LOWER_RIGHT;
        displayMode = MjpegView.SIZE_STANDARD;
        dispWidth = getWidth();
        dispHeight = getHeight();
    }
    
    public void startPlayback() { 
        if(mIn != null) {
            mRun = true;
            thread.start();    		
        }
    }
    
    public void resumePlayback() { 
        if(suspending){
            if(mIn != null) {
                mRun = true;
                SurfaceHolder holder = getHolder();
                holder.addCallback(this);
                thread = new MjpegViewThread(holder, saved_context);		
                thread.start();
            }
        	suspending=false;
        }
    }
    public void stopPlayback() { 
    	if(mRun){
    		suspending = true;
    	}
        mRun = false;
        boolean retry = true;
        while(retry) {
            try {
                thread.join();
                retry = false;
            } catch (InterruptedException e) {}
        }
    }

    public static void setImageSize( int width, int height){
    	MjpegView.imgWidth = width;
    	MjpegView.imgHeight = height;
    }
    
    public void freeCameraMemory(){
    	if(mIn!=null){
    		mIn.freeCameraMemory();
    	}
    }
    
    public void surfaceChanged(SurfaceHolder holder, int f, int w, int h) {
        thread.setSurfaceSize(w, h); 
    }

    
    @Override
    public void surfaceCreated(SurfaceHolder arg0) {
        // otherwise onDraw(Canvas) won't be called
        this.setWillNotDraw(false); 
    	//thread.setRunning(true);
    	//thread.start();
        surfaceDone = true;
    }
    
    public void surfaceDestroyed(SurfaceHolder holder) { 
        surfaceDone = false; 
        stopPlayback(); 
        
        /*From LunarLander
        boolean retry = true;
        thread.setRunning(false);
        while (retry){
        	try{
        		thread.join();
        		retry = false;
        		
        	} catch(InterruptedException e){
        	}
        }*/
    }
    public MjpegView(Context context, AttributeSet attrs) { 
        super(context, attrs); init(context); 
        
        // we need to get a call for onSurfaceCreated
	    SurfaceHolder sh = this.getHolder();
	    sh.addCallback(this);
	
	    // for zooming (scaling) the view with two fingers
	    mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());	
	    boundingBox.set(88, 1, 1706, 1078);
	    //boundingBox.set(0,0,320,240);
	
	    paint.setColor(Color.GREEN);
	    paint.setStyle(Style.STROKE);
	
	    setFocusable(true);
	
	    // initial center/touch point of the view (otherwise the view would jump
	    // around on first pan/move touch
	    DisplayMetrics metrics = context.getResources().getDisplayMetrics();
	    mTouchX = metrics.widthPixels / 2;  //1794p/2=897
	    mTouchY = metrics.heightPixels / 2; //1080p/2=540
    }
    
    public MjpegView(Context context) { super(context); init(context); }

	@SuppressLint("ClickableViewAccessibility")
	@Override
    public boolean onTouchEvent(MotionEvent event) {
		mScaleDetector.onTouchEvent(event);
        if (!this.mScaleDetector.isInProgress()) {
            switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                // similar to ScaleListener.onScaleEnd (as long as we don't
                // handle indices of touch events)
                mode = MjpegView.None;
            case MotionEvent.ACTION_DOWN:
                Log.d(TAG, "Touch down event");

                mTouchDownX = event.getX();
                mTouchDownY = event.getY();
                mTouchBackupX = mTouchX;
                mTouchBackupY = mTouchY;

                // pan/move started
                mode = MjpegView.Pan;
                break;
            case MotionEvent.ACTION_MOVE:
                // make sure we don't handle the last move event when the first
                // finger is still down and the second finger is lifted up
                // already after a zoom/scale interaction. see
                // ScaleListener.onScaleEnd
                if (mode == MjpegView.Pan) {
                    Log.d(TAG, "Touch move event");

                    // get current location
                    final float x = event.getX();
                    final float y = event.getY();

                    // get distance vector from where the finger touched down to
                    // current location
                    final float diffX = x - mTouchDownX;
                    final float diffY = y - mTouchDownY;

                    mTouchX = mTouchBackupX + diffX;
                    mTouchY = mTouchBackupY + diffY;

                    CalculateMatrix(true);
                }

                break;
            }
        }

        return true;
    }
    
    @Override
    public void onDraw(Canvas canvas) {
        int saveCount = canvas.getSaveCount();
        canvas.save();
        canvas.concat(mMatrix);
        //canvas.drawColor(Color.BLACK);
        //canvas.drawRect(boundingBox, paint);
        canvas.restoreToCount(saveCount);
    }
    
    void CalculateMatrix(boolean invalidate) {
        float sizeX = this.getWidth() / 2;
        float sizeY = this.getHeight() / 2;

        mMatrix.reset();

        // move the view so that it's center point is located in 0,0
        mMatrix.postTranslate(-sizeX, -sizeY);

        // scale the view
        mMatrix.postScale(mScaleFactor, mScaleFactor);
        Log.i(TAG, "Scale Factor: "+ mScaleFactor);
        // re-move the view to it's desired location
        mMatrix.postTranslate(mTouchX, mTouchY);
		Log.i(TAG, mTouchX+ " ");
		Log.i(TAG, mTouchY+ " ");

        if (invalidate)
            invalidate(); // re-draw
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        float mFocusStartX;
        float mFocusStartY;
        float mZoomBackupX;
        float mZoomBackupY;

        public ScaleListener() {
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {

            mode = MjpegView.Zoom;

            mFocusStartX = detector.getFocusX();
            mFocusStartY = detector.getFocusY();
            mZoomBackupX = mTouchX;
            mZoomBackupY = mTouchY;

            return super.onScaleBegin(detector);
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {

            mode = MjpegView.None;

            super.onScaleEnd(detector);
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {

            if (mode != MjpegView.Zoom)
                return true;

            Log.d(TAG, "Touch scale event");

            // get current scale and fix its value
            float scale = detector.getScaleFactor();
            mScaleFactor *= scale;
            mScaleFactor = Math.max(1f, Math.min(mScaleFactor, maxScaleFactor));

            // get current focal point between both fingers (changes due to
            // movement)
            float focusX = detector.getFocusX();
            float focusY = detector.getFocusY();

            // get distance vector from initial event (onScaleBegin) to current
            float diffX = focusX - mFocusStartX;
            float diffY = focusY - mFocusStartY;

            // scale the distance vector accordingly
            diffX *= scale;
            diffY *= scale;

            // set new touch position
            mTouchX = mZoomBackupX + diffX;
            mTouchY = mZoomBackupY + diffY;

            CalculateMatrix(true);

            return true;
        }

    }


    
    
    //Original Methods Below, new functions above
    //public void surfaceCreated(SurfaceHolder holder) { surfaceDone = true; }
    public void showFps(boolean b) { showFps = b; }
    public void setSource(MjpegInputStream source) {
    	mIn = source; 
    	startPlayback();
    }
    public void setOverlayPaint(Paint p) { overlayPaint = p; }
    public void setOverlayTextColor(int c) { overlayTextColor = c; }
    public void setOverlayBackgroundColor(int c) { overlayBackgroundColor = c; }
    public void setOverlayPosition(int p) { ovlPos = p; }
    public static void setDisplayMode(int s) { displayMode = s; }
}
