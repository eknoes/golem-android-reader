package de.eknoes.inofficialgolem.updater;

import android.content.Context;
import android.preference.PreferenceManager;
import android.util.Log;
import com.android.volley.NoConnectionError;
import com.android.volley.RequestQueue;
import com.android.volley.TimeoutError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.Volley;
import de.eknoes.inofficialgolem.R;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Fetches articles and their meta information from the golem api
 */
public class NewestArticleUpdater extends GolemUpdater {

    public final String LOG_TAG = this.getClass().getCanonicalName();
    private final RequestQueue queue;


    public NewestArticleUpdater(Context c) {
        super(c);
        queue = Volley.newRequestQueue(context);
    }


    private String buildArticleAPIUrl(String key) {
        Integer limit = PreferenceManager.getDefaultSharedPreferences(context).getInt("article_limit", 50);
        if (limit > 50) {
            limit = 50;
        }
        return "http://api.golem.de/api/article/" + key + "/?key=" + context.getString(R.string.golem_api_key) + "&format=json&limit=" + Integer.toString(limit);
    }

    @Override
    public List<GolemItem> getItems() throws NoConnectionError, TimeoutError {
        List<GolemItem> items = new LinkedList<>();


        RequestFuture<JSONObject> future = RequestFuture.newFuture();
        JsonObjectRequest request = new JsonObjectRequest(buildArticleAPIUrl("latest"), future, future);
        queue.add(request);

        try {
            JSONObject response = future.get(60, TimeUnit.SECONDS);
            if (response.getBoolean("success")) {
                Log.d(TAG, "doInBackground: Got positive Response");
                JSONArray data = response.getJSONArray("data");
                for (int i = 0; i < data.length(); i++) {
                    JSONObject jsonItem = data.getJSONObject(i);
                    GolemItem item = new GolemItem();

                    item.setId(jsonItem.getInt("articleid"));
                    item.setUrl(jsonItem.getString("url"));
                    item.setType(GolemItem.Type.ARTICLE);
                    item.setProp(GolemItem.ItemProperties.TEASER, jsonItem.getString("abstracttext"));
                    item.setProp(GolemItem.ItemProperties.TITLE, jsonItem.getString("headline"));
                    item.setProp(GolemItem.ItemProperties.DATE, jsonItem.getString("date"));
                    item.setProp(GolemItem.ItemProperties.IMG_URL, jsonItem.getJSONObject("leadimg").getString("url"));

                    items.add(item);
                }
            } else {
                Log.w(TAG, "doInBackground: Got negative response " + response.getInt("errorCode") + ": " + response.getString("errorMessage"));
            }

        } catch (InterruptedException | TimeoutException | JSONException e) {
            Log.e(TAG, "doInBackground: " + e.getMessage());
            throw new TimeoutError();
        } catch (ExecutionException e) {
            Log.e(TAG, "doInBackground: " + e.getMessage());
            if (e.getCause() instanceof NoConnectionError || e.getCause() instanceof TimeoutError) {
                Log.d(TAG, "doInBackground: No Connection");
                throw new NoConnectionError();
            }

            e.printStackTrace();
        }
        return items;
    }
}
