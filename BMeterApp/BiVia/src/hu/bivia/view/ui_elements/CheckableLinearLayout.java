/**
 * 
 */
package hu.bivia.view.ui_elements;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Checkable;
import android.widget.LinearLayout;

/**
 * @author horvath.robert 
 * 
 * found here:
 * http://stackoverflow.com/questions/19060760/android-checkable-linearlayout-states
 */
public class CheckableLinearLayout extends LinearLayout implements Checkable {	

	private boolean mChecked = false;	

	public CheckableLinearLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public boolean isChecked() {
		return mChecked;
	}

	public void setChecked(boolean b) {
		if (b != mChecked) {
			mChecked = b;
			refreshDrawableState();			
		}
	}

	public void toggle() {
		setChecked(!mChecked);
	}

	/*
	@Override
	public boolean performClick() {
		if (isChecked()) {
			toggle();
			return false;
		} else
			return super.performClick();
	}
	*/
}