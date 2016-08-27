package de.eknoes.inofficialgolem.updater;

import android.content.Context;
import com.android.volley.AuthFailureError;
import com.android.volley.NoConnectionError;
import com.android.volley.TimeoutError;

import java.util.List;

/**
 * Created by soenke on 20.04.16.
 */
public abstract class GolemUpdater {
    protected Context context;
    protected String TAG = this.getClass().getCanonicalName();

    /**
     * Class that fetches data from Golem API, e.g. articles or videos
     * @param context
     */
    GolemUpdater(Context context) {
        this.context = context;
    }

    /**
     * Fetches the data
     * @return List of fetched items
     * @throws TimeoutError
     * @throws NoConnectionError
     * @throws AuthFailureError
     */
    public abstract List<GolemItem> getItems() throws TimeoutError, NoConnectionError, AuthFailureError;
}
