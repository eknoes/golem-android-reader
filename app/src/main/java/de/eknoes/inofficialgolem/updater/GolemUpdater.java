package de.eknoes.inofficialgolem.updater;

import android.content.Context;
import com.android.volley.AuthFailureError;
import com.android.volley.NoConnectionError;
import com.android.volley.TimeoutError;

import java.util.List;

import de.eknoes.inofficialgolem.entities.Article;

/**
 * Created by soenke on 20.04.16.
 */
abstract class GolemUpdater {
    final Context context;
    final String TAG = this.getClass().getCanonicalName();

    /**
     * Class that fetches data from Golem RSS Feed, e.g. articles or videos
     *
     */
    GolemUpdater(Context context) {
        this.context = context;
    }

    /**
     * Fetches the data
     *
     * @return List of fetched items
     * @throws TimeoutError on Timeout
     * @throws NoConnectionError e.g. on Connection Error
     * @throws AuthFailureError e.g. on Invalid Abo Key
     */
    public abstract List<Article> getItems() throws TimeoutError, NoConnectionError, AuthFailureError;
}
