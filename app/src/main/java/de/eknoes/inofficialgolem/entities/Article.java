package de.eknoes.inofficialgolem.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(indices = {@Index(value = {"article_url"}, unique = true)})
public class Article {
    @PrimaryKey
    int id;
    @ColumnInfo(name = "title")
    String title;
    @ColumnInfo(name = "article_url")
    String articleUrl;
    @ColumnInfo(name = "sub_headline")
    String subHeadline;
    @ColumnInfo(name = "teaser")
    String teaser;
    @ColumnInfo(name = "offline")
    boolean isOffline;
    @ColumnInfo(name = "full_text")
    String fullText;
    @ColumnInfo(name = "date")
    String date;
    @ColumnInfo(name = "image_url")
    String imgUrl;
    @ColumnInfo(name = "comment_url")
    String commentUrl;
    @ColumnInfo(name = "comment_number")
    String commentNr;
    @ColumnInfo(name = "already_read")
    boolean alreadyRead;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        String[] splitTitle = title.split(":");
        if (splitTitle.length == 2) {
            this.title = splitTitle[1].trim();
            this.subHeadline = splitTitle[0].trim();
        }
    }

    public String getArticleUrl() {
        return articleUrl;
    }

    public void setArticleUrl(String articleUrl) {
        this.articleUrl = articleUrl;
    }

    public String getSubHeadline() {
        return subHeadline;
    }

    public void setSubHeadline(String subHeadline) {
        this.subHeadline = subHeadline;
    }

    public String getTeaser() {
        return teaser;
    }

    public void setTeaser(String teaser) {
        this.teaser = teaser;
    }

    public boolean isOffline() {
        return isOffline;
    }

    public void setOffline(boolean offline) {
        isOffline = offline;
    }

    public String getFullText() {
        return fullText;
    }

    public void setFullText(String fullText) {
        this.fullText = fullText;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getCommentUrl() {
        return commentUrl;
    }

    public void setCommentUrl(String commentUrl) {
        this.commentUrl = commentUrl;
    }

    public String getCommentNr() {
        return commentNr;
    }

    public void setCommentNr(String commentNr) {
        this.commentNr = commentNr;
    }

    public boolean isAlreadyRead() {
        return alreadyRead;
    }

    public void setAlreadyRead(boolean alreadyRead) {
        this.alreadyRead = alreadyRead;
    }


    public static final class ArticleBuilder {
        private Article article;

        private ArticleBuilder() {
            article = new Article();
        }

        public static ArticleBuilder anArticle() {
            return new ArticleBuilder();
        }

        public ArticleBuilder withId(int id) {
            article.setId(id);
            return this;
        }

        public ArticleBuilder withTitle(String title) {
            article.setTitle(title);
            return this;
        }

        public ArticleBuilder withArticleUrl(String articleUrl) {
            article.setArticleUrl(articleUrl);
            return this;
        }

        public ArticleBuilder withSubHeadline(String subHeadline) {
            article.setSubHeadline(subHeadline);
            return this;
        }

        public ArticleBuilder withTeaser(String teaser) {
            article.setTeaser(teaser);
            return this;
        }

        public ArticleBuilder withFullText(String fullText) {
            article.setFullText(fullText);
            return this;
        }

        public ArticleBuilder withDate(String date) {
            article.setDate(date);
            return this;
        }

        public ArticleBuilder withImgUrl(String imgUrl) {
            article.setImgUrl(imgUrl);
            return this;
        }

        public ArticleBuilder withCommentUrl(String commentUrl) {
            article.setCommentUrl(commentUrl);
            return this;
        }

        public ArticleBuilder withCommentNr(String commentNr) {
            article.setCommentNr(commentNr);
            return this;
        }

        public ArticleBuilder withAlreadyRead(boolean alreadyRead) {
            article.setAlreadyRead(alreadyRead);
            return this;
        }

        public ArticleBuilder withOffline(boolean offline) {
            article.setOffline(offline);
            return this;
        }

        public Article build() {
            return article;
        }
    }
}
