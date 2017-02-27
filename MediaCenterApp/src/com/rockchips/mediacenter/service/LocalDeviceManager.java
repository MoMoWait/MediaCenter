/**
 * Title: LocalDevDiskManager.java<br>
 * Package: com.rockchips.android.mediacenter.api.localImp<br>
 * Description: TODO<br>
 * @author w00190739
 * @version v1.0<br>
 * Date: 2014-7-9下午5:00:38<br> 
 * Copyright © Huawei Technologies Co., Ltd. 2014. All rights reserved.
 */

package com.rockchips.mediacenter.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.rockchips.mediacenter.bean.LocalDeviceInfo;
import com.rockchips.mediacenter.bean.LocalMediaInfo;
import com.rockchips.mediacenter.data.ConstData;
import com.rockchips.mediacenter.data.ConstData.EBrowerType;
import com.rockchips.mediacenter.data.DeviceDataConst;
import com.rockchips.mediacenter.data.LocDevProvConst;
import com.rockchips.mediacenter.utils.IICLOG;
import com.rockchips.mediacenter.utils.StringUtils;
import com.rockchips.mediacenter.service.ILocalDataListner;
import com.rockchips.mediacenter.service.ILocalDeviceMgr;
import com.rockchips.mediacenter.modle.db.LocAVIProjectionProvider;
import com.rockchips.mediacenter.modle.db.LocDevProjectionProvider;
import com.rockchips.mediacenter.modle.db.LocalData;
import com.rockchips.mediacenter.modle.db.ProjectionProvider;

/**
 * Description: 本地设备管理接口实现类<br>
 * @author w00190739
 * @version v1.0 Date: 2014-7-9 下午5:00:38<br>
 */

public class LocalDeviceManager implements ILocalDeviceMgr
{

    private static final String TAG = "LocalDeviceManager";

    private IICLOG Log = IICLOG.getInstance();

    private static LocalDeviceManager mInstance;

    private Context mContext;

    private List<ILocalDataListner> mILocalDataListnerList;

    private static Object mLock = new Object();
    
    private List<String> mOffLineDevList = new ArrayList<String>();
    
    private void addOffLineDev(String mountPath)
    {
        Log.d(TAG, "addOffLineDev mountPath = " + mountPath);
        if (null == mountPath)
        {
            return;
        }
        for (String string : mOffLineDevList)
        {
            if (mountPath.equals(string))
            {
                Log.i(TAG, "addOffLineDev but mountPath has in list");
                return;
            }
        }
        mOffLineDevList.add(mountPath);
    }
    
    private void removeOffLineDev(String mountPath)
    {
        Log.d(TAG, "removeOffLineDev mountPath = " + mountPath);
        if (null == mountPath)
        {
            return;
        }
        for (String string : mOffLineDevList)
        {
            if (mountPath.equals(string))
            {
                mOffLineDevList.remove(string);
                Log.i(TAG, "removeOffLineDev remove mountPath from list");
                break;
            }
        }
    }
    
    private boolean isDevOffLine(String parentPath)
    {
        if (null == parentPath)
        {
            Log.d(TAG, "isDevOffLine false as null == parentPath");
            return false;
        }
        if ("".equals(parentPath.trim()))
        {
            if (!mOffLineDevList.isEmpty())
            {
                Log.d(TAG, "isDevOffLine parentPath = " + parentPath);
                Log.d(TAG, "isDevOffLine true as parentPath is empty but offlineList is not empty");
                return true;
            }
            else
            {
                return false;
            }
        }
        for (String string : mOffLineDevList)
        {
            if (parentPath.startsWith(string))
            {
                Log.d(TAG, "isDevOffLine parentPath = " + parentPath);
                Log.d(TAG, "isDevOffLine true");
                return true;
            }
        }
        return false;
    }

    /**
     * 构造方法，用于获取本地设备数据库操作的句柄
     */

    private LocalDeviceManager(Context context)
    {
        mContext = context;
        mILocalDataListnerList = Collections.synchronizedList(new ArrayList<ILocalDataListner>());
        registerLocalDataBroadcastReceiver();
    }

    /**
     * TODO 获取静态示例方法
     * @return LocalDevManager
     * @throws
     */

