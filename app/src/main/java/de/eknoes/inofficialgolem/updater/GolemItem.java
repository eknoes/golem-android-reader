package de.eknoes.inofficialgolem.updater;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by soenke on 20.04.16.
 */
public class GolemItem {
    private int id = 0;
    private String url;
    private final Map<ItemProperties, String> properties = new HashMap<>();

    boolean hasProp(ItemProperties key) {
        return properties.containsKey(key);
    }

    String getProp(ItemProperties key) {
        if (hasProp(key)) {
            return properties.get(key);
        } else {
            return null;
        }
    }

    public void setProp(ItemProperties key, String value) {
        properties.put(key, value);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public enum ItemProperties {TITLE, TEASER, IMG_URL, COMMENT_URL, COMMENT_NR, DATE, FULLTEXT, OFFLINE_AVAILABLE, HAS_MEDIA_FULLTEXT, ALREADY_READ}
}
