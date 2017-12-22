package com.github.liyang.androidjsbridge;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import com.github.liyang.js_bridge.JsBridgeWebView;

/**
 * author : LiYang
 * email  : yang.li@meiauto.cn
 * time   : 2017/12/19
 */
public class WebFragment extends Fragment {

    public static final String TAG = "js_bridge";

    private JsBridgeWebView mWebView;
    private Fragment2Activity mFragment2Activity;

    public static WebFragment newInstance(String url) {
        WebFragment webFragment = new WebFragment();
        Bundle bundle = new Bundle();
        bundle.putString("url", url);
        webFragment.setArguments(bundle);
        return webFragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Fragment2Activity) {
            mFragment2Activity = (Fragment2Activity) context;
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mWebView = JsBridgeWebView.create(this);
        mWebView.enableFileChoose(this);
        mWebView.addJavascriptInterface(new WebAppInterface(getActivity()), "Android");
        mWebView.loadUrl(getArguments().getString("url"));
        return mWebView;
    }

    @Override
    public void onDestroyView() {
        mWebView.onDestroy();
        super.onDestroyView();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mWebView.onActivityResult(requestCode, resultCode, data);
    }

    public class WebAppInterface {
        Context mContext;

        WebAppInterface(Context context) {
            mContext = context;
        }

        @JavascriptInterface
        void showToast(String toast) {
            Log.d(TAG, toast);

            Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
            if (mFragment2Activity != null) {
                mFragment2Activity.send();
            }
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

    public interface Fragment2Activity {
        void send();
    }
}
