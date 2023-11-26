package de.eknoes.inofficialgolem;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import de.eknoes.inofficialgolem.entities.DBColumns;

public class FeedReaderDbHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 15;
    private static final String DATABASE_NAME = "FeedReader.db";
    private static final String TAG = "FeedReaderDbHelper";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + DBColumns.getTableName() + " (" +
                    DBColumns.COLUMN_NAME_ID.getColumnName() + " INTEGER PRIMARY KEY," +
                    DBColumns.COLUMN_NAME_TITLE.getColumnName() + " TEXT, " +
                    DBColumns.COLUMN_NAME_SUBHEADING.getColumnName() + " TEXT, " +
                    DBColumns.COLUMN_NAME_TEASER.getColumnName() + " TEXT, " +
                    DBColumns.COLUMN_NAME_URL.getColumnName() + " TEXT, " +
                    DBColumns.COLUMN_NAME_COMMENTURL.getColumnName() + " TEXT, " +
                    DBColumns.COLUMN_NAME_COMMENTNR.getColumnName() + " TEXT, " +
                    DBColumns.COLUMN_NAME_IMG.getColumnName() + " TEXT, " +
                    DBColumns.COLUMN_NAME_DATE.getColumnName() + " INTEGER," +
                    DBColumns.COLUMN_NAME_FULLTEXT.getColumnName() + " TEXT," +
                    //DBColumns.COLUMN_NAME_AUTHORS + " TEXT," +
                    DBColumns.COLUMN_NAME_OFFLINE.getColumnName() + " INTEGER," +
                    DBColumns.COLUMN_NAME_ALREADY_READ.getColumnName() + " INTEGER DEFAULT FALSE" +
                    ")";
    private static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + DBColumns.getTableName();
    private static FeedReaderDbHelper self;


    private FeedReaderDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static FeedReaderDbHelper getInstance(Context c) {
        if (self == null) {
            self = new FeedReaderDbHelper(c);
        }
        return self;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            if (oldVersion < 15) {
                db.execSQL(SQL_DELETE_ENTRIES);
                onCreate(db);
            }
        } catch (Exception e) {
            Log.e(TAG, "onUpgrade: Exception on Database upgrade. Recreation!");
            db.execSQL(SQL_DELETE_ENTRIES);
            onCreate(db);
        }
    }
}
