package edu.purdue.FieldNotebook.view;

import java.util.ArrayList;
import java.util.Random;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import edu.purdue.FieldNotebook.R;
import edu.purdue.libwaterapps.note.Note;

public class NotesScrollView extends ScrollView {
	private Context context;
	private ArrayList<Note> notes;
	private LinearLayout viewList;

	public NotesScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		this.context = context;
		
		notes = Note.getNotes(context);
	}
	
	@Override
	public void onFinishInflate() {
		super.onFinishInflate();
		
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		viewList = new LinearLayout(context);
		viewList.setOrientation(LinearLayout.VERTICAL);
		viewList.setClipChildren(true);
		addView(viewList, params);
		
		updateViews();
	}
	
	public ArrayList<Note> getNotes() {
		return notes; 
	}
	
	public void addNote() {
		Random random = new Random();
		
		byte[] b = new byte[1];
		random.nextBytes(b);
		int randomIndex = ((int)b[0]) % 3;
		
		float greyControl = 0.05f;
		float mixRatio1 = (randomIndex == 0) ? random.nextFloat() * greyControl : random.nextFloat();
		float mixRatio2 = (randomIndex == 1) ? random.nextFloat() * greyControl : random.nextFloat();
		float mixRatio3 = (randomIndex == 2) ? random.nextFloat() * greyControl : random.nextFloat();
		
		float sum = mixRatio1 + mixRatio2 + mixRatio3;
		
		mixRatio1 /= sum;
		mixRatio2 /= sum;
		mixRatio3 /= sum;
		
		int r1 = 255;
		int g1 = 0;
		int b1 = 0;
		int r2 = 0;
		int g2 = 255;
		int b2 = 0;
		int r3 = 0;
		int g3 = 0;
		int b3 = 255;
		
		int color = Color.rgb((int)(mixRatio1*r1 + mixRatio2*r2 + mixRatio3*r3),
							  (int)(mixRatio1*g1 + mixRatio2*g2 + mixRatio3*g3),
							  (int)(mixRatio1*b1 + mixRatio2*b2 + mixRatio3*b3));
		
		Note note = new Note(context, color, "");
		note.save();
		notes.add(note);
		
		updateViews();
		
		// Show new note card
		post(new Runnable() {
			public void run() {
				fullScroll(FOCUS_DOWN);
			}	
		});
	}
	
	private void updateViews() {
		int start = viewList.getChildCount();
		
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		for(int i = start; i < notes.size(); i++) {
			
			View v = inflater.inflate(R.layout.note, viewList, false);
			
			v.setBackgroundColor(notes.get(i).getColor());
			((NotePaperEditText)v.findViewById(R.id.note_area)).setText(notes.get(i).getComments());
			((NotePaperEditText)v.findViewById(R.id.note_area)).setOnEditorActionListener(new NotesEditorActionListener());
			((NotePaperEditText)v.findViewById(R.id.note_area)).setTextColor(Color.BLACK);
			viewList.addView(v);
		}
	}

	private class NotesEditorActionListener implements OnEditorActionListener {

		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			int cnt = getChildCount();
			
			for(int i = 0; i < cnt; i++) {
				View view = getChildAt(i);
				
				notes.get(i).setComments(((NotePaperEditText)view.findViewById(R.id.note_area)).getText().toString());
				notes.get(i).save();
				
			}
			return false;
		}
		
	}

}
