# IMPORTANT

This project is archived and no longer maintained. Please use the official
AndroidX API [androidx.webkit.WebViewAssetLoader](https://developer.android.com/reference/androidx/webkit/WebViewAssetLoader)
instead.

# WebView-Local-Server

## Overview

The purpose of this library is to enable hosting local content (such as assets
or resources) under an http(s):// URL.
The traditional way to access local resources is to use `file:///android_asset`
or `file://android_res/` URLs but using the `file:` scheme poses problems with
[the Same-Origin policy](http://en.wikipedia.org/wiki/Same-origin_policy) and
makes it problematic to reference local content from content loaded over
a secure (`https:`) connection.

## Usage

Using the WebView-Local-Server requires the following steps:

1. Create a `WebViewLocalServer` instance.

        WebViewLocalServer assetServer = new WebViewLocalServer(context);

1. Tell the server where to host the resources.

        // The server uses a random prefix to make it harder for unauthorized content to guess.
        WebViewLocalServer.AssetHostingDetails details =
                    assetServer.hostAssets("/www");
        // Assuming you want to know the http address of assets/www/index.hml:
        String indexUrl =
            details.getHttpPrefix().buildUpon().appendPath("index.html").toString();

1. Hook up the server in the `shouldInterceptRequest` method.

        class MyWebViewClient extends WebViewClient {
            // For KitKat and earlier.
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                return assetServer.shouldInterceptRequest(url);
            }
            // For Lollipop and above.
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                return assetServer.shouldInterceptRequest(request);
            }
        }

1. Consider using the following settings in order to maximize security:

        webView.getSettings().setAllowFileAccessFromFileURLs(false);
        webView.getSettings().setAllowUniversalAccessFromFileURLs(false);

        // Keeping these off is less critical but still a good idea, especially
        // if your app is not using file:// or content:// URLs.
        webView.getSettings().setAllowFileAccess(false);
        webView.getSettings().setAllowContentAccess(false);

## Picking a domain.

One potential problem of hosting local resources on a http(s):// URL is that
doing so may conflict with a real website. This means that local resources
should only be hosted on domains that the user has control of or which have
been dedicated for this purpose.
The `androidplatform.net` domain has been specifically reserved for this
purpose and you are free to use it.

By default the `WebViewLocalServer` will attempt to host assets/resources on
a random subdomain of `androidplatform.net` (something like
`123e4567-e89b-12d3-a456-426655440000.androidplatform.net`). This random
subdomain is chosen once per `WebViewLocalServer`.
To find out which prefix has been assigned to your resources you need to look at
the `AssetHostingDetails` instance returned by the call to `hostAssets` or
`hostResources`.

Should using a random subdomain be inconvenient for some reason it is possible
to use a fixed domain (like `androidplatform.net` or a domain you own).

## Disclaimer

This is not an official Google product (experimental or otherwise), it is just
code that happens to be owned by Google.

