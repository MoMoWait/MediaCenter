/*******************************************************************
* Company:     Fuzhou Rockchip Electronics Co., Ltd
* Filename:    SetShareDialogActivity.java  
* Description:   
* @author:     fxw@rock-chips.com
* Create at:   2012-4-15 下午07:52:21  
* 
* Modification History:  
* Date         Author      Version     Description  
* ------------------------------------------------------------------  
* 2012-4-15      fxw         1.0         create
*******************************************************************/   


package com.rockchip.mediacenter.mediaserver.ui;

import com.rockchip.mediacenter.R;
import com.rockchip.mediacenter.mediaserver.MediaServer;
import com.rockchip.mediacenter.mediaserver.MediaShareType;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

/**
 *
 * @author fxw
 * @since 1.0
 */
public class SetShareChoiceActivity extends Activity implements AdapterView.OnItemClickListener, AdapterView.OnItemSelectedListener, View.OnClickListener {

	private ListView mListView;
	private SetShareChoiceAdapter mAdapter;
	private Button mSureButton;
	private MediaShareType mLastShareType;
	
	/** 
	 * <p>Title: onCreate</p> 
	 * <p>Description: </p> 
	 * @param savedInstanceState 
	 * @see android.app.Activity#onCreate(android.os.Bundle) 
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setWindowAnimations(R.style.PopupAnimation);
		setContentView(R.layout.dms_share_choice);
		mListView = (ListView)findViewById(R.id.dms_share_choice_listview);
		mSureButton = (Button)findViewById(R.id.button1);
		mSureButton.setOnClickListener(this);
		findViewById(R.id.button2).setOnClickListener(this);
		mAdapter = new SetShareChoiceAdapter(this);
		mLastShareType = (MediaShareType)getIntent().getSerializableExtra(MediaServer.KEY_SHARE_TYPE);
		if(MediaShareType.FOLDER_SHARE==mLastShareType){
			mAdapter.setSelectedIndex(1);
			mAdapter.setFocusPosition(1);
		}else{
			mAdapter.setSelectedIndex(0);
			mAdapter.setFocusPosition(0);
		}
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(this);
		mListView.setOnItemSelectedListener(this);
	}
	
	protected void onResume() {
		super.onResume();
		if(MediaShareType.FOLDER_SHARE==mLastShareType){
			mListView.setSelection(1);
		}else{
			mListView.setSelection(0);
		}
		mListView.requestFocus();
	}

	/** 
	 * <p>Title: onItemClick</p> 
	 * <p>Description: </p> 
	 * @param adapter
	 * @param view
	 * @param position
	 * @param id 
	 * @see android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget.AdapterView, android.view.View, int, long) 
	 */
	@Override
	public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
		mAdapter.setSelectedIndex(position);
		mAdapter.setFocusPosition(position);
		mAdapter.notifyDataSetChanged();
	}

	/** 
	 * <p>Title: onClick</p> 
	 * <p>Description: </p> 
	 * @param view 
	 * @see android.view.View.OnClickListener#onClick(android.view.View) 
	 */
	@Override
	public void onClick(View view) {
		int viewId = view.getId();
		if(viewId == R.id.button1){
			int selected = mAdapter.getSelectedIndex();
			Intent data = new Intent();
			if(selected==0){
				data.putExtra(MediaServer.KEY_SHARE_TYPE, MediaShareType.MEDIA_SHARE);
			}else{
				data.putExtra(MediaServer.KEY_SHARE_TYPE, MediaShareType.FOLDER_SHARE);
			}
			setResult(RESULT_OK, data);
			finish();
		}else if(viewId == R.id.button2){
			finish();
		}
	}

	/** 
	 * <p>Title: onItemSelected</p> 
	 * <p>Description: </p> 
	 */
	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		mAdapter.setFocusPosition(position);
		mAdapter.notifyDataSetChanged();
	}

	/** 
	 * <p>Title: onNothingSelected</p> 
	 * <p>Description: </p> 
	 * @param arg0 
	 * @see android.widget.AdapterView.OnItemSelectedListener#onNothingSelected(android.widget.AdapterView) 
	 */
	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		int selectedIndex = mAdapter.getSelectedIndex();
		if(selectedIndex>=0 && selectedIndex<parent.getChildCount()){
			mAdapter.setFocusPosition(selectedIndex);
			mAdapter.notifyDataSetChanged();
		}
	}
	
}