    public static LocalDeviceManager getInstance(Context context)
    {
        synchronized (mLock)
        {
            if (null == mInstance)
            {
                mInstance = new LocalDeviceManager(context);
            }
            return mInstance;
        }
    }

    @Override
    public synchronized void addILocalDataListner(ILocalDataListner localDataListner)
    {
        if (null == localDataListner)
        {
            return;
        }

        if (!mILocalDataListnerList.contains(localDataListner))
        {
            mILocalDataListnerList.add(localDataListner);
        }
    }

    @Override
    public synchronized void removeILocalDataListner(ILocalDataListner localDataListner)
    {
        if (null == localDataListner)
        {
            return;
        }

        if (mILocalDataListnerList.contains(localDataListner))
        {
            mILocalDataListnerList.remove(localDataListner);
        }
    }

    private Cursor getLocalDevDiskCursor(String devPhysicId)
    {
        Log.d(TAG, "getLocalDevDiskCursor    E");
        ProjectionProvider pp = new LocDevProjectionProvider();
        LocalData devDisk = new LocalData(pp);
        devDisk.setUri(LocDevProvConst.DISK_URI);
        String[] selectionArgs = new String[1];
        selectionArgs[0] = devPhysicId;
        String selection = String.valueOf(new StringBuffer().append(LocalDeviceInfo.PHYSIC_ID).append("=?"));
        Cursor cursor = mContext.getContentResolver().query(devDisk.getUri(), devDisk.getProjection(), selection, selectionArgs, null);

        if (cursor == null)
        {
            Log.e(TAG, "getLocalDevDiskCursor() cursor == null");
        }
        Log.d(TAG, "getLocalDevDiskCursor    X cursor = " + cursor);
        return cursor;
    }

    /**
     * TODO
     * @param: devPhysicId 目标查询的设备disk的物理分区id
     * @return
     * @throws
     * @see com.rockchips.android.mediacenter.api.ILocalDevDiskMgr#getLocalDevDiskInfoList()
     */
    @Override
    public List<LocalDeviceInfo> getLocalDevDiskInfoList(String devPhysicId)
    {
        List<LocalDeviceInfo> list = new ArrayList<LocalDeviceInfo>();
        if (StringUtils.isEmpty(devPhysicId))
        {
            Log.e(TAG, "devPhysicId == null, So return listSize == 0");
            return list;
        }
        Cursor cursor = getLocalDevDiskCursor(devPhysicId);

        if (cursor == null)
        {
            Log.d(TAG, "getLocalDevDiskInfoList() cursor == null, So return listSize == 0");
            return list;
        }
        // 解析数据
        ProjectionProvider ppLoc = new LocDevProjectionProvider();
        LocalData devDisk = null;
        // 遍历检索数据
        while (cursor.moveToNext())
        {
            if (isDevOffLine(devPhysicId))
            {
                list = new ArrayList<LocalDeviceInfo>();
                break;
            }
            // 存在的数据行数
            devDisk = new LocalData(ppLoc);
            devDisk.importRecord(cursor);
            list.add((LocalDeviceInfo) ppLoc.getLocalObj());
            if (isDevOffLine(devPhysicId))
            {
                list = new ArrayList<LocalDeviceInfo>();
                break;
            }
        }
        cursor.close();
        return list;
    }

    private Cursor getLocalDevCursor()
    {
    	//android.util.Log.i(TAG, android.util.Log.getStackTraceString(new Throwable()));
        Log.d(TAG, "getLocalDevCursor    E");
        ProjectionProvider pp = new LocDevProjectionProvider();
        LocalData device = new LocalData(pp);
        device.setUri(LocDevProvConst.DEVICES_URI);
        Cursor cursor = mContext.getContentResolver().query(device.getUri(), device.getProjection(), device.getWhere(null), null, null);

        if (cursor == null)
        {
            Log.e(TAG, "getLocalDevCursor() cursor == null");
        }
        Log.d(TAG, "getLocalDevCursor    X cursor = " + cursor);
        return cursor;
    }

