package com.rockchips.mediacenter.util;

import android.content.Context;
import com.rockchips.mediacenter.view.LoadingDialog;
import com.rockchips.mediacenter.R;
/**
 * @author GaoFei
 * 对话框工具，用于启动加载对话框等
 */
public class DialogUtils {
	private DialogUtils(){
		
	}
	
	  /**载入对话框*/
    private static LoadingDialog mLoadingDialog;
    /**显示载入对话框*/
    public static void showLoadingDialog(Context context,boolean isCancelable) {

        mLoadingDialog = new LoadingDialog(context, R.style.Loading_Dialog_Style, isCancelable);
        mLoadingDialog.show();
    }
    /**关闭载入对话框*/
    public static void closeLoadingDialog() {

        if (mLoadingDialog != null && mLoadingDialog.isShowing())
            mLoadingDialog.dismiss();
    }

}
