package com.laughfly.fc4web;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import java.lang.ref.WeakReference;

/**
 * Created by cwy on 2018/3/27.
 */
public class FileChooserChromeClient extends WebChromeClient {

    private static String TAG = "FileChooserChromeClient";

    private ValueCallback<Uri> mUploadMessage;
    private ValueCallback<Uri[]> mUploadCallbackAboveL;
    private WeakReference<Context> mContextRef;
    private Application.ActivityLifecycleCallbacks mActivityLifecycleCallbacks;

    @Override
    public void onReceivedTitle(WebView view, String title) {
        super.onReceivedTitle(view, title);
        mContextRef = new WeakReference<>(view.getContext());
    }

    public void openFileChooser(ValueCallback<Uri> uploadMsg) {
        openFileChooser(uploadMsg, "", "");
    }

    public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
        openFileChooser(uploadMsg, acceptType, "");
    }

    public void openFileChooser(ValueCallback<Uri> uploadFile, String acceptType, String capture) {
        mUploadMessage = uploadFile;
        String[] acceptTypes = Utility.parseAcceptTypes(new String[]{acceptType});
        startDelegateActivity(acceptTypes);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback,
                                     FileChooserParams fileChooserParams) {
        mUploadCallbackAboveL = filePathCallback;
        String[] acceptTypes = Utility.parseAcceptTypes(fileChooserParams != null ? fileChooserParams.getAcceptTypes() : null);
        startDelegateActivity(acceptTypes);
        return true;
    }

    private Context getContext() {
        return mContextRef != null ? mContextRef.get() : null;
    }

    private void startDelegateActivity(String[] mimeTypes) {
        Context context = getContext();
        if (context != null) {
            initLifecycle();
            Intent intent = new Intent(context, FileChooserDelegateActivity.class);
            intent.putExtra("mimeTypes", mimeTypes);
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

    void handleResult(int resultCode, Uri result) {
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

    void handleFinish() {
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