    @Override
    public List<LocalDeviceInfo> getLocalDevInfoList()
    {
        Cursor cursor = getLocalDevCursor();

        List<LocalDeviceInfo> list = new ArrayList<LocalDeviceInfo>();
        if (cursor == null)
        {
            Log.d(TAG, "getLocalDevDiskInfoList() cursor == null, So return listSize == 0");
            return list;
        }
        // 解析数据
        ProjectionProvider ppLoc = new LocDevProjectionProvider();
        LocalData device = null;
        // 遍历检索数据
        while (cursor.moveToNext())
        {
            // 存在的数据行数
            device = new LocalData(ppLoc);
            device.importRecord(cursor);
            list.add((LocalDeviceInfo) ppLoc.getLocalObj());
			Log.i(TAG, "getLocalDevInfoList->LocalDeviceInfo:" + (LocalDeviceInfo) ppLoc.getLocalObj());
        }
		//Log.i(TAG, "getLocalDevInfoList->device:" + device);
        cursor.close();
        return list;
    }

    // ////////////////////////////////////////////////////////////////////////////////////
    // private Cursor getFlatComFileCursor(String parentPath, int mediaType)
    // {
    // Log.d(TAG, "getCurFolderCursor    E");
    // SelectionParam sp = new SelectionParam(LocDevProvConst.FOLDER_URI, null, parentPath, mediaType);
    // Cursor cursor = getAVICursorByUri(sp);
    // ？？？
    // // ProjectionProvider pp = new LocAVIProjectionProvider();
    // // LocalData folder = new LocalData(pp);
    // // folder.setUri();
    // // String selection = String.valueOf(new StringBuffer().append(parentPath).append(ConstData.SpecialCode.SELECTION_CONN_CODE).append(mediaType));
    // //
    // // Cursor cursor = mContext.getContentResolver().query(folder.getUri(), folder.getProjection(), selection, null, null);
    // if (cursor == null)
    // {
    // Log.e(TAG, "getCurFolderCursor() cursor == null");
    // }
    // Log.d(TAG, "getCurFolderCursor    X cursor = " + cursor);
    // return cursor;
    // }

    /**
     * TODO
     * @return
     * @throws
     * @see com.rockchips.android.mediacenter.api.ILocalFolderMgr#getFlatComFile()
     */
    @Override
    public List<LocalMediaInfo> getComFileListInDisk(String parentPath, int offset, int count, EBrowerType orderType)
    {
        List<LocalMediaInfo> list = new ArrayList<LocalMediaInfo>();

        if (StringUtils.isEmpty(parentPath))
        {
            Log.e(TAG, " parentPath is Empty!!!!!");
            return list;
        }

        SelectionParam sp = new SelectionParam(LocDevProvConst.FOLDER_URI, parentPath, null, ConstData.MediaType.FOLDER, offset, count);
        Cursor cursor = getAVICursorByUri(sp, orderType);
        if (null == cursor)
        {
            return list;
        }
        list = parseAVICursor(parentPath, cursor);
        cursor.close();

        return list;
    }

    // ******************************[ mediaInfo ]*****************************//
    class SelectionParam
    {
        public String pathSegment;

        public String devPhysicId;

        public String parentPath;

        public int mediaType;
        
        public int offset;
        
        public int count;

        public SelectionParam(String pathSegment, String devPhysicId, String parentPath, int mediaType, int offset, int count)
        {
            this.pathSegment = pathSegment;
            this.devPhysicId = devPhysicId;
            this.parentPath = parentPath;
            this.mediaType = mediaType;
            this.offset = offset;
            this.count = count;
        }
    }

