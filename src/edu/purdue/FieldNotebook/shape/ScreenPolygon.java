package edu.purdue.FieldNotebook.shape;

import java.util.Arrays;
import java.util.Iterator;

import android.graphics.Point;

public class ScreenPolygon implements Iterable<Point> {
 
	private int x[];
	private int y[];
	private Object accessLock = new Object();
	
	public ScreenPolygon() {
	}
	
	public ScreenPolygon(int x[], int y[]) {
		addPoints(x, y);
	}
	
	public void addPoint(int x, int y) {
		synchronized(accessLock) {
			this.x = Arrays.copyOf(this.x, this.x.length+1);
			this.x[this.x.length-1] = x;
			this.y = Arrays.copyOf(this.y, this.y.length+1);
			this.y[this.y.length-1] = y;
		}
	}
	
	public void addPoint(Point p) {
		addPoint(p.x, p.y);
	}
	
	public void addPoints(int x[], int y[]) {
		if(x.length == y.length && x.length > 0) {
			synchronized(accessLock) {
				// Resize this.x and copy the passed in x point the new spots in this.x
				this.x = Arrays.copyOf(this.x, this.x.length + x.length);
				System.arraycopy(x, 0, this.x, this.x.length, x.length);
				
				// Resize this.y and copy the passed in y point the new spots in this.y
				this.y = Arrays.copyOf(this.y, this.y.length + y.length);
				System.arraycopy(y, 0, this.y, this.y.length, y.length);
			}
		}
	}
	
	public void clear() {
		synchronized(accessLock) {
			x = new int[]{};
			y = new int[]{};
		}
	}
	
	public Point get(int i) {
		Point p = null;
		if(i >= 0 && i < x.length) {
			p = new Point(x[i], y[i]);
		}
		
		return p;
	}
	
	public Point getLast() {
		return get(x.length-1);
	}
	
	public void close() {
		addPoint(x[0], y[0]);
	}
	
	public boolean isEmpty() {
		return x.length == 0; 
	}
	
	public int size() {
		return x.length;
	}

	public Iterator<Point> iterator() {
		return new ScreenPolygonIterator();
	}
	
	class ScreenPolygonIterator implements Iterator<Point> {
		int current = 0;
		
		public boolean hasNext() {
			if(current < x.length)
				return true;
			else
				return false;
		}

		public Point next() {
			Point p = new Point(x[current], y[current]);
			current++;
			return p;
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
}
