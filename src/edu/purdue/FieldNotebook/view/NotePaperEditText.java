package edu.purdue.FieldNotebook.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.EditText;

public class NotePaperEditText extends EditText {
	private Paint paint;
	private Rect r;
	
	public NotePaperEditText(Context context) {
		super(context);
		setupNotePaper();
	}
	
	public NotePaperEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		setupNotePaper();
	}

	public NotePaperEditText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setupNotePaper();
	}
	
	private void setupNotePaper() {
		paint = new Paint();
		paint.setStyle(Paint.Style.FILL_AND_STROKE);
		paint.setColor(Color.BLUE);
		
		r = new Rect();
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		// Calculate how many lines there are.
		// getLineCount() will return only the number of actual lines (so if height is larger
		// then the number of lines it wont be counted)
		int lineCount = getHeight()/getLineHeight();
		
		// Find where the first line starts
		int baseline = getLineBounds(0, r);
		for(int i = 0; i < lineCount; i++){
			canvas.drawLine(r.left, baseline+3, r.right, baseline+3, paint);
			
			// Move down with the lines
			baseline += getLineHeight();
		}
		
		// Draw the normal EditText
		super.onDraw(canvas);
	}

}