    private Cursor getAVICursorByUri(SelectionParam selectionParam, EBrowerType orderType)
    {
        Log.d(TAG, "getAVICursorByUri    E");
        if (null == selectionParam)
        {
            Log.e(TAG, "selectionParam == null");
            return null;
        }

        ProjectionProvider pp = new LocAVIProjectionProvider();
        LocalData flatAVI = new LocalData(pp);
        flatAVI.setUri(selectionParam.pathSegment);
        String[] selectionArgs = new String[4];
        StringBuffer selectionBuf = new StringBuffer();
        if (null != selectionParam.devPhysicId)
        {
            selectionArgs[0] = selectionParam.devPhysicId;
            selectionBuf.append(LocalDeviceInfo.PHYSIC_ID).append("=?");
            // strBuf.append(selectionParam.devPhysicId).append(ConstData.SpecialCode.SELECTION_CONN_CODE);
        }
        else if (null != selectionParam.parentPath)
        {
            selectionArgs[0] = selectionParam.parentPath;
            selectionBuf.append(LocalMediaInfo.PARENT_PATH).append("=?");
            // strBuf.append(selectionParam.parentPath).append(ConstData.SpecialCode.SELECTION_CONN_CODE);
        }
        if (ConstData.MediaType.UNKNOWN_TYPE != selectionParam.mediaType)
        {
            selectionArgs[1] = String.valueOf(selectionParam.mediaType);
            selectionBuf.append(LocalMediaInfo.FILE_TYPE).append("=?");
            // strBuf.append(selectionParam.mediaType);
        }

        selectionArgs[2] = String.valueOf(selectionParam.count);
        selectionArgs[3] = String.valueOf(selectionParam.offset);        
        
        String sortOrder = null;
        if (null != orderType)
        {
            switch(orderType)
            {
                case ORDER_TYPE_TIME:
                    sortOrder = LocalMediaInfo.MODIFY_DATE;
                    break;
                case ORDER_TYPE_CHARACTER:
                    sortOrder = LocalMediaInfo.PIN_YIN;
                    break;
                default:
                    sortOrder = LocalMediaInfo.MODIFY_DATE;
                    break;
            }
        }
        String selection = String.valueOf(selectionBuf);
        Cursor cursor = mContext.getContentResolver().query(flatAVI.getUri(), flatAVI.getProjection(), selection, selectionArgs, sortOrder);
        if (cursor == null)
        {
            Log.e(TAG, "getAVICursorByUri() cursor == null");
        }
        Log.d(TAG, "getAVICursorByUri    X cursor = " + cursor);
        return cursor;
    }

    private List<LocalMediaInfo> parseAVICursor(String parentPath, Cursor cursor)
    {
        List<LocalMediaInfo> list = new ArrayList<LocalMediaInfo>();
        if (cursor == null)
        {
            Log.d(TAG, "parseAVICursor() cursor == null, So return listSize == 0");
            return list;
        }
        // 解析数据
        ProjectionProvider ppLoc = new LocAVIProjectionProvider();
        LocalData flatDir = null;
        // 遍历检索数据
        while (cursor.moveToNext())
        {
            if (isDevOffLine(parentPath))
            {
                return new ArrayList<LocalMediaInfo>();
            }
            // 存在的数据行数
            flatDir = new LocalData(ppLoc);
            flatDir.importRecord(cursor);
            list.add((LocalMediaInfo) ppLoc.getLocalObj());
            if (isDevOffLine(parentPath))
            {
                return new ArrayList<LocalMediaInfo>();
            }
        }
        return list;
    }

    /**
     * @return
     * @throws
     * @see com.rockchips.android.mediacenter.api.ILocalFolderMgr#getFlatAVIFile()
     */
    @Override
    public List<LocalMediaInfo> getFlatAVIFile(String devPhysicId, int mediaType, int offset, int count, EBrowerType orderType)
    {
        List<LocalMediaInfo> list = new ArrayList<LocalMediaInfo>();

        if (StringUtils.isEmpty(devPhysicId))
        {
            Log.e(TAG, "devPhysicId is Empty   !!!!!");
            return list;
        }

        SelectionParam sp = new SelectionParam(LocDevProvConst.GET_FLAT_DIR_URI, devPhysicId, null, mediaType, offset, count);
        Cursor cursor = getAVICursorByUri(sp, orderType);
        if (null == cursor)
        {
            return list;
        }
        list = parseAVICursor(devPhysicId, cursor);
        cursor.close();

        int folderType = ConstData.MediaType.FOLDER;
        switch (mediaType)
        {
            case ConstData.MediaType.IMAGE:
                folderType = ConstData.MediaType.IMAGEFOLDER;
                break;
            case ConstData.MediaType.AUDIO:
                folderType = ConstData.MediaType.AUDIOFOLDER;
                break;
            case ConstData.MediaType.VIDEO:
                folderType = ConstData.MediaType.VIDEOFOLDER;
                break;
            default:
                break;
        }
        for (LocalMediaInfo lm : list)
        {
            lm.setmFileType(folderType);
        }
        return list;
    }

