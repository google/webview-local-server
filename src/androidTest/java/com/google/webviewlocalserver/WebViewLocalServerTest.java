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

import android.net.Uri;
import android.test.InstrumentationTestCase;
import android.util.Log;
import android.webkit.WebResourceResponse;

import com.google.webviewlocalserver.third_party.chromium.AndroidProtocolHandler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Random;

public class WebViewLocalServerTest extends InstrumentationTestCase {
    private final static String TAG = "WebViewAssetServerTest";

    private static class RandomString {
        private static final Random random = new Random();

        public static String next(int length) {
            StringBuilder sb = new StringBuilder(length);
            for (int i = 0; i < length; ++i) {
                sb.append('a' + random.nextInt('z' - 'a'));
            }
            return sb.toString();
        }
    }

    private static String readAsString(InputStream is, String encoding) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] buffer = new byte[512];
        int len = 0;
        try {
            while ((len = is.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
            return new String(os.toByteArray(), encoding);
        } catch (IOException e) {
            Log.e(TAG, "exception when reading the string", e);
            return "";
        }
    }

    private static class MockProtocolHandler extends AndroidProtocolHandler {
        public MockProtocolHandler() {
            super(null);
        }

        @Override
        public InputStream openAsset(String path) throws IOException {
            return null;
        }

        @Override
        public InputStream openResource(Uri uri) {
            return null;
        }
    }

    public void testCustomPathHandler() {
        WebViewLocalServer assetServer = new WebViewLocalServer(new MockProtocolHandler());
        final String contents = RandomString.next(2000);
        final String encoding = "utf-8";

        WebViewLocalServer.PathHandler pathHandler = new WebViewLocalServer.PathHandler() {
            @Override
            public String getEncoding() {
                return encoding;
            }

            @Override
            public InputStream handle(Uri url) {
                try {
                    return new ByteArrayInputStream(contents.getBytes(encoding));
                } catch (UnsupportedEncodingException e) {
                    Log.e(TAG, "exception when creating response", e);
                }
                return null;
            }
        };

        String url = "http://androidplatform.net/test";
        Uri uri = Uri.parse(url);
        assetServer.register(uri, pathHandler);
        WebResourceResponse response = assetServer.shouldInterceptRequest(url);

        assertEquals(encoding, response.getEncoding());
        assertEquals(contents, readAsString(response.getData(), encoding));

        assertNull(assetServer.shouldInterceptRequest("http://foo.bar/"));
    }

    public void testHostAssets() {
        final String testHtmlContents = "<body><div>hah</div></body>";

        WebViewLocalServer assetServer = new WebViewLocalServer(new MockProtocolHandler() {
            @Override
            public InputStream openAsset(String path) throws IOException {
                if (path.equals("/www/test.html")) {
                    return new ByteArrayInputStream(testHtmlContents.getBytes("utf-8"));
                }
                return null;
            }
        });

        WebViewLocalServer.AssetHostingDetails details =
                assetServer.hostAssets("androidplatform.net", "/www", "/assets", true, true);
        assertEquals(details.getHttpPrefix(), Uri.parse("http://androidplatform.net/assets"));
        assertEquals(details.getHttpsPrefix(), Uri.parse("https://androidplatform.net/assets"));

        WebResourceResponse response =
                assetServer.shouldInterceptRequest("http://androidplatform.net/assets/test.html");
        assertNotNull(response);
        assertEquals(testHtmlContents, readAsString(response.getData(), "utf-8"));
    }

    public void testHostResources() {
        final String testHtmlContents = "<body><div>hah</div></body>";

        WebViewLocalServer assetServer = new WebViewLocalServer(new MockProtocolHandler() {
            @Override
            public InputStream openResource(Uri uri) {
                Log.i(TAG, "host res: " + uri);
                try {
                    if (uri.getPath().equals("/res/raw/test.html")) {
                        return new ByteArrayInputStream(testHtmlContents.getBytes("utf-8"));
                    }
                } catch (IOException e) {
                    Log.e(TAG, "exception when creating response", e);
                }
                return null;
            }
        });

        WebViewLocalServer.AssetHostingDetails details =
            assetServer.hostResources("androidplatform.net", "/res", true, true);
        assertEquals(details.getHttpPrefix(), Uri.parse("http://androidplatform.net/res"));
        assertEquals(details.getHttpsPrefix(), Uri.parse("https://androidplatform.net/res"));

        WebResourceResponse response =
                assetServer.shouldInterceptRequest("http://androidplatform.net/res/raw/test.html");
        assertNotNull(response);
        assertEquals(testHtmlContents, readAsString(response.getData(), "utf-8"));
    }
}
