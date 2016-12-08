package com.rockchips.mediacenter.videoplayer.widget;

import java.io.File;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.rockchips.mediacenter.R;
import com.rockchips.mediacenter.api.localImp.LocalDeviceManager;
import com.rockchips.mediacenter.basicutils.bean.LocalMediaInfo;
import com.rockchips.mediacenter.basicutils.constant.Constant;
import com.rockchips.mediacenter.basicutils.constant.Constant.EBrowerType;
import com.rockchips.mediacenter.basicutils.util.HanziToPinyin;
import com.rockchips.mediacenter.basicutils.util.HanziToPinyin.Token;
import com.rockchips.mediacenter.videoplayer.VideoPlayerActivity;
import com.rockchips.mediacenter.videoplayer.data.SubTitleInfo.SubTitleFile;

public class SubtitleSelectPopup extends Dialog
{
    private static final String TAG = "MediaCenterApp";  
    private Context mContext;
    
    private GridView mGvSubtitleSelect;
    private SubtitleSelectAdapter mSubtitleSelectAdapter;
    
    private LayoutInflater mInflater;
    private View mVSubtitleContainer;
        
    private List<LocalMediaInfo> mFolderPaths;
    
    private List<LocalMediaInfo> mMediaFiles;
    private static final String NULL_STRING = "";   
    private String mSelectedFilePath;
    
    private int mCurrSelectFocusPos;
    private int mFolderSelectFocusPos = 0;
    
    private OnSubtileSelectListener mOnSubtileSelectListener;
    
    public SubtitleSelectPopup(Context context, List<LocalMediaInfo> folderPaths)
    {
        super(context); 
        init(context, folderPaths);        
    }
    
    public SubtitleSelectPopup(Context context, List<LocalMediaInfo> folderPaths, int theme)
    {
        super(context, theme);
        init(context, folderPaths);
    }  
    
    public void setOnSelectListener(OnSubtileSelectListener onSubtileSelectListener)
    {
        mOnSubtileSelectListener = onSubtileSelectListener;
    }
    
    private void init(Context context, List<LocalMediaInfo> folderPaths)
    {
        mFolderPaths = folderPaths;    
        getOrderedList(mFolderPaths);
        mMediaFiles = mFolderPaths;
        
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
        initParam();        
        mContext = context;
        
        mInflater = LayoutInflater.from(context);
        mVSubtitleContainer = mInflater.inflate(R.layout.video_subtile_setting_select, null, true);
        mVSubtitleContainer.setFocusable(true);
        mVSubtitleContainer.requestFocus();            
//        mVSubtitleContainer.setBackgroundDrawable(new BitmapDrawable(mContext.getResources()));
        
        this.setContentView(mVSubtitleContainer);
        
        mGvSubtitleSelect = (GridView)mVSubtitleContainer.findViewById(R.id.gv_video_subtitle_folder_select);
        mGvSubtitleSelect.setFocusable(true);
        mGvSubtitleSelect.requestFocus();
        mGvSubtitleSelect.setOnItemSelectedListener(mOnItemSelectedListener);
//        mGvSubtitleSelect.setOnItemClickListener(mOnItemClickListener); 
        
        mSubtitleSelectAdapter = new SubtitleSelectAdapter(mInflater, mMediaFiles);
        mGvSubtitleSelect.setAdapter(mSubtitleSelectAdapter);                
        setOnKeyListener(mKeyListener);
    }
    
    public void show(View parent)
    {
        showDialog();
    }
    
    public void hide() {        
        dismiss();
    }
    
    public void showDialog()
    {
        //TODO
        windowDeploy();
        setCanceledOnTouchOutside(true);
        show();
        mGvSubtitleSelect.requestFocus();
    }
    
    // 设置窗口显示
    private void windowDeploy() {       
        /* BEGIN: Modified by c00224451 for  DTS2014031902847  2014/3/19 */
        Window window = getWindow(); // 得到对话框        
        window.setBackgroundDrawableResource(R.color.transparent_color); // 设置对话框背景为透明            
        WindowManager.LayoutParams wl = window.getAttributes();
        // wl.alpha = 0.6f; //设置透明度
        wl.gravity = Gravity.CENTER; // 设置重力
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
        window.getDecorView().setSystemUiVisibility(View.INVISIBLE);
        window.setAttributes(wl);		
        /* END: Modified by c00224451 for  DTS2014031902847  2014/3/19 */
    }
    
    private OnItemSelectedListener mOnItemSelectedListener = new OnItemSelectedListener(){

        @Override
        public void onItemSelected(AdapterView<?> arg0, View view, int arg2, long arg3)
        {
            // TODO Auto-generated method stub            
//            itemFocusChanged(view);
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0)
        {
            // TODO Auto-generated method stub
            
        }
        
    };
    
