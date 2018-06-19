package com.laughfly.fc4web;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import java.lang.ref.WeakReference;

/**
 * Created by cwy on 2018/3/27.
 */
public class FileChooserChromeClient extends WebChromeClient implements IChromeClient{

    private ValueCallback<Uri> mUploadMessage;
    private ValueCallback<Uri[]> mUploadCallbackAboveL;
    private WeakReference<Context> mContextRef;
    private Application.ActivityLifecycleCallbacks mActivityLifecycleCallbacks;

    @Override
    public void onReceivedTitle(WebView view, String title) {
        super.onReceivedTitle(view, title);
        mContextRef = new WeakReference<>(view.getContext());
    }

    public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
        openFileChooser(uploadMsg, acceptType, "");
    }

    public void openFileChooser(ValueCallback<Uri> uploadMsg) {
        openFileChooser(uploadMsg, "", "");
    }

    public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
        mUploadMessage = uploadMsg;
        startDelegateActivity();
    }

    public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback,
                                     FileChooserParams fileChooserParams) {
        mUploadCallbackAboveL = filePathCallback;
        startDelegateActivity();
        return true;
    }

    private Context getContext() {
        return mContextRef != null ? mContextRef.get() : null;
    }

    private void startDelegateActivity() {
        Context context = getContext();
        if (context != null) {
            initLifecycle();
            Intent intent = new Intent(context, FileChooserDelegateActivity.class);
            context.startActivity(intent);
        }
    }

    private void initLifecycle() {
        Context context = getContext();
        if (context != null) {
            final Application application = (Application) context.getApplicationContext();
            application.registerActivityLifecycleCallbacks(mActivityLifecycleCallbacks = new Application.ActivityLifecycleCallbacks() {
                @Override
                public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                    if (activity instanceof FileChooserDelegateActivity) {
                        ((FileChooserDelegateActivity) activity).setClient(FileChooserChromeClient.this);
                        application.unregisterActivityLifecycleCallbacks(this);
                    }
                }

                @Override
                public void onActivityStarted(Activity activity) {

                }

                @Override
                public void onActivityResumed(Activity activity) {

                }

                @Override
                public void onActivityPaused(Activity activity) {

                }

                @Override
                public void onActivityStopped(Activity activity) {

                }

                @Override
                public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

                }

                @Override
                public void onActivityDestroyed(Activity activity) {
                }
            });
        }
    }

    @Override
    public void handleResult(int resultCode, Uri result) {
        if (Activity.RESULT_CANCELED == resultCode) {
            if (mUploadMessage != null) {
                mUploadMessage.onReceiveValue(null);
            }
            if (mUploadCallbackAboveL != null) {
                mUploadCallbackAboveL.onReceiveValue(null);
            }
        } else if (Activity.RESULT_OK == resultCode) {
            if (mUploadMessage != null) {
                mUploadMessage.onReceiveValue(result);
            }
            if (mUploadCallbackAboveL != null) {
                mUploadCallbackAboveL.onReceiveValue(new Uri[]{result});
            }
        }
        mUploadMessage = null;
        mUploadCallbackAboveL = null;
    }

    @Override
    public void handleFinish() {
        if (mUploadMessage != null) {
            mUploadMessage.onReceiveValue(null);
        }
        if (mUploadCallbackAboveL != null) {
            mUploadCallbackAboveL.onReceiveValue(null);
        }
        mUploadMessage = null;
        mUploadCallbackAboveL = null;
    }
}
