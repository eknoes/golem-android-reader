package de.eknoes.inofficialgolem.updater;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
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

import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by soenke on 27.08.16.
 */
public class GolemFetcher extends AsyncTask<Void, Float, GolemFetcher.FETCH_STATE> {
    private static final String TAG = "GolemFetcher";
    private SQLiteDatabase db;
    private final WeakReference<ProgressBar> mProgress;
    private final GolemUpdater[] updater;
    private final WeakReference<Context> context;
    private final Callable<Void> notifier;

    public GolemFetcher(Context context, ProgressBar mProgress, Callable<Void> notifier) {
        try {
            this.db = FeedReaderDbHelper.getInstance(context.getApplicationContext()).getWritableDatabase();
        } catch (SQLException exception) {
            Log.e(TAG, "GolemFetcher: Could not open Database: ", exception);
            Toast.makeText(context, R.string.error_database, Toast.LENGTH_LONG).show();
            this.cancel(true);
        }
        this.mProgress = new WeakReference<>(mProgress);
        this.context = new WeakReference<>(context);
        this.notifier = notifier;
        if(PreferenceManager.getDefaultSharedPreferences(context).getBoolean("has_abo", false)) {
            updater = new GolemUpdater[]{new articleUpdater(context, false), new articleUpdater(context, true)};
        } else {
            updater = new GolemUpdater[]{new articleUpdater(context, false)};
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Context ctx = context.get();
        if(ctx != null) {
            ConnectivityManager connMgr = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = null;
            if (connMgr != null) {
                networkInfo = connMgr.getActiveNetworkInfo();
            }
            if (networkInfo != null && networkInfo.isConnected()) {
                ProgressBar progressBar = mProgress.get();
                if(progressBar != null) {
                    progressBar.setProgress(1);
                    progressBar.setIndeterminate(true);
                    progressBar.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    @Override
    protected GolemFetcher.FETCH_STATE doInBackground(Void... voids) {
        Context ctx = context.get();
        if(ctx != null) {
            ConnectivityManager connMgr = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = null;
            if (connMgr != null) {
                networkInfo = connMgr.getActiveNetworkInfo();
            }
            if (networkInfo != null && networkInfo.isConnected()) {
                FETCH_STATE result = FETCH_STATE.SUCCESS;
                for (GolemUpdater u : updater) {
                    try {
                        List<GolemItem> items = u.getItems();
                        if (items != null && items.size() != 0) {
                            writeArticles(items);
                        } else {
                            Log.d(TAG, "doInBackground: Updater did not return items");
                        }
                    } catch (TimeoutError e) {
                        result = FETCH_STATE.TIMEOUT;
                    } catch (NoConnectionError e) {
                        result = FETCH_STATE.NO_CONNECTION;
                    } catch (AuthFailureError authFailureError) {
                        result = FETCH_STATE.ABO_INVALID;
                    }
                }

                if (result == FETCH_STATE.SUCCESS) {
                    PreferenceManager.getDefaultSharedPreferences(ctx).edit().putLong("last_refresh", new Date().getTime()).apply();
                }
                return result;
            }
            return FETCH_STATE.NO_CONNECTION;
        }
        return FETCH_STATE.UNDEFINED_ERROR;
    }

    @Override
    protected void onPostExecute(GolemFetcher.FETCH_STATE finished) {
        super.onPostExecute(finished);
        ProgressBar progressBar = mProgress.get();
        if(progressBar != null) {
            progressBar.setVisibility(View.GONE);
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
        Context ctx = context.get();
        if(ctx != null) {
            Toast.makeText(ctx, msgString, Toast.LENGTH_SHORT).show();
            if (finished == FETCH_STATE.ABO_INVALID) {
                PreferenceManager.getDefaultSharedPreferences(ctx).edit().putBoolean("has_abo", false).apply();
                Toast.makeText(ctx, R.string.golem_token_reset, Toast.LENGTH_SHORT).show();
            }
        }
        try {
            notifier.call();
        } catch (Exception e) {
            Log.w(TAG, "onPostExecute: " + e.getMessage());
        }
    }

    private void writeArticles(List<GolemItem> articles) {
        for (GolemItem item : articles) {
            if (isCancelled() || db == null) {
                return;
            }

            int id = item.getId();

            if (item.getUrl().startsWith("http://")) {
                item.setUrl(item.getUrl().replace("http://", "https://"));
            }

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
            }

            cursor.close();



            ContentValues values = new ContentValues();

            values.put(FeedReaderContract.Article.COLUMN_NAME_URL, item.getUrl());

            if (item.hasProp(GolemItem.ItemProperties.TITLE)) {
                String[] titles = item.getProp(GolemItem.ItemProperties.TITLE).split(":");
                if (titles.length != 2) {
                    values.put(FeedReaderContract.Article.COLUMN_NAME_TITLE, item.getProp(GolemItem.ItemProperties.TITLE));
                } else {
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

            if (item.hasProp(GolemItem.ItemProperties.IMG_URL)) {
                values.put(FeedReaderContract.Article.COLUMN_NAME_IMG, item.getProp(GolemItem.ItemProperties.IMG_URL));
            }

            if (item.hasProp(GolemItem.ItemProperties.COMMENT_URL)) {
                values.put(FeedReaderContract.Article.COLUMN_NAME_COMMENTURL, item.getProp(GolemItem.ItemProperties.COMMENT_URL));
            }

            if (id != 0) {
                Log.d(TAG, "doInBackground: Updating article with id " + id + ": Date " + item.getProp(GolemItem.ItemProperties.DATE));
                db.update(FeedReaderContract.Article.TABLE_NAME, values, FeedReaderContract.Article._ID + "='" + id + "'", null);
            } else {
                Log.d(TAG, "doInBackground: Creating new article with Title " + item.getProp(GolemItem.ItemProperties.TITLE));
                db.insert(FeedReaderContract.Article.TABLE_NAME, null, values);
            }
        }
    }

    enum FETCH_STATE {SUCCESS, NO_CONNECTION, TIMEOUT, ABO_INVALID, UNDEFINED_ERROR}
}
