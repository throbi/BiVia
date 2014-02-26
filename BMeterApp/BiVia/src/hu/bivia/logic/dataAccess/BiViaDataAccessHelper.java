package hu.bivia.logic.dataAccess;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import hu.bivia.logic.Measurer;
import hu.bivia.logic.dataAccess.BiViaDataAccessContract.BiViaRideEntry;
import hu.bivia.model.Ride;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class BiViaDataAccessHelper extends SQLiteOpenHelper{
	// If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "BiVia.db";
	
    private SQLiteDatabase myWritableDB;
	private SQLiteDatabase myReadableDB;

    //region --- overrides -----------------------------------------------------
    public BiViaDataAccessHelper(Context context) {
    	super(context, DATABASE_NAME, null, DATABASE_VERSION);        
    }
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(BiViaDataAccessContract.SQL_CREATE_ENTRIES);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersin, int newVersion) {
		// TODO implement version-to-version migration rules
	}

	@Override
	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO implement version-to-version migration rules
	}
	//endregion --- overrides --------------------------------------------------

	//region--- BiVia specific API ---------------------------------------------
	
	/**
	 * Reads from the SQLite database
	 * @return all rides ordered by start time, oldest first
	 */
	public List<Ride> getAllRides(){
		
		String[] columns = {
				BiViaRideEntry.COLUMN_NAME_RIDE_START_TIME,
				BiViaRideEntry.COLUMN_NAME_RIDE_DISTANCE,
				BiViaRideEntry.COLUMN_NAME_RIDE_ELAPSED_TIME_MS
		};
		
		String sortOrder =
				BiViaRideEntry.COLUMN_NAME_RIDE_START_TIME + " ASC";

		Cursor cursor = getReadableDB().query(
			    BiViaRideEntry.TABLE_NAME, 
			    columns,
			    null,   // The columns for the WHERE clause
			    null,   // The values for the WHERE clause
			    null,   // don't group the rows
			    null,   // don't filter by row groups
			    sortOrder
			    );

		List<Ride> allRides = new LinkedList<Ride>();
		
		int rideCount = cursor.getCount();
		for(int i=0; i<rideCount; i++){
			cursor.moveToPosition(i);
			Date startTime = new Date(cursor.getLong(
					cursor.getColumnIndexOrThrow(BiViaRideEntry.COLUMN_NAME_RIDE_START_TIME)));
			float distanceKm = cursor.getFloat(
					cursor.getColumnIndexOrThrow(BiViaRideEntry.COLUMN_NAME_RIDE_DISTANCE));
			long rideTimeMs = cursor.getLong(
					cursor.getColumnIndexOrThrow(BiViaRideEntry.COLUMN_NAME_RIDE_ELAPSED_TIME_MS));
			float averageSpeed = Measurer.calculateSpeedInKmPerHour(distanceKm, rideTimeMs);
			
			allRides.add(
					new Ride(startTime, distanceKm, averageSpeed, rideTimeMs));
		}
		
		return allRides;
	}
	
	/**
	 * Puts a new ride into the DB
	 * @param ride
	 */
	public void saveRide(Ride ride){	
		ContentValues values = new ContentValues();
		values.put(BiViaRideEntry.COLUMN_NAME_RIDE_START_TIME, ride.getStartTime().getTime());
		values.put(BiViaRideEntry.COLUMN_NAME_RIDE_DISTANCE, ride.getDistance());
		values.put(BiViaRideEntry.COLUMN_NAME_RIDE_ELAPSED_TIME_MS, ride.getRideTimeMs());
	
		getWritableDB().insert(
				BiViaRideEntry.TABLE_NAME,
				null, // do not save null values
				values);
	}
	
	/**
	 * Deletes the specified ride from the DB.
	 * @param ride
	 */
	public void deleteRide(Ride ride){
		String whereClause = 
				BiViaRideEntry.COLUMN_NAME_RIDE_START_TIME + " = ? AND " +
				BiViaRideEntry.COLUMN_NAME_RIDE_ELAPSED_TIME_MS  + " = ?";
		String[] whereArgs = {
				Long.toString(ride.getStartTime().getTime()),
				Long.toString(ride.getRideTimeMs())};

		getWritableDB().delete(BiViaRideEntry.TABLE_NAME, whereClause, whereArgs);
	}
	
	//endregion --- BiVia specific API -----------------------------------------

	//region --- utils ---------------------------------------------------------
	
	/**
	 * Getter for the writable DB.
	 * @return
	 */
	private SQLiteDatabase getWritableDB() {
		if(myWritableDB == null){
			myWritableDB = getWritableDatabase();
		}
		
		return myWritableDB;
	}

	/**
	 * Getter for the readable DB.
	 * @return
	 */
	private SQLiteDatabase getReadableDB() {
		if(myReadableDB == null){
			myReadableDB = getReadableDatabase();
		}
		
		return myReadableDB;
	}
	
	//endregion --- utils ------------------------------------------------------
}
