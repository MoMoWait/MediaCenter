package com.rockchips.mediacenter.imageplayer;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.widget.TextView;
import android.widget.Toast;
import com.rockchips.mediacenter.R;
import com.rockchips.mediacenter.service.LocalDeviceManager;
import com.rockchips.mediacenter.bean.LocalDeviceInfo;
import com.rockchips.mediacenter.bean.LocalMediaInfo;
import com.rockchips.mediacenter.data.ConstData;
import com.rockchips.mediacenter.data.ConstData.EBrowerType;
import com.rockchips.mediacenter.data.ConstData.MediaType;
import com.rockchips.mediacenter.utils.IICLOG;
import com.rockchips.mediacenter.bean.PlayStateInfo;
import com.rockchips.mediacenter.audioplayer.BackgroundAudioPreviewWidget;
import com.rockchips.mediacenter.view.ListSelectItem;
import com.rockchips.mediacenter.view.ListSelectPopup;
import com.rockchips.mediacenter.view.ListSelectPopup.OnSelectPopupListener;

/**
 * 图片浏览器
 * @author GaoFei
 *
 */
public class InternalImagePlayer extends ImagePlayerActivity implements OnSelectPopupListener
{
    private static String TAG = "MediaCenterApp";

    private static IICLOG mLog = IICLOG.getInstance();

    /**
     * 当前设备hashcode| 默认情况或者首页返回为-1
     */
    private int mDeviceIdHashCode = -1;

    /**
     * 该设备是否已经下线标志位，默认为false:没有下线
     */
    protected boolean mbDeviceDown = false;

    /**
     * UI Handler
     */
    protected Handler mUIHandler = new UIHandler();

//    private static final int MSG_UI_GOHOME = 0x0013;

    protected static final int DEVICES_DOWN_FLAG = 0x0012;

    private static final int MSG_ALERTDIALOG_SHOW = 0x0014;

    private static final int MSG_ALERTDIALOG_DISMISS = 0x0015;

    private int mCurrentPlayIndex;

    private static String mCurDevId;

    public static final String MEDIAPLAYERINDEX_URL = "content://com.rockchips.mediaIndex";

    // 内置一个定时器，用于二秒计时
    private Timer mTimer = null;

    private TimerTask mTimerTask = null;

    private static final int DELAY = 1000;

    private static final int PERIOD = 1000;

    private int GETGAP = 20;

    private Toast mToast;

    private TextView mTextView = null;

    /** add by 2014.3.7 xWX184171 */
    // 显示图片时弹出的歌曲信息
    private BackgroundAudioPreviewWidget mBackgroundAudioPreviewWidget;

    private static int index;

//    private boolean isNeedGoHome = false;

    /**
     * 音频文件错误不能播放
     */
    protected static final int SHOW_MSG_ERRORMUSIC = 2;

    /**
     * 连续音频文件错误，退出背景音乐播放
     */
    protected static final int SHOW_MSG_ERROR = 3;

    /**
     * 自定义背景音乐序列化到file文件目录下的文件名
     */
    private String mMusicSerializeFileName = "background_music";

    /**
     * 自定义音乐序列化最大条数
     */
    private int mMusicSericalizeMaxNum = 100;

//    private int deviceType;
    
    private LocalDeviceInfo mLocalDeviceInfo;
    
    private LocalDeviceManager mLocalDeviceManager;
    
    /**
     * 自定义背景发生改变
     */
    private static boolean bBackGroupAudioChange = false;

    private class UIHandler extends Handler
    {
        @Override
        public void handleMessage(Message msg)
        {
            super.handleMessage(msg);

            switch (msg.what)
            {
//            // 跳到首页
//                case MSG_UI_GOHOME:
//                    gotoHome();
//                    break;
                case MSG_ALERTDIALOG_SHOW:

                    if (mTextView == null)
                    {
                        mTextView = new TextView(getApplicationContext());
                        mTextView.setTextSize(18);
                        mTextView.setPadding(15, 5, 15, 5);
                        mTextView.setBackgroundColor(Color.BLACK);
                    }
                    mTextView.setText(getString(R.string.nowplaying) + msg.obj);

                    mToast.setView(mTextView);
                    mToast.show();

                    LocalMediaInfo mMediaBaseInfo = mediaFileInfoToBaseInto();
                    mBackgroundAudioPreviewWidget.setBaseMediaInfo(mMediaBaseInfo);
                    mBackgroundAudioPreviewWidget.show();
                    break;
                case MSG_ALERTDIALOG_DISMISS:
                    if (mToast != null)
                    {
                        mToast.cancel();
                    }
                    break;
                case SHOW_MSG_ERROR:

                    if (mTextView == null)
                    {
                        mTextView = new TextView(getApplicationContext());
                        mTextView.setTextSize(18);
                        mTextView.setPadding(15, 5, 15, 5);
                        mTextView.setBackgroundColor(Color.BLACK);
                    }
                    mTextView.setText(getString(R.string.nomusictoplay));

                    mToast.setView(mTextView);
                    mToast.show();
                    break;
                case SHOW_MSG_ERRORMUSIC:

                    break;
                default:
                    break;
            }
        }
    }

