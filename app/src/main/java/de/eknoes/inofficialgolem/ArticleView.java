package de.eknoes.inofficialgolem;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

public class ArticleView extends AppCompatActivity {

    private WebView webView;
    private final Article article = new Article();
    private static final String TAG = "ArticleView";

    public static final String ARTICLE_URL = "de.eknoes.inofficialgolem.ARTICLE_URL";
    public static final String FORCE_WEBVIEW = "de.eknoes.inofficialgolem.FORCE_WEBVIEW";
    public static final String OPEN_URL = "de.eknoes.inofficialgolem.OPEN_URL";
    private String url;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_view);

        FeedReaderDbHelper dbHelper = FeedReaderDbHelper.getInstance(getApplicationContext());
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        //Fetch article from db + display in WebView
        long articleId = getIntent().getLongExtra(ARTICLE_URL, 0);
        boolean forceWebview = getIntent().getBooleanExtra(FORCE_WEBVIEW, false);

        webView = (WebView) findViewById(R.id.articleWebView);
        webView.setWebViewClient(new GolemWebViewClient());
        webView.getSettings().setJavaScriptEnabled(true);

        if(articleId != 0) {

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


            article.setId(cursor.getInt(cursor.getColumnIndex(FeedReaderContract.Article.COLUMN_NAME_ID)));
            article.setTitle(cursor.getString(cursor.getColumnIndex(FeedReaderContract.Article.COLUMN_NAME_TITLE)));
            article.setSubheadline(cursor.getString(cursor.getColumnIndex(FeedReaderContract.Article.COLUMN_NAME_SUBHEADING)));
            article.setUrl(cursor.getString(cursor.getColumnIndex(FeedReaderContract.Article.COLUMN_NAME_URL)));
            article.setOffline(cursor.getInt(cursor.getColumnIndex(FeedReaderContract.Article.COLUMN_NAME_OFFLINE)) == 1);
            article.setFulltext(cursor.getString(cursor.getColumnIndex(FeedReaderContract.Article.COLUMN_NAME_FULLTEXT)));

            if (!article.isOffline() || forceWebview) {

                ConnectivityManager connMgr = (ConnectivityManager)
                        getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.isConnected()) {
                    webView.loadUrl(article.getUrl());
                } else {
                    webView.loadData(getResources().getString(R.string.err_no_network), "text/html; charset=utf-8", "UTF-8");
                }

            } else {
                webView.loadData(article.getFulltext(), "text/html; charset=utf-8", "UTF-8");
            }

            cursor.close();
        } else {
            url = getIntent().getStringExtra(OPEN_URL);
            if(url != null) {
                webView.loadUrl(url);
            } else {
                Log.w(TAG, "onCreate: No Article ID and open_url empty!");
                Toast.makeText(getApplicationContext(), R.string.undefined_error, Toast.LENGTH_LONG).show();
            }
        }
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void calculateSettings() {
        WebSettings settings = webView.getSettings();
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        settings.setDefaultTextEncodingName("utf-8");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            int value;
            switch (PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("text_zoom", "normal")) {
                case "smaller":
                    value = 90;
                    break;
                case "bigger":
                    value = 110;
                    break;
                default:
                    value = 100;
            }

            settings.setTextZoom(value);
        } else {
            WebSettings.TextSize value;
            switch (PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("text_zoom", "normal")) {
                case "smaller":
                    value = WebSettings.TextSize.SMALLER;
                    break;
                case "larger":
                    value = WebSettings.TextSize.LARGER;
                    break;
                default:
                    value = WebSettings.TextSize.NORMAL;
            }

            settings.setTextSize(value);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        calculateSettings();
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
            String link = null;
            if(article.getUrl() != null) {
                link = article.getUrl();
            } else if(url != null) {
                link = url;
            }

            if(link != null) {
                shareIntent.putExtra(Intent.EXTRA_TEXT, article.getSubheadline() + ": " + article.getTitle() + " - " + link);
                shareIntent.setType("text/plain");
                startActivity(shareIntent);
            }
        } else if (id == R.id.action_settings) {
            Intent i = new Intent(this, SettingsActivity.class);
            startActivity(i);
        } else if (id == R.id.action_open) {
            Intent webIntent = new Intent(Intent.ACTION_VIEW);
            String link = null;
            if(article.getUrl() != null) {
                link = article.getUrl();
            } else if(url != null) {
                link = url;
            }
            if(link != null) {
                webIntent.setData(Uri.parse(link));
                startActivity(webIntent);
            }
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
    protected void onDestroy() {
        super.onDestroy();
        webView.destroy();
    }
}
