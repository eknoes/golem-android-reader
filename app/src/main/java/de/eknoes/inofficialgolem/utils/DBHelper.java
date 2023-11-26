package de.eknoes.inofficialgolem.utils;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import de.eknoes.inofficialgolem.Article;
import de.eknoes.inofficialgolem.FeedReaderDbHelper;
import de.eknoes.inofficialgolem.MainApplication;
import de.eknoes.inofficialgolem.entities.DBColumns;
import de.eknoes.inofficialgolem.entities.QueryRequest;

public class DBHelper {
    private final static String TAG = "DBHelper";

    public static SQLiteDatabase getSQLiteDatabase() {
        FeedReaderDbHelper dbHelper = FeedReaderDbHelper.getInstance(MainApplication.getAppContext());
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        return db;
    }

    public static Article getArticle(QueryRequest queryRequest) {
        try (Cursor cursor = getCursor(queryRequest)) {
            if (cursor.moveToFirst()) {
                return getArticleFromCursor(cursor);
            }
            cursor.close();
            return null;
        }
    }

    public static void updateArticleReadState(Article article) {
        SQLiteDatabase sqLiteDatabase = getSQLiteDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBColumns.COLUMN_NAME_ALREADY_READ.getColumnName(), true);
        sqLiteDatabase.update(DBColumns.getTableName(), contentValues, DBColumns.COLUMN_NAME_ID.getColumnName() + "=" + article.getId(), null);
    }

    private static Cursor getCursor(QueryRequest queryRequest) {
        SQLiteDatabase database = getSQLiteDatabase();
        Cursor cursor = database.query(DBColumns.getTableName(),
                DBColumns.getColumnNames(), queryRequest.getSelection(),
                queryRequest.getSelectionArgs(), queryRequest.getGroupBy(),
                queryRequest.getHaving(), queryRequest.getOrderBy());
        return cursor;
    }

    public static List<Article> getArticles(QueryRequest queryRequest) {
        List<Article> articles = new ArrayList<>();
        try (Cursor cursor = getCursor(queryRequest)) {
            if (cursor.moveToFirst()) {
                articles.add(getArticleFromCursor(cursor));
                while (cursor.moveToNext()) {
                    articles.add(getArticleFromCursor(cursor));
                }
            }
            return articles;
        }
    }

    public static Article getArticleFromCursor(Cursor cursor) {
        try {
            Article a = new Article();
            Object value = getValueFromCursor(cursor, DBColumns.COLUMN_NAME_ID);
            if (value != null) {
                a.setId((Integer) value);
            }
            value = getValueFromCursor(cursor, DBColumns.COLUMN_NAME_TITLE);
            if (value != null) {
                a.setTitle((String) value);
            }
            value = getValueFromCursor(cursor, DBColumns.COLUMN_NAME_SUBHEADING);
            if (value != null) {
                a.setSubheadline((String) value);
            }
            value = getValueFromCursor(cursor, DBColumns.COLUMN_NAME_TEASER);
            if (value != null) {
                a.setTeaser((String) value);
            }
            value = getValueFromCursor(cursor, DBColumns.COLUMN_NAME_DATE);
            if (value != null) {
                a.setDate((Long) value);
            }
            value = getValueFromCursor(cursor, DBColumns.COLUMN_NAME_IMG);
            if (value != null) {
                a.setImgUrl((String) value);
            }
            value = getValueFromCursor(cursor, DBColumns.COLUMN_NAME_COMMENTURL);
            if (value != null) {
                a.setCommentUrl((String) value);
            }
            value = getValueFromCursor(cursor, DBColumns.COLUMN_NAME_COMMENTNR);
            if (value != null) {
                a.setCommentNr((String) value);
            }
            value = getValueFromCursor(cursor, DBColumns.COLUMN_NAME_URL);
            if (value != null) {
                a.setUrl((String) value);
            }
            value = getValueFromCursor(cursor, DBColumns.COLUMN_NAME_OFFLINE);
            if (value != null) {
                a.setOffline((Boolean) value);
            }
            value = getValueFromCursor(cursor, DBColumns.COLUMN_NAME_FULLTEXT);
            if (value != null) {
                a.setFulltext((String) value);
            }
            value = getValueFromCursor(cursor, DBColumns.COLUMN_NAME_ALREADY_READ);
            if (value != null) {
                a.setAlreadyRead((Boolean) value);
            }
            return a;
        } catch (NullPointerException e) {
            Log.e(TAG, "Error while getting article from local DB");
        }
        return null;
    }

    private static Object getValueFromCursor(Cursor cursor, DBColumns name) {
        int index = cursor.getColumnIndex(name.getColumnName());
        if (index == -1) {
            return null;
        } else {
            if (name.getClazz().equals(Long.class)) {
                return cursor.getLong(index);
            } else if (name.getClazz().equals(String.class)) {
                return cursor.getString(index);
            } else if (name.getClazz().equals(Integer.class)) {
                return cursor.getInt(index);
            } else if (name.getClazz().equals(Boolean.class)) {
                return cursor.getInt(index) == 1;
            } else {
                return null;
            }
        }
    }


}
