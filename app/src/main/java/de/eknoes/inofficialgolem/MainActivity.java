package de.eknoes.inofficialgolem;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.net.URL;

public class MainActivity extends AppCompatActivity implements ArticleListFragment.OnArticleSelectedListener {
    private static final String TAG = "MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            ((ArticleListFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_articlelist)).refresh();
            return true;
        } else if (id == R.id.action_share) {
            //Get Package Link
            final String appPackageName = getPackageName();
            Uri storeUri = Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName);
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_TEXT, String.format(getString(R.string.shareAppText), storeUri));
            shareIntent.setType("text/plain");
            startActivity(shareIntent);

        } else if (id == R.id.action_settings) {
            Intent i = new Intent(this, SettingsActivity.class);
            startActivity(i);
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onArticleSelected(URL articleUrl, boolean forceWebview) {
        ArticleFragment articleFragment = (ArticleFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_article);
        if(articleFragment != null) {
            articleFragment.updateArticle(articleUrl.toExternalForm(), forceWebview);
        } else {
            Log.d(TAG, "onArticleSelected: Creating new Article Fragment");

            Intent articleIntent = new Intent(this, ArticleView.class);
            articleIntent.putExtra(ArticleFragment.ARTICLE_URL, articleUrl.toExternalForm());
            articleIntent.putExtra(ArticleFragment.FORCE_WEBVIEW, forceWebview);
            startActivity(articleIntent);

/*            articleFragment = ArticleFragment.newInstance(articleUrl.toExternalForm(), forceWebview);

            FragmentTransaction mTransaction = getSupportFragmentManager().beginTransaction();
            mTransaction.replace(R.id.fragment_articlelist, articleFragment);
            mTransaction.addToBackStack(null);
            mTransaction.commit();*/
        }
    }

    @Override
    public void onArticleSelected(URL articleUrl) {
        onArticleSelected(articleUrl, false);
    }
}