    private OnItemClickListener mOnItemClickListener = new OnItemClickListener(){

        @Override
        public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3)
        {
            // TODO Auto-generated method stub
//            itemClickOperate(view, position);
        }       
        
    };
    
    private void itemClickOperate(int position)
    {
        View view = getViewByPos(position);
        if (view == null)
        {
            Log.e(TAG, "itemClickOperate getViewByPos view == null!");
            return;
        }
        ViewHolder viewHolder = (ViewHolder)view.getTag();
        LocalMediaInfo mediaFile = mMediaFiles.get(position);
        
        if (Constant.MediaType.SUBTITLE != mediaFile.getmFileType())
        {                        
            mMediaFiles =
                    LocalDeviceManager.getInstance(mContext).
                    getFlatAVIFileSubWithType(mediaFile.getUrl(), Constant.MediaType.SUBTITLE, 0, 100, EBrowerType.ORDER_TYPE_CHARACTER);  
            getOrderedList(mMediaFiles);
            refreshAndLoadData();
            mFolderSelectFocusPos = position;
        }
        else
        {            
            mSelectedFilePath = mediaFile.getmData();
            if (null != mOnSubtileSelectListener)
            {
                mOnSubtileSelectListener.onSelect(mSelectedFilePath);
            }
            hide();
        }
    }
    
