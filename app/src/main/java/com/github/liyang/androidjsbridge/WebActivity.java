package com.github.liyang.androidjsbridge;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.github.liyang.js_bridge.JsBridgeWebView;
import com.github.liyang.js_bridge.JsCallback;
import com.github.liyang.js_bridge.JsObject;
import com.github.liyang.js_bridge.OnImageLongClick;
import com.squareup.leakcanary.RefWatcher;

import java.lang.ref.WeakReference;

/**
 * author : LiYang
 * email  : yang.li@meiauto.cn
 * time   : 2017/12/15
 */
public class WebActivity extends AppCompatActivity {

    public static final String TAG = "js_bridge";

    private JsBridgeWebView mWebView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);

        setTitle("WebActivity");

        FrameLayout rootView = findViewById(R.id.root_web);

        mWebView = JsBridgeWebView.create(this);

        JsBridgeWebView.setWebDebugEnabled(true);

        mWebView.enableFileChoose(this);
        rootView.addView(mWebView);

        mWebView.addJavascriptInterface(new WebAppInterface(this), "Android");

//        mWebView.loadUrl("https://www.baidu.com");
        mWebView.loadUrl("file:///android_asset/test.html");

        mWebView.setOnImageLongClick(new OnImageLongClick() {
            @Override
            public void onLongClick(View v, String imageUrl) {
                Log.d(TAG, "onLongClick() called with: imageUrl = [" + imageUrl + "]");
            }
        });
    }

    public class WebAppInterface {
        private WeakReference<Context> mContext;

        WebAppInterface(Context context) {
            mContext = new WeakReference<>(context);
        }

        @JavascriptInterface
        public void showToast(String toast) {
            Context context = mContext.get();
            if (context == null) {
                return;
            }
            Toast.makeText(context, toast, Toast.LENGTH_SHORT).show();
        }

        @JavascriptInterface
        public int plus(int a, int b) {
            return a + b;
        }

        @JavascriptInterface
        public void debug(String text) {
            Log.d(TAG, "log: " + text);
        }

        @JavascriptInterface
        public void jump(String url) {
            Context context = mContext.get();
            if (context == null) {
                return;
            }
            mWebView.loadUrl(url);
        }

        @JavascriptInterface
        double plus(double a, double b) {
            return a + b;
        }

        @JavascriptInterface
        String getNumber() {
            return "10086";
        }

        void obj(@Nullable WebObject webObject) {
            showToast(webObject == null ? "object is null" : webObject.toString());
        }

        void testCallback(final JsCallback delayCallback) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    delayCallback.confirm(1, 1.2, true, new WebObject(), "asdasd", null);
                }
            }, 2000);
        }
    }

    @Override
    protected void onResume() {
        mWebView.onResume();
        super.onResume();
    }

    @Override
    protected void onPause() {
        mWebView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mWebView.destroy();
        RefWatcher refWatcher = App.getRefWatcher();
        refWatcher.watch(mWebView);
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mWebView.onActivityResult(requestCode, resultCode, data);
    }

    class WebObject extends JsObject {
        Brand brand;
        String name;

        class Brand {
            String version;

            @Override
            public String toString() {
                return "Brand{" +
                        "version='" + version + '\'' +
                        '}';
            }
        }

        @Override
        public String toString() {
            return "WebObject{" +
                    "brand=" + brand +
                    ", name='" + name + '\'' +
                    '}';
        }
    }
}
