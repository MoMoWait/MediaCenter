package com.rockchips.mediacenter.audioplayer;

import java.util.List;

import android.app.Activity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.rockchips.mediacenter.R;
import com.rockchips.mediacenter.basicutils.util.StringUtils;
import com.rockchips.mediacenter.viewutils.playlist.IListDataAdapter;

public class TextViewListAdapter implements IListDataAdapter
{
    private List<String> mDataList;

    private Activity mContext;

    private Object listLock = new Object();

    private LayoutInflater mInflater;

    private TextView mItemName;

    private ImageView mItemBg;

    // private ImageView mItemIcon;

    /**
     * 初始化ListView时需要高亮显示的数据项index
     */
    private int mIndexNeedColored;

    private int mTextViewLeftPadding = 63;

    private ImageView mPlayControlImage;

    public TextViewListAdapter(Activity mContext)
    {
        super();
        this.mContext = mContext;
        mInflater = mContext.getLayoutInflater();

        // if (PlayerBaseActivity.mbAR) {
        // mTextViewLeftPadding = 0;
        // } else {
        // mTextViewLeftPadding = 63;
        // }
    }

    @Override
    public int getCount()
    {
        return getDataList() == null ? 0 : getDataList().size();
    }

    @Override
    public View createItemView(int index, int offset, int visibleItemCount)
    {
        View v = mInflater.inflate(R.layout.audio_list_item, null);
        mPlayControlImage = (ImageView) v.findViewById(R.id.music_playcontrol_icon);
        mItemName = (TextView) v.findViewById(R.id.itemname);

        mItemBg = (ImageView) v.findViewById(R.id.item_bg);

        if (index + offset >= mDataList.size())
        {
            mItemName.setText("");
            mItemName.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
            return v;
        }

        String info = null;
        info = mDataList.get(index + offset);

        mItemName.setText(info);
        mItemName.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
        mItemName.setPadding(mTextViewLeftPadding, 0, 0, 0);
        if (index == mIndexNeedColored)
        {
            mItemName.setTextColor(mContext.getResources().getColor(R.color.white));
            mPlayControlImage.setVisibility(View.VISIBLE);
        }
        else
        {
            mItemName.setTextColor(mContext.getResources().getColor(R.color.itemUnFocused));
        }

        if ((index + offset) != (mDataList.size() - 1) && index != visibleItemCount - 1)
        {
            mItemBg.setVisibility(View.VISIBLE);
        }

        return v;
    }

    @Override
    public List<View> updateItemView(List<View> viewList, int visibleItemCount, int offset)
    {
        View tempView = null;
        TextView textView = null;
        ImageView imageView = null;

        String info = null;
        String title;

        synchronized (listLock)
        {
            for (int i = 0; i < visibleItemCount; i++)
            {
                tempView = viewList.get(i);
                textView = (TextView) tempView.findViewById(R.id.itemname);
                imageView = (ImageView) tempView.findViewById(R.id.item_bg);

                if ((i + offset) <= (mDataList.size() - 1))
                {
                    info = mDataList.get(i + offset);

                    // textView.setText("");
                    textView.setText(info);

                    textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
                    textView.setPadding(mTextViewLeftPadding, 0, 0, 0);

                    tempView.setTag(i + offset);
                    if ((i + offset) != (mDataList.size() - 1) && i != visibleItemCount - 1)
                    {
                        imageView.setVisibility(View.VISIBLE);
                    }

                    viewList.set(i, tempView);
                }
            }
        }

        return viewList;
    }

    @Override
    public List<String> getDataList()
    {
        return mDataList;
    }

    @Override
    public void setDataList(List<String> list)
    {
        this.mDataList = list;
    }

    @Override
    public void setIndexNeedColored(int indexNeedColored)
    {
        this.mIndexNeedColored = indexNeedColored;
    }

}
