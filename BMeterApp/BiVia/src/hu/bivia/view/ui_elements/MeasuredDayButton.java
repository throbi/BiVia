package hu.bivia.view.ui_elements;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageButton;
import hu.bivia.model.MeasuredDay;

/**
 * A button that has a measured day assigned to it. 
 */
public class MeasuredDayButton extends ImageButton {

	public MeasuredDayButton(Context context) {
		super(context);
	}
	
	public MeasuredDayButton (Context context, AttributeSet attrs) {
	    super(context, attrs);
	}

	public MeasuredDayButton (Context context, AttributeSet attrs, int defStyle) {
	    super(context, attrs, defStyle);
	    }
	
	public MeasuredDay MeasuredDay;
}
