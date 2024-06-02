package de.eknoes.inofficialgolem.utils;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import de.eknoes.inofficialgolem.entities.Article;

@Database(entities = {Article.class}, version = 1)
public abstract class ArticleDatabase extends RoomDatabase {
    public abstract ArticleDao articleDao();
}
