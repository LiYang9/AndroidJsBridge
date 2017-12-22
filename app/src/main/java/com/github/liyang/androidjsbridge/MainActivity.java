package com.github.liyang.androidjsbridge;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.RenderProcessGoneDetail;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "js_bridge";

    private WebView mWebView;
    private AlertDialog mAlertDialog;
    private String mUrl;
    private ProgressBar mProgressBar;
    private TextView mTvError;

    private ValueCallback<Uri> uploadMessage;
    private ValueCallback<Uri[]> uploadMessageAboveL;
    private final static int FILE_CHOOSER_RESULT_CODE = 10000;

    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("MainActivity");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WebView.getCurrentWebViewPackage();
            Log.w(TAG, "WebView Package --> " + WebView.getCurrentWebViewPackage());
        }

        mProgressBar = findViewById(R.id.pb_main);
        mTvError = findViewById(R.id.tv_main_error);

        mWebView = findViewById(R.id.wv_main);

        mWebView.removeJavascriptInterface("searchBoxJavaBridge_");
        mWebView.removeJavascriptInterface("accessibility");
        mWebView.removeJavascriptInterface("accessibilityTraversal");

        mWebView.getSettings().setJavaScriptEnabled(true);
        if (Build.VERSION.SDK_INT >= 19) {
            mWebView.getSettings().setLoadsImagesAutomatically(true);
        } else {
            mWebView.getSettings().setLoadsImagesAutomatically(false);
        }

        mWebView.setWebChromeClient(new WebChromeClient() {

            //android 3.0以下：用的这个方法
            @SuppressWarnings("unused")
            public void openFileChooser(ValueCallback<Uri> valueCallback) {
                uploadMessage = valueCallback;
                openImageChooserActivity();
            }

            //android 3.0以上，android4.0以下：用的这个方法
            @SuppressWarnings("unused")
            public void openFileChooser(ValueCallback<Uri> valueCallback, String acceptType) {
                uploadMessage = valueCallback;
                openImageChooserActivity();
            }

            //android 4.0 - android 4.3  安卓4.4.4也用的这个方法
            @SuppressWarnings("unused")
            public void openFileChooser(ValueCallback<Uri> valueCallback, String acceptType, String capture) {
                uploadMessage = valueCallback;
                openImageChooserActivity();
            }

            //android4.4 无方法。。。

            //android 5.0及以上用的这个方法
            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]>
                    filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
                Log.d(TAG, "onShowFileChooser() filePathCallback = [" + new Gson().toJson(filePathCallback) + "], fileChooserParams = [" + new Gson().toJson(fileChooserParams) + "]");
                uploadMessageAboveL = filePathCallback;
                openImageChooserActivity();
                return true;
            }

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress == 100) {
                    if (mProgressBar.isShown()) {
                        mProgressBar.setVisibility(View.INVISIBLE);
                    }
                } else {
                    if (!mProgressBar.isShown()) {
                        mProgressBar.setVisibility(View.VISIBLE);
                    }
                    mProgressBar.setProgress(newProgress);
                }
            }

            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                Log.d(TAG, "onJsAlert() called with:  url = [" + url + "], message = [" + message + "]");
                return super.onJsAlert(view, url, message, result);
            }

            @Override
            public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
                Log.d(TAG, "onJsConfirm() called with:  url = [" + url + "], message = [" + message + "]");
                return super.onJsConfirm(view, url, message, result);
            }

            @Override
            public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
                Log.d(TAG, "onJsPrompt() called with:  url = [" + url + "], message = [" + message + "], defaultValue = [" + defaultValue + "]");
                if (TextUtils.equals(message, "js-bridge")) {
                    Log.w(TAG, "onJsPrompt: defaultValue = " + defaultValue);
                    result.confirm("你好啊JS");
                    return true;
                } else {
                    return super.onJsPrompt(view, url, message, defaultValue, result);
                }
            }
        });

        mAlertDialog = new AlertDialog.Builder(this)
                .setTitle("tips")
                .setMessage("Click ok to jump web browser.")
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mUrl));
                        startActivity(intent);
                    }
                })
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mAlertDialog.isShowing()) {
                            mAlertDialog.dismiss();
                        }
                    }
                })
                .create();

        mWebView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                WebView.HitTestResult result = ((WebView) v).getHitTestResult();
                Log.d(TAG, "result: " + result);
                if (null == result) {
                    return false;
                }
                int type = result.getType();
                Log.d(TAG, "type: " + type + " extra: " + result.getExtra());
                if (type == WebView.HitTestResult.UNKNOWN_TYPE) {
                    return false;
                }
                // 这里可以拦截很多类型，我们只处理图片类型就可以了
                switch (type) {
                    case WebView.HitTestResult.PHONE_TYPE: // 处理拨号
                        break;
                    case WebView.HitTestResult.EMAIL_TYPE: // 处理Email
                        break;
                    case WebView.HitTestResult.GEO_TYPE: // 地图类型
                        break;
                    case WebView.HitTestResult.SRC_ANCHOR_TYPE: // 超链接
                        break;
                    case WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE:
                        break;
                    case WebView.HitTestResult.IMAGE_TYPE: // 处理长按图片的菜单项
                        // 获取图片的路径
                        String saveImgUrl = result.getExtra();
                        Log.d(TAG, "saveImgUrl: " + saveImgUrl);

                        // 跳转到图片详情页，显示图片
                        Intent i = new Intent(MainActivity.this, ImageActivity.class);
                        i.putExtra("imgUrl", saveImgUrl);
                        startActivity(i);
                        break;
                    default:
                        break;
                }
                return true;
            }
        });

        mWebView.setWebViewClient(new MyWebViewClient());
        mWebView.addJavascriptInterface(new WebAppInterface(this), "Android");
        mWebView.loadUrl("file:///android_asset/test.html");
