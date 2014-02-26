package hu.bivia.view;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import hu.bivia.bivia.R;
import hu.bivia.model.MeasuredDay;
import hu.bivia.model.Ride;
import hu.bivia.view.ui_elements.CheckableLinearLayout;
import hu.bivia.view.ui_elements.MeasuredDayButton;
import hu.bivia.view.ui_elements.RideButton;
import hu.bivia.viewModel.BiViaMainPageViewModel;
import android.app.Activity;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

public class MeasuredDayExpandalbleAdapter extends BaseExpandableListAdapter {

	private ArrayList<MeasuredDay> myMeasuredDays;
	private LayoutInflater myInflater;
	private BiViaMainPageViewModel myViewModel;

	// hack for keeping track what to expand
	private Map<View, Integer> measuredDayPositions = new HashMap<View, Integer>();

	// hack for keeping track of current uploads
	private Map<MeasuredDay, View> activeUploads = new HashMap<MeasuredDay, View>();

	// region --- public API
	// -------------------------------------------------------------------------
	/** Constructor */
	public MeasuredDayExpandalbleAdapter(Activity activity,
			ArrayList<MeasuredDay> measuredDays,
			BiViaMainPageViewModel viewModel) {
		myInflater = activity.getLayoutInflater();
		myMeasuredDays = measuredDays;
		myViewModel = viewModel;
	}

	/**
	 * Called when an upload finished, so the progress bar can be hidden for the
	 * given day
	 */
	public void uploadFinished(MeasuredDay day) {
		View view = activeUploads.get(day);

		if (view != null) {
			((View) view.findViewById(R.id.upload_progress))
					.setVisibility(View.INVISIBLE);
			((View) view.findViewById(R.id.upload_day_button))
					.setVisibility(View.VISIBLE);
			activeUploads.remove(day);
		}
	}

	// endregion --- public API
	// -------------------------------------------------------------------------

	// region --- overrides
	// -------------------------------------------------------------------------

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return myMeasuredDays.get(groupPosition).getRide(childPosition);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return 0;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {

		Ride ride = (Ride) getChild(groupPosition, childPosition);

		if (convertView == null) {
			convertView = myInflater.inflate(R.layout.measured_ride, null);
		}

		MeasuredDay day = (MeasuredDay) getGroup(groupPosition);
		convertView
				.setBackgroundColor(calculateAlternateWeekBackgroundColor(day));

		((TextView) convertView.findViewById(R.id.ride_startTime))
				.setText(BiViaMainActivityView.timeFormatter.format(ride
						.getStartTime()));
		((TextView) convertView.findViewById(R.id.ride_distance))
				.setText(BiViaMainActivityView.decimalFormatter.format(ride
						.getDistance()) + " km /");
		((TextView) convertView.findViewById(R.id.ride_time))
				.setText(BiViaMainActivityView.formatElapsedMillis(ride
						.getRideTimeMs()));
		;
		((TextView) convertView.findViewById(R.id.ride_averageSpeed))
				.setText((BiViaMainActivityView.decimalFormatter.format(ride
						.getAverageSpeed()) + " km/h"));

		RideButton deleteButton = ((RideButton) convertView
				.findViewById(R.id.ride_delete));
		deleteButton.Ride = ride;
		deleteButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				myViewModel.deleteRide(((RideButton) (view)).Ride);
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
		MeasuredDay day = myMeasuredDays.get(groupPosition);
		return day;
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
	public View getGroupView(final int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {

		final MeasuredDay day = (MeasuredDay) getGroup(groupPosition);

		if (convertView == null) {

			convertView = myInflater.inflate(R.layout.measured_day, null);

			convertView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View view) {
					CheckableLinearLayout measuredDayView = (CheckableLinearLayout) view;

					if (!measuredDayView.isChecked()) {
						myViewModel.expandMeasuredDay(measuredDayPositions
								.get(view));
						measuredDayView.setChecked(true);

					} else {
						myViewModel.collapseMeasuredDay(measuredDayPositions
								.get(view));
						measuredDayView.setChecked(false);
					}

				}
			});
		}

		// views are recycled for performance optimization by Android
		// must set content each time it is shown

		convertView
				.setBackgroundColor(calculateAlternateWeekBackgroundColor(day));

		String header = "<b>"
				+ BiViaMainActivityView.dateFormatter.format(day.getDate())
				+ "</b><br/> "
				+ BiViaMainActivityView.decimalFormatter.format(day
						.getTotalDistance())
				+ " km <font color=\"#352b2b\">/</font> "
				+ BiViaMainActivityView.formatElapsedMillis(day
						.getTotalTimeMillis())
				+ " <font color=\"#352b2b\">=</font> "
				+ BiViaMainActivityView.decimalFormatter.format(day
						.getAverageSpeed()) + " km/h ";

		((TextView) convertView.findViewById(R.id.measuredDayData))
				.setText(Html.fromHtml(header));
		((CheckableLinearLayout) convertView.findViewById(R.id.measuredDay))
				.setChecked(isExpanded);

		MeasuredDayButton uploadDayButton = ((MeasuredDayButton) convertView
				.findViewById(R.id.upload_day_button));
		View progressBar = convertView.findViewById(R.id.upload_progress);
		
		// is this day being uploaded right now?
		if(activeUploads.containsKey(day)){
			progressBar.setVisibility(View.VISIBLE);
			uploadDayButton.setVisibility(View.INVISIBLE);
		} else {
			progressBar.setVisibility(View.INVISIBLE);
			uploadDayButton.setVisibility(View.VISIBLE);
		}

		// prepare button for action
		uploadDayButton.MeasuredDay = day;
		uploadDayButton.setFocusable(false);
		uploadDayButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				MeasuredDay day = ((MeasuredDayButton) (view)).MeasuredDay;
				view.setVisibility(View.INVISIBLE);
				View parentView = (View) view.getParent();
				parentView.findViewById(R.id.upload_progress).setVisibility(
						View.VISIBLE);
				activeUploads.put(day, parentView);

				myViewModel.uploadeMeasuredDay(day);
			}
		});

		measuredDayPositions.put(convertView, groupPosition);

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

	// endregion --- overrides
	// --------------------------------------------------

	// region --- private stuff
	// -------------------------------------------------

	private GregorianCalendar myCalendar = new GregorianCalendar();

	private int calculateAlternateWeekBackgroundColor(MeasuredDay day) {
		myCalendar.setTime(day.getDate());
		int weekOfYear = myCalendar.get(Calendar.WEEK_OF_YEAR);
		if (weekOfYear % 2 == 0) {
			return 0xFF503030;
		} else {
			return 0xFF664141;
		}
	}

	// endregion --- private stuff
	// ----------------------------------------------
}
