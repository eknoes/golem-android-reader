package de.eknoes.inofficialgolem;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

public class ArticleView extends AppCompatActivity {

    private ArticleFragment articleFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_view);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }


        String url = getIntent().getStringExtra(ArticleFragment.ARTICLE_URL);
        boolean forceWebview = getIntent().getBooleanExtra(ArticleFragment.FORCE_WEBVIEW, false);
        boolean noArticle = getIntent().getBooleanExtra(ArticleFragment.NO_ARTICLE, false);

        articleFragment = ArticleFragment.newInstance(url, forceWebview, noArticle);

        getSupportFragmentManager().beginTransaction().replace(R.id.articleFragment, articleFragment).commit();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_articleview, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent i = new Intent(this, SettingsActivity.class);
            startActivity(i);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (!articleFragment.handleBackPressed()) {
            super.onBackPressed();
        }
    }
}
