package hu.bivia.bivia.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;
import hu.bivia.bivia.model.Ride;

/**
 * A button that has a ride assigned to it. 
 */
public class RideButton extends Button {

	public RideButton(Context context) {
		super(context);
	}
	public RideButton (Context context, AttributeSet attrs) {
	    super(context, attrs);
	}

	public RideButton (Context context, AttributeSet attrs, int defStyle) {
	    super(context, attrs, defStyle);
	    }
	
	public Ride Ride;
}
