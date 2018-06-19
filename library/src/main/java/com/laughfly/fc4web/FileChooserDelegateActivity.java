package com.laughfly.fc4web;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by cwy on 2018/3/27.
 */

public class FileChooserDelegateActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_IMAGE_CAPTURE = 1024;
    private static final int REQUEST_CODE_PICK_IMAGE = 2046;
    private static final int P_CODE_PERMISSIONS = 101;
    private static final String TAG = FileChooserDelegateActivity.class.getSimpleName();

    private Intent mSourceIntent;

    private  WeakReference<IChromeClient> mClientRef;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showOptions();
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
        IChromeClient client = getClient();
        if (client != null) {
            client.handleFinish();
        }
        mClientRef = null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IChromeClient client = getClient();
        if (client == null) {
            onChooseCancel();
            return;
        }

        Uri uri = pickUri(data);
        client.handleResult(resultCode, uri);

        onChooseFinish();
    }

    void setClient(IChromeClient client) {
        mClientRef = new WeakReference<>(client);
    }

    private IChromeClient getClient() {
        return mClientRef != null ? mClientRef.get() : null;
    }

    private Uri pickUri(Intent data) {
        String sourcePath = ImageUtil.retrievePath(this, mSourceIntent, data);
        if (TextUtils.isEmpty(sourcePath) || !new File(sourcePath).exists()) {
            Log.e(TAG, "sourcePath empty or not exists.");
            return null;
        }
        return Uri.fromFile(new File(sourcePath));
    }

    private void onChooseCancel() {
        finish();
    }

    private void onChooseFinish() {
        finish();
    }

    public void showOptions() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this, R.style.Theme_AppCompat_Light_Dialog_Alert);
        alertDialog.setCancelable(true);
        alertDialog.setOnCancelListener(new DialogOnCancelListener());

        alertDialog.setTitle("请选择操作");
        // gallery, camera.
        String[] options = {"相册", "拍照"};

        alertDialog.setItems(options, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (which == 0) {
                        if (PermissionUtil.isOverMarshmallow()) {
                            if (!PermissionUtil.isPermissionValid(FileChooserDelegateActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                                Toast.makeText(FileChooserDelegateActivity.this,
                                    "请去\"设置\"中开启本应用的图片媒体访问权限",
                                    Toast.LENGTH_SHORT).show();

                                requestPermissionsAndroidM();
                                return;
                            }

                        }

                        try {
                            mSourceIntent = ImageUtil.choosePicture();
                            startActivityForResult(mSourceIntent, REQUEST_CODE_PICK_IMAGE);
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(FileChooserDelegateActivity.this,
                                "请去\"设置\"中开启本应用的图片媒体访问权限",
                                Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        if (PermissionUtil.isOverMarshmallow()) {
                            if (!PermissionUtil.isPermissionValid(FileChooserDelegateActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                                Toast.makeText(FileChooserDelegateActivity.this,
                                    "请去\"设置\"中开启本应用的图片媒体访问权限",
                                    Toast.LENGTH_SHORT).show();
                                requestPermissionsAndroidM();
                                return;
                            }

                            if (!PermissionUtil.isPermissionValid(FileChooserDelegateActivity.this, Manifest.permission.CAMERA)) {
                                Toast.makeText(FileChooserDelegateActivity.this,
                                    "请去\"设置\"中开启本应用的相机权限",
                                    Toast.LENGTH_SHORT).show();

                                requestPermissionsAndroidM();
                                return;
                            }
                        }

                        try {
                            mSourceIntent = ImageUtil.takeBigPicture(FileChooserDelegateActivity.this);
                            startActivityForResult(mSourceIntent, REQUEST_CODE_IMAGE_CAPTURE);

                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(FileChooserDelegateActivity.this,
                                "请去\"设置\"中开启本应用的相机和图片媒体访问权限",
                                Toast.LENGTH_SHORT).show();
                            onChooseCancel();
                        }
                    }
                }
            }
        );

        alertDialog.show();
    }

    private class DialogOnCancelListener implements DialogInterface.OnCancelListener {
        @Override
        public void onCancel(DialogInterface dialogInterface) {
            onChooseCancel();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case P_CODE_PERMISSIONS:
                requestResult(permissions, grantResults);
                break;

            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void requestPermissionsAndroidM() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            List<String> needPermissionList = new ArrayList<>();
            needPermissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            needPermissionList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            needPermissionList.add(Manifest.permission.CAMERA);

            PermissionUtil.requestPermissions(FileChooserDelegateActivity.this, P_CODE_PERMISSIONS, needPermissionList);

        } else {
            onChooseCancel();
        }
    }

    public void requestResult(String[] permissions, int[] grantResults) {
        ArrayList<String> needPermissions = new ArrayList<String>();

        for (int i = 0; i < grantResults.length; i++) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                if (PermissionUtil.isOverMarshmallow()) {

                    needPermissions.add(permissions[i]);
                }
            }
        }

        if (needPermissions.size() > 0) {
            StringBuilder permissionsMsg = new StringBuilder();

            for (int i = 0; i < needPermissions.size(); i++) {
                String strPermissions = needPermissions.get(i);

                if (Manifest.permission.WRITE_EXTERNAL_STORAGE.equals(strPermissions)) {
                    permissionsMsg.append("," + getString(R.string.permission_storage));

                } else if (Manifest.permission.READ_EXTERNAL_STORAGE.equals(strPermissions)) {
                    permissionsMsg.append("," + getString(R.string.permission_storage));

                } else if (Manifest.permission.CAMERA.equals(strPermissions)) {
                    permissionsMsg.append("," + getString(R.string.permission_camera));

                }
            }

            String strMessage = "请允许使用\"" + permissionsMsg.substring(1).toString() + "\"权限, 以正常使用APP的所有功能.";

            Toast.makeText(FileChooserDelegateActivity.this, strMessage, Toast.LENGTH_SHORT).show();

            onChooseCancel();
        } else {

        }

    }
}
