package com.laughfly.fc4web;

import android.net.Uri;

interface IChromeClient {
    void handleResult(int resultCode, Uri result);

    void handleFinish();
}
