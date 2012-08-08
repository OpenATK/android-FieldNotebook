package edu.purdue.FieldNotebook.view;

import java.util.ArrayList;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.drawable.Drawable;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.google.android.maps.Projection;

import edu.purdue.FieldNotebook.shape.GeoPolygon;

public class PolygonOverlay extends ItemizedOverlay<OverlayItem> {
	private ArrayList<OverlayItem> overlays;
	private ArrayList<GeoPolygon> polygons;
	private Paint paint;
	private Paint paintLine;
	
	public PolygonOverlay(Drawable drawable) {
		super(drawable);
		
		overlays = new ArrayList<OverlayItem>();
		polygons = new ArrayList<GeoPolygon>();
		
		paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setColor(Color.YELLOW);
		paint.setStyle(Paint.Style.FILL);
		paint.setStrokeWidth(3);
		paint.setStrokeCap(Paint.Cap.ROUND);
		paint.setStrokeJoin(Paint.Join.MITER);
		paint.setAlpha(75);
		
		paintLine = new Paint(Paint.ANTI_ALIAS_FLAG);
		paintLine.setColor(Color.GRAY);
		paintLine.setStyle(Paint.Style.STROKE);
		paintLine.setStrokeWidth(3);
		paintLine.setStrokeCap(Paint.Cap.ROUND);
		paintLine.setStrokeJoin(Paint.Join.MITER);
		
		// Known work around to Android ArrayIndexOutOfBounds exception when
		// list is empty but added to a MapView
		this.populate();
	}
	
	
	/* This is called by Android after a call to populate. It is asking 
	 * for each OverlayItem individually to draw them */
	@Override
	protected OverlayItem createItem(int i) {
		return overlays.get(i);
	}
	
	/* This is called by Android to get the size of the overlay list
	 * so that it can safely call createItem() */
	@Override
	public int size() {
		return overlays.size();
	}
	
	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		if(!shadow) {
			for(GeoPolygon polygon : polygons ) {
			
				Projection projection = mapView.getProjection();
				
				Point point = projection.toPixels(polygon.get(0), null);
				
				Path path = new Path();
				path.moveTo((float)point.x, (float)point.y);
				
				for(GeoPoint p : polygon) {
					projection.toPixels(p, point);
					
					path.lineTo((float)point.x, (float)point.y);
				}
				
				path.close();
				
				canvas.drawPath(path, paint);
				canvas.drawPath(path, paintLine);
			}
		}
	}
	
	public void addPolygon(GeoPolygon polygon) {
		polygons.add(polygon);
		overlays.add(new OverlayItem(polygon.get(0), "Polygon", "Polygon"));
		
		// Known work around to Android ArrayIndexOutOfBounds exception when
		// list is empty but added to a MapView
		this.setLastFocusedIndex(-1);
		
		// Update the display after changing the list
		
		this.populate();
	}
}
