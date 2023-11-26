package de.eknoes.inofficialgolem.entities;

import java.util.ArrayList;
import java.util.List;

public enum DBColumns {
    COLUMN_NAME_ID("_ID", Integer.class),
    COLUMN_NAME_TITLE("title", String.class),
    COLUMN_NAME_SUBHEADING("subheading", String.class),
    COLUMN_NAME_TEASER("teaser", String.class),
    COLUMN_NAME_URL("url", String.class),
    COLUMN_NAME_COMMENTURL("commenturl", String.class),
    COLUMN_NAME_COMMENTNR("commentnr", String.class),
    COLUMN_NAME_IMG("img", String.class),
    COLUMN_NAME_DATE("date", Long.class),
    COLUMN_NAME_FULLTEXT("fulltext", String.class),
    COLUMN_NAME_OFFLINE("offline_available", Boolean.class),
    COLUMN_NAME_ALREADY_READ("already_read", Boolean.class);;


    private Class clazz;
    private String name;

    DBColumns(String columnName, Class clazz) {
        this.name = columnName;
        this.clazz = clazz;
    }

    public static String getTableName() {
        return "articles";
    }

    public static String[] getColumnNames() {
        List<String> names = new ArrayList<>();
        for (DBColumns dbColumn : DBColumns.values()) {
            names.add(dbColumn.getColumnName());
        }
        String[] arr = names.toArray(new String[0]);
        return arr;
    }

    public Class getClazz() {
        return clazz;
    }

    public void setClazz(Class clazz) {
        this.clazz = clazz;
    }

    public String getColumnName() {
        return name;
    }

    public void setColumnName(String columnName) {
        this.name = columnName;
    }
}
