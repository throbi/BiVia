package hu.bivia.bivia.view;

import java.util.ArrayList;

import hu.bivia.bivia.R;
import hu.bivia.bivia.model.MeasuredDay;
import hu.bivia.bivia.model.Ride;
import android.app.Activity;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.TextView;
import android.widget.Toast;

public class MeasuredDayExpandalbleAdapter extends BaseExpandableListAdapter  {

	private ArrayList<MeasuredDay> myMeasuredDays;
	private LayoutInflater myInflater;
	private Activity myActivity;
	
	public MeasuredDayExpandalbleAdapter(Activity activity, ArrayList<MeasuredDay> measuredDays) {
		myActivity = activity;
		myInflater = activity.getLayoutInflater();
		myMeasuredDays = measuredDays;
	}
	
	//region --- overrides -----------------------------------------------------		
	
	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return myMeasuredDays.get(groupPosition).getRide(childPosition);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return 0;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView,
			ViewGroup parent) {

		Ride ride = (Ride) getChild(groupPosition, childPosition);
	    
	    if (convertView == null) {
	      convertView = myInflater.inflate(R.layout.measured_ride, null);
	    }
	    
	    ((TextView)convertView.findViewById(R.id.ride_startTime)).
	    	setText(BiViaMainActivityView.timeFormatter.format(ride.getStartTime()));
	    ((TextView)convertView.findViewById(R.id.ride_distance)).
	    	setText(BiViaMainActivityView.decimalFormatter.format(ride.getDistance()) + " km /");
	    ((TextView)convertView.findViewById(R.id.ride_time)).
	    		setText(BiViaMainActivityView.formatElapsedMillis(ride.getRideTimeMs()));;
	    ((TextView)convertView.findViewById(R.id.ride_averageSpeed)).
	    		setText((BiViaMainActivityView.decimalFormatter.format(ride.getAverageSpeed()) + " km/h"));
	    
	    ((Button)convertView.findViewById(R.id.ride_delete)).
	    	setOnClickListener(new OnClickListener() {				
				@Override
				public void onClick(View v) {
					Toast.makeText(myActivity, myActivity.getString(R.string.ride_deleted), Toast.LENGTH_SHORT).show();					
				}
			}); 	      
	    
	    return convertView;
	
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return myMeasuredDays.get(groupPosition).getRideCount();
	}

	@Override
	public Object getGroup(int groupPosition) {
		return myMeasuredDays.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		return myMeasuredDays.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
		
		MeasuredDay day = (MeasuredDay)getGroup(groupPosition);
		
		if (convertView == null) {
	      convertView = myInflater.inflate(R.layout.measured_day, null);
	    }
	    
	    String header = "<b>" + BiViaMainActivityView.dateFormatter.format(day.getDate()) + "</b><br/> " + 
	    BiViaMainActivityView.decimalFormatter.format(day.getTotalDistance()) + " km <font color=\"#352b2b\">/</font> " +
	    BiViaMainActivityView.formatElapsedMillis(day.getTotalTimeMillis()) + " <font color=\"#352b2b\">=</font> " +
	    BiViaMainActivityView.decimalFormatter.format(day.getAverageSpeed()) + " km/h "; 
	    
	    ((CheckedTextView)convertView.findViewById(R.id.measuredDayRow)).setText(Html.fromHtml(header));
	    ((CheckedTextView)convertView.findViewById(R.id.measuredDayRow)).setChecked(isExpanded);
	   		
		return convertView;
	}
	
	@Override
	public void onGroupCollapsed(int groupPosition) {
		super.onGroupCollapsed(groupPosition);
	}

	@Override
	public void onGroupExpanded(int groupPosition) {
		super.onGroupExpanded(groupPosition);
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public boolean isChildSelectable(int arg0, int arg1) {
		return false;
	}
	//endregion --- overrides --------------------------------------------------
	
}
