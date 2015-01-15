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
import android.test.ActivityInstrumentationTestCase2;

import java.util.concurrent.atomic.AtomicReference;

public class IntegrationTest extends ActivityInstrumentationTestCase2<TestActivity> {
    public IntegrationTest() {
        super(TestActivity.class);
    }

    public void testAssetHosting() throws Exception {
        final TestActivity activity = getActivity();
        final AtomicReference<String> url = new AtomicReference<String>();
        final String test_with_title_path = "test_with_title.html";

        getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                WebViewLocalServer.AssetHostingDetails hostingDetails =
                        activity.getAssetServer().hostAssets("www/", "/", true, true);
                Uri.Builder testPath =
                        hostingDetails.getHttpPrefix().buildUpon().appendPath(test_with_title_path);
                url.set(testPath.toString());
                android.util.Log.i("test", "loading: " + url.get());
                activity.getWebView().loadUrl(url.get());
            }
        });

        String onPageFinishedUrl = activity.getOnPageFinishedUrl().take();
        assertEquals(url.get(), onPageFinishedUrl);

        final AtomicReference<String> title = new AtomicReference<String>();
        getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                title.set(activity.getWebView().getTitle());
            }
        });
        assertEquals("WebViewVirtualServerTest", title.get());
    }

    public void testResourcesHosting() throws Exception {
        final TestActivity activity = getActivity();
        final AtomicReference<String> url = new AtomicReference<String>();
        final String test_with_title_path = "test_with_title.html";

        getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                WebViewLocalServer.AssetHostingDetails hostingDetails =
                    activity.getAssetServer().hostResources();
                Uri.Builder testPath =
                        hostingDetails.getHttpPrefix().buildUpon()
                        .appendPath("res")
                        .appendPath("raw")
                        .appendPath(test_with_title_path);
                url.set(testPath.toString());
                activity.getWebView().loadUrl(url.get());
            }
        });

        String onPageFinishedUrl = activity.getOnPageFinishedUrl().take();
        assertEquals(url.get(), onPageFinishedUrl);

        final AtomicReference<String> title = new AtomicReference<String>();
        getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                title.set(activity.getWebView().getTitle());
            }
        });
        assertEquals("WebViewVirtualServerTest", title.get());
    }
}