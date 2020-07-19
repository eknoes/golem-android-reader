package de.eknoes.inofficialgolem;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.view.MotionEvent;
import android.view.View;
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
        if (host == null || host.equals("www.golem.de") || host.equals("golem.de") || host.equals("forum.golem.de") || host.equals("account.golem.de")) {
            return false;
        }
        // Otherwise, the link is not for a page on my site, so launch another Activity that handles URLs
        if(!url.startsWith("http")) {
            url = "http://" + url;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        view.getContext().startActivity(intent);
        return true;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        if (request.getUrl().getHost() == null || request.getUrl().getHost().equals("www.golem.de") || request.getUrl().getHost().equals("golem.de") || request.getUrl().getHost().equals("account.golem.de") || request.getUrl().getHost().equals("forum.golem.de")) {
            return false;
        }
        // Otherwise, the link is not for a page on my site, so launch another Activity that handles URLs
        Intent intent = new Intent(Intent.ACTION_VIEW, request.getUrl());
        view.getContext().startActivity(intent);
        return true;
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
        if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT_WATCH) {
            // disable scroll on touch
            view.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return (event.getAction() == MotionEvent.ACTION_MOVE);
                }
            });
        }
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT_WATCH) {
            // disable scroll on touch
            view.setOnTouchListener(null);
        }
    }


}
