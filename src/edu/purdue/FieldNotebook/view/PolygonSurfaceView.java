package edu.purdue.FieldNotebook.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import edu.purdue.FieldNotebook.shape.ScreenPolygon;
import edu.purdue.libwaterapps.note.Object;

public class PolygonSurfaceView extends SurfaceView implements Runnable {
	private boolean running = false;
	private Thread thread = null;
	private SurfaceHolder surfaceHolder;
	private Paint paintFixed;
	private Paint paintTemp;
	private Object pointLock = new Object();
	private ScreenPolygon polygon = new ScreenPolygon();
	private Point possNextPoint = null;
	private int type;
	
	public PolygonSurfaceView(Context context) {
		super(context);
		setupPolygonSurfaceView(context);
	}
	
	public PolygonSurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setupPolygonSurfaceView(context);
	}

	public PolygonSurfaceView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setupPolygonSurfaceView(context);
	}
	
	private void setupPolygonSurfaceView(Context context) {
		// Get the surface's holder
		surfaceHolder = getHolder();
		
		// Make sure the surface is top most so transparency will work
		setZOrderOnTop(true);
		// Set the surface format to Translucent to background view can shin through
		surfaceHolder.setFormat(PixelFormat.TRANSLUCENT);
		
		// Setup a paint to use for the set polygon lines
		paintFixed = new Paint(Paint.ANTI_ALIAS_FLAG);
		paintFixed.setColor(Color.YELLOW);
		paintFixed.setStyle(Paint.Style.STROKE);
		paintFixed.setStrokeWidth(3);
		paintFixed.setStrokeCap(Paint.Cap.ROUND);
		paintFixed.setStrokeJoin(Paint.Join.MITER);
		paintFixed.setAlpha(75);
		
		// Setup a paint to use for the possible next line
		paintTemp = new Paint(Paint.ANTI_ALIAS_FLAG);
		paintTemp.setColor(Color.YELLOW);
		paintTemp.setStyle(Paint.Style.STROKE);
		paintTemp.setPathEffect(new DashPathEffect(new float[] {10, 10}, 0));
		paintTemp.setStrokeWidth(3);
		paintTemp.setStrokeCap(Paint.Cap.ROUND);
		paintTemp.setStrokeJoin(Paint.Join.MITER);
	}
	
	public boolean isRunning() {
		return running;
	}
	
	// Start the thread that updates the drawing
	public void startDrawing(int color, int type) {
		paintFixed.setColor(color);
		paintFixed.setAlpha(175);
		paintTemp.setColor(color);
		
		this.type = type;
		
		// Clear any old points
		polygon.clear();
		
		thread = new Thread(this);
		
		running = true;
		thread.start();
	}
	
	// Stop the thread that is updating the drawing
	public ScreenPolygon stopDrawing() {
		boolean isDead = false;
		
		running = false; 
		while(!isDead) {
			try {
				thread.join();
				isDead = true;
			} catch(InterruptedException e) {
				/* Do nothing */
			}
		}
		
		// Clear the polygon from the surface
		Canvas canvas = surfaceHolder.lockCanvas();
		canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
		surfaceHolder.unlockCanvasAndPost(canvas);
		
		//polygons are assumed closed
		//polygon.close();
		
		return polygon;
	}
	
	private void updatePossNextPoint(Point p) {
		synchronized (pointLock) {
			possNextPoint = p;
		}
	}
	

	public void run() {
		while(running) {
			Canvas canvas = surfaceHolder.lockCanvas();
			
			canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
			
			if(!polygon.isEmpty()) {
				Path path = new Path();
				
				path.moveTo(polygon.get(0).x, polygon.get(0).y);
				for(Point p : polygon) {
					path.lineTo(p.x, p.y);
					paintFixed.setStyle(Paint.Style.FILL_AND_STROKE);
					canvas.drawCircle(p.x, p.y, 6, paintFixed);
					paintFixed.setStyle(Paint.Style.STROKE);
				}
				
				if(type == Object.TYPE_LINE || type == Object.TYPE_POLYGON) {
					canvas.drawPath(path, paintFixed);
				}
				
				if(type == Object.TYPE_POLYGON && polygon.size() >= 3) {
					paintFixed.setStyle(Paint.Style.FILL);
					canvas.drawPath(path, paintFixed);
					paintFixed.setStyle(Paint.Style.STROKE);
				}
				
				if(possNextPoint != null && (type == Object.TYPE_POLYGON || type == Object.TYPE_LINE)) {
					path = new Path();
					
					path.moveTo(polygon.getLast().x, polygon.getLast().y);
					
					synchronized(pointLock) {
						path.lineTo(possNextPoint.x, possNextPoint.y);
					}
					
					canvas.drawPath(path, paintTemp);
				}
			}
			
			surfaceHolder.unlockCanvasAndPost(canvas);
		}
		
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		boolean handledEvent = false;
		
		if(running) {
			Point p = new Point((int)event.getX(), (int)event.getY());
			
			switch(event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					if(polygon.isEmpty()) {
						polygon.addPoint(p);
					} else {
						updatePossNextPoint(p);
					}
					
					handledEvent = true;
				
				break;
				
				case MotionEvent.ACTION_MOVE:
					updatePossNextPoint(p);
					
					handledEvent = true;
				break;
				
				case MotionEvent.ACTION_UP:
					updatePossNextPoint(null);
					
					polygon.addPoint(p);
					
					handledEvent = true;
				break;
				
				case MotionEvent.ACTION_CANCEL:
					updatePossNextPoint(null);
					
					handledEvent = true;
				break;
			}
		}
		
		return handledEvent;
	}
}
