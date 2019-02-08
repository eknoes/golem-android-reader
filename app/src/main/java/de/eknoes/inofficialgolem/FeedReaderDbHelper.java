package de.eknoes.inofficialgolem;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class FeedReaderDbHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 15;
    private static final String DATABASE_NAME = "FeedReader.db";
    private static final String TAG = "FeedReaderDbHelper";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + FeedReaderContract.Article.TABLE_NAME + " (" +
                    FeedReaderContract.Article._ID + " INTEGER PRIMARY KEY," +
                    FeedReaderContract.Article.COLUMN_NAME_TITLE + " TEXT, " +
                    FeedReaderContract.Article.COLUMN_NAME_SUBHEADING + " TEXT, " +
                    FeedReaderContract.Article.COLUMN_NAME_TEASER + " TEXT, " +
                    FeedReaderContract.Article.COLUMN_NAME_URL + " TEXT, " +
                    FeedReaderContract.Article.COLUMN_NAME_COMMENTURL + " TEXT, " +
                    FeedReaderContract.Article.COLUMN_NAME_COMMENTNR + " TEXT, " +
                    FeedReaderContract.Article.COLUMN_NAME_IMG + " TEXT, " +
                    FeedReaderContract.Article.COLUMN_NAME_DATE + " INTEGER," +
                    FeedReaderContract.Article.COLUMN_NAME_FULLTEXT + " TEXT," +
                    //FeedReaderContract.Article.COLUMN_NAME_AUTHORS + " TEXT," +
                    FeedReaderContract.Article.COLUMN_NAME_OFFLINE + " INTEGER" +
                    ")";
    private static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + FeedReaderContract.Article.TABLE_NAME;
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