//    private void itemFocusChanged(int position)
//    {
//        View view = getViewByPos(position);
//        hideFocusBackground(mPrevView);
//        mPrevView = mCurrView;
//        mCurrView = view;
//        showFocusBackground(mCurrView);
//    }
    
    private void showFocusBackground(int position)
    {        
        View view = getViewByPos(position);
        if (null != view)
        {
            ViewHolder viewHolder = (ViewHolder)view.getTag();            
            if (null != viewHolder)
            {
                viewHolder.ivFocused.setVisibility(View.VISIBLE);  
                mSelectedFilePath = NULL_STRING; 
            }
        }
    }
    
    private void hideFocusBackground(int position)
    {        
        View view = getViewByPos(position);
        if (null != view)
        {            
            ViewHolder viewHolder = (ViewHolder)view.getTag();            
            if (null != viewHolder)
            {
                viewHolder.ivFocused.setVisibility(View.INVISIBLE);                        
            }
        }        
    }
        
    private OnKeyListener mKeyListener = new OnKeyListener() {
        
        @Override
        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event)
        {            
            Log.d(TAG, "keyCode = " + keyCode + " enter !");
            int NUM_COLUMNS = mGvSubtitleSelect.getNumColumns();
            boolean ret = false;
            if (event.getAction() == KeyEvent.ACTION_UP)
            {
                return false;
            }
            Log.d(TAG, "keyCode = " + keyCode + " mCurrSelectFocusPos = " + mCurrSelectFocusPos + " totoal item = "+ mMediaFiles.size());
            switch (keyCode) {
                case KeyEvent.KEYCODE_DPAD_DOWN:
                    if (mCurrSelectFocusPos + NUM_COLUMNS < mMediaFiles.size())
                    {
                        hideFocusBackground(mCurrSelectFocusPos);
                        mCurrSelectFocusPos += NUM_COLUMNS;
                        showFocusBackground(mCurrSelectFocusPos);    
                    }
                    else if ((mMediaFiles.size() - 1) / NUM_COLUMNS > (mCurrSelectFocusPos) / NUM_COLUMNS )
                    {
                        hideFocusBackground(mCurrSelectFocusPos);
                        mCurrSelectFocusPos = mMediaFiles.size() - 1;
                        showFocusBackground(mCurrSelectFocusPos);
                    }
                    break;
                case KeyEvent.KEYCODE_DPAD_UP:
                    if (mCurrSelectFocusPos - NUM_COLUMNS >= 0)
                    {
                        hideFocusBackground(mCurrSelectFocusPos);
                        mCurrSelectFocusPos -= NUM_COLUMNS;
                        showFocusBackground(mCurrSelectFocusPos);                        
                    }
                    break;
                case KeyEvent.KEYCODE_DPAD_LEFT:                
                    if (mCurrSelectFocusPos > 0)
                    {
                        hideFocusBackground(mCurrSelectFocusPos);
                        mCurrSelectFocusPos -= 1;
                        showFocusBackground(mCurrSelectFocusPos);    
                    }
                    break;
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    if (mCurrSelectFocusPos < mMediaFiles.size() - 1)
                    {
                        hideFocusBackground(mCurrSelectFocusPos);
                        mCurrSelectFocusPos += 1;
                        showFocusBackground(mCurrSelectFocusPos);    
                    }
                    break;
                case KeyEvent.KEYCODE_ENTER:
                case KeyEvent.KEYCODE_DPAD_CENTER:
                    itemClickOperate(mCurrSelectFocusPos);
                    break;
                case KeyEvent.KEYCODE_BACK:
                    backClickOperate();  
                    ret = true;
                    break;
            }
            return ret;
        }
        
    };
    
    private Handler mHander = new Handler()
    {

        @Override
        public void handleMessage(Message msg)
        {            
            super.handleMessage(msg);
            mSubtitleSelectAdapter.notifyDataSetChanged();            
        }
        
    };
    
    public void initParam()
    {
        mSelectedFilePath = NULL_STRING;        
        mCurrSelectFocusPos = 0; 
        if (mGvSubtitleSelect != null)            
        {
            backToCurrSelectPosition();
        }
    }
    
    private void backToCurrSelectPosition()
    {
        mGvSubtitleSelect.post(new Runnable()
        {
            @Override
            public void run()
            {
                mGvSubtitleSelect.setSelection(mCurrSelectFocusPos);
            }
        });
        
    }
    private View getViewByPos(int position)
    {
        View view = mGvSubtitleSelect.getChildAt(position - mGvSubtitleSelect.getFirstVisiblePosition());
        return view;
    }
    
    private void backClickOperate()
    {
        if (mMediaFiles != mFolderPaths)
        {
            restoreData();      
            Log.d(TAG, "Back to first folder!");
        }
        else
        {
            Log.d(TAG, "Back to menu click!");
            hide();
        }
    }   
    
    public void restoreData()
    {                
        mMediaFiles = mFolderPaths;        
        refreshAndLoadData();  
        mCurrSelectFocusPos = mFolderSelectFocusPos;
        if (mGvSubtitleSelect != null)            
        {
            backToCurrSelectPosition();
        }
    }
    
    public void refreshAndLoadData()
    {
        mCurrSelectFocusPos = 0;
        mSubtitleSelectAdapter.resetData(mMediaFiles);
        mHander.sendEmptyMessage(0);        
    }
    
    private List<LocalMediaInfo> getOrderedList(List<LocalMediaInfo> list)
    {         
        Collections.sort(list, mCompater); 
        return list;
    }
    
    private Comparator<LocalMediaInfo> mCompater = new  Comparator<LocalMediaInfo>()
    {
        @Override
        public int compare(LocalMediaInfo lhs, LocalMediaInfo rhs)
        {
            if (lhs.getmFileName() == null)
            {
                return -1;
            }
            else if (rhs.getmFileName() == null)
            {
                return 1;
            }
            
            return getFullPinYin(lhs.getmFileName()).toLowerCase().compareTo(getFullPinYin(rhs.getmFileName()).toLowerCase());
        }
    };
    
    public String getFullPinYin(String source)
    { 
        
        if (!Arrays.asList(Collator.getAvailableLocales()).contains(Locale.CHINA))
        { 
            return source; 
        }  
    
        ArrayList<Token> tokens = HanziToPinyin.getInstance().get(source); 
    
        if (tokens == null || tokens.size() == 0) 
        { 
           return source; 
        } 
    
        StringBuffer result = new StringBuffer(); 
    
        for (Token token : tokens)
        { 
           if (token.type == Token.PINYIN) 
           { 
               result.append(token.target); 
           } 
           else 
           { 
               result.append(token.source); 
           } 
        }
        
        return result.toString(); 
    }
    
    public class SubtitleSelectAdapter extends BaseAdapter
    {    
        private List<LocalMediaInfo> mFolderPaths;
        private LayoutInflater mInflater;        
        
        SubtitleSelectAdapter(LayoutInflater inflater, List<LocalMediaInfo> folderPaths)
        {        
            mFolderPaths = folderPaths;
            mInflater = inflater;
        }
        
        public void resetData(List<LocalMediaInfo> folderPaths)
        {
            mFolderPaths = folderPaths;
        }
        @Override
        public int getCount()
        {
            if (mFolderPaths == null)
            {
                return 0;
            }
            return mFolderPaths.size();
        }
        
        @Override
        public Object getItem(int position)
        {
            if (position < 0 || position >= mFolderPaths.size())
            {
                return null;
            }
            return mFolderPaths.get(position);
        }
        
        @Override
        public long getItemId(int position)
        {
            // TODO Auto-generated method stub
            return 0;
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            ViewHolder viewHolder = null;
            if (convertView == null)
            {
                convertView = mInflater.inflate(R.layout.video_subtile_setting_item, null, true);
                viewHolder = new ViewHolder(convertView);
                convertView.setTag(viewHolder);
            }
            else
            {
                viewHolder = (ViewHolder)convertView.getTag();
            }
            
            LocalMediaInfo subtileFile = mFolderPaths.get(position);
//            if (!subtileFile.isFolder)
//            {
//                //TODO
//            }
            
            if (mCurrSelectFocusPos == position)
            {
                viewHolder.ivFocused.setVisibility(View.VISIBLE);
            }
            else
            {
                viewHolder.ivFocused.setVisibility(View.INVISIBLE);
            }
                           
            viewHolder.tvFileName.setText(subtileFile.getmFileName());
            
            return convertView;
        }       
        
    }
    
    public class ViewHolder{
        public ImageView ivIcon;
        public ImageView ivFocused;        
        public TextView  tvFileName;
        
        ViewHolder(View view)
        {
            ivIcon = (ImageView)view.findViewById(R.id.iv_video_subtitle_item_folder);
            ivFocused = (ImageView)view.findViewById(R.id.iv_subtile_image_focused);            
            tvFileName = (TextView)view.findViewById(R.id.tv_video_subtitle_item_filename);
        }
    }
    
    public interface OnSubtileSelectListener
    {
        void onSelect(String fullPath);        
    }
}
