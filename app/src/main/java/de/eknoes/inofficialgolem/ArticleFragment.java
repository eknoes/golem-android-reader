package de.eknoes.inofficialgolem;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.jetbrains.annotations.NotNull;

import de.eknoes.inofficialgolem.entities.DBColumns;
import de.eknoes.inofficialgolem.entities.QueryRequest;
import de.eknoes.inofficialgolem.utils.DBHelper;

public class ArticleFragment extends Fragment {
    static final String ARTICLE_URL = "de.eknoes.inofficialgolem.ARTICLE_URL";
    static final String FORCE_WEBVIEW = "de.eknoes.inofficialgolem.FORCE_WEBVIEW";
    static final String NO_ARTICLE = "de.eknoes.inofficialgolem.NO_ARTICLE";

    private static final String TAG = "ArticleFragment";
    private String url;
    private boolean forceWebview;
    private boolean noArticle;
    private WebView webView;

    private Article article;
    private loadArticleTask mTask;
    private SwipeRefreshLayout mArticleSwipeRefresh;

    public ArticleFragment() {
        super(R.layout.fragment_article);
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param articleUrl   URL of the article or Page to open
     * @param forceWebview Force webView even if offline version is available
     * @return A new instance of fragment ArticleFragment.
     */
    static ArticleFragment newInstance(String articleUrl, boolean forceWebview, boolean noArticle) {
        ArticleFragment fragment = new ArticleFragment();
        Bundle args = new Bundle();
        args.putString(ARTICLE_URL, articleUrl);
        args.putBoolean(FORCE_WEBVIEW, forceWebview);
        args.putBoolean(NO_ARTICLE, noArticle);
        fragment.setArguments(args);
        return fragment;
    }

    static ArticleFragment newInstance(String articleUrl, boolean forceWebview) {
        return ArticleFragment.newInstance(articleUrl, forceWebview, false);
    }

    void updateArticle(String url, boolean forceWebview) {
        this.url = url;
        this.forceWebview = forceWebview;
        mTask = new loadArticleTask();
        mTask.execute();

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            url = getArguments().getString(ARTICLE_URL);
            forceWebview = getArguments().getBoolean(FORCE_WEBVIEW);
            noArticle = getArguments().getBoolean(NO_ARTICLE);
        } else if (savedInstanceState != null) {
            url = savedInstanceState.getString(ARTICLE_URL);
            forceWebview = savedInstanceState.getBoolean(FORCE_WEBVIEW);
            noArticle = savedInstanceState.getBoolean(NO_ARTICLE);
        }

        setHasOptionsMenu();
    }

    private void setHasOptionsMenu() {
        setHasOptionsMenu(!noArticle);
    }

