package de.eknoes.inofficialgolem.updater;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.android.volley.AuthFailureError;
import com.android.volley.NoConnectionError;
import com.android.volley.TimeoutError;
import de.eknoes.inofficialgolem.FeedReaderContract;
import de.eknoes.inofficialgolem.FeedReaderDbHelper;
import de.eknoes.inofficialgolem.R;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by soenke on 27.08.16.
 */
public class GolemFetcher extends AsyncTask<Void, Float, GolemFetcher.FETCH_STATE> {
    private final SQLiteDatabase db;
    private final ProgressBar mProgress;
    private static final String TAG = "GolemFetcher";
    private final GolemUpdater[] updater;
    private final Context context;
    private final Callable<Void> notifier;

    enum FETCH_STATE {SUCCESS, NO_CONNECTION, TIMEOUT, ABO_INVALID, UNDEFINED_ERROR}

    public GolemFetcher(Context context, ProgressBar mProgress, Callable<Void> notifier) {
        this.db = FeedReaderDbHelper.getInstance(context).getWritableDatabase();
        this.mProgress = mProgress;
        this.context = context;
        this.notifier = notifier;
        updater = new GolemUpdater[]{new NewestArticleUpdater(context), new AboArticleUpdater(context)};
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected() && mProgress != null) {
            mProgress.setProgress(1);
            mProgress.setIndeterminate(true);
            mProgress.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected GolemFetcher.FETCH_STATE doInBackground(Void... voids) {
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            FETCH_STATE result = FETCH_STATE.SUCCESS;
            for (GolemUpdater u : updater) {
                try {
                    writeArticles(u.getItems());
                } catch (TimeoutError e) {
                    result = FETCH_STATE.TIMEOUT;
                } catch (NoConnectionError e) {
                    result = FETCH_STATE.NO_CONNECTION;
                } catch (AuthFailureError authFailureError) {
                    result = FETCH_STATE.ABO_INVALID;
                }
            }

            if(result == FETCH_STATE.SUCCESS) {
                PreferenceManager.getDefaultSharedPreferences(context).edit().putLong("last_refresh", new Date().getTime()).apply();
            }
            return result;
        }
        return FETCH_STATE.NO_CONNECTION;
    }

    @Override
    protected void onPostExecute(GolemFetcher.FETCH_STATE finished) {
        super.onPostExecute(finished);
        if (mProgress != null) {
            mProgress.setVisibility(View.GONE);
        }
        int msgString;
        switch (finished) {
            case SUCCESS:
                msgString = R.string.refresh_success;
                break;
            case NO_CONNECTION:
                msgString = R.string.refresh_error_connection;
                break;
            case TIMEOUT:
                msgString = R.string.refresh_error_timeout;
                break;
            case ABO_INVALID:
                msgString = R.string.refresh_error_invalid_abo;
                break;
            case UNDEFINED_ERROR:
            default:
                msgString = R.string.refresh_error_undefined;
        }
        Toast.makeText(context, msgString, Toast.LENGTH_SHORT).show();
        if (finished == FETCH_STATE.ABO_INVALID) {
            PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean("has_abo", false).apply();
            Toast.makeText(context, R.string.golem_token_reset, Toast.LENGTH_SHORT).show();
        }
        try {
            notifier.call();
        } catch (Exception e) {
            Log.w(TAG, "onPostExecute: " + e.getMessage());
        }
    }