    public static void setMediaList(List<Bundle> mediaInfoList, int index)
    {        
        mLog.d(TAG, "setMediaList-->playStateInfo:" + mediaInfoList);
        mStMediaInfoList = mediaInfoList;
        mLog.d(TAG, "setMediaList-->setCurrentIndex:" + index);
        mStIndex = index;
    }

//    public static void addList(List<Bundle> mediaInfoList)
//    {        
//        mPlayStateInfo.insertList(mediaInfoList);
//    }

    /**
     * 添加背景音乐列表 <功能详细描述>
     * @see [类、类#方法、类#成员]
     */
    public static void setBackGroupAudioList(List<LocalMediaInfo> mediaList)
    {
        mLog.d(TAG, "setBackGroupAudioList :" + mediaList.size());        
        PlayStateInfo.setmBackGroupAudiolist(mediaList);

        // 存入deviceId
        PlayStateInfo.setBgAudioDeviceId(mCurDevId);

        bBackGroupAudioChange = true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mLog.d(TAG, "parseIntent---start");        
        mLocalDeviceManager = LocalDeviceManager.getInstance(getBaseContext());
        
        mLocalDeviceInfo = getDeviceInfo();
        parseDeviceInfo();
        mBackgroundAudioPreviewWidget = new BackgroundAudioPreviewWidget(this);
        
        loadAudioPreferences();
        // zkf61715 背景音乐只播放同一设备上的音乐，如果设备换了，播放默认音乐        
        List<LocalMediaInfo> palylist = PlayStateInfo.getmBackGroupAudiolist();
        if (palylist != null && !palylist.isEmpty())
        {
            mLog.i(TAG, "mCurId=" + mCurDevId + "---------------deviceId=" + PlayStateInfo.getBgAudioDeviceId());
            if (PlayStateInfo.getDevIdForSelectAud() == null || !PlayStateInfo.getDevIdForSelectAud().equals(mCurDevId))
            {
                setPlayDefaultBgMusic(true);
            }
            else
            {
                setPlayDefaultBgMusic(false);
            }
        }        

        mLog.d(TAG, "parseIntent---end");
        startTimer();

        mToast = Toast.makeText(getApplicationContext(), "", Toast.LENGTH_LONG);

    }

    @Override
    protected void onPause()
    {
        super.onPause();
        if (mBackgroundAudioPreviewWidget.isShown())
        {
            mBackgroundAudioPreviewWidget.hide();
        }
//        mUIHandler.removeMessages(MSG_UI_GOHOME);
    }