//        mWebView.loadUrl("https://www.baidu.com");
//        mWebView.loadUrl("http://www.jianshu.com/p/b9164500d3fbasdasda");
    }

    private class MyWebViewClient extends WebViewClient {

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
//            Log.d(TAG, "onReceivedHttpError() called with: request = [" + new Gson().toJson(request) + "], errorResponse = [" + new Gson().toJson(errorResponse) + "]");
            mTvError.setText(String.valueOf(errorResponse.getStatusCode()));
            mTvError.setVisibility(View.VISIBLE);
            mWebView.setVisibility(View.GONE);
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
//            Log.d(TAG, "onReceivedError() called with: errorCode = [" + errorCode + "], description = [" + description + "], failingUrl = [" + failingUrl + "]");
            mTvError.setText("网络错误");
            mTvError.setVisibility(View.VISIBLE);
            mWebView.setVisibility(View.GONE);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
//            Log.d(TAG, "onPageStarted() called with: url = [" + url + "], favicon = [" + favicon + "]");
            mWebView.setVisibility(View.VISIBLE);
            mTvError.setVisibility(View.GONE);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
//            Log.d(TAG, "onPageFinished() called with: url = [" + url + "]");
            if (!mWebView.getSettings().getLoadsImagesAutomatically()) {
                mWebView.getSettings().setLoadsImagesAutomatically(true);
            }
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            return super.shouldInterceptRequest(view, request);
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
            return super.shouldInterceptRequest(view, url);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            mUrl = url;
            Log.d(TAG, "url = [" + url + "]");
            if (Uri.parse(url).getHost().equals("www.baidu.com")) {
                return false;
            } else {
                if (!mAlertDialog.isShowing()) {
                    mAlertDialog.show();
                }
                return true;
            }
        }

        @Override
        public boolean onRenderProcessGone(WebView view, RenderProcessGoneDetail detail) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!detail.didCrash()) {
                    // Renderer was killed because the system ran out of memory.
                    // The app can recover gracefully by creating a new WebView instance
                    // in the foreground.
                    Log.e("MY_APP_TAG", "System killed the WebView rendering process to reclaim memory. Recreating...");

                    if (mWebView != null) {
                        ViewGroup webViewContainer = findViewById(R.id.fl_main);
                        webViewContainer.removeView(mWebView);
                        mWebView.destroy();
                        mWebView = null;
                    }

                    // By this point, the instance variable "mWebView" is guaranteed
                    // to be null, so it's safe to reinitialize it.

                    return true; // The app continues executing.
                }
            }
            // Renderer crashed because of an internal error, such as a memory
            // access violation.
            Log.e(TAG, "The WebView rendering process crashed!");

            // In this example, the app itself crashes after detecting that the
            // renderer crashed. If you choose to handle the crash more gracefully
            // and allow your app to continue executing, you should 1) destroy the
            // current WebView instance, 2) specify logic for how the app can
            // continue executing, and 3) return "true" instead.
            return false;
        }
    }

    public class WebAppInterface {
        Context mContext;

        WebAppInterface(Context context) {
            mContext = context;
        }

        @JavascriptInterface
        public void showToast(String toast) {
            Log.d(TAG, toast);
            Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
        }

        @JavascriptInterface
        public int plus(int a, int b) {
            return a + b;
        }

        @JavascriptInterface
        public void log(String text) {
            Log.d(TAG, "log: " + text);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && mWebView.canGoBack()) {
            mWebView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void openImageChooserActivity() {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("image/*");
        startActivityForResult(Intent.createChooser(i, "Image Chooser"),
                FILE_CHOOSER_RESULT_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_CHOOSER_RESULT_CODE) {
            if (null == uploadMessage && null == uploadMessageAboveL) return;
            Uri result = data == null || resultCode != RESULT_OK ? null : data.getData();
            if (uploadMessageAboveL != null) {
                onActivityResultAboveL(requestCode, resultCode, data);
            } else if (uploadMessage != null) {
                uploadMessage.onReceiveValue(result);
                uploadMessage = null;
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void onActivityResultAboveL(int requestCode, int resultCode, Intent intent) {
        if (requestCode != FILE_CHOOSER_RESULT_CODE || uploadMessageAboveL == null) {
            return;
        }
        Uri[] results = null;
        if (resultCode == RESULT_OK) {
            if (intent != null) {
                String dataString = intent.getDataString();
                ClipData clipData = intent.getClipData();
                if (clipData != null) {
                    results = new Uri[clipData.getItemCount()];
                    for (int i = 0; i < clipData.getItemCount(); i++) {
                        ClipData.Item item = clipData.getItemAt(i);
                        results[i] = item.getUri();
                    }
                }
                if (dataString != null) {
                    results = new Uri[]{Uri.parse(dataString)};
                }
            }
        }
        uploadMessageAboveL.onReceiveValue(results);
        uploadMessageAboveL = null;
    }
}