    private boolean writeArticles(List<GolemItem> articles) {
        for (GolemItem item : articles) {
            if (item.getType() == GolemItem.Type.ARTICLE) {
                int id = item.getId();
                boolean createNew = false;
                if (item.getId() == 0 && item.getUrl() != null) {
                    if (item.getUrl().endsWith("-rss.html")) {
                        item.setUrl(item.getUrl().substring(0, (item.getUrl().length() - "-rss.html".length())) + ".html");
                    }

                    String[] cols = {FeedReaderContract.Article.COLUMN_NAME_ID};
                    Cursor cursor = db.query(
                            FeedReaderContract.Article.TABLE_NAME,
                            cols,
                            "url='" + item.getUrl() + "'",
                            null,
                            null,
                            null,
                            null);
                    if (cursor.getCount() > 0) {
                        cursor.moveToFirst();
                        id = cursor.getInt(cursor.getColumnIndex(FeedReaderContract.Article.COLUMN_NAME_ID));
                        cursor.close();
                    } else {
                        Log.w(TAG, "doInBackground: URL given, but nothing found:" + item.getUrl());
                        cursor.close();
                        continue;
                    }
                } else if (id != 0) {
                    String[] cols = {FeedReaderContract.Article.COLUMN_NAME_ID};
                    Cursor cursor = db.query(
                            FeedReaderContract.Article.TABLE_NAME,
                            cols,
                            "id='" + id + "'",
                            null,
                            null,
                            null,
                            null);
                    if (cursor.getCount() == 0) {
                        createNew = true;
                    }
                    cursor.close();
                } else {
                    Log.d(TAG, "doInBackground: No id given. Continue");
                    continue;
                }

                ContentValues values = new ContentValues();

                values.put(FeedReaderContract.Article.COLUMN_NAME_ID, id);
                values.put(FeedReaderContract.Article.COLUMN_NAME_URL, item.getUrl());

                if (item.hasProp(GolemItem.ItemProperties.TITLE)) {
                    String[] titles = item.getProp(GolemItem.ItemProperties.TITLE).split(":");
                    if (titles.length == 1) {
                        values.put(FeedReaderContract.Article.COLUMN_NAME_TITLE, titles[0]);
                    } else if (titles.length == 2) {
                        values.put(FeedReaderContract.Article.COLUMN_NAME_TITLE, titles[1].trim());
                        values.put(FeedReaderContract.Article.COLUMN_NAME_SUBHEADING, titles[0].trim());
                    }
                }
                if (item.hasProp(GolemItem.ItemProperties.TEASER)) {
                    values.put(FeedReaderContract.Article.COLUMN_NAME_TEASER, item.getProp(GolemItem.ItemProperties.TEASER));
                }

                if (item.hasProp(GolemItem.ItemProperties.DATE)) {
                    values.put(FeedReaderContract.Article.COLUMN_NAME_DATE, item.getProp(GolemItem.ItemProperties.DATE));
                }

                if (item.hasProp(GolemItem.ItemProperties.FULLTEXT)) {
                    values.put(FeedReaderContract.Article.COLUMN_NAME_FULLTEXT, item.getProp(GolemItem.ItemProperties.FULLTEXT));
                    values.put(FeedReaderContract.Article.COLUMN_NAME_OFFLINE, item.getProp(GolemItem.ItemProperties.OFFLINE_AVAILABLE));
                }

                if (item.hasProp(GolemItem.ItemProperties.OFFLINE_AVAILABLE)) {
                    values.put(FeedReaderContract.Article.COLUMN_NAME_OFFLINE, Boolean.valueOf(item.getProp(GolemItem.ItemProperties.OFFLINE_AVAILABLE)));
                }

                if (item.hasProp(GolemItem.ItemProperties.AUTHORS)) {
                    values.put(FeedReaderContract.Article.COLUMN_NAME_AUTHORS, item.getProp(GolemItem.ItemProperties.AUTHORS));
                }

                if (item.hasProp(GolemItem.ItemProperties.IMG_URL)) {
                    values.put(FeedReaderContract.Article.COLUMN_NAME_IMG, item.getProp(GolemItem.ItemProperties.IMG_URL));
                }

                if (!createNew) {
                    Log.d(TAG, "doInBackground: Updating article with id " + Integer.toString(id));
                    db.update(FeedReaderContract.Article.TABLE_NAME, values, "id='" + Integer.toString(id) + "'", null);
                } else {
                    Log.d(TAG, "doInBackground: Creating new article with Title " + item.getProp(GolemItem.ItemProperties.TITLE));
                    db.insert(FeedReaderContract.Article.TABLE_NAME, null, values);
                }

                if (isCancelled()) {
                    return false;
                }
            }
        }
        return true;
    }
}
