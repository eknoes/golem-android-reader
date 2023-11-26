package de.eknoes.inofficialgolem.entities;

public class QueryRequest {
    String tableName, selection, groupBy, having, orderBy, limit, sort;
    String[] selectionArgs;

    public QueryRequest(String tableName, String selection, String groupBy, String having, String orderBy, String[] selectionArgs, String limit, String sort) {
        this.tableName = tableName;
        this.selection = selection;
        this.groupBy = groupBy;
        this.having = having;
        this.orderBy = orderBy;
        this.selectionArgs = selectionArgs;
        this.sort = sort;
        this.limit = limit;
    }

    public QueryRequest() {
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getSelection() {
        return selection;
    }

    public void setSelection(String selection) {
        this.selection = selection;
    }

    public String getGroupBy() {
        return groupBy;
    }

    public void setGroupBy(String groupBy) {
        this.groupBy = groupBy;
    }

    public String getHaving() {
        return having;
    }

    public void setHaving(String having) {
        this.having = having;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }

    public String[] getSelectionArgs() {
        return selectionArgs;
    }

    public void setSelectionArgs(String[] selectionArgs) {
        this.selectionArgs = selectionArgs;
    }

    public String getLimit() {
        return limit;
    }

    public void setLimit(String limit) {
        this.limit = limit;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    public static class QueryRequestBuilder {
        String tableName, selection, groupBy, having, orderBy, limit, sort;
        String[] selectionArgs;
        int count;

        public QueryRequestBuilder() {
        }

        public QueryRequestBuilder withTableName(String tableName) {
            this.tableName = tableName;
            return this;
        }

        public QueryRequestBuilder withSelection(String selection) {
            this.selection = selection;
            return this;
        }

        public QueryRequestBuilder withGroupBy(String groupBy) {
            this.groupBy = groupBy;
            return this;
        }

        public QueryRequestBuilder withHaving(String having) {
            this.having = having;
            return this;
        }

        public QueryRequestBuilder withOrderBy(String orderBy) {
            this.orderBy = orderBy;
            return this;
        }

        public QueryRequestBuilder withSelectionArgs(String[] selectionArgs) {
            this.selectionArgs = selectionArgs;
            return this;
        }

        public QueryRequestBuilder withLimit(String limit) {
            this.limit = limit;
            return this;
        }

        public QueryRequestBuilder withSort(String sort) {
            this.sort = sort;
            return this;
        }

        public QueryRequest build() {
            QueryRequest queryRequest = new QueryRequest();
            queryRequest.tableName = this.tableName;
            queryRequest.selection = this.selection;
            queryRequest.groupBy = this.groupBy;
            queryRequest.having = this.having;
            queryRequest.orderBy = this.orderBy;
            queryRequest.selectionArgs = this.selectionArgs;
            queryRequest.limit = this.limit;
            queryRequest.sort = this.sort;
            return queryRequest;
        }


    }
}
