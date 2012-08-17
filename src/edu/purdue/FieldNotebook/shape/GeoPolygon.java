package edu.purdue.FieldNotebook.shape;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import android.graphics.Point;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.Projection;

public class GeoPolygon implements Iterable<GeoPoint> {

	private GeoPoint points[] = {};
	private Object accessLock = new Object();
	
	public GeoPolygon() {
	}
	
	public GeoPolygon(ScreenPolygon polygon, Projection projection) {
		addScreenPolygon(polygon, projection);
	}
	
	public GeoPolygon(int lat[], int lon[]) {
		if(lat.length == lon.length && lat.length > 0) {
			GeoPoint[] points = new GeoPoint[lat.length];
			
			for(int i = 0; i < lat.length; i++) {
				points[i] = new GeoPoint(lat[i], lon[i]);
			}
			
			addPoints(points);
		}
	}
	
	public void addScreenPolygon(ScreenPolygon polygon, Projection projection) {
		for(Point p : polygon) {
			addPoint(projection.fromPixels(p.x, p.y));
		}
	}
	
	public void addPoint(GeoPoint p) {
		synchronized(accessLock) {
			this.points = Arrays.copyOf(this.points, this.points.length+1);
			this.points[this.points.length-1] = p;
		}
	}
	
	public void addPoint(int lat, int lon) {
		addPoint(new GeoPoint(lat, lon));
	}
	
	public void addPoints(GeoPoint[] points) {
		if(points.length > 0) {
			synchronized(accessLock) {
					// Resize this.x and copy the passed in x point the new spots in this.x
					this.points = Arrays.copyOf(this.points, this.points.length + points.length);
					System.arraycopy(points, 0, this.points, this.points.length, points.length);
			}
		}
	}
	
	public GeoPoint get(int i) {
		GeoPoint p = null;
		if(i >= 0 && i < points.length) {
			p = points[i];
		}
		
		return p;
	}
	
	public ArrayList<GeoPoint> getPoints() {
		ArrayList<GeoPoint> p =  new ArrayList<GeoPoint>();
		
		for(int i = 0; i < points.length; i++) {
			p.add(points[i]);
		}
		
		return p;
	}
	
	public GeoPoint getLast() {
		return get(points.length-1);
	}
	
	/*
	public int getArea() {
		int area = 0;
		
		synchronized(accessLock) {
			if(x.length == y.length && x.length > 0) {
				int i;
				for(i = 0; i < x.length-1; i++) {
					area += (x[i] + x[i+1])*(y[i] - y[i+1]);
				}
			
				area += (x[i+1] + x[0])*(y[i+1] - y[0]);
			}
		}
		
		return area/2;
	}
	*/
	public Iterator<GeoPoint> iterator() {
		return new ScreenPolygonIterator();
	}
	
	class ScreenPolygonIterator implements Iterator<GeoPoint> {
		int current = 0;
		
		public boolean hasNext() {
			if(current < points.length)
				return true;
			else
				return false;
		}

		public GeoPoint next() {
			return points[current++];
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
}
