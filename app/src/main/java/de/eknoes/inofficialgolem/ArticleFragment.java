package de.eknoes.inofficialgolem;

import android.annotation.SuppressLint;
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
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.*;
import android.webkit.WebSettings;
import android.webkit.WebView;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * Use the {@link ArticleFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ArticleFragment extends Fragment {
    public static final String ARTICLE_URL = "de.eknoes.inofficialgolem.ARTICLE_URL";
    public static final String FORCE_WEBVIEW = "de.eknoes.inofficialgolem.FORCE_WEBVIEW";

    private static final String TAG = "ArticleFragment";
    private String url;
    private boolean forceWebview;
    private WebView webView;

    private Article article;

    public ArticleFragment() {
        super();
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param articleUrl   URL of the article or Page to open
     * @param forceWebview Force webView even if offline version is available
     * @return A new instance of fragment ArticleFragment.
     */
    static ArticleFragment newInstance(String articleUrl, boolean forceWebview) {
        ArticleFragment fragment = new ArticleFragment();
        Bundle args = new Bundle();
        args.putString(ARTICLE_URL, articleUrl);
        args.putBoolean(FORCE_WEBVIEW, forceWebview);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(FORCE_WEBVIEW, forceWebview);
        outState.putString(ARTICLE_URL, url);
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getArguments() != null) {
            url = getArguments().getString(ARTICLE_URL);
            forceWebview = getArguments().getBoolean(FORCE_WEBVIEW);
        } else if (savedInstanceState != null) {
            url = savedInstanceState.getString(ARTICLE_URL);
            forceWebview = savedInstanceState.getBoolean(FORCE_WEBVIEW);
        }
        if (webView != null) {
            webView.setWebViewClient(new GolemWebViewClient());
            webView.getSettings().setJavaScriptEnabled(true);

            FeedReaderDbHelper dbHelper = FeedReaderDbHelper.getInstance(getContext());
            SQLiteDatabase db = dbHelper.getReadableDatabase();

            if (url != null) {
                String[] columns = {
                        FeedReaderContract.Article.COLUMN_NAME_ID,
                        FeedReaderContract.Article.COLUMN_NAME_TITLE,
                        FeedReaderContract.Article.COLUMN_NAME_SUBHEADING,
                        FeedReaderContract.Article.COLUMN_NAME_URL,
                        FeedReaderContract.Article.COLUMN_NAME_OFFLINE,
                        FeedReaderContract.Article.COLUMN_NAME_FULLTEXT,
                };
                Cursor cursor = db.query(
                        FeedReaderContract.Article.TABLE_NAME,
                        columns,
                        "url=\"" + url + "\"",
                        null,
                        null,
                        null,
                        null);
                if (cursor.moveToFirst()) {
                    article = new Article();
                    article.setId(cursor.getInt(cursor.getColumnIndex(FeedReaderContract.Article.COLUMN_NAME_ID)));
                    article.setTitle(cursor.getString(cursor.getColumnIndex(FeedReaderContract.Article.COLUMN_NAME_TITLE)));
                    article.setSubheadline(cursor.getString(cursor.getColumnIndex(FeedReaderContract.Article.COLUMN_NAME_SUBHEADING)));
                    article.setUrl(cursor.getString(cursor.getColumnIndex(FeedReaderContract.Article.COLUMN_NAME_URL)));
                    article.setOffline(cursor.getInt(cursor.getColumnIndex(FeedReaderContract.Article.COLUMN_NAME_OFFLINE)) == 1);
                    article.setFulltext(cursor.getString(cursor.getColumnIndex(FeedReaderContract.Article.COLUMN_NAME_FULLTEXT)));
                }

                if (article == null || !article.isOffline() || forceWebview) {
                    ConnectivityManager connMgr = (ConnectivityManager)
                            getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                    if (networkInfo != null && networkInfo.isConnected()) {
                        webView.loadUrl(url);
                    } else {
                        webView.loadData(getResources().getString(R.string.err_no_network), "text/html; charset=utf-8", "UTF-8");
                    }
                } else {
                    webView.loadData(article.getFulltext(), "text/html; charset=utf-8", "UTF-8");
                }

                cursor.close();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        Log.d(TAG, "onCreateView: Inflating Fragment layout");
        View v = inflater.inflate(R.layout.fragment_article, container, false);
        webView = (WebView) v.findViewById(R.id.articleWebView);
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        calculateSettings();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (menu.size() == 0) {
            inflater.inflate(R.menu.menu_webview, menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_open) {
            Intent webIntent = new Intent(Intent.ACTION_VIEW);
            if (url != null) {
                webIntent.setData(Uri.parse(url));
                startActivity(webIntent);
            }
        }
        return super.onOptionsItemSelected(item);
    }


    private void calculateSettings() {
        WebSettings settings = webView.getSettings();
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        settings.setDefaultTextEncodingName("utf-8");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            int value;
            switch (PreferenceManager.getDefaultSharedPreferences(getContext()).getString("text_zoom", "normal")) {
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
            switch (PreferenceManager.getDefaultSharedPreferences(getContext()).getString("text_zoom", "normal")) {
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

}
