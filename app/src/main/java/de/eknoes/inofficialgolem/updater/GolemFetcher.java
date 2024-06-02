package de.eknoes.inofficialgolem.updater;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import androidx.room.Room;

import com.android.volley.AuthFailureError;
import com.android.volley.NoConnectionError;
import com.android.volley.TimeoutError;

import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

import de.eknoes.inofficialgolem.R;
import de.eknoes.inofficialgolem.entities.Article;
import de.eknoes.inofficialgolem.entities.DATABASES;
import de.eknoes.inofficialgolem.utils.ArticleDao;
import de.eknoes.inofficialgolem.utils.ArticleDatabase;

/**
 * Created by soenke on 27.08.16.
 */
public class GolemFetcher extends AsyncTaskExecutorService<Void> {
    private static final String TAG = "GolemFetcher";
    private final GolemUpdater[] updater;
    private final WeakReference<Context> context;
    private final Callable<Void> notifier;

    public GolemFetcher(Context context, Callable<Void> notifier) {
        this.context = new WeakReference<>(context);
        this.notifier = notifier;
        if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("has_abo", false)) {
            updater = new GolemUpdater[]{new ArticleUpdater(context, true), new ArticleUpdater(context, false)};
        } else {
            updater = new GolemUpdater[]{new ArticleUpdater(context, false)};
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Context ctx = context.get();
        if (ctx != null) {
            ConnectivityManager connMgr = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = null;
            if (connMgr != null) {
                networkInfo = connMgr.getActiveNetworkInfo();
            }
        }
    }

    @Override
    protected FETCH_STATE doInBackground(Void unused) {
        Context ctx = context.get();
        if (ctx != null) {
            ConnectivityManager connMgr = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = null;
            if (connMgr != null) {
                networkInfo = connMgr.getActiveNetworkInfo();
            }
            if (networkInfo != null && networkInfo.isConnected()) {
                FETCH_STATE result = FETCH_STATE.SUCCESS;
                for (int i = 0; i < updater.length; i++) {
                    GolemUpdater u = updater[i];
                    try {
                        List<Article> items = u.getItems();
                        if (items != null && !items.isEmpty()) {
                            // Only insert new articles for the first updater (AboKey updater needs to be first in the list)
                            writeArticles(items, i == 0);
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
    protected void onPostExecute(FETCH_STATE finished) {

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
        if (ctx != null) {
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

    private void writeArticles(List<Article> articles, boolean insertNew) {
        ArticleDatabase database = Room.databaseBuilder(context.get(), ArticleDatabase.class, DATABASES.ARTICLE.name()).build();
        ArticleDao dao = database.articleDao();

        for (Article item : articles) {
            if (insertNew) {
                Log.d(TAG, "doInBackground: Creating new article with Title " + item.getTitle());
                dao.addArticle(item);
            }
        }
    }

    enum FETCH_STATE {SUCCESS, NO_CONNECTION, TIMEOUT, ABO_INVALID, UNDEFINED_ERROR}
}
