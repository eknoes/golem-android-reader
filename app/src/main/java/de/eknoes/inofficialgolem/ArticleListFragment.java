package de.eknoes.inofficialgolem;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.util.LruCache;
import android.view.*;
import android.widget.*;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.Volley;
import de.eknoes.inofficialgolem.updater.GolemFetcher;

import java.net.URI;
import java.text.DateFormat;
import java.util.Date;
import java.util.concurrent.Callable;

import static android.util.TypedValue.COMPLEX_UNIT_PX;

/**
 * A placeholder fragment containing a simple view.
 */
public class ArticleListFragment extends ListFragment {
    private GolemFetcher fetcher;
    private ArticleAdapter adapter;
    private ProgressBar mProgress; //Not yet implemented
    private static final String TAG = "ArticleListFragment";
    private OnArticleSelectedListener mListener;

    public interface OnArticleSelectedListener {
        public void onArticleSelected(int id, boolean forceWebview);
        public void onArticleSelected(URI externUrl);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_articlelist, menu);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (OnArticleSelectedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnArticleSelectedListener");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if(getListAdapter() == null) {
            Log.d(TAG, "onStart: Creating Article List Adapter");
            adapter = new ArticleAdapter();
            setListAdapter(adapter);
            getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    mListener.onArticleSelected((int) id, false);
                }
            });
            getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    mListener.onArticleSelected((int) id, true);
                    return true;
                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        long last_refresh = PreferenceManager.getDefaultSharedPreferences(getContext()).getLong("last_refresh", 0);
        int refresh_limit = PreferenceManager.getDefaultSharedPreferences(getContext()).getInt("refresh_limit", 5) * 1000 * 60; //Saved as minutes

        if (last_refresh + refresh_limit < new Date().getTime()) {
            Log.d(TAG, "onCreate: Refresh, last refresh was " + ((new Date().getTime() - last_refresh) / 1000) + "sec ago");
            refresh();
        } else {
            Log.d(TAG, "onCreate: No refresh, last refresh was " + (new Date().getTime() - last_refresh) / 1000 + "sec ago");
        }
        adapter.calculateZoom();
    }


    public void refresh() {
        if (fetcher == null || fetcher.getStatus() != AsyncTask.Status.RUNNING) {
            fetcher = new GolemFetcher(getContext(), mProgress, new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    if(adapter != null) {
                        adapter.notifyDataSetChanged();
                        setListShown(true);
                    }
                    return null;
                }
            });
            setListShown(false);
            fetcher.execute();
        }
    }


    private class ArticleAdapter extends BaseAdapter {

        private final LayoutInflater inflater;
        private Cursor cursor;
        private final SQLiteDatabase db;
        private final ImageLoader imgLoader;
        private final Context context;
        private float zoom = 1;

        ArticleAdapter() {
            super();
            context = getContext();
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
            calculateZoom();
        }

        private void calculateZoom() {
            float value;
            switch (PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext()).getString("text_zoom", "normal")) {
                case "smaller":
                    value = -1;
                    break;
                case "larger":
                    value = 1;
                    break;
                default:
                    value = 0;
            }
            value = (value * 0.15f + 1);
            if(value != zoom) {
                zoom = value;
                this.notifyDataSetChanged();
            }
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
                    FeedReaderContract.Article.COLUMN_NAME_AUTHORS,
                    FeedReaderContract.Article.COLUMN_NAME_OFFLINE,
                    FeedReaderContract.Article.COLUMN_NAME_FULLTEXT
            };

            String sort = FeedReaderContract.Article.COLUMN_NAME_DATE + " DESC";
            String limit = "0, " + Integer.toString(PreferenceManager.getDefaultSharedPreferences(context).getInt("article_limit", 200));


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
        public Object getItem(int position) {
            cursor.moveToPosition(position);
            Article a = new Article();
            a.setId(cursor.getInt(cursor.getColumnIndex(FeedReaderContract.Article.COLUMN_NAME_ID)));
            a.setTitle(cursor.getString(cursor.getColumnIndex(FeedReaderContract.Article.COLUMN_NAME_TITLE)));
            a.setSubheadline(cursor.getString(cursor.getColumnIndex(FeedReaderContract.Article.COLUMN_NAME_SUBHEADING)));
            a.setTeaser(cursor.getString(cursor.getColumnIndex(FeedReaderContract.Article.COLUMN_NAME_TEASER)));
            a.setDate(cursor.getInt(cursor.getColumnIndex(FeedReaderContract.Article.COLUMN_NAME_DATE)));
            a.setImgUrl(cursor.getString(cursor.getColumnIndex(FeedReaderContract.Article.COLUMN_NAME_IMG)));
            a.setUrl(cursor.getString(cursor.getColumnIndex(FeedReaderContract.Article.COLUMN_NAME_URL)));
            a.setAuthors(cursor.getString(cursor.getColumnIndex(FeedReaderContract.Article.COLUMN_NAME_AUTHORS)));
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

            Article art = (Article) getItem(position);
            String infoText = String.format(context.getResources().getString(R.string.article_published), DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT).format(art.getDate()));

            TextView teaser = (TextView) view.findViewById(R.id.articleTeaser);
            TextView title = (TextView) view.findViewById(R.id.articleTitle);
            TextView subheading = (TextView) view.findViewById(R.id.articleSubtitle);
            TextView info = (TextView) view.findViewById(R.id.articleInfo);
            NetworkImageView image = (NetworkImageView) view.findViewById(R.id.articleImage);
            ImageView offlineImage = (ImageView) view.findViewById(R.id.articleOfflineAvailable);

            Resources res = context.getResources();

            title.setText(art.getTitle());
            title.setTextSize(COMPLEX_UNIT_PX, res.getDimension(R.dimen.title_size) * zoom);
            subheading.setText(art.getSubheadline());
            subheading.setTextSize(COMPLEX_UNIT_PX, res.getDimension(R.dimen.subheading_size) * zoom);
            teaser.setText(art.getTeaser());
            teaser.setTextSize(COMPLEX_UNIT_PX, res.getDimension(R.dimen.text_size) * zoom);
            info.setText(infoText);
            info.setTextSize(COMPLEX_UNIT_PX, res.getDimension(R.dimen.info_size) * zoom);

            image.setImageUrl(art.getImgUrl(), imgLoader);
            if (art.isOffline()) {
                offlineImage.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_offline_pin_black_24dp));
            } else {
                offlineImage.setImageDrawable(null);
            }

            art = null;
            return view;
        }


    }


}
