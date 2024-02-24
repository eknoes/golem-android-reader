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
}
