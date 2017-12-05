package com.github.liyang.js_bridge;

import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebView;

/**
 * 2017/11/30
 */
public class JsBridgeWebview extends WebView {
    public JsBridgeWebview(Context context) {
        super(context);
        init();
    }

    public JsBridgeWebview(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public JsBridgeWebview(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        
    }
}
