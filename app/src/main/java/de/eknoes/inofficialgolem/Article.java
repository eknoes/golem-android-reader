package de.eknoes.inofficialgolem;

import de.eknoes.inofficialgolem.updater.GolemItem;

import java.util.Date;

/**
 * Created by soenke on 10.04.16.
 */
class Article extends GolemItem {
    private String title;
    private String subheadline;
    private String teaser;
    private boolean offline = false;
    private String authors = null;
    private String fulltext = null;
    private Date date;
    private String imgUrl;

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubheadline() {
        if (subheadline == null) {
            return null;
        }
        return subheadline.toUpperCase();
    }

    public void setSubheadline(String subheadline) {
        this.subheadline = subheadline;
    }

    public String getTeaser() {
        return teaser;
    }

    public void setTeaser(String teaser) {
        this.teaser = teaser;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(int date) {
        this.date = new java.sql.Date((long) date * 1000);
    }

    public boolean isOffline() {
        return offline;
    }

    public void setOffline(boolean offline) {
        this.offline = offline;
    }

    public String getAuthors() {
        return authors;
    }

    public void setAuthors(String authors) {
        this.authors = authors;
    }

    public String getFulltext() {
        return fulltext;
    }

    public void setFulltext(String fulltext) {
        this.fulltext = fulltext;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
