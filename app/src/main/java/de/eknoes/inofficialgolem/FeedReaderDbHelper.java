package de.eknoes.inofficialgolem;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class FeedReaderDbHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 11;
    private static final String DATABASE_NAME = "FeedReader.db";
    private static final String TAG = "FeedReaderDbHelper";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + FeedReaderContract.Article.TABLE_NAME + " (" +
                    FeedReaderContract.Article._ID + " INTEGER PRIMARY KEY," +
                    FeedReaderContract.Article.COLUMN_NAME_ID + " INTEGER," +
                    FeedReaderContract.Article.COLUMN_NAME_TITLE + " TEXT, " +
                    FeedReaderContract.Article.COLUMN_NAME_SUBHEADING + " TEXT, " +
                    FeedReaderContract.Article.COLUMN_NAME_TEASER + " TEXT, " +
                    FeedReaderContract.Article.COLUMN_NAME_URL + " TEXT, " +
                    FeedReaderContract.Article.COLUMN_NAME_IMG + " TEXT, " +
                    FeedReaderContract.Article.COLUMN_NAME_DATE + " INTEGER," +
                    FeedReaderContract.Article.COLUMN_NAME_FULLTEXT + " TEXT," +
                    FeedReaderContract.Article.COLUMN_NAME_AUTHORS + " TEXT," +
                    FeedReaderContract.Article.COLUMN_NAME_OFFLINE + " INTEGER" +
                    " )";
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
            if (oldVersion < 3) {
                db.execSQL("CREATE TABLE " + FeedReaderContract.Article.TABLE_NAME + "_temp (" +
                        FeedReaderContract.Article._ID + " INTEGER PRIMARY KEY," +
                        FeedReaderContract.Article.COLUMN_NAME_ID + " INTEGER," +
                        FeedReaderContract.Article.COLUMN_NAME_TITLE + " TEXT, " +
                        FeedReaderContract.Article.COLUMN_NAME_SUBHEADING + " TEXT, " +
                        FeedReaderContract.Article.COLUMN_NAME_TEASER + " TEXT, " +
                        FeedReaderContract.Article.COLUMN_NAME_URL + " TEXT, " +
                        FeedReaderContract.Article.COLUMN_NAME_IMG + " TEXT, " +
                        FeedReaderContract.Article.COLUMN_NAME_DATE + " INTEGER" +
                        " )");
                db.execSQL("INSERT INTO " + FeedReaderContract.Article.TABLE_NAME + "_temp SELECT * FROM " + FeedReaderContract.Article.TABLE_NAME);
                db.execSQL(SQL_DELETE_ENTRIES);
                db.execSQL("ALTER TABLE " + FeedReaderContract.Article.TABLE_NAME + "_temp RENAME TO " + FeedReaderContract.Article.TABLE_NAME);
            }
            if (oldVersion < 4) {
                db.execSQL("ALTER TABLE " + FeedReaderContract.Article.TABLE_NAME + " ADD COLUMN " + FeedReaderContract.Article.COLUMN_NAME_FULLTEXT + " TEXT;");
                db.execSQL("ALTER TABLE " + FeedReaderContract.Article.TABLE_NAME + " ADD COLUMN " + FeedReaderContract.Article.COLUMN_NAME_AUTHORS + " TEXT;");
                db.execSQL("ALTER TABLE " + FeedReaderContract.Article.TABLE_NAME + " ADD COLUMN " + FeedReaderContract.Article.COLUMN_NAME_OFFLINE + " INTEGER;");
            }
        /*if (oldVersion < 5) {
            db.execSQL("ALTER TABLE " + FeedReaderContract.Article.TABLE_NAME + " ADD COLUMN " + FeedReaderContract.Article.COLUMN_NAME_MEDIA_FULLTEXT + " INTEGER;");
        }*/
            if (oldVersion < 8) {
                db.execSQL("ALTER TABLE " + FeedReaderContract.Article.TABLE_NAME + " RENAME TO backup;");
                db.execSQL(SQL_CREATE_ENTRIES);
                db.execSQL("INSERT INTO " + FeedReaderContract.Article.TABLE_NAME + " SELECT _id, id, title, subtitle as subheading, teaser, url, img, date, fulltext, authors, offline_available FROM backup");
                db.execSQL("DROP TABLE backup");
            }

            if (oldVersion < 11) {
                String[] columns = {
                        FeedReaderContract.Article.COLUMN_NAME_ID,
                        FeedReaderContract.Article.COLUMN_NAME_TITLE,
                        FeedReaderContract.Article.COLUMN_NAME_SUBHEADING,
                        FeedReaderContract.Article.COLUMN_NAME_TEASER,
                        FeedReaderContract.Article.COLUMN_NAME_DATE,
                        FeedReaderContract.Article.COLUMN_NAME_IMG,
                        FeedReaderContract.Article.COLUMN_NAME_URL,
                        FeedReaderContract.Article.COLUMN_NAME_AUTHORS,
                        FeedReaderContract.Article.COLUMN_NAME_OFFLINE,
                        FeedReaderContract.Article.COLUMN_NAME_FULLTEXT
                };

                Cursor cursor = db.query(
                        FeedReaderContract.Article.TABLE_NAME,
                        columns,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null);

                while (cursor.moveToNext()) {
                    String url = cursor.getString(cursor.getColumnIndex(FeedReaderContract.Article.COLUMN_NAME_URL));
                    String newUrl = url.replace("http://", "https://");

                    Log.d(TAG, "onUpgrade: Changed URL From " + url + " to " + newUrl);

                    ContentValues values = new ContentValues();
                    values.put(FeedReaderContract.Article.COLUMN_NAME_URL, newUrl);

                    int affected = db.update(FeedReaderContract.Article.TABLE_NAME,
                            values,
                            FeedReaderContract.Article.COLUMN_NAME_ID + "=?",
                            new String[]{String.valueOf(cursor.getInt(cursor.getColumnIndex(FeedReaderContract.Article.COLUMN_NAME_ID)))});

                    if (affected != 1) {
                        Log.d(TAG, "onUpgrade: Affected " + affected + " rows!");
                    }
                }
                cursor.close();


            }

            if (oldVersion > 9) {
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
