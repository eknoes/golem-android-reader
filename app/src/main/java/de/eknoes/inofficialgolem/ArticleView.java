package de.eknoes.inofficialgolem;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.LruCache;
import android.view.*;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.InvalidParameterException;
import java.text.DateFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class ArticleView extends AppCompatActivity {

    private WebView webView;
    private final Article article = new Article();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FeedReaderDbHelper dbHelper = FeedReaderDbHelper.getInstance(getApplicationContext());
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        //Fetch article from db + display in WebView
        long articleId = getIntent().getLongExtra(MainActivity.ARTICLE_URL, 0);
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
                FeedReaderContract.Article.COLUMN_NAME_FULLTEXT,
                FeedReaderContract.Article.COLUMN_NAME_MEDIA_FULLTEXT
        };
        Cursor cursor = db.query(
                FeedReaderContract.Article.TABLE_NAME,
                columns,
                "id=" + articleId,
                null,
                null,
                null,
                null);
        cursor.moveToFirst();

        if (cursor.getInt(cursor.getColumnIndex(FeedReaderContract.Article.COLUMN_NAME_OFFLINE)) != 1) {
            setContentView(R.layout.activity_article_view);
            webView = (WebView) findViewById(R.id.articleWebView);

            ConnectivityManager connMgr = (ConnectivityManager)
                    getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {

                String url = cursor.getString(cursor.getColumnIndex(FeedReaderContract.Article.COLUMN_NAME_URL));

                webView.loadUrl(url);
                webView.setWebViewClient(new GolemWebViewClient());
                webView.getSettings().setJavaScriptEnabled(true);
            } else {
                webView.getSettings().setJavaScriptEnabled(false);
                webView.getSettings().setDefaultTextEncodingName("utf-8");
                webView.setWebViewClient(new GolemWebViewClient());
                webView.loadData(getResources().getString(R.string.err_no_network), "text/html; charset=utf-8", "UTF-8");
            }

        } else {

            ImageLoader imgLoader = new ImageLoader(Volley.newRequestQueue(this), new ImageLoader.ImageCache() {
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

            article.setId(cursor.getInt(cursor.getColumnIndex(FeedReaderContract.Article.COLUMN_NAME_ID)));
            article.setTitle(cursor.getString(cursor.getColumnIndex(FeedReaderContract.Article.COLUMN_NAME_TITLE)));
            article.setSubheadline(cursor.getString(cursor.getColumnIndex(FeedReaderContract.Article.COLUMN_NAME_SUBHEADING)));
            article.setTeaser(cursor.getString(cursor.getColumnIndex(FeedReaderContract.Article.COLUMN_NAME_TEASER)));
            article.setDate(cursor.getInt(cursor.getColumnIndex(FeedReaderContract.Article.COLUMN_NAME_DATE)));
            article.setImgUrl(cursor.getString(cursor.getColumnIndex(FeedReaderContract.Article.COLUMN_NAME_IMG)));
            article.setUrl(cursor.getString(cursor.getColumnIndex(FeedReaderContract.Article.COLUMN_NAME_URL)));
            article.setAuthors(cursor.getString(cursor.getColumnIndex(FeedReaderContract.Article.COLUMN_NAME_AUTHORS)));
            article.setOffline(cursor.getInt(cursor.getColumnIndex(FeedReaderContract.Article.COLUMN_NAME_OFFLINE)) == 1);
            article.setFulltext(cursor.getString(cursor.getColumnIndex(FeedReaderContract.Article.COLUMN_NAME_FULLTEXT)));
            article.setMediaFulltext((cursor.getInt(cursor.getColumnIndex(FeedReaderContract.Article.COLUMN_NAME_MEDIA_FULLTEXT)) == 1));


            if (article.isMediaFulltext()) {
                setContentView(R.layout.activity_article_view);
                webView = (WebView) findViewById(R.id.articleWebView);
                webView.getSettings().setJavaScriptEnabled(true);
                webView.getSettings().setDefaultTextEncodingName("utf-8");
                webView.setWebViewClient(new GolemWebViewClient());
                webView.loadData(article.getFulltext(), "text/html; charset=utf-8", "UTF-8");

            } else {
                setContentView(R.layout.activity_article_view_offline);
                TextView text = (TextView) findViewById(R.id.articleText);
                String infoText = String.format(getResources().getString(R.string.article_published_with_authors), DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT).format(article.getDate()), article.getAuthors());
                TextView title = (TextView) findViewById(R.id.articleTitle);
                TextView subheading = (TextView) findViewById(R.id.articleSubtitle);
                TextView info = (TextView) findViewById(R.id.articleInfo);
                NetworkImageView image = (NetworkImageView) findViewById(R.id.articleImage);

                title.setText(article.getTitle());
                subheading.setText(article.getSubheadline());
                image.setImageUrl(article.getImgUrl(), imgLoader);
                info.setText(infoText);

                text.setText(Html.fromHtml(article.getFulltext()));
                text.setMovementMethod(LinkMovementMethod.getInstance());
                GalleryBuilder gb = new GalleryBuilder();
                gb.execute(articleId);
            }
        }
        cursor.close();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_webview, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_share) {
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            String link = "";
            if (webView != null && !article.isMediaFulltext()) {
                link = webView.getUrl();
            } else if (article != null) {
                link = article.getUrl();
            }
            shareIntent.putExtra(Intent.EXTRA_TEXT, String.format(getString(R.string.shareArticle), link));
            shareIntent.setType("text/plain");
            startActivity(shareIntent);
        } else if (id == R.id.action_settings) {
            Intent i = new Intent(this, SettingsActivity.class);
            startActivity(i);
        } else if (id == R.id.action_open) {
            Intent webIntent = new Intent(Intent.ACTION_VIEW);
            String link;
            if (webView != null && !article.isMediaFulltext()) {
                link = webView.getUrl();
            } else {
                link = article.getUrl();
            }
            webIntent.setData(Uri.parse(link));
            startActivity(webIntent);
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    if (webView == null) {
                        break;
                    } else if (webView.canGoBack()) {
                        webView.goBack();
                        return true;
                    }
            }

        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private class GalleryBuilder extends AsyncTask<Long, Void, List<GalleryBuilder.GolemImg>> {

        private RequestQueue queue;
        private ProgressBar prgrs;
        private LinearLayout gallery;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            prgrs = new ProgressBar(getApplicationContext(), null, android.R.attr.progressBarStyleLarge);
            prgrs.setPadding(10, 10, 10, 10);

            gallery = (LinearLayout) findViewById(R.id.galleryLayout);
            gallery.addView(prgrs);
            prgrs.setVisibility(View.VISIBLE);
        }

        @Override
        protected List<GolemImg> doInBackground(Long... params) {
            if (params.length != 1) {
                throw new InvalidParameterException("No article id provided");
            } else {
                Long articleId = params[0];
                RequestFuture<JSONObject> future = RequestFuture.newFuture();
                JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, buildUrl(articleId), future, future);
                queue = Volley.newRequestQueue(getApplicationContext());
                queue.add(request);

                try {
                    JSONArray result = future.get().getJSONArray("data"); // this line will block
                    LinkedList<GolemImg> imageUrls = new LinkedList<>();
                    for (int i = 0; i < result.length(); i++) {
                        JSONObject img = result.getJSONObject(i);
                        JSONObject medImg = img.getJSONObject("medium");
                        imageUrls.add(new GolemImg(medImg.getString("url"), img.getString("subtext"), medImg.getInt("width"), medImg.getInt("height")));
                    }
                    return imageUrls;
                } catch (InterruptedException | JSONException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
            return new LinkedList<>();
        }

        @Override
        protected void onPostExecute(List<GolemImg> imgs) {
            super.onPostExecute(imgs);
            int maxWidth = findViewById(R.id.articleScroller).getWidth();

            if (imgs.size() == 0) {

            } else {
                ImageLoader imgLoader = new ImageLoader(queue, new ImageLoader.ImageCache() {
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


                for (GolemImg img : imgs) {
                    LinearLayout galleryItem = new LinearLayout(getApplicationContext());
                    galleryItem.setOrientation(LinearLayout.VERTICAL);
                    galleryItem.setPadding(10, 10, 10, 10);

                    NetworkImageView newImg = new NetworkImageView(getApplicationContext());
                    newImg.setImageUrl(img.getUrl(), imgLoader);
                    newImg.setMaxWidth(maxWidth);
                    newImg.setMinimumWidth(maxWidth);
                    galleryItem.addView(newImg);

                    TextView subTxt = new TextView(getApplicationContext());
                    subTxt.setText(Html.fromHtml(img.getText()));
                    subTxt.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimaryDark));
                    subTxt.setWidth(maxWidth);
                    subTxt.setGravity(Gravity.CENTER_HORIZONTAL);

                    galleryItem.addView(subTxt);
                    gallery.addView(galleryItem);
                }
            }

            prgrs.setVisibility(View.INVISIBLE);
            ((ViewGroup) prgrs.getParent()).removeView(prgrs);

        }

        private String buildUrl(Long id) {
            return "http://api.golem.de/api/article/images/" + String.valueOf(id) + "/?key=" + getString(R.string.golem_api_key) + "&format=json";

        }

        class GolemImg {
            private final String url;
            private final String text;
            private final int height;
            private final int width;

            public GolemImg(String url, String text, int width, int height) {
                this.url = url;
                this.text = text;
                this.height = height;
                this.width = width;
            }

            public String getUrl() {
                return url;
            }

            public String getText() {
                return text;
            }

            public int getHeight() {
                return height;
            }

            public int getWidth() {
                return width;
            }
        }
    }

}
