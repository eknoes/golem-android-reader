package de.eknoes.inofficialgolem;

import android.annotation.TargetApi;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Created by soenke on 12.04.16.
 */
class GolemWebViewClient extends WebViewClient {

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        String host = Uri.parse(url).getHost();
        if (host != null && (host.equals("www.golem.de") || host.equals("golem.de") || host.equals("forum.golem.de"))) {
            return false;
        }
        // Otherwise, the link is not for a page on my site, so launch another Activity that handles URLs
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        view.getContext().startActivity(intent);
        return true;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        if (request.getUrl().getHost().equals("www.golem.de") || request.getUrl().getHost().equals("golem.de") || request.getUrl().getHost().equals("forum.golem.de")) {
            return false;
        }
        // Otherwise, the link is not for a page on my site, so launch another Activity that handles URLs
        Intent intent = new Intent(Intent.ACTION_VIEW, request.getUrl());
        view.getContext().startActivity(intent);
        return true;
    }

}
