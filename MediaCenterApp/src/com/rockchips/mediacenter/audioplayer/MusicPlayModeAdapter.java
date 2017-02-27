package com.rockchips.mediacenter.audioplayer;

import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rockchips.mediacenter.R;
import com.rockchips.mediacenter.view.AbstractWheelTextAdapter;

public class MusicPlayModeAdapter extends AbstractWheelTextAdapter {

	public MusicPlayModeAdapter(Context context) {
		super(context, R.layout.music_option_type_layout, NO_RESOURCE);
		setItemTextResource(R.id.item_name);
	}

	private List<String> typeItemList;

	public void setTypeItemList(List<String> itemList) {
		this.typeItemList = itemList;
	}


	@Override
	public int getItemsCount() {
		return typeItemList != null ? typeItemList.size() : 0;
	}

	@Override
	protected CharSequence getItemText(int index) {
		return typeItemList.get(index);
	}

	@Override
    public View getItem(int index, View convertView, ViewGroup parent)
    {
        View view = (View) super.getItem(index, convertView, parent);
        if (null != view)
        {
            TextView textView = (TextView) view.findViewById(R.id.item_name);
            textView.setTextColor(Color.WHITE);
        }
        return view;
    }

}
