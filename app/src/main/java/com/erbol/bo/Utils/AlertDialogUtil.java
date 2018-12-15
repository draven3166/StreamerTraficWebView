package com.erbol.bo.Utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

import com.erbol.bo.R;

public class AlertDialogUtil {
    public static void showAlertDialog(final Activity ctx){
        AlertDialog.Builder dialog = new AlertDialog.Builder(ctx);//, R.style.CustomDialog
        dialog.setCancelable(false);
        dialog.setIcon(android.R.drawable.ic_dialog_info);
        dialog.setTitle(ctx.getResources().getString(R.string.dlg_title));
        dialog.setMessage(ctx.getResources().getString(R.string.dlg_error));
        dialog.setNeutralButton(ctx.getResources().getString(R.string.txt_close), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                ctx.finish();
            }
        });
        dialog.show();
    }
}