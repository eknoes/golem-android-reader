package de.eknoes.inofficialgolem;

import android.content.Context;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.Volley;

import java.text.DateFormat;


/**
 * Created by soenke on 10.04.16.
 */
public class ArticleAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private Cursor cursor;
    private FeedReaderDbHelper dbHelper;
    private SQLiteDatabase db;
    private ImageLoader imgLoader;
    private Context context;

    public ArticleAdapter(Context context) {
        super();
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        dbHelper = FeedReaderDbHelper.getInstance(context);
        db = dbHelper.getReadableDatabase();
        this.context = context;
        loadData();
        this.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                loadData();
            }
        });

        imgLoader = new ImageLoader(Volley.newRequestQueue(context), new ImageLoader.ImageCache() {
            private final LruCache<String, Bitmap> mCache = new LruCache<String, Bitmap>(10);
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

    public void loadData() {
        String[] columns = {
                FeedReaderContract.Article.COLUMN_NAME_ID,
                FeedReaderContract.Article.COLUMN_NAME_TITLE,
                FeedReaderContract.Article.COLUMN_NAME_SUBHEADLINE,
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
        a.setSubheadline(cursor.getString(cursor.getColumnIndex(FeedReaderContract.Article.COLUMN_NAME_SUBHEADLINE)));
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

        if(convertView == null) {
            view = inflater.inflate(R.layout.list_article, parent, false);
        }

        Article art = (Article) getItem(position);

        TextView teaser = (TextView) view.findViewById(R.id.articleTeaser);
        TextView title = (TextView) view.findViewById(R.id.articleTitle);
        TextView subheadline = (TextView) view.findViewById(R.id.articleSubtitle);
        TextView info = (TextView) view.findViewById(R.id.articleInfo);
        NetworkImageView image = (NetworkImageView) view.findViewById(R.id.articleImage);
        ImageView offlineImage = (ImageView) view.findViewById(R.id.articleOfflineAvailable);

        title.setText(art.getTitle());
        subheadline.setText(art.getSubheadline());
        teaser.setText(art.getTeaser());
        image.setImageUrl(art.getImgUrl(), imgLoader);
        String infoText = String.format(context.getResources().getString(R.string.article_published), DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT).format(art.getDate()));
        if(art.isOffline()) {
            offlineImage.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_offline_pin_black_24dp));
        } else {
            offlineImage.setImageDrawable(null);
        }

        info.setText(infoText);

        return view;
    }



}
