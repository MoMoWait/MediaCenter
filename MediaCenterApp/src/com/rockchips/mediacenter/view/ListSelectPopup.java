package com.rockchips.mediacenter.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.rockchips.mediacenter.R;

/**
 * 
 * Description: 宫格形式的选择对话框<br>
 * @author s00211113
 * @version v1.0 Date: 2014-7-23 下午2:57:31<br>
 */
public class ListSelectPopup extends Dialog
{
    private static final String TAG = "ListSelectPopup";
    
    private Context context;

    private GridView mGvListSelect;

    private SubtitleSelectAdapter mListSelectAdapter;

    private LayoutInflater mInflater;

    private View mVListContainer;

    private List<ListSelectItem> mListItems;

    private int mCurrSelectFocusPos;

    private OnSelectPopupListener mOnSelectPopupListener;

    private Map<String, Boolean> mapSelected = new HashMap<String, Boolean>();

    private ImageView mIvLogo;

    private TextView mTvSelectTip;

    private TextView mIvNoAlbum;
    
    private String PIC_FOLDER = "PIC_FOLDER";
    
    private String PIC_NAME = "PIC_NAME";
    
    private StringBuffer picName = new StringBuffer();

    public ListSelectPopup(Context context, List<ListSelectItem> list)
    {
        super(context);
        init(context, list);
    }

    public ListSelectPopup(Context context, List<ListSelectItem> list, int theme)
    {
        super(context, theme);
        init(context, list);
    }
    
    public ListSelectPopup(Context context, List<ListSelectItem> list, String devices, int style, int theme)
    {
        super(context, theme);
        if(style == 1){
        	if(devices.indexOf(":") > -1){
            	PIC_FOLDER = devices.substring(0, devices.indexOf(":"));
            }else{
            	PIC_FOLDER = devices.substring(devices.lastIndexOf("/")+1);
            }
        	PIC_FOLDER = PIC_FOLDER+"M";//music
        }else if(style == 0){
        	if(devices.indexOf(":") > -1){
            	PIC_FOLDER = devices.substring(0, devices.indexOf(":"));
            }else{
            	PIC_FOLDER = devices;
            }
        	PIC_FOLDER = PIC_FOLDER+"P";//picture
        }        
        Log.d(TAG,"=sp_device="+PIC_FOLDER);
        init(context, list);
    }

    private void init(Context context, List<ListSelectItem> list)
    {
        mListItems = list;
        this.context = context;
        for (int i = 0; i < mListItems.size(); i++)
        {
        	mapSelected.put(String.valueOf(i), false);
        }
               
        SharedPreferences share=context.getSharedPreferences(PIC_FOLDER, Context.MODE_PRIVATE);
    	Log.d(TAG,"=PIC_NAME="+share.getString(PIC_NAME,""));
        picName.append(share.getString(PIC_NAME,""));

        initParam();

        mInflater = LayoutInflater.from(context);
        mVListContainer = mInflater.inflate(R.layout.list_setting_select, null, true);
        mVListContainer.setFocusable(true);
        mVListContainer.requestFocus();

        this.setContentView(mVListContainer);

        mGvListSelect = (GridView) mVListContainer.findViewById(R.id.gv_list_folder_select);
        mGvListSelect.setFocusable(true);
        mGvListSelect.requestFocus();
        mGvListSelect.setOnItemSelectedListener(mOnItemSelectedListener);

        mListSelectAdapter = new SubtitleSelectAdapter(mInflater, mListItems);
        mGvListSelect.setAdapter(mListSelectAdapter);

        mIvLogo = (ImageView) mVListContainer.findViewById(R.id.iv_list_setting_logo);
        mTvSelectTip = (TextView) mVListContainer.findViewById(R.id.tv_select_tip);
        mIvNoAlbum = (TextView) mVListContainer.findViewById(R.id.iv_no_album);

        setOnKeyListener(mKeyListener);
    }

    public void show(View parent)
    {
        showDialog();
    }

    public void hide()
    {
        if (mOnSelectPopupListener != null)
        {
            mOnSelectPopupListener.onSelectPopupHide();
        }
        dismiss();
    }

