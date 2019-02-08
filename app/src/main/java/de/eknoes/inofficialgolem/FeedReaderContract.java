package de.eknoes.inofficialgolem;

import android.provider.BaseColumns;

/**
 * Created by soenke on 10.04.16.
 */
public final class FeedReaderContract {

    public FeedReaderContract() {
    }

    public static abstract class Article implements BaseColumns {
        public static final String TABLE_NAME = "articles";
        public static final String COLUMN_NAME_ID = _ID;
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_SUBHEADING = "subheading";
        public static final String COLUMN_NAME_TEASER = "teaser";
        public static final String COLUMN_NAME_URL = "url";
        public static final String COLUMN_NAME_COMMENTURL = "commenturl";
        public static final String COLUMN_NAME_COMMENTNR = "commentnr";
        public static final String COLUMN_NAME_IMG = "img";
        public static final String COLUMN_NAME_DATE = "date";
        public static final String COLUMN_NAME_FULLTEXT = "fulltext";
        public static final String COLUMN_NAME_OFFLINE = "offline_available";
    }
}

