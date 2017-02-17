package de.eknoes.inofficialgolem.updater;

import android.content.Context;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;
import com.android.volley.*;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Fetches articles for users with golem abo token
 */
public class AboArticleUpdater extends GolemUpdater {

    private final RequestQueue queue;

    public AboArticleUpdater(Context c) {
        super(c);
        queue = Volley.newRequestQueue(context);

    }

    @Override
    public List<GolemItem> getItems() throws TimeoutError, NoConnectionError, AuthFailureError {
        if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("has_abo", false)) {
            Log.d(TAG, "fetchAbo: Fetching Abo RSS Feed");
            RequestFuture<String> future = RequestFuture.newFuture();
            StringRequest request = new StringRequest(buildAboURL(PreferenceManager.getDefaultSharedPreferences(context).getString("abo_key", null)), future, future);
            queue.add(request);
            try {
                String response = future.get(15, TimeUnit.SECONDS);
                InputStream stream = new ByteArrayInputStream(response.getBytes());
                return new GolemRSSParser(PreferenceManager.getDefaultSharedPreferences(context).getBoolean("media_rss", true)).parse(stream);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                if (e.getCause() instanceof NoConnectionError || e.getCause() instanceof TimeoutError) {
                    Log.d(TAG, "getItems: No Connection");
                    throw new NoConnectionError();
                } else if (e.getCause() instanceof AuthFailureError) {
                    Log.d(TAG, "getItems: Invalid Abo key");
                    throw new AuthFailureError();
                } else if (e.getCause() instanceof RedirectError) {
                    Log.w(TAG, "getItems: Redirect Error: Can not get Feed items");
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (TimeoutException e) {
                e.printStackTrace();
                throw new TimeoutError();
            }
        } else {
            Log.d(TAG, "fetchAbo: No Abo available");
        }
        return new LinkedList<>();
    }

    private String buildAboURL(String key) {
        return "https://rss.golem.de/rss_sub_media.php?token=" + Uri.encode(key);
    }

    private class GolemRSSParser {

        private final String ns = "";
        private final String TAG = this.getClass().getCanonicalName();
        private Boolean mediaRss = false;

        GolemRSSParser() {
            super();
        }

        GolemRSSParser(Boolean media_rss) {
            this();
            mediaRss = media_rss;
        }

        List<GolemItem> parse(InputStream in) throws IOException {
            try {
                Log.d(TAG, "parse: Got InputStream, start parsing RSS");
                XmlPullParser parser = Xml.newPullParser();
                parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                parser.setInput(in, null);
                parser.nextTag();
                return readFeed(parser);
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            } finally {
                in.close();
            }
            return new ArrayList<>();
        }

        private List<GolemItem> readFeed(XmlPullParser parser) throws IOException, XmlPullParserException {
            List<GolemItem> entries = new ArrayList<>();

            parser.require(XmlPullParser.START_TAG, ns, "feed");
            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }
                String name = parser.getName();
                if (name.equals("entry")) {
                    entries.add(readEntry(parser));
                } else {
                    skip(parser);
                }
            }

            return entries;
        }

        private GolemItem readEntry(XmlPullParser parser) throws IOException, XmlPullParserException {
            parser.require(XmlPullParser.START_TAG, ns, "entry");
            String title = null;
            String text = null;
            String author = null;
            String url = null;


            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }
                String name = parser.getName();
                switch (name) {
                    case "title":
                        title = readText(parser);
                        break;
                    case "link":
                        url = readLink(parser);
                        break;
                    case "author":
                        author = readAuthor(parser);
                        break;
                    case "summary":
                        text = readText(parser);
                        break;
                    default:
                        skip(parser);
                        break;
                }
            }
            GolemItem article = new GolemItem();
            article.setUrl(url);
            article.setType(GolemItem.Type.ARTICLE);
            article.setProp(GolemItem.ItemProperties.TITLE, title);
            article.setProp(GolemItem.ItemProperties.AUTHORS, author);
            article.setProp(GolemItem.ItemProperties.FULLTEXT, text);
            article.setProp(GolemItem.ItemProperties.OFFLINE_AVAILABLE, "true");
            article.setProp(GolemItem.ItemProperties.HAS_MEDIA_FULLTEXT, String.valueOf(mediaRss));
            return article;
        }

        private String readAuthor(XmlPullParser parser) throws IOException, XmlPullParserException {
            parser.require(XmlPullParser.START_TAG, ns, "author");
            List<String> authors = new ArrayList<>();

            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }
                String name = parser.getName();
                if (name.equals("name")) {
                    authors.add(readText(parser));
                } else {
                    skip(parser);
                }
            }
            return TextUtils.join(", ", authors);
        }

        // For the tags title and summary, extracts their text values.
        private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
            String result = "";
            if (parser.next() == XmlPullParser.TEXT) {
                result = parser.getText();
                parser.nextTag();
            }
            return result;
        }

        // Processes link tags in the feed.
        private String readLink(XmlPullParser parser) throws IOException, XmlPullParserException {
            String link = "";
            parser.require(XmlPullParser.START_TAG, ns, "link");
            String tag = parser.getName();
            String relType = parser.getAttributeValue(null, "rel");
            if (tag.equals("link")) {
                if (relType.equals("alternate")) {
                    link = parser.getAttributeValue(null, "href");
                    parser.nextTag();
                }
            }
            parser.require(XmlPullParser.END_TAG, ns, "link");
            return link;
        }


        private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                throw new IllegalStateException();
            }
            int depth = 1;
            while (depth != 0) {
                switch (parser.next()) {
                    case XmlPullParser.END_TAG:
                        depth--;
                        break;
                    case XmlPullParser.START_TAG:
                        depth++;
                        break;
                }
            }
        }
    }

}