    public void showDialog()
    {
        windowDeploy();
        setCanceledOnTouchOutside(true);

        if (mListItems == null || mListItems.size() == 0)
        {
            mIvNoAlbum.setVisibility(View.VISIBLE);
            mGvListSelect.setVisibility(View.GONE);
        }
        else
        {
            mIvNoAlbum.setVisibility(View.GONE);
            mGvListSelect.setVisibility(View.VISIBLE);
        }
        show();
        mGvListSelect.requestFocus();
    }

    public void setOnSelectPopupListener(OnSelectPopupListener l)
    {
        mOnSelectPopupListener = l;
    }

    /**
     * 
     * Description: 回调<br>
     * @author s00211113
     * @version v1.0 Date: 2014-7-23 下午2:59:54<br>
     */
    public interface OnSelectPopupListener
    {
        void onListSelected(List<ListSelectItem> list, ArrayList<Integer> selectedIdxList);

        void onSelectPopupHide();
    }

    public void setLogoAndTip(int logoId, int tipId, int notifyId)
    {
        mIvLogo.setImageResource(logoId);
        mTvSelectTip.setText(tipId);
        mIvNoAlbum.setText(notifyId);
    }

    public void setSelected(ArrayList<Integer> selectedlist)
    {
        if (selectedlist == null || selectedlist.size() == 0)
        {
            return;
        }
        for (Integer selected : selectedlist)
        {
            mapSelected.put(String.valueOf(selected), true);
        }
    }

    // 设置窗口显示
    @SuppressLint("InlinedApi")
    private void windowDeploy()
    {
        Window window = getWindow(); // 得到对话框
        window.setBackgroundDrawableResource(R.color.transparent_color); // 设置对话框背景为透明
        WindowManager.LayoutParams wl = window.getAttributes();
        wl.gravity = Gravity.CENTER; // 设置重力
        wl.width = LayoutParams.MATCH_PARENT;
        wl.height = LayoutParams.MATCH_PARENT;
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
        window.getDecorView().setSystemUiVisibility(View.INVISIBLE);
        window.setAttributes(wl);
    }

    private OnItemSelectedListener mOnItemSelectedListener = new OnItemSelectedListener()
    {

        @Override
        public void onItemSelected(AdapterView<?> arg0, View view, int arg2, long arg3)
        {
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0)
        {
        }

    };

    private void itemClickOperate(int position)
    {
        Boolean isSelected = mapSelected.get(String.valueOf(position));

        View view = getViewByPos(position);
        if (null != view)
        {
            ViewHolder viewHolder = (ViewHolder) view.getTag();
            if (null != viewHolder)
            {
            	SharedPreferences share=context.getSharedPreferences(PIC_FOLDER, Context.MODE_PRIVATE);
                Editor ed = share.edit();
                if (isSelected.booleanValue())
                {
                    Log.d(TAG, "itemClickOperate, isSelected ture-->false");
                    mapSelected.put(String.valueOf(position), false);
                    viewHolder.ivSelected.setVisibility(View.GONE);                    
                    if(picName.indexOf(mListItems.get(position).getName()+"/")>-1){
                    	if(picName.indexOf(mListItems.get(position).getName()+"/")==0){
                    		picName.delete(picName.indexOf(mListItems.get(position).getName()+"/"), (picName.indexOf(mListItems.get(position).getName()+"/")+(mListItems.get(position).getName()+"/").length()));
                    	}else if(picName.indexOf("/"+mListItems.get(position).getName()+"/")>-1){
                    		picName.delete(picName.indexOf("/"+mListItems.get(position).getName()+"/"), (picName.indexOf("/"+mListItems.get(position).getName()+"/")+(mListItems.get(position).getName()+"/").length()));
                    	}
                    	
                    }
                    ed.putString(PIC_NAME, picName.toString());
                    ed.commit();                    
                    Log.d(TAG, "item false==getName= "+picName.toString());
                }
                else
                {
                    Log.d(TAG, "itemClickOperate, isSelected false-->ture");
                    mapSelected.put(String.valueOf(position), true);
                    viewHolder.ivSelected.setVisibility(View.VISIBLE);
                    picName.append(mListItems.get(position).getName()+"/");
                    ed.putString(PIC_NAME, picName.toString());
                    ed.commit();                    
                    Log.d(TAG, "item true==getName= "+picName.toString());
                    
                }
            }
        }
    }

