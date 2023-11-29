package de.eknoes.inofficialgolem;

import android.content.Context;
import android.content.res.Configuration;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.Volley;

import org.jetbrains.annotations.NotNull;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

import de.eknoes.inofficialgolem.entities.DBColumns;
import de.eknoes.inofficialgolem.entities.QueryRequest;
import de.eknoes.inofficialgolem.updater.GolemFetcher;
import de.eknoes.inofficialgolem.utils.DBHelper;
import de.eknoes.inofficialgolem.utils.NetworkUtils;

/**
 * A placeholder fragment containing a simple view.
 */
public class ArticleListFragment extends Fragment {
    private static final String TAG = "ArticleListFragment";
    private GolemFetcher fetcher;
    private ArticleAdapter listAdapter;
    private SwipeRefreshLayout mSwipeLayout;
    private OnArticleSelectedListener mListener;

    public ArticleListFragment() {
        super(R.layout.fragment_articlelist);
    }

    @Override
    public void onCreateOptionsMenu(@NotNull Menu menu, @NotNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_articlelist, menu);
    }

    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        try {
            mListener = (OnArticleSelectedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnArticleSelectedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mSwipeLayout = view.findViewById(R.id.swipeRefresh);
        ListView listView = view.findViewById(R.id.articleList);

        Log.d(TAG, "onStart: Creating Article List Adapter");
        listAdapter = new ArticleAdapter();
        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mListener.onArticleSelected(listAdapter.getItem(position).getUrl(), false);
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                mListener.onArticleSelected(listAdapter.getItem(position).getUrl(), true);
                return true;
            }
        });

        mSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh(true);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        long last_refresh = PreferenceManager.getDefaultSharedPreferences(getContext()).getLong("last_refresh", 0);
        int refresh_limit = PreferenceManager.getDefaultSharedPreferences(getContext()).getInt("refresh_limit", 5); //Saved as minutes


        if (refresh_limit != 0 && last_refresh + ((long) refresh_limit * 1000 * 60) < new Date().getTime()) {
            Log.d(TAG, "onCreate: Refresh, last refresh was " + ((new Date().getTime() - last_refresh) / 1000) + "sec ago");
            refresh(false);
        } else {
            Log.d(TAG, "onCreate: No refresh, last refresh was " + (new Date().getTime() - last_refresh) / 1000 + "sec ago");
        }
    }

    void refresh(boolean force) {
        if (!force && PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("only_wifi", false)) {
            if (!NetworkUtils.hasWifiConnection(getContext())) {
                return;
            }
        }
        if (fetcher == null || fetcher.getStatus() != AsyncTask.Status.RUNNING) {
            mSwipeLayout.setRefreshing(true);
            fetcher = new GolemFetcher(requireContext(), new Callable<Void>() {
                @Override
                public Void call() {
                    if (listAdapter != null) {
                        listAdapter.notifyDataSetChanged();
                    }
                    mSwipeLayout.setRefreshing(false);
                    return null;
                }
            });
            fetcher.execute();
        }
    }


    public interface OnArticleSelectedListener {
        void onArticleSelected(String articleUrl, boolean forceWebview);

        void onArticleSelected(String articleUrl);
    }

    private class ArticleAdapter extends BaseAdapter {

        private final LayoutInflater inflater;
        private final ImageLoader imgLoader;
        private final Context context;
        private List<Article> articles;

        ArticleAdapter() {
            super();
            context = requireContext().getApplicationContext();
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            loadData();
            this.registerDataSetObserver(new DataSetObserver() {
                @Override
                public void onChanged() {
                    super.onChanged();
                    loadData();
                }
            });

            imgLoader = new ImageLoader(Volley.newRequestQueue(context), new ImageLoader.ImageCache() {
                private final LruCache<String, Bitmap> mCache = new LruCache<>(10);

                @Override
                public Bitmap getBitmap(String url) {
                    return mCache.get(url);
                }

                @Override
                public void putBitmap(String url, Bitmap bitmap) {
                    mCache.put(url, bitmap);
                }
            });
        }

        private void loadData() {
            QueryRequest queryRequest = new QueryRequest.QueryRequestBuilder()
                    .withSort(DBColumns.COLUMN_NAME_DATE + " DESC")
                    .withLimit("0, " + PreferenceManager.getDefaultSharedPreferences(context).getInt("article_limit", 200))
                    .withTableName(DBColumns.getTableName())
                    .build();

            articles = DBHelper.getArticles(queryRequest);

        }

        @Override
        public int getCount() {
            return articles.size();
        }

        @Override
        public Article getItem(int position) {
            return articles.get(position);
        }

        @Override
        public long getItemId(int position) {
            return articles.get(position).getId();
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;

            if (convertView == null) {
                view = inflater.inflate(R.layout.list_article, parent, false);
            }


            Article art = getItem(position);
            String infoText = String.format(context.getResources().getString(R.string.article_published), DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT).format(art.getDate()));

            TextView teaser = view.findViewById(R.id.articleTeaser);
            TextView title = view.findViewById(R.id.articleTitle);
            TextView subheading = view.findViewById(R.id.articleSubtitle);
            TextView info = view.findViewById(R.id.articleInfo);
            NetworkImageView image = view.findViewById(R.id.articleImage);

            title.setText(art.getTitle());
            subheading.setText(art.getSubheadline());
            teaser.setText(art.getTeaser());
            info.setText(infoText);
            if (art.getAlreadyRead().equals(true)) {
                int nightMode = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
                // Danke an Alma für die Idee mit dem Text
                switch (nightMode) {
                    case Configuration.UI_MODE_NIGHT_YES:
                        title.setTextColor(Color.parseColor("#424040"));
                        subheading.setTextColor(Color.parseColor("#424040"));
                        teaser.setTextColor(Color.parseColor("#424040"));
                        info.setTextColor(Color.parseColor("#424040"));
                        break;
                    case Configuration.UI_MODE_NIGHT_NO:
                        title.setTextColor(Color.parseColor("#8a8484"));
                        subheading.setTextColor(Color.parseColor("#8a8484"));
                        teaser.setTextColor(Color.parseColor("#8a8484"));
                        info.setTextColor(Color.parseColor("#8a8484"));
                        break;
                    default:
                        break;
                }
            }

            image.setImageUrl(art.getImgUrl(), imgLoader);
            return view;
        }


    }


}
