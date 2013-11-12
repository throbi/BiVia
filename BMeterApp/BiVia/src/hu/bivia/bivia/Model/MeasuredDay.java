package hu.bivia.bivia.Model;

import java.util.ArrayList;
import java.util.Date;

/**
 * Groups measurements for a day.
 * @author horvath.robert
 *
 */
public class MeasuredDay {

	private ArrayList<Measurement> myMeasurements = new ArrayList<Measurement>();
	private Date myDate;
	
	public MeasuredDay() {
		// means now
		this(new Date());
	}

	public MeasuredDay(Date date) {		
		myDate = date;
	}
	
	public void addMeasurement(Measurement newMeasurement){
		myMeasurements.add(newMeasurement);
	}
	
	public Date getDate(){
		return myDate;
	}
	
	//public get
	
	
}
