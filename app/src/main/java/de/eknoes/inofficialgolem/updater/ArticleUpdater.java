package de.eknoes.inofficialgolem.updater;

import android.content.Context;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;
import com.android.volley.*;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import de.eknoes.inofficialgolem.entities.Article;

/**
 * Fetches articles for users with golem abo token
 */
public class ArticleUpdater extends GolemUpdater {

    private final RequestQueue queue;
    private boolean useAbo;
    private String aboKey;

    public ArticleUpdater(Context c, boolean useAbo) {
        super(c);
        queue = Volley.newRequestQueue(context);
        this.useAbo = useAbo;
        if (useAbo)
            aboKey = PreferenceManager.getDefaultSharedPreferences(context).getString("abo_key", null);

        if (aboKey == null)
            this.useAbo = false;

    }

    @Override
    public List<Article> getItems() throws TimeoutError, NoConnectionError, AuthFailureError {
        Log.d(TAG, "getItems: Fetching RSS Feed");
        RequestFuture<String> future = RequestFuture.newFuture();
        StringRequest request = new StringRequest(buildFeedURL(), future, future);
        queue.add(request);
        try {
            String response = future.get(15, TimeUnit.SECONDS);
            if(useAbo)
                return getItems(response, new GolemAboParser());
            return getItems(response, new GolemRSSParser());

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            if (e.getCause() instanceof NoConnectionError || e.getCause() instanceof TimeoutError) {
                Log.d(TAG, "getItems: No Connection");
                throw new NoConnectionError();
            } else if (e.getCause() instanceof AuthFailureError) {
                Log.d(TAG, "getItems: Invalid Abo key");
                throw new AuthFailureError();
            }
        } catch (TimeoutException e) {
            e.printStackTrace();
            throw new TimeoutError();
        }

        return new LinkedList<>();
    }

    static List<Article> getItems(String feed, GolemRSSParser parser) {
        return parser.parse(feed);
    }

    private String buildFeedURL() {
        if(!this.useAbo) {
            Log.d(TAG, "buildFeedURL: Use public RSS feed");
            return "https://rss.golem.de/rss.php?feed=RSS2.0";
        }

        Log.d(TAG, "buildFeedURL: Using private feed");
        return "https://rss.golem.de/rss_sub_media.php?token=" + Uri.encode(this.aboKey);
    }

}
