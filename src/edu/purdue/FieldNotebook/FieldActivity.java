package edu.purdue.FieldNotebook;

import java.util.ArrayList;

import android.os.Bundle;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MapView.ReticleDrawMode;
import com.google.android.maps.MyLocationOverlay;

import edu.purdue.FieldNotebook.shape.GeoPolygon;
import edu.purdue.FieldNotebook.shape.ScreenPolygon;
import edu.purdue.FieldNotebook.view.NotesScrollView;
import edu.purdue.FieldNotebook.view.PolygonOverlay;
import edu.purdue.FieldNotebook.view.PolygonSurfaceView;
import edu.purdue.libwaterapps.note.Note;
import edu.purdue.libwaterapps.note.Object;
import edu.purdue.libwaterapps.view.maps.RockMapOverlay;

public class FieldActivity extends MapActivity {
	
	private MapView mMapView;
	private MapController mMapController;
	private MyLocationOverlay mMyLocationOverlay;
	private PolygonOverlay mPolygonOverlay;
	private NotesScrollView mNotesScrollView;
	private PolygonSurfaceView mPolygonView;
	private RockMapOverlay mRockOverlay;
	private int lastType;
	private int lastColor;
	
	static final int SPAN_LAT = 3000;
	static final int SPAN_LONG = 3000;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Give the activity a view
		setContentView(R.layout.field);
		
		// Get the MapView, turn on satellite, and show the current position
		mMapView = (MapView)findViewById(R.id.map);
		mMapView.setSatellite(true);
		mMapView.setReticleDrawMode(ReticleDrawMode.DRAW_RETICLE_OVER);
		
		// Get the mMap controller
		mMapController = mMapView.getController();
		
		// Make a location overlay to track device	
		mMyLocationOverlay = new MyLocationOverlay(this, this.mMapView);
		mMapView.getOverlays().add(mMyLocationOverlay);
		
		// Make a PolygonOverlay
		mPolygonOverlay = new PolygonOverlay(getResources().getDrawable(R.drawable.ic_launcher));
		mMapView.getOverlays().add(mPolygonOverlay);
		
		// Get a hold of the notes list
		mNotesScrollView = (NotesScrollView)findViewById(R.id.notes_scroll_view);
		
		// Get a hold of the polygon surface
		mPolygonView = (PolygonSurfaceView)findViewById(R.id.polySurface);
		
		
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		enableLocation();
		
		if(mNotesScrollView.getNotes().size() == 0) {
			mNotesScrollView.addNote();
		} 
		
		int max = Object.getNewGroupId(this)-1;
		for(int i = 0; i < max; i++) {
			Object obj = Object.getObjectByGroup(this, i);
			
			if(obj != null) {
				mPolygonOverlay.addPolygon(obj);
			} 
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
	
		disableLocation();
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
	
	/*
	 * Creates the ActionBar with the main menu
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem importRocks = menu.findItem(R.id.import_rocks);
		
		if(mRockOverlay == null) {
			importRocks.setTitle(getString(R.string.menu_import_rocks));
		} else {
			importRocks.setTitle(getString(R.string.menu_hide_rocks));
		}
		
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean result = false;
		
		switch(item.getItemId()) {
			case R.id.new_note:
				mNotesScrollView.addNote();
				result = true;
			break;
			
			case R.id.import_rocks:
				if(mRockOverlay == null) {
					// Make a overlay for the rocks
					mRockOverlay = new RockMapOverlay(this);
					mMapView.getOverlays().add(mRockOverlay);
					mMapView.postInvalidate();
				} else {
					mMapView.getOverlays().remove(mRockOverlay);
					mMapView.postInvalidate();
					mRockOverlay = null;
				}
				
			break; 
			
			case R.id.add_polygon:
				startPolygon(Object.TYPE_POLYGON);
				lastType = Object.TYPE_POLYGON;
			break;
			
			case R.id.add_point:
				startPolygon(Object.TYPE_POINT);
				lastType = Object.TYPE_POINT;
			break;
			
			case R.id.add_line:
				startPolygon(Object.TYPE_LINE);
				lastType = Object.TYPE_LINE;
			break;
		}
		
		return result;
	}
	
	
	// Ask location to track GPS and display on the map
	private void enableLocation() {
		mMyLocationOverlay.enableMyLocation();
		
		// Animate the map to the current location
		mMyLocationOverlay.runOnFirstFix(new Runnable() {
			public void run() {
				mMapController.animateTo(mMyLocationOverlay.getMyLocation());
				mMapController.zoomToSpan(SPAN_LAT, SPAN_LONG);
			}
		});
	}
	
	// Stop tracking the GPS
	private void disableLocation() {
		mMyLocationOverlay.disableMyLocation();
		
	}
	
	public void startPolygon(int type) {
		ArrayList<Note> notes = mNotesScrollView.getNotes();
		Note note = notes.get(notes.size()-1);
		
		lastColor = note.getColor();
		
		if(!mPolygonView.isRunning()) {
			mPolygonView.startDrawing(lastColor, type);
			startActionMode(new DrawAcceptActionModeCallback());
		}
	}

	public void finishPolygon() {
		ScreenPolygon polygon = null;
		
		if(mPolygonView.isRunning()) {
			polygon = mPolygonView.stopDrawing();
			
			GeoPolygon geoPolygon = new GeoPolygon(polygon, mMapView.getProjection());
			
			Object obj = new Object(this, Object.getNewGroupId(this), lastType, geoPolygon.getPoints(), lastColor);
			obj.save();
		
//			mPolygonOverlay.addPolygon(geoPolygon);
			mPolygonOverlay.addPolygon(obj);
			
			mMapView.postInvalidate();
		}
	}
	
	public void clearPolygon() {
		if(mPolygonView.isRunning()) {
			mPolygonView.stopDrawing();
		}
		
	}
	
	private class DrawAcceptActionModeCallback implements ActionMode.Callback {
		
		// Call when startActionMode() is called
		// Should inflate the menu
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			MenuInflater inflater = mode.getMenuInflater();
			inflater.inflate(R.menu.draw_accept, menu);
				
			return true;
		}
		
		// Called when the mode is invalidated
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return false;
		}
		
		// Called when the user selects a menu item
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			boolean result;
			
			switch(item.getItemId()) {
				case R.id.accept:
					finishPolygon();
					
					mode.finish();
					result = true;
				break;
				
				case R.id.reject:
					clearPolygon();
					
					// No longer need to show the action bar after taking a new picture
					mode.finish();
					result = true;
				break;
				
				default:
					result = false;
				break;
			}
			
			return result;
		}

		// Called when the user exists the action mode
		public void onDestroyActionMode(ActionMode mode) {
			clearPolygon();
		}
	}
}