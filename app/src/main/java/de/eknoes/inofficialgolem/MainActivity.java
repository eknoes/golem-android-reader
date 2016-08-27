package de.eknoes.inofficialgolem;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private ArticleAdapter adapter;

    public static final String ARTICLE_URL = "de.eknoes.inofficialgolem.ARTICLE_URL";

    private final String TAG = this.getClass().getCanonicalName();
    private GolemFetcher fetcher;
    private ProgressBar mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ListView listView = (ListView) findViewById(R.id.articleList);
        adapter = new ArticleAdapter(this);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, ArticleView.class);
                intent.putExtra(ARTICLE_URL, id);
                startActivity(intent);

            }
        });

        mProgress = (ProgressBar) findViewById(R.id.progressBar);
        mProgress.setVisibility(View.GONE);

        /*if(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("first_start_v1", true)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.firstStartTitle);
            builder.setMessage(R.string.firstStart);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putBoolean("first_start_v1", false).apply();
                }
            });
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putBoolean("first_start_v1", false).apply();
                }
            });
            builder.create().show();
        }*/
    }

    @Override
    protected void onResume() {
        super.onResume();
        mProgress = (ProgressBar) findViewById(R.id.progressBar);
        mProgress.setVisibility(View.GONE);
        long last_refresh = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getLong("last_refresh", 0);
        int refresh_limit = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getInt("refresh_limit", 5) * 1000 * 60; //Saved as minutes

        if(last_refresh + refresh_limit < new Date().getTime()) {
            Log.d(TAG, "onCreate: Refresh, last refresh was " + ((new Date().getTime() - last_refresh) / 1000) + "sec ago");
            refresh();
        } else {
            Log.d(TAG, "onCreate: No refresh, last refresh was " + (new Date().getTime() - last_refresh) / 1000 + "sec ago");
        }

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
            refresh();
            return true;
        } else if(id == R.id.action_share) {
            //Get Package Link
            final String appPackageName = getPackageName();
            Uri storeUri = Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName);
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_TEXT, String.format(getString(R.string.shareAppText), storeUri));
            shareIntent.setType("text/plain");
            startActivity(shareIntent);

        } else if(id == R.id.action_settings) {
            Intent i = new Intent(this, SettingsActivity.class);
            startActivity(i);
        }

        return super.onOptionsItemSelected(item);
    }

    public void refresh() {
        if(fetcher == null || fetcher.getStatus() != AsyncTask.Status.RUNNING) {
            fetcher = new GolemFetcher(getApplicationContext(), mProgress);
            fetcher.execute();
        }
    }



}