    @Override
    public long getFlatAVIFileCount(String devPhysicId, int mediaType)
    {
        if (StringUtils.isEmpty(devPhysicId))
        {
            Log.e(TAG, "devPhysicId is Empty   !!!!!");
            return 0;
        }

        SelectionParam sp = new SelectionParam(LocDevProvConst.GET_FLAT_DIR_URI, devPhysicId, null, mediaType, 0, 0);
        Cursor cursor = getAVICursorByUri(sp, null);
        if (null == cursor)
        {
            return 0;
        }
        long count = cursor.getCount();
        cursor.close();
        return count;
    }

    @Override
    public List<LocalMediaInfo> getFlatAVIFileSubWithType(String parentPath, int mediaType, int offset, int count, EBrowerType orderType)
    {
        List<LocalMediaInfo> list = new ArrayList<LocalMediaInfo>();

        if (StringUtils.isEmpty(parentPath))
        {
            Log.e(TAG, "parentPath is Empty   !!!!!");
            return list;
        }

        SelectionParam sp = new SelectionParam(LocDevProvConst.GET_FLAT_DIR_FILE_URI, null, parentPath, mediaType, offset, count);
        Cursor cursor = getAVICursorByUri(sp, orderType);
        if (null == cursor)
        {
            return list;
        }
        list = parseAVICursor(parentPath, cursor);
        cursor.close();
        for (LocalMediaInfo lm : list)
        {
            lm.setmFileType(mediaType);
        }
        return list;
    }

    @Override
    public List<LocalMediaInfo> getComFileListByPath(String parentPath, int offset, int count, EBrowerType orderType)
    {
        List<LocalMediaInfo> list = new ArrayList<LocalMediaInfo>();

        if (StringUtils.isEmpty(parentPath))
        {
            Log.e(TAG, "parentPath is Empty   !!!!!");
            return list;
        }
        SelectionParam sp = new SelectionParam(LocDevProvConst.QUERY_DIR_FILE_URI, null, parentPath, ConstData.MediaType.UNKNOWN_TYPE, offset, count);
        Cursor cursor = getAVICursorByUri(sp, orderType);
        if (null == cursor)
        {
            return list;
        }
        list = parseAVICursor(parentPath, cursor);
        cursor.close();
        return list;
    }

    @Override
    public int getSubFilesNumByPath(String parentPath)
    {
        if (StringUtils.isEmpty(parentPath))
        {
            Log.e(TAG, "getSubFilesNum   parentPath is Empty   !!!!!");
            return 0;
        }
        SelectionParam sp = new SelectionParam(LocDevProvConst.DIR_PAGE_URI, null, parentPath, ConstData.MediaType.FOLDER, 0, 0);
        Cursor cursor = getAVICursorByUri(sp, null);
        if (null == cursor)
        {
            return 0;
        }
        int number = 0;
        if (cursor.moveToNext())
        {
            number = cursor.getInt(cursor.getColumnIndex(LocalMediaInfo.FILES));
            Log.d(TAG, "number = " + number);
        }
        //
        // /************ FOR Test ****************/
        // int numberWR = parseAVICursor(cursor).get(0).getmFiles();
        // Log.w(TAG, "numberWR = " + numberWR);
        // /************ FOR Test ****************/
        cursor.close();
        return number;

    }

    // @Override
    // public List<LocalMediaInfo> getDirSubFileInfoAsy(String parentPath, int mediaType)
    // {
    // List<LocalMediaInfo> list = new ArrayList<LocalMediaInfo>();
    //
    // if (StringUtils.isEmpty(parentPath))
    // {
    // Log.e(TAG, "parentPath is Empty   !!!!!");
    // return list;
    // }
    // SelectionParam sp = new SelectionParam(LocDevProvConst.DIR_INFO_URI, null, parentPath, mediaType);
    // Cursor cursor = getAVICursorByUri(sp);
    // list = parseAVICursor(cursor);
    // return list;
    // }

    @Override
    public void delSelectFileRecord(String fullPath)
    {
        Log.d(TAG, "delSelectFileRecord fullPath = " + fullPath);
        if (null == fullPath)
        {
            return;
        }
        Uri deleteUri = Uri.withAppendedPath(ConstData.URI.LOCAL_PROVIDER_URI, LocDevProvConst.FILE_DEL_URI);
        String[] selectionArgs =
        { fullPath };
        if (isDevOffLine(fullPath))
        {
            Log.d(TAG, "delSelectFileRecord dev offLine");
            return;
        }
        mContext.getContentResolver().delete(deleteUri, "", selectionArgs);
    }

