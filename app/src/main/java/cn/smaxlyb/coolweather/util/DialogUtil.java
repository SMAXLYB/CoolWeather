package cn.smaxlyb.coolweather.util;

import android.content.Context;

import androidx.appcompat.app.AlertDialog;

import cn.smaxlyb.coolweather.R;

/**
 * @author smaxlyb
 * @date 2020/3/29 20:15
 * website: https://smaxlyb.cn
 */
public class DialogUtil {
    private static AlertDialog dialog;

    public static void showDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(false);
        builder.setView(R.layout.layout_dialog);
        dialog = builder.create();
        dialog.show();
    }

    public static void dismissDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }
}
