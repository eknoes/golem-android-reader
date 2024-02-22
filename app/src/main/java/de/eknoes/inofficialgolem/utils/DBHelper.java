package de.eknoes.inofficialgolem.utils;

import android.content.Context;

import androidx.room.Room;

import de.eknoes.inofficialgolem.entities.Article;
import de.eknoes.inofficialgolem.entities.DATABASES;

public class DBHelper {
    private final static String TAG = "DBHelper";

    public static void updateArticle( Context context,Article article){
        ArticleDatabase db = Room.databaseBuilder(context,ArticleDatabase.class, DATABASES.ARTICLE.name()).build();
        ArticleDao dao = db.articleDao();
        dao.updateArticle(article);
    }
}