    @Override
    public void delSelectFolderRecord(String fullPath)
    {
        Log.d(TAG, "delSelectFolderRecord fullPath = " + fullPath);
        if (null == fullPath)
        {
            return;
        }
        Uri deleteUri = Uri.withAppendedPath(ConstData.URI.LOCAL_PROVIDER_URI, LocDevProvConst.DIR_DEL_URI);
        String[] selectionArgs =
        { fullPath };
        if (isDevOffLine(fullPath))
        {
            Log.d(TAG, "delSelectFolderRecord dev offLine");
            return;
        }
        mContext.getContentResolver().delete(deleteUri, "true", selectionArgs);
    }

    private LocalDataBroadcastReceiver mLocalDataBroadcastReceiver;

    private synchronized void registerLocalDataBroadcastReceiver()
    {
        if (null == mLocalDataBroadcastReceiver)
        {
            mLocalDataBroadcastReceiver = new LocalDataBroadcastReceiver();
            IntentFilter mIntentFilter = new IntentFilter();
            mIntentFilter.addAction(DeviceDataConst.ACTION_ON_DMS_UP);
            mIntentFilter.addAction(DeviceDataConst.ACTION_ON_DMS_DOWN);
            mIntentFilter.addAction(DeviceDataConst.ACTION_ON_DMS_BROWSE_RESULT);

            mContext.registerReceiver(mLocalDataBroadcastReceiver, mIntentFilter);//, ConstData.BROADCAST_PERMISSION_MOUNT_UNMOUNT_FILESYSTEMS, null);
        }
    }

    private synchronized void unRegisterLocalDataBroadcastReceiver()
    {
        if (null == mLocalDataBroadcastReceiver)
        {
            return;
        }

        mContext.unregisterReceiver(mLocalDataBroadcastReceiver);
        mLocalDataBroadcastReceiver = null;
    }

    private class LocalDataBroadcastReceiver extends BroadcastReceiver
    {
        private static final int MSG_WHAT_MOUNT = 1;

        private static final int MSG_WHAT_UNMOUNT = 2;

        private static final int MSG_WHAT_MEDIACHANGE = 3;

        private static final long SEND_MSG_DELAY_MILLIS = 500L;

