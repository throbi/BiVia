package hu.bivia.bivia.logic.dataAccess;

import android.provider.BaseColumns;

/**
 * Self-explanatory SQL schema definition, and more...
 */
public final class BiViaDataAccessContract {
	// To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public BiViaDataAccessContract() {}
    
    private static final String DATE_TYPE = " INTEGER"; // SQLite translates date to integer
    private static final String LONG_TYPE = " INTEGER";
    private static final String FLOAT_TYPE = " REAL";
    private static final String COMMA_SEP = ",";
    public static final String SQL_CREATE_ENTRIES =
        "CREATE TABLE " + BiViaRideEntry.TABLE_NAME + 
        " (" +
        BiViaRideEntry.COLUMN_NAME_RIDE_START_TIME + DATE_TYPE + " PRIMARY KEY"+ COMMA_SEP +
        BiViaRideEntry.COLUMN_NAME_RIDE_DISTANCE + FLOAT_TYPE + COMMA_SEP +
        BiViaRideEntry.COLUMN_NAME_RIDE_ELAPSED_TIME_MS + LONG_TYPE + 
        " )";

    public static final String SQL_DELETE_ENTRIES =
        "DROP TABLE IF EXISTS " + BiViaRideEntry.TABLE_NAME;

    /**
     * Inner class that defines the BiVia table contents. Only rides are stored,
     * measured day model is calculated from the rides. Average speed is also 
     * calculated.
     */
    public static abstract class BiViaRideEntry implements BaseColumns {
        public static final String TABLE_NAME = "BiViaRides";
        public static final String COLUMN_NAME_RIDE_START_TIME = "rideStartDate";
        public static final String COLUMN_NAME_RIDE_DISTANCE = "rideDistance";
        public static final String COLUMN_NAME_RIDE_ELAPSED_TIME_MS = "rideTimeMillis";
    }

}
