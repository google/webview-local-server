/*
Copyright 2015 Google Inc. All rights reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
package com.google.webviewlocalserver;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.concurrent.ArrayBlockingQueue;

public class TestActivity extends Activity {
    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return false;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            onPageFinishedUrl.add(url);
        }

        @SuppressWarnings({"deprecated"})
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
            return assetServer.shouldInterceptRequest(url);
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            return assetServer.shouldInterceptRequest(request);
        }
    }

    private WebViewLocalServer assetServer;
    private WebView webView;
    private ArrayBlockingQueue<String> onPageFinishedUrl = new ArrayBlockingQueue<String>(5);

    public WebViewLocalServer getAssetServer() {
        return assetServer;

    }

    public WebView getWebView() {
        return webView;
    }

    public ArrayBlockingQueue<String> getOnPageFinishedUrl() {
        return onPageFinishedUrl;
    }

    private void setUpWebView(WebView view) {
        view.setWebViewClient(new MyWebViewClient());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        assetServer = new WebViewLocalServer(this);
        webView = new WebView(this);
        setUpWebView(webView);
        setContentView(webView);
    }
}
