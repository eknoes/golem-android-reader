package de.eknoes.inofficialgolem;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

public class ArticleView extends AppCompatActivity {

    private static final String TAG = "ArticleView";
    private String url;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        url = getIntent().getStringExtra(ArticleFragment.ARTICLE_URL);
        boolean forceWebview = getIntent().getBooleanExtra(ArticleFragment.FORCE_WEBVIEW, false);
        ArticleFragment articleFragment = ArticleFragment.newInstance(url, forceWebview);

        fragmentTransaction.add(R.id.articleFragment, articleFragment);
        fragmentTransaction.commit();
    }
}
