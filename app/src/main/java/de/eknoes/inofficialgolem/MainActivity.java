package de.eknoes.inofficialgolem;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static de.eknoes.inofficialgolem.ArticleFragment.*;

public class MainActivity extends AppCompatActivity implements ArticleListFragment.OnArticleSelectedListener {
    private static final String TAG = "MainActivity";
    private static final String CURRENT_ARTICLE = "currentArticle";
    private String currentArticle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (savedInstanceState != null) {
            currentArticle = savedInstanceState.getString(CURRENT_ARTICLE);
            if (currentArticle != null && getResources().getBoolean(R.bool.twoPaneMode)) {
                onArticleSelected(currentArticle);
            }
        }

        Button mailBtn = findViewById(R.id.mailBtn);
        Button storeBtn = findViewById(R.id.storeBtn);
        Button layoutBtn = findViewById(R.id.mobileViewBtn);

        if (mailBtn != null && storeBtn != null) {
            storeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String packageName = getApplicationContext().getPackageName();
                    Intent playStoreIntent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse("market://details?id=" + packageName));
                    playStoreIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        playStoreIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
                    } else {
                        playStoreIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                    }

                    try {
                        startActivity(playStoreIntent);
                    } catch (Exception e) {
                        Intent webIntent = new Intent(Intent.ACTION_VIEW,
                                Uri.parse("https://play.google.com/store/apps/details?id=" + packageName));

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
                        } else {
                            webIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                        }

                        startActivity(webIntent);
                    }
                }
            });

            mailBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                            "mailto", "projekte@eknoes.de", null));
                    emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Inofficial golem.de Reader");
                    startActivity(Intent.createChooser(emailIntent, getResources().getString(R.string.sendMail)));
                }
            });

            if (layoutBtn != null) {
                layoutBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(MainActivity.this, ArticleView.class);
                        intent.putExtra(ARTICLE_URL, "https://www.golem.de/sonstiges/ansicht/");
                        intent.putExtra(FORCE_WEBVIEW, true);
                        intent.putExtra(NO_ARTICLE, true);
                        startActivity(intent);
                    }
                });
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onSaveInstanceState(@NotNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(CURRENT_ARTICLE, currentArticle);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!getResources().getBoolean(R.bool.twoPaneMode)) {
            ArticleFragment articleFragment = (ArticleFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_article);

            if (articleFragment != null) {
                getSupportFragmentManager().beginTransaction().remove(articleFragment).commit();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            ((ArticleListFragment) Objects.requireNonNull(getSupportFragmentManager().findFragmentById(R.id.fragment_articlelist))).refresh();
            return true;
        } else if (id == R.id.action_share) {
            //Get Package Link
            final String appPackageName = getPackageName();
            Uri storeUri = Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName);
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_TEXT, String.format(getString(R.string.shareAppText), storeUri));
            shareIntent.setType("text/plain");
            startActivity(Intent.createChooser(shareIntent, getResources().getString(R.string.choose_share_app)));
            Log.d(TAG, "onOptionsItemSelected: Share App");

        } else if (id == R.id.action_settings) {
            Intent i = new Intent(this, SettingsActivity.class);
            startActivity(i);
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onArticleSelected(String articleUrl, boolean forceWebview) {
        ArticleFragment articleFragment;

        currentArticle = articleUrl;

        if (getResources().getBoolean(R.bool.twoPaneMode)) {
            articleFragment = (ArticleFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_article);
            if (articleFragment == null) {
                Log.d(TAG, "onArticleSelected: Creating new Article Fragment");
                articleFragment = ArticleFragment.newInstance(articleUrl, forceWebview);
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_article, articleFragment).commit();
            } else {
                Log.d(TAG, "onArticleSelected: Article Fragment present, update it");
                articleFragment.updateArticle(articleUrl, forceWebview);
            }
        } else {
            Log.d(TAG, "onArticleSelected: Creating new Article View");

            Intent articleIntent = new Intent(this, ArticleView.class);
            articleIntent.putExtra(ArticleFragment.ARTICLE_URL, articleUrl);
            articleIntent.putExtra(ArticleFragment.FORCE_WEBVIEW, forceWebview);
            startActivity(articleIntent);
        }
    }

    @Override
    public void onBackPressed() {
        if (getResources().getBoolean(R.bool.twoPaneMode)) {
            ArticleFragment articleFragment = (ArticleFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_article);
            if (articleFragment != null && articleFragment.handleBackPressed()) {
                return;
            }
        }
        super.onBackPressed();
    }

    @Override
    public void onArticleSelected(String articleUrl) {
        onArticleSelected(articleUrl, false);
    }
}