    private void showFocusBackground(int position)
    {
        View view = getViewByPos(position);
        if (null != view)
        {
            ViewHolder viewHolder = (ViewHolder) view.getTag();
            if (null != viewHolder)
            {
                viewHolder.ivFocused.setVisibility(View.VISIBLE);
            }
        }
    }

    private void hideFocusBackground(int position)
    {
        View view = getViewByPos(position);
        if (null != view)
        {
            ViewHolder viewHolder = (ViewHolder) view.getTag();
            if (null != viewHolder)
            {
                viewHolder.ivFocused.setVisibility(View.INVISIBLE);
            }
        }
    }

    private OnKeyListener mKeyListener = new OnKeyListener()
    {

        @Override
        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event)
        {
            Log.d(TAG, "keyCode = " + keyCode + " enter !");
            int numColumns = mGvListSelect.getNumColumns();
            boolean ret = false;
            if (event.getAction() == KeyEvent.ACTION_UP)
            {
                return false;
            }
            Log.d(TAG, "keyCode = " + keyCode + " mCurrSelectFocusPos = " + mCurrSelectFocusPos + " totoal item = " + mListItems.size());
            switch (keyCode)
            {
                case KeyEvent.KEYCODE_DPAD_DOWN:
                    if (mCurrSelectFocusPos + numColumns < mListItems.size())
                    {
                        hideFocusBackground(mCurrSelectFocusPos);
                        mCurrSelectFocusPos += numColumns;
                        showFocusBackground(mCurrSelectFocusPos);
                    }
                    else if ((mListItems.size() - 1) / numColumns > (mCurrSelectFocusPos) / numColumns)
                    {
                        hideFocusBackground(mCurrSelectFocusPos);
                        mCurrSelectFocusPos = mListItems.size() - 1;
                        showFocusBackground(mCurrSelectFocusPos);
                    }
                    break;
                case KeyEvent.KEYCODE_DPAD_UP:
                    if (mCurrSelectFocusPos - numColumns >= 0)
                    {
                        hideFocusBackground(mCurrSelectFocusPos);
                        mCurrSelectFocusPos -= numColumns;
                        showFocusBackground(mCurrSelectFocusPos);
                    }
                    break;
                case KeyEvent.KEYCODE_DPAD_LEFT:
                    if (mCurrSelectFocusPos > 0)
                    {
                        hideFocusBackground(mCurrSelectFocusPos);
                        mCurrSelectFocusPos -= 1;
                        showFocusBackground(mCurrSelectFocusPos);
                        mGvListSelect.setSelection(mCurrSelectFocusPos);
                    }
                    break;
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    if (mCurrSelectFocusPos < mListItems.size() - 1)
                    {
                        hideFocusBackground(mCurrSelectFocusPos);
                        mCurrSelectFocusPos += 1;
                        showFocusBackground(mCurrSelectFocusPos);
                        mGvListSelect.setSelection(mCurrSelectFocusPos);
                    }
                    break;
                case KeyEvent.KEYCODE_ENTER:
                case KeyEvent.KEYCODE_DPAD_CENTER:
                    itemClickOperate(mCurrSelectFocusPos);
                    break;
                case KeyEvent.KEYCODE_BACK:
                    if (mOnSelectPopupListener != null)
                    {
                        List<ListSelectItem> selectedList = new ArrayList<ListSelectItem>();
                        ArrayList<Integer> selectedIdxList = new ArrayList<Integer>();
                        selectedList.clear();
                        Collection<String> keys = mapSelected.keySet();
                        for (String str : keys)
                        {
                            int index = Integer.parseInt(str);
                            if (0 > index || index >= mListItems.size())
                            {
                                continue;
                            }
                            if (mapSelected.get(str).booleanValue() == true)
                            {
                                selectedList.add(mListItems.get(index));
                                selectedIdxList.add(Integer.parseInt(str));
                            }
                        }
                        mOnSelectPopupListener.onListSelected(selectedList, selectedIdxList);
                    }
                    backClickOperate();
                    ret = true;
                    break;
                default:
                    break;
            }
            return ret;
        }

    };

    public void initParam()
    {
        mCurrSelectFocusPos = 0;
        if (mGvListSelect != null)
        {
            backToFirstLine();
        }
    }

    private void backToFirstLine()
    {
        mGvListSelect.post(new Runnable()
        {
            @Override
            public void run()
            {
                mGvListSelect.setSelection(mCurrSelectFocusPos);
            }
        });

    }

    private View getViewByPos(int position)
    {
        View view = mGvListSelect.getChildAt(position - mGvListSelect.getFirstVisiblePosition());
        return view;
    }

    private void backClickOperate()
    {
        Log.d(TAG, "Back to menu click!");
        hide();
    }

    /**
     * 
     * Description: 选择对话框适配器<br>
     * @author s00211113
     * @version v1.0 Date: 2014-7-23 下午3:01:07<br>
     */
    public class SubtitleSelectAdapter extends BaseAdapter
    {
        private List<ListSelectItem> mFolderPaths;

        private LayoutInflater mInflater;

        SubtitleSelectAdapter(LayoutInflater inflater, List<ListSelectItem> folderPaths)
        {
            mFolderPaths = folderPaths;
            mInflater = inflater;
        }

        public void resetData(List<ListSelectItem> folderPaths)
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
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            ViewHolder viewHolder = null;
            if (convertView == null)
            {
                convertView = mInflater.inflate(R.layout.list_setting_item, null, true);
                viewHolder = new ViewHolder(convertView);
                convertView.setTag(viewHolder);
            }
            else
            {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            ListSelectItem subtileFile = mFolderPaths.get(position);

            if (mCurrSelectFocusPos == position)
            {
                viewHolder.ivFocused.setVisibility(View.VISIBLE);
            }
            else
            {
                viewHolder.ivFocused.setVisibility(View.INVISIBLE);
            }

            SharedPreferences share=context.getSharedPreferences(PIC_FOLDER, Context.MODE_PRIVATE);
            String name=share.getString(PIC_NAME,"");
            if(name.indexOf(subtileFile.getName()+"/")>-1){
            	if(name.indexOf(subtileFile.getName()+"/")==0){
            		viewHolder.ivSelected.setVisibility(View.VISIBLE);
            		mapSelected.put(String.valueOf(position), true);
            	}else if(name.indexOf("/"+subtileFile.getName()+"/")>-1){
            		viewHolder.ivSelected.setVisibility(View.VISIBLE);
            		mapSelected.put(String.valueOf(position), true);
            	}else{
                	viewHolder.ivSelected.setVisibility(View.GONE);
                }
            }else{
            	viewHolder.ivSelected.setVisibility(View.GONE);
            }
           
            
//            if (mapSelected.get(String.valueOf(position)).booleanValue())
//            {
//                viewHolder.ivSelected.setVisibility(View.VISIBLE);
//            }
//            else
//            {
//                viewHolder.ivSelected.setVisibility(View.GONE);
//            }

            viewHolder.tvFileName.setText(subtileFile.getName());
            viewHolder.tvFileNum.setText(subtileFile.getOther());
            return convertView;
        }

    }

    /**
     * 
     * Description: item项画图用的ViewHolder<br>
     * @author s00211113
     * @version v1.0 Date: 2014-7-23 下午3:01:26<br>
     */
    public class ViewHolder
    {
        public ImageView ivIcon;

        public ImageView ivFocused;

        public TextView tvFileName;

        public TextView tvFileNum;

        public ImageView ivSelected;

        ViewHolder(View view)
        {
            ivIcon = (ImageView) view.findViewById(R.id.iv_list_item_folder);
            ivFocused = (ImageView) view.findViewById(R.id.iv_list_image_focused);
            tvFileName = (TextView) view.findViewById(R.id.tv_list_item_filename);
            tvFileNum = (TextView) view.findViewById(R.id.tv_list_item_filenum);
            ivSelected = (ImageView) view.findViewById(R.id.iv_list_selected);
        }
    }
}