    @Override
    public void onSaveInstanceState(@NotNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(FORCE_WEBVIEW, forceWebview);
        outState.putString(ARTICLE_URL, url);
        outState.putBoolean(NO_ARTICLE, noArticle);
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if(webView != null) {
            webView.setWebViewClient(new GolemWebViewClient() {
                @Override
                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    super.onPageStarted(view, url, favicon);
                    webView.setVisibility(View.INVISIBLE);
                    if (!mArticleSwipeRefresh.isRefreshing()) {
                        mArticleSwipeRefresh.setRefreshing(true);
                    }
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                    webView.setVisibility(View.VISIBLE);
                    if (mArticleSwipeRefresh.isRefreshing()) {
                        mArticleSwipeRefresh.setRefreshing(false);
                    }
                }
            });
            webView.getSettings().setJavaScriptEnabled(true);
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.setAcceptCookie(true);
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                cookieManager.setAcceptThirdPartyCookies(webView,true);
            }
        }

        if (url != null) {
            mTask = new loadArticleTask();
            mTask.execute();
        } else {
            Log.d(TAG, "onActivityCreated: URL is Null, do not fetch article");
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        webView = view.findViewById(R.id.articleWebView);
        mArticleSwipeRefresh = view.findViewById(R.id.articleSwipeRefresh);
        mArticleSwipeRefresh.setOnRefreshListener(() -> webView.reload());
    }

    @Override
    public void onResume() {
        super.onResume();
        calculateSettings();
        setHasOptionsMenu();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if(mTask != null) {
            mTask.cancel(false);
        }
    }

    @Override
    public void onCreateOptionsMenu(@NotNull Menu menu, @NotNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_webview, menu);

        if(article == null || article.getCommentUrl() == null) {
            MenuItem item = menu.findItem(R.id.action_comments);
            if(item != null) {
                item.setVisible(false);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_open) {
            if (url != null) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(Intent.createChooser(intent, getString(R.string.open_browser)));
            }
        } else if (id == R.id.action_comments) {
            Log.d(TAG, "onOptionsItemSelected: Open Comments: " + article.getCommentUrl());
            webView.loadUrl(article.getCommentUrl());
        } else if (id == R.id.action_share_article) {
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            String link = null;
            if (article != null && article.getUrl() != null) {
                link = article.getUrl();
            } else if (url != null) {
                link = url;
            }

            if (link != null && article != null) {
                shareIntent.putExtra(Intent.EXTRA_TEXT, article.getSubheadline() + ": " + article.getTitle() + " - " + link);
            } else {
                shareIntent.putExtra(Intent.EXTRA_TEXT, link);
            }

            shareIntent.setType("text/plain");
            startActivity(Intent.createChooser(shareIntent, getResources().getString(R.string.choose_share_article)));

        }

        return super.onOptionsItemSelected(item);
    }


    private void calculateSettings() {
        WebSettings settings = webView.getSettings();
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        settings.setDefaultTextEncodingName("utf-8");
    }

    boolean handleBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        return false;
    }


    private class loadArticleTask extends AsyncTask<Void, Void, Void> {

        /**
         * Override this method to perform a computation on a background thread. The
         * specified parameters are the parameters passed to {@link #execute}
         * by the caller of this task.
         * <p>
         * This method can call {@link #publishProgress} to publish updates
         * on the UI thread.
         *
         * @param params The parameters of the task.
         * @return A result, defined by the subclass of this task.
         * @see #onPreExecute()
         * @see #onPostExecute
         * @see #publishProgress
         */

        @Override
        protected Void doInBackground(Void... params) {
                if (url != null) {
                    QueryRequest queryRequest = new QueryRequest.QueryRequestBuilder()
                            .withTableName(DBColumns.getTableName())
                            .withSelection("url=\"" + url + "\"")
                            .build();
                    article = DBHelper.getArticle(queryRequest);
                }
            return null;
        }

        @Override
        protected void onPostExecute(Void vVoid) {
            if (isCancelled() || webView == null) {
                return;
            }
            if (article == null || !article.isOffline() || forceWebview) {
                Context c = getContext();
                if (c != null) {
                    ConnectivityManager connMgr = (ConnectivityManager)
                            getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo networkInfo = null;
                    if (connMgr != null) {
                        networkInfo = connMgr.getActiveNetworkInfo();
                    }
                    if (networkInfo != null && networkInfo.isConnected()) {
                        DBHelper.updateArticleReadState(article);
                        webView.loadUrl(url);
                    } else {
                        webView.loadData(getResources().getString(R.string.err_no_network), "text/html; charset=utf-8", "UTF-8");
                    }
                } else {
                    DBHelper.updateArticleReadState(article);
                    webView.loadUrl(url);
                }
            } else {
                Log.d(TAG, "onPostExecute: Fill Webview");
                String fulltext = article.getFulltext();

                // Change CSS for Dark Mode
                if((getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES) {
                    if (fulltext != null) {
                        fulltext = fulltext.replace("</head>", "<style type=\"text/css\">#screen, body, html {\n" +
                                "color: white;\n" +
                                "background-color: black;\n" +
                                "}" +
                                ".article #related a, .header--centered, .dh1, .dh2, .authors {\n" +
                                "  color: white !important;\n" +
                                "}</style></head>");
                    }
                } else if (fulltext != null) {
                    fulltext = fulltext.replace("</head>", "<style type=\"text/css\">#screen, body, html {\n" +
                            "color: black;\n" +
                            "background-color: white;\n" +
                            "}\n" +
                            ".article #related a, .header--centered, .dh1, .dh2, .authors {\n" +
                            "  color: black !important;\n" +
                            "}</style></head>");
                }
                webView.loadDataWithBaseURL(article.getUrl(), fulltext, "text/html", "UTF-8", null);
                Log.d(TAG, "onPostExecute: Filled Webview");

            }

            //If comment menu button should appear or disappear
            Activity mainActivity = getActivity();
            if (mainActivity != null)
                mainActivity.invalidateOptionsMenu();

        }
    }
}