        private Handler mHandler = new Handler()
        {
            public void handleMessage(android.os.Message msg)
            {
                Log.d(TAG, "notifyLocalDataChanged");
                switch (msg.what)
                {
                    case MSG_WHAT_MOUNT:
                        removeOffLineDev((String) msg.obj);
                        synchronized (mILocalDataListnerList)
                        {
                            for (ILocalDataListner localDataListner : mILocalDataListnerList)
                            {
                                localDataListner.onDiskMount();
                            }
                        }
                        break;
                    case MSG_WHAT_UNMOUNT:
                        addOffLineDev((String) msg.obj);
                        synchronized (mILocalDataListnerList)
                        {
                            for (ILocalDataListner localDataListner : mILocalDataListnerList)
                            {
                                localDataListner.onDiskUnMount((String) msg.obj);
                            }
                        }
                        removeOffLineDev((String) msg.obj);
                        break;
                    case MSG_WHAT_MEDIACHANGE:
                        synchronized (mILocalDataListnerList)
                        {
                            for (ILocalDataListner localDataListner : mILocalDataListnerList)
                            {
                                localDataListner.onMediaChanged();
                            }
                        }
                        break;
                    default:
                        Log.d(TAG, "Unknown  MSG What !!!!! ");
                        break;
                }
            }
        };

        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            Log.d(TAG, "action = " + action);
            if (null == action)
            {
                Log.d(TAG, "action is Null   !!!!! ");
                return;
            }
            Message msg = mHandler.obtainMessage();
            if (DeviceDataConst.ACTION_ON_DMS_UP.equals(action))
            {
                msg.what = MSG_WHAT_MOUNT;
                String mountPath = null;
                Bundle bundle = intent.getExtras();
                if (null != bundle)
                {
                    mountPath = bundle.getString(DeviceDataConst.EXTRA_RESERVE);
                }
                Log.d(TAG, "mountPath =  " + mountPath);
                msg.obj = mountPath;
            }
            if (DeviceDataConst.ACTION_ON_DMS_DOWN.equals(action))
            {
                msg.what = MSG_WHAT_UNMOUNT;
                String unMountPath = null;
                Bundle bundle = intent.getExtras();
                if (null != bundle)
                {
                    unMountPath = bundle.getString(DeviceDataConst.EXTRA_RESERVE);
                }
                Log.d(TAG, "unMountPath =  " + unMountPath);
                msg.obj = unMountPath;
            }
            if (DeviceDataConst.ACTION_ON_DMS_BROWSE_RESULT.equals(action))
            {
                msg.what = MSG_WHAT_MEDIACHANGE;
            }
            mHandler.removeMessages(msg.what);
            mHandler.sendMessageDelayed(msg, SEND_MSG_DELAY_MILLIS);
        }
    }

    public void destoryInstance()
    {
        unRegisterLocalDataBroadcastReceiver();
        if (null != mILocalDataListnerList)
        {
            mILocalDataListnerList.clear();
            mILocalDataListnerList = null;
        }
        if (null != mContext)
        {
            mContext = null;
        }
    }

    @Override
    public List<LocalMediaInfo> getAllFlatAVIFolders(int type, int offset, int count)
    {
        List<LocalMediaInfo> list = new ArrayList<LocalMediaInfo>();

        SelectionParam sp = new SelectionParam(LocDevProvConst.GET_ALL_FLAT_DIR_URI, "all", null, type, offset, count);
        Cursor cursor = getAVICursorByUri(sp, null);
        if (null == cursor)
        {
            return list;
        }
        list = parseAVICursor("", cursor);
        cursor.close();
        return list;
    }

    @Override
    public boolean isExistMultiMedia(String mountPath, int type)
    {
        Log.d(TAG, "cc msg isExistMultiMedia() path = " + mountPath);
        if (null == mountPath)
        {
            return false;
        }

        Uri uri = Uri.withAppendedPath(ConstData.URI.LOCAL_PROVIDER_URI, LocDevProvConst.IS_EXIST_MEDIA_FILES_URI);
        String[] selectionArgs =
        { mountPath, String.valueOf(type) };
        Cursor cursor = mContext.getContentResolver().query(uri, null, null, selectionArgs, null);
        if (cursor == null)
        {
            return false;
        }
        else if (cursor.getCount() == 0)
        {
            cursor.close();
            return false;
        }

        cursor.close();
        return true;
    }

    @Override
    public List<LocalMediaInfo> searchMediaFiles(String keyWord)
    {
        return searchMediaFiles(null, keyWord);
    }

    @Override
    public List<LocalMediaInfo> searchMediaFiles(String physicId, String keyWord)
    {
        if (null == keyWord)
        {
            return null;
        }

        String[] selectionArgs;
        if (StringUtils.isEmpty(physicId))
        {
            selectionArgs = new String[]
            { keyWord };
        }
        else
        {
            selectionArgs = new String[]
            { physicId, keyWord };
        }

        List<LocalMediaInfo> list = new ArrayList<LocalMediaInfo>();
        Uri uri = Uri.withAppendedPath(ConstData.URI.LOCAL_PROVIDER_URI, LocDevProvConst.SEARCH_URI);
        Cursor cursor = mContext.getContentResolver().query(uri, null, null, selectionArgs, null);
        if (null == cursor)
        {
            return list;
        }
        if (StringUtils.isEmpty(physicId))
        {
            list = parseAVICursor("", cursor);
        }
        else
        {
            list = parseAVICursor(physicId, cursor);
        }
        cursor.close();
        return list;
    }

    @Override
    public void delSelectAllFileRecord(String parentPath)
    {
        Log.d(TAG, "delSelectAllFileRecord parentPath = " + parentPath);
        if (null == parentPath)
        {
            return;
        }
        Uri deleteUri = Uri.withAppendedPath(ConstData.URI.LOCAL_PROVIDER_URI, LocDevProvConst.DIR_DEL_URI);
        String[] selectionArgs =
        { parentPath };
        if (isDevOffLine(parentPath))
        {
            Log.d(TAG, "delSelectAllFileRecord dev offLine");
            return;
        }
        mContext.getContentResolver().delete(deleteUri, "false", selectionArgs);
    }

}
