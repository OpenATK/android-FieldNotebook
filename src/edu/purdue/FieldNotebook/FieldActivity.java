package edu.purdue.FieldNotebook;

import java.util.ArrayList;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MapView.ReticleDrawMode;
import com.google.android.maps.MyLocationOverlay;

import edu.purdue.FieldNotebook.shape.GeoPolygon;
import edu.purdue.FieldNotebook.shape.ScreenPolygon;
import edu.purdue.FieldNotebook.view.PolygonOverlay;
import edu.purdue.FieldNotebook.view.PolygonSurfaceView;

import edu.purdue.libwaterapps.rock.Rock;

public class FieldActivity extends MapActivity {
	
	MapView mMapView;
	MapController mMapController;
	MyLocationOverlay mMyLocationOverlay;
	PolygonOverlay mPolygonOverlay;
	LinearLayout mNotesView;
	PolygonSurfaceView mPolygonView;
	RockOverlay mPickedRocks;
	RockOverlay mNotPickedRocks;
	
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
		
		// Make a overlay for the rocks
		mPickedRocks = new RockOverlay(this.getResources().getDrawable(R.drawable.rock_up));
		mNotPickedRocks = new RockOverlay(this.getResources().getDrawable(R.drawable.rock_down));
		
		mMapView.getOverlays().add(mPickedRocks);
		mMapView.getOverlays().add(mNotPickedRocks);
		
		
		// Get a hold of the notes list
		mNotesView = (LinearLayout)findViewById(R.id.notes);
		
		// Get a hold of the polygon surface
		mPolygonView = (PolygonSurfaceView)findViewById(R.id.polySurface);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		enableLocation();
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
	
	public void addNote(View view) {
		getLayoutInflater().inflate(R.layout.note, mNotesView);
	}
	
	public void togglePolygon(View view) {
		togglePolygonViews();
		
		if(!mPolygonView.isRunning()) {
			mPolygonView.startDrawing();
		}
	}

	public void finishPolygon(View view) {
		ScreenPolygon polygon = null;
		togglePolygonViews();
		
		if(mPolygonView.isRunning()) {
			polygon = mPolygonView.stopDrawing();
		
			GeoPolygon geoPolygon = new GeoPolygon(polygon, mMapView.getProjection());
		
			mPolygonOverlay.addPolygon(geoPolygon);
			
			mMapView.postInvalidate();
		}
	}
	
	public void clearPolygon(View view) {
		togglePolygonViews();
		
		if(mPolygonView.isRunning()) {
			mPolygonView.stopDrawing();
		}
		
	}
	
	public void togglePolygonViews() {
		Button addPoly = (Button)findViewById(R.id.add_polygon);
		Button finishPoly = (Button)findViewById(R.id.finish_polygon);
		Button clearPoly = (Button)findViewById(R.id.clear_polygon);
		
		if(addPoly.getVisibility() == View.VISIBLE) {
			addPoly.setVisibility(View.GONE);
			finishPoly.setVisibility(View.VISIBLE);
			clearPoly.setVisibility(View.VISIBLE);
		} else {
			finishPoly.setVisibility(View.GONE);
			clearPoly.setVisibility(View.GONE);
			addPoly.setVisibility(View.VISIBLE);
		}
		
	}
	
	public void importRocks(View view) {
		ArrayList<Rock> rocks = Rock.getAllRocks(this);
		
		mPickedRocks.clear();
		mNotPickedRocks.clear();
		for(Rock rock : rocks) {
			if(rock.isPicked()) {
				mPickedRocks.addRock(rock);
			} else {
				mNotPickedRocks.addRock(rock);
			}
		}
		
		mMapView.postInvalidate();
		
	}
}
