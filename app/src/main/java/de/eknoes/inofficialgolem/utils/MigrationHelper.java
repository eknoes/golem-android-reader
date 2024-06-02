package de.eknoes.inofficialgolem.utils;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;

import androidx.room.Room;

import java.util.ArrayList;
import java.util.List;

import de.eknoes.inofficialgolem.entities.Article;
import de.eknoes.inofficialgolem.entities.DATABASES;

public class MigrationHelper {

    private static boolean checkOldSqlLiteDBExists(Context context) {
        try (SQLiteDatabase database = getOldDatabase(context)) {
            database.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean migrateOldDatabase(Context context) {
        if (checkOldSqlLiteDBExists(context)) {
            List<Article> oldArticles = getArticlesFromOldDatabase(context);
            writeOldArticles(context, oldArticles);
            return deleteOldDatabase(context);
        }
        return false;
    }

    private static List<Article> getArticlesFromOldDatabase(Context context) {
        List<Article> articles = new ArrayList<>();
        try (SQLiteDatabase database = getOldDatabase(context);
             Cursor cursor = database.query("articles", new String[]{"_id","title","subheading","teaser","url","commenturl","commentnr","img","date","fulltext","offline_available"}, null, null, null, null, null)) {
            if(cursor.getCount() > 0){
                cursor.moveToFirst();
                while (cursor.moveToNext()) {
                    Article article = Article.ArticleBuilder.anArticle()
                            .withId(cursor.getInt(cursor.getColumnIndexOrThrow("_id")))
                            .withTitle(cursor.getString(cursor.getColumnIndexOrThrow("title")))
                            .withSubHeadline(cursor.getString(cursor.getColumnIndexOrThrow("subheading")))
                            .withTeaser(cursor.getString(cursor.getColumnIndexOrThrow("teaser")))
                            .withArticleUrl(cursor.getString(cursor.getColumnIndexOrThrow("url")))
                            .withCommentUrl(cursor.getString(cursor.getColumnIndexOrThrow("commenturl")))
                            .withCommentNr(cursor.getString(cursor.getColumnIndexOrThrow("commentnr")))
                            .withImgUrl(cursor.getString(cursor.getColumnIndexOrThrow("img")))
                            .withDate(cursor.getString(cursor.getColumnIndexOrThrow("date")))
                            .withFullText(cursor.getString(cursor.getColumnIndexOrThrow("fulltext")))
                            .withOffline(cursor.getInt(cursor.getColumnIndexOrThrow("offline_available")) == 1)
                            .build();
                    articles.add(article);
                }
            }
        }
        return articles;
    }

    private static void writeOldArticles(Context context, List<Article> articles){
        ArticleDatabase database = Room.databaseBuilder(context, ArticleDatabase.class, DATABASES.ARTICLE.name()).build();
        ArticleDao dao = database.articleDao();
        AsyncTask.execute(() -> {
            dao.addArticle(articles);
        });
    }

    private static boolean deleteOldDatabase(Context context){
        return SQLiteDatabase.deleteDatabase(context.getDatabasePath("FeedReader.db"));
    }

    private static SQLiteDatabase getOldDatabase(Context context) throws SQLiteException {
        return SQLiteDatabase.openDatabase(context.getDatabasePath("FeedReader.db").getPath(), null, SQLiteDatabase.OPEN_READONLY);
    }
}
