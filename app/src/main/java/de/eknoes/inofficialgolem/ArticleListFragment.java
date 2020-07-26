package de.eknoes.inofficialgolem;

import android.content.Context;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.LruCache;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.Volley;
import de.eknoes.inofficialgolem.updater.GolemFetcher;
import org.jetbrains.annotations.NotNull;

import java.text.DateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.Callable;

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
                refresh();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        long last_refresh = PreferenceManager.getDefaultSharedPreferences(getContext()).getLong("last_refresh", 0);
        int refresh_limit = PreferenceManager.getDefaultSharedPreferences(getContext()).getInt("refresh_limit", 5); //Saved as minutes


        if (refresh_limit != 0 && last_refresh + (refresh_limit * 1000 * 60) < new Date().getTime()) {
            Log.d(TAG, "onCreate: Refresh, last refresh was " + ((new Date().getTime() - last_refresh) / 1000) + "sec ago");
            refresh();
        } else {
            Log.d(TAG, "onCreate: No refresh, last refresh was " + (new Date().getTime() - last_refresh) / 1000 + "sec ago");
        }
    }

    void refresh() {
        if (fetcher == null || fetcher.getStatus() != AsyncTask.Status.RUNNING) {
            mSwipeLayout.setRefreshing(true);
            fetcher = new GolemFetcher(Objects.requireNonNull(getContext()), new Callable<Void>() {
                @Override
                public Void call() {
                    if(listAdapter != null) {
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
        private final SQLiteDatabase db;
        private final ImageLoader imgLoader;
        private final Context context;
        private Cursor cursor;

        ArticleAdapter() {
            super();
            context = Objects.requireNonNull(getContext()).getApplicationContext();
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            FeedReaderDbHelper dbHelper = FeedReaderDbHelper.getInstance(context);
            db = dbHelper.getReadableDatabase();
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
            String[] columns = {
                    FeedReaderContract.Article.COLUMN_NAME_ID,
                    FeedReaderContract.Article.COLUMN_NAME_TITLE,
                    FeedReaderContract.Article.COLUMN_NAME_SUBHEADING,
                    FeedReaderContract.Article.COLUMN_NAME_TEASER,
                    FeedReaderContract.Article.COLUMN_NAME_DATE,
                    FeedReaderContract.Article.COLUMN_NAME_IMG,
                    FeedReaderContract.Article.COLUMN_NAME_URL,
                    FeedReaderContract.Article.COLUMN_NAME_COMMENTURL,
                    FeedReaderContract.Article.COLUMN_NAME_COMMENTNR,
                    FeedReaderContract.Article.COLUMN_NAME_OFFLINE,
                    FeedReaderContract.Article.COLUMN_NAME_FULLTEXT
            };

            String sort = FeedReaderContract.Article.COLUMN_NAME_DATE + " DESC";
            String limit = "0, " + PreferenceManager.getDefaultSharedPreferences(context).getInt("article_limit", 200);


            cursor = db.query(
                    FeedReaderContract.Article.TABLE_NAME,
                    columns,
                    null,
                    null,
                    null,
                    null,
                    sort,
                    limit);
        }

        @Override
        public int getCount() {
            return cursor.getCount();
        }

        @Override
        public Article getItem(int position) {
            cursor.moveToPosition(position);
            Article a = new Article();
            a.setId(cursor.getInt(cursor.getColumnIndex(FeedReaderContract.Article.COLUMN_NAME_ID)));
            a.setTitle(cursor.getString(cursor.getColumnIndex(FeedReaderContract.Article.COLUMN_NAME_TITLE)));
            a.setSubheadline(cursor.getString(cursor.getColumnIndex(FeedReaderContract.Article.COLUMN_NAME_SUBHEADING)));
            a.setTeaser(cursor.getString(cursor.getColumnIndex(FeedReaderContract.Article.COLUMN_NAME_TEASER)));
            a.setDate(cursor.getLong(cursor.getColumnIndex(FeedReaderContract.Article.COLUMN_NAME_DATE)));
            a.setImgUrl(cursor.getString(cursor.getColumnIndex(FeedReaderContract.Article.COLUMN_NAME_IMG)));
            a.setCommentUrl(cursor.getString(cursor.getColumnIndex(FeedReaderContract.Article.COLUMN_NAME_COMMENTURL)));
            a.setCommentNr(cursor.getString(cursor.getColumnIndex(FeedReaderContract.Article.COLUMN_NAME_COMMENTNR)));
            a.setUrl(cursor.getString(cursor.getColumnIndex(FeedReaderContract.Article.COLUMN_NAME_URL)));
            a.setOffline(cursor.getInt(cursor.getColumnIndex(FeedReaderContract.Article.COLUMN_NAME_OFFLINE)) == 1);
            a.setFulltext(cursor.getString(cursor.getColumnIndex(FeedReaderContract.Article.COLUMN_NAME_FULLTEXT)));
            return a;
        }

        @Override
        public long getItemId(int position) {
            cursor.moveToPosition(position);
            return cursor.getLong(cursor.getColumnIndex(FeedReaderContract.Article.COLUMN_NAME_ID));
        }

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

            image.setImageUrl(art.getImgUrl(), imgLoader);
            return view;
        }


    }


}