    private LocalMediaInfo mediaFileInfoToBaseInto()
    {
        LocalMediaInfo mMediaBaseInfo = new LocalMediaInfo();
        List<LocalMediaInfo> palylist = PlayStateInfo.getmBackGroupAudiolist();
        /* BEGIN: Modified by s00211113 for DTS2014033000145 2014/3/31 */
        if (palylist != null && palylist.size() != 0 && !getPlayDefaultBgMusic())
        {      
            mMediaBaseInfo = palylist.get(index);
        }
        /* END: Modified by s00211113 for DTS2014033000145 2014/3/31 */
        return mMediaBaseInfo;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (mBackgroundAudioPreviewWidget.isShown())
        {
            mBackgroundAudioPreviewWidget.hide();
        }
//        if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN || keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT || keyCode == KeyEvent.KEYCODE_DPAD_CENTER
//                || keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_MENU || keyCode == KeyEvent.KEYCODE_BACK)
//        {
//            if (isNeedGoHome)
//            {
//                isNeedGoHome = false;
//                mUIHandler.removeMessages(MSG_UI_GOHOME);
//                DataNotify.list.clear();
//                if (!currentFileExists())
//                {
//                    mUIHandler.sendEmptyMessage(MSG_UI_GOHOME);
//                    return true;
//                }
//            }
//
//        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected String getMusicPath(int mIndex)
    {
        /* BEGIN: Modified by s00211113 for DTS2014033000145 2014/3/31 */
        List<LocalMediaInfo> palylist = null;
        if (!getPlayDefaultBgMusic())
        {            
            palylist = PlayStateInfo.getmBackGroupAudiolist();
        }
        /* END: Modified by s00211113 for DTS2014033000145 2014/3/31 */
        // 自定义背景音乐发生改变时，重新序列化保存
        if (bBackGroupAudioChange)
        {
            bBackGroupAudioChange = false;
        }

        if (mIndex < 0)
        {
            if (palylist != null && palylist.size() == 1)
            {
                mUIHandler.post(new Runnable()
                {

                    @Override
                    public void run()
                    {
                        Toast.makeText(getApplicationContext(), R.string.without_music, Toast.LENGTH_SHORT).show();

                    }
                });
            }
            return null;
        }

        if (palylist != null && !palylist.isEmpty())
        {
            mIndex %= palylist.size();
            index = mIndex;
            Message msg = mUIHandler.obtainMessage(MSG_ALERTDIALOG_SHOW);

            if (mUIHandler.hasMessages(MSG_ALERTDIALOG_DISMISS))
            {
                mUIHandler.removeMessages(MSG_ALERTDIALOG_DISMISS);
            }
            final LocalMediaInfo media = palylist.get(mIndex);
            msg.obj = media.getmFileName();
            mUIHandler.sendMessage(msg);
            // 显示3s后消失
            mUIHandler.sendEmptyMessageDelayed(MSG_ALERTDIALOG_DISMISS, 3000);
            mLog.i("keke", "path: " + media.getUrl());
            return media.getUrl();
        }

        return null;

    }

    @Override
    protected int getMusicSize()
    {
        /* BEGIN: Modified by s00211113 for DTS2014033000145 2014/3/31 */
        if (getPlayDefaultBgMusic())
        {
            return 0;
        }
        /* END: Modified by s00211113 for DTS2014033000145 2014/3/31 */
        List<LocalMediaInfo> palylist = PlayStateInfo.getmBackGroupAudiolist();
        if (palylist != null && palylist.size() > 0)
        {
            return palylist.size();
        }

        return 0;
    }

    // 清除自定义背景音乐
    @Override
    protected void clearMusic()
    {
        // 获取音乐列表
        List<LocalMediaInfo> palylist = PlayStateInfo.getmBackGroupAudiolist();
        if (palylist != null)
        {
            // 清除列表
            palylist.clear();
        }
        // 把列表引用置空
        PlayStateInfo.setmBackGroupAudiolist(null);
        // zkf61715 保存的文件夹信息置空
        PlayStateInfo.getmFavoriteSet().clear();

        // 删除序列换的自定义背景音乐
        File serializeMusicFile = getFileStreamPath(mMusicSerializeFileName);
        if (serializeMusicFile != null && serializeMusicFile.exists())
        {
            serializeMusicFile.delete();
        }
    }

    protected void showToast(int errorindex)
    {
        if (errorindex == SHOW_MSG_ERROR)
        {
            mUIHandler.sendEmptyMessage(SHOW_MSG_ERROR);
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();
    }
    

    /**
     * 跳转到首页
     */
    protected void gotoHome(){}

    /**
     * 分析intent 获取设备id，用于设备上下线比较
     */
    private void parseDeviceInfo()
    {
        int deviceType = mLocalDeviceInfo.getDeviceType();
        String deviceId = null;
        if (deviceType == ConstData.DeviceType.DEVICE_TYPE_DMS)
        { // DMS设备类型
//            deviceId = bundle.getString("device_id");
//            PlayStateInfo.getInstance().setCurrentDevId(deviceId);
            deviceId = String.valueOf(mLocalDeviceInfo.getmDeviceId());
            PlayStateInfo.setCurrentDevId(deviceId);
        }
        else if (deviceType == ConstData.DeviceType.DEVICE_TYPE_U || deviceType == ConstData.DeviceType.DEVICE_TYPE_SD
        		|| deviceType == ConstData.DeviceType.DEVICE_TYPE_INTERNEL_STORAGE)
        { // U盘设备类型
          // SD盘设备类型,返回的类型为sta
            deviceId = mLocalDeviceInfo.getMountPath();
            PlayStateInfo.setCurrentDevId(deviceId);
        }
        mCurDevId = deviceId;
        if (deviceId != null && !deviceId.trim().equals(""))
        {
            mDeviceIdHashCode = deviceId.hashCode();
            mLog.d(TAG, "parseIntent--device_id--hashCode:" + mDeviceIdHashCode);
        }
    }
    

    @Override
    protected void onDestroy()
    {
        // zkf61715
        mPlayStateInfo.setbFromImage(false);
        List<LocalMediaInfo> palylist = PlayStateInfo.getmBackGroupAudiolist();
        if (palylist != null && palylist.size() > 0)
        {
        }        
        stopTimer();
        super.onDestroy();
    }

//    @Override
//    public void onDeviceBrowser(String deviceId, String dms_id, int deviceType)
//    {
//        // TODO Auto-generated method stub
//
//    }
//
//    /**
//     * 下线
//     */
//    @Override
//    public void onDeviceDown(String deviceId, String dms_id, int deviceType)
//    {
//        mLog.d(TAG, "deviceId--->" + deviceId);
//        mLog.d(TAG, "dms_id--->" + dms_id);
//        mLog.d(TAG, "mCurId--->" + mCurDevId);
//        boolean isCurrentDev = isCurrentDev(deviceId, deviceType);
//        if (isCurrentDev)
//        {
//            mUIHandler.sendEmptyMessage(MSG_UI_GOHOME);
//        }
//
//    }
//
//    @Override
//    public void onDeviceUp(String deviceId, String dms_id, int deviceType)
//    {
//
//    }
//
//    @Override
//    public void onNetworkDisconnected()
//    {
//        mLog.d(TAG, "onNetworkDisconnected()------->start");
//    }
//
//    @Override
//    public void onNetworkConnected()
//    {
//        // TODO Auto-generated method stub
//
//    }
//
//  @Override
//  public void onAddSharedResult(String result)
//  {
//      // TODO Auto-generated method stub
//
//  }
//
//  @Override
//  public void onDeleteSharedResult(String result)
//  {
//      // TODO Auto-generated method stub
//
//  }

    public int getDeviceIdHashCode()
    {
        return mDeviceIdHashCode;
    }

    public void setDeviceIdHashCode(int mDeviceIdHashCode)
    {
        this.mDeviceIdHashCode = mDeviceIdHashCode;
    }

    @Override
    public void onBackPressed()
    {
        if (mBackgroundAudioPreviewWidget.isShown())
        {
            mBackgroundAudioPreviewWidget.hide();
        }        
        mCurrentPlayIndex = mPlayStateInfo.getCurrentIndex();       
        mLog.d(TAG, "onBackPressed-->mCurrentPlayIndex--->" + mCurrentPlayIndex);
        passIntentForImageBrowser();
        super.onBackPressed();
    }

    /**
     * 将当前播放索引传回图片浏览界面
     * @see [类、类#方法、类#成员]
     */
    public void passIntentForImageBrowser()
    {
        Bundle bundle = new Bundle();
        bundle.putInt("playIndex", mCurrentPlayIndex);
        bundle.putInt("mediaType", ConstData.MediaType.IMAGE);

        Intent intent = new Intent();
        intent.putExtras(bundle);

        setResult(RESULT_OK, intent);
    }

    @Override
    protected boolean jumpToMusicBrowser()
    {
        showImageSelectDialog();
        return true;
    }

    // 加一个定时器，定期检测播放索引
    private void startTimer()
    {
        if (mTimer == null)
        {
            mTimer = new Timer();
        }
        else
        {
            return;
        }

        if (mTimerTask != null)
        {
            mTimerTask.cancel();
            mTimerTask = null;
        }
        
        mTimerTask = new TimerTask()
        {
            @Override
            public void run()
            {
                mCurrentPlayIndex = mPlayStateInfo.getCurrentIndex();
                if (mPlayStateInfo.getMediaList() != null)
                {
                    if (mPlayStateInfo.getMediaList().size() - mCurrentPlayIndex == GETGAP)
                    {
                        mLog.e(TAG, "notifyChange");
                        getContentResolver().notifyChange(Uri.parse(MEDIAPLAYERINDEX_URL), null);

                    }
                }
            }
        };

        if (mTimer != null && mTimerTask != null)
            mTimer.schedule(mTimerTask, DELAY, PERIOD);

    }

    private void stopTimer()
    {
        if (mTimer != null)
        {
            mTimer.cancel();
            mTimer = null;
        }

        if (mTimerTask != null)
        {
            mTimerTask.cancel();
            mTimerTask = null;
        }

    }
    
    
    private LocalDeviceInfo getDeviceInfo()
    {
        LocalDeviceInfo localDeviceInfo = new LocalDeviceInfo();
        Intent intent = getIntent();
        if (null != intent)
        {
            Bundle bundle = intent.getBundleExtra(LocalDeviceInfo.DEVICE_EXTRA_NAME);
            if (bundle != null)
            {
                localDeviceInfo.decompress(bundle);
            }
        }
        return localDeviceInfo;
    }

    private ListSelectPopup mListSelectPopup;

    private void showImageSelectDialog()
    {
        mIsplayBackgroundMusic = false;
//        playBackgroundMusic(null);
        if (mListSelectPopup == null)
        {
            mListSelectPopup = new ListSelectPopup(this, getDataList(mLocalDeviceInfo.getDeviceType(), mCurDevId), mLocalDeviceInfo.getPhysicId(), 0, R.style.DialogCustomizeStyle);
        }
        else
        {
            mListSelectPopup.initParam();
        }
//        mListSelectPopup.setFolderMediaType(MediaType.AUDIO);
        if (PlayStateInfo.getDevIdForSelectAud() == null || PlayStateInfo.getDevIdForSelectAud().equals(mCurDevId))
        {
            mListSelectPopup.setSelected(PlayStateInfo.getSelectedAudioIdxListForImagePlayer());
        }
        mListSelectPopup.setOnSelectPopupListener(this);
        mListSelectPopup.setLogoAndTip(R.drawable.audio_folder_empty, R.string.select_bg_audio, R.string.no_audio);
//        mListSelectPopup.show();
        mListSelectPopup.showDialog();
    }

    private List<ListSelectItem> getDataList(int deviceType, String deviceId)
    {
        List<ListSelectItem> miList = new ArrayList<ListSelectItem>();
        /*
        if (ConstData.DeviceType.isDLNADevice(deviceType))
        {
            int devId = -1;
            try{
                devId = Integer.valueOf(deviceId);
            }catch(Exception e)
            {                
            }
            if (devId == -1)
            {
                return miList;
            }
            //暂时注释，待DLNA调测时候打开
            List<DlnaBaseObjectInfo> tmpList = ObjectFactory.getMediaBrowserClient().getFlatFileFolder(
                    devId, MediaInfoConvertor.LocalType2DlnaType(ConstData.MediaType.AUDIO));
            List<LocalMediaInfo> mediaInfos = MediaInfoConvertor.DlnaBaseObjectInfoList2LocalMediaInfoList(tmpList);
            ListSelectItem item;
            if (mediaInfos != null && mediaInfos.size() != 0)
            {
                for (LocalMediaInfo info : mediaInfos)
                {
                    item = new ListSelectItem(info.getmFileName(), info.getmFiles() + getString(R.string.audio_unit), info);
                    miList.add(item);
                }
            }
            else
            {
                mLog.i(TAG, "no video data in it");
            }
            
        }
        else*/ if (deviceType == ConstData.DeviceType.DEVICE_TYPE_U || deviceType == ConstData.DeviceType.DEVICE_TYPE_SD)
        {
            mLog.i(TAG, "DEVICE_TYPE_U");
            List<LocalMediaInfo> mediaInfoList = mLocalDeviceManager.getFlatAVIFile(deviceId, MediaType.AUDIO, 0, 100, EBrowerType.ORDER_TYPE_CHARACTER);
            ListSelectItem item;
            if (mediaInfoList != null && mediaInfoList.size() != 0)
            {
                for (LocalMediaInfo info : mediaInfoList)
                {
                    item = new ListSelectItem(info.getmFileName(), info.getmFiles() + getString(R.string.audio_unit), info);
                    miList.add(item);
                }
            }
            else
            {
                mLog.i(TAG, "no video data in it");
            }
        }
        else
        {
            mLog.i(TAG, "unknown device type");
        }
        return miList;
    }

    /* BEGIN: Added by s00211113 for DTS2014031904523 2014/3/19 */
    @Override
    public void onListSelected(List<ListSelectItem> list, ArrayList<Integer> selectedIdxList)
    {
        List<LocalMediaInfo> mediaList = new ArrayList<LocalMediaInfo>();

        int deviceType = mLocalDeviceInfo.getDeviceType();
        /*
        if (ConstData.DeviceType.isDLNADevice(deviceType))
        {
            for (ListSelectItem mi : list)
            {
                LocalMediaInfo info = (LocalMediaInfo) mi.getObject();
                List<DlnaBaseObjectInfo> tmpList = ObjectFactory.getMediaBrowserClient().getMediaListByTypeInFolder(mLocalDeviceInfo.getmDeviceId(), info.getmObjectId(), 
                        MediaInfoConvertor.LocalType2DlnaType(ConstData.MediaType.AUDIO), EDlnaSortType.DLNA_SORT_TYPE_BY_DATE_DESC);               
                List<LocalMediaInfo> mediaInfos = MediaInfoConvertor.DlnaBaseObjectInfoList2LocalMediaInfoList(tmpList);
                mediaList.addAll(mediaInfos);
            }
        }
        else */if (deviceType == ConstData.DeviceType.DEVICE_TYPE_U || deviceType == ConstData.DeviceType.DEVICE_TYPE_SD)
        {
            Set<String> urls = new HashSet<String>();
            for (ListSelectItem mi : list)
            {
                LocalMediaInfo info = (LocalMediaInfo) mi.getObject();
                List<LocalMediaInfo> listflat = getAudiosByUrl(info.getmData());
                mediaList.addAll(listflat);
                urls.add(info.getmData());
            }
            
            saveBgAudioUrls(urls);
            saveBgAudioDeviceId(mCurDevId);
        }

        setPlayDefaultBgMusic(false);

        setBackGroupAudioList(mediaList);

        PlayStateInfo.setDevIdForSelectAud(mCurDevId);
        PlayStateInfo.setSelectedAudioIdxListForImagePlayer(selectedIdxList);
        mIsplayBackgroundMusic = true;
        playBackgroundMusic();

    }

    /* END: Added by s00211113 for DTS2014031904523 2014/3/19 */

    @Override
    public void onSelectPopupHide()
    {

    }

    /* BEGIN: Modified by s00211113 for DTS2014033000145 2014/3/31 */
    private static Boolean mIsPlayDefaultBgMusic = false;

    private static void setPlayDefaultBgMusic(Boolean isPlayDefaultBgMusic)
    {
        mIsPlayDefaultBgMusic = isPlayDefaultBgMusic;
    }

    private static Boolean getPlayDefaultBgMusic()
    {
        return mIsPlayDefaultBgMusic;
    }
    /* END: Modified by s00211113 for DTS2014033000145 2014/3/31 */
    
    protected List<LocalMediaInfo> getBgAudiosFromPreferences()
    {
        List<LocalMediaInfo> lists = new ArrayList<LocalMediaInfo>();
        Set<String> urls =  getBgAudioUrls();
        if (urls == null || urls.size() == 0)
        {
            return lists;
        }
        Iterator<String> iterator = urls.iterator();
        while (iterator.hasNext())
        {
            String url = iterator.next();
            if (isFileExist(url))
            {
                List<LocalMediaInfo> listflat = getAudiosByUrl(url);
                if (!isListEmpty(listflat))
                {
                    lists.addAll(listflat);
                }
            }
        }
        
        return lists;
    }
    
    private List<LocalMediaInfo> getAudiosByUrl(String url)
    {
        return mLocalDeviceManager.getFlatAVIFileSubWithType(url, MediaType.AUDIO, 0, 100, EBrowerType.ORDER_TYPE_CHARACTER);
    }
    
    private void loadAudioPreferences()
    {
        List<LocalMediaInfo> lists;
        String bgDevId;
        int deviceType = mLocalDeviceInfo.getDeviceType();
        if (deviceType == ConstData.DeviceType.DEVICE_TYPE_U || deviceType == ConstData.DeviceType.DEVICE_TYPE_SD)
        {
            lists = getBgAudiosFromPreferences();
            bgDevId = getBgAudioDeviceId();
            if (!isListEmpty(lists) && bgDevId != null && bgDevId.equals(mCurDevId))
            {
                if (isListEmpty(PlayStateInfo.getmBackGroupAudiolist()))
                {
                    PlayStateInfo.setDevIdForSelectAud(bgDevId);
                    PlayStateInfo.setmBackGroupAudiolist(lists);
                }
            }
        }
        
    }
}
