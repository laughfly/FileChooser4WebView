package com.laughfly.fc4web;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;

import java.io.File;
import java.lang.ref.WeakReference;

/**
 * Created by cwy on 2018/3/27.
 */

public class FileChooserDelegateActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_PICK_FILE = 2046;
    private static final int P_CODE_PERMISSIONS = 101;
    private static final String TAG = FileChooserDelegateActivity.class.getSimpleName();

    private WeakReference<FileChooserChromeClient> mClientRef;

    private String[] mAcceptMimeTypes;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAcceptMimeTypes = getIntent().getStringArrayExtra("mimeTypes");
        if(requestPermissionsIfNeed()) {
            return;
        }
        startPick();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            finish();
            return false;
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FileChooserChromeClient client = getClient();
        if (client != null) {
            client.handleFinish();
        }
        mClientRef = null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        FileChooserChromeClient client = getClient();
        if (client == null) {
            onChooseCancel();
            return;
        }


        Uri uri = pickUri(data);
        client.handleResult(resultCode, uri);
        Log.d(TAG, "onActivityResult: " + uri);

        onChooseFinish();
    }

    void setClient(FileChooserChromeClient client) {
        mClientRef = new WeakReference<>(client);
    }

    private FileChooserChromeClient getClient() {
        return mClientRef != null ? mClientRef.get() : null;
    }

    private Uri pickUri(Intent data) {
        String sourcePath = Utility.getPath(this, data != null ? data.getData() : null);
        if (TextUtils.isEmpty(sourcePath) || !new File(sourcePath).exists()) {
            Log.e(TAG, "sourcePath empty or not exists.");
            return null;
        }
        return Uri.fromFile(new File(sourcePath));
    }

    private void startPick() {
        Intent intent = Utility.createIntent(mAcceptMimeTypes);
        startActivityForResult(intent, REQUEST_CODE_PICK_FILE);
    }

    private void onChooseCancel() {
        finish();
    }

    private void onChooseFinish() {
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case P_CODE_PERMISSIONS:
                requestPermissionsResult(permissions, grantResults);
                break;

            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private boolean requestPermissionsIfNeed() {
        if(PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, P_CODE_PERMISSIONS);
            return true;
        }
        return false;
    }

    public void requestPermissionsResult(String[] permissions, int[] grantResults) {
        if(PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            startPick();
        } else {
            Toast.makeText(this, getString(R.string.permission_denied), Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}
