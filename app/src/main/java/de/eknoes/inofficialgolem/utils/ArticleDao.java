package de.eknoes.inofficialgolem.utils;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import de.eknoes.inofficialgolem.entities.Article;

@Dao
public interface ArticleDao {
    @Query("SELECT * FROM article")
    List<Article> getAllArticles();

    @Insert
    void addArticle(Article article);

    @Insert
    void addArticle(List<Article> articles);

    @Query("SELECT * FROM article where article_url = :url")
    Article getArticleByUrl(String url);

    @Query("SELECT * FROM article order by date desc limit :limit")
    List<Article> getArticlesWithLimit(int limit);

    @Update
    void updateArticle(Article article);
}
