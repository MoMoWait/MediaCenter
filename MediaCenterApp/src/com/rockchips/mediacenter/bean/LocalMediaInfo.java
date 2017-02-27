package com.rockchips.mediacenter.bean;

import java.io.File;

import android.os.Bundle;

import com.rockchips.mediacenter.data.ConstData;;

/**
 * Description: 数据库对应的查询数据信息<br>
 * @author c00224451
 * @version v1.0 Date: 2014-7-7 上午10:50:08<br>
 */

public class LocalMediaInfo
{
    // 媒体文件名
    private String mFileName = "";
    public static final String FILE_NAME = "name";

    // 媒体文件父目录路径
    private String mParentPath;

    public static final String PARENT_PATH = "path";

    // 媒体文件子文件夹数
    private int mFolders;

    public static final String FOLDERS = "folders";

    // 媒体文件媒体文件数目
    private int mFiles;

    public static final String FILES = "files";

    // 媒体文件类型
    private int mFileType;

    public static final String FILE_TYPE = "type";

    // 媒体文件大小
    private long mFileSize;

    public static final String FILE_SIZE = "size";

    // 媒体文件排序依据，数据库不存在字段
    private int mOrderField;

    public static final String ORDER_FIELD = "order_field";

    // 媒体文件最后修改时间
    private int mModifyDate;

    public static final String MODIFY_DATE = "last_modify_date";

    // 媒体文件最后修改时间(字串)
    private String mModifyDateStr;

    public static final String MODIFY_DATE_STR = "last_modify_date_str";

    // 媒体文件名称对应的拼音
    private String mPinyin;

    public static final String PIN_YIN = "pinyin";

    // 统计字段，数据库不存在字段
    private String mData;

    public static final String DATA = "data";

    private String mThumbNail;

    public static final String THUMBNAIL = "thumbNail";

    // 设备类型（SD卡，USB设备）
    private int mDeviceType;

    public static final String DEVICETYPE = "devicetype";

    private String mAlbum;

    public static final String ALBUM = "album";

    private String mArtist;

    public static final String ARTIST = "artist";

    private String mTitle;

    public static final String TITLE = "title";
    
    private String mDuration;
    
    public static final String DURATION = "duration";

    // 设备ID
    private String mPhysicId;

    public static final String PHYSIC_ID = "physic_dev_id";

    private String mMimeType;

    public static final String MIMETYPE = "mimetype";

    private String mResUri;

    public static final String RES_URI = "res_uri";

    private String mObjectId;

    public static final String OBJECT_ID = "object_id";
    
    private String mResoulution;

    public static final String RESOULUTION = "resoulution";
	
	private String mDescription;
    
    public static final String DESCRIPTION = "description";

    private String firstPhotoUrl;
    
    private String mUrl;
    
    public Bundle compress()
    {
        Bundle bundle = new Bundle();

        bundle.putString(FILE_NAME, getmFileName());
        bundle.putString(PARENT_PATH, getmParentPath());
        bundle.putInt(FOLDERS, getmFolders());
        bundle.putInt(FILES, getmFiles());
        bundle.putInt(FILE_TYPE, getmFileType());
        bundle.putLong(FILE_SIZE, getmFileSize());
        bundle.putInt(ORDER_FIELD, getmOrderField());
        bundle.putInt(MODIFY_DATE, getmModifyDate());
        bundle.putString(MODIFY_DATE_STR, getmModifyDateStr());
        bundle.putString(PIN_YIN, getmPinyin());
        bundle.putString(DATA, getmData());
        bundle.putString(THUMBNAIL, getmThumbNail());
        bundle.putInt(DEVICETYPE, getmDeviceType());
        bundle.putString(ALBUM, getmAlbum());
        bundle.putString(ARTIST, getmArtist());
        bundle.putString(TITLE, getmTitle());
        bundle.putString(PHYSIC_ID, getmPhysicId());
        bundle.putString(MIMETYPE, getmMimeType());
        bundle.putString(RES_URI, getmResUri());
        bundle.putString(OBJECT_ID, getmObjectId());
        bundle.putString(RESOULUTION, getmResoulution());
        bundle.putString(DURATION, getmDuration());
		bundle.putString(DESCRIPTION, getmDescription());

        return bundle;
    }

    public void decompress(Bundle bundle)
    {
        setmFileName(bundle.getString(FILE_NAME));
        setmParentPath(bundle.getString(PARENT_PATH));
        setmFolders(bundle.getInt(FOLDERS, 0));
        setmFiles(bundle.getInt(FILES, 0));
        setmFileType(bundle.getInt(FILE_TYPE, ConstData.MediaType.UNKNOWN_TYPE));
        setmFileSize(bundle.getLong(FILE_SIZE, 0));
        setmOrderField(bundle.getInt(ORDER_FIELD, 0));
        setmModifyDate(bundle.getInt(MODIFY_DATE, 0));
        setmModifyDateStr(bundle.getString(MODIFY_DATE_STR));
        setmPinyin(bundle.getString(PIN_YIN));
        setmData(bundle.getString(DATA));
        setmThumbNail(bundle.getString(THUMBNAIL));
        setmDeviceType(bundle.getInt(DEVICETYPE, ConstData.DeviceType.DEVICE_TYPE_UNKNOWN));
        setmAlbum(bundle.getString(ALBUM));
        setmArtist(bundle.getString(ARTIST));
        setmTitle(bundle.getString(TITLE));
        setmPhysicId(bundle.getString(PHYSIC_ID));
        setmMimeType(bundle.getString(MIMETYPE));
        setmResUri(bundle.getString(RES_URI));
        setmObjectId(bundle.getString(OBJECT_ID));
        setmResoulution(bundle.getString(RESOULUTION));
        setmDuration(bundle.getString(DURATION));
		setmDescription(bundle.getString(DESCRIPTION));
    }

    /**
     * @return mFileName
     */

    public String getmFileName()
    {
        return mFileName;
    }

    /**
     * @param mFileName 要设置的 mFileName
     */

    public void setmFileName(String mFileName)
    {
        this.mFileName = mFileName;
    }

    /**
     * @return mParentPath
     */

    public String getmParentPath()
    {
        return mParentPath;
    }

    /**
     * @param mParentPath 要设置的 mParentPath
     */

    public void setmParentPath(String mParentPath)
    {
        this.mParentPath = mParentPath;
    }

    /**
     * @return mFolders
     */

    public int getmFolders()
    {
        return mFolders;
    }

    /**
     * @param mFolders 要设置的 mFolders
     */

    public void setmFolders(int mFolders)
    {
        this.mFolders = mFolders;
    }

    /**
     * @return mFiles
     */

    public int getmFiles()
    {
        return mFiles;
    }

    /**
     * @param mFiles 要设置的 mFiles
     */

    public void setmFiles(int mFiles)
    {
        this.mFiles = mFiles;
    }

    /**
     * @return mFileType
     */

    public int getmFileType()
    {
        return mFileType;
    }

    /**
     * @param mFileType 要设置的 mFileType
     */

    public void setmFileType(int mFileType)
    {
        this.mFileType = mFileType;
    }

    /**
     * @return mFileSize
     */

    public long getmFileSize()
    {
        return mFileSize;
    }

    /**
     * @param mFileSize 要设置的 mFileSize
     */

    public void setmFileSize(long mFileSize)
    {
        this.mFileSize = mFileSize;
    }

    /**
     * @return mOrderField
     */

    public int getmOrderField()
    {
        return mOrderField;
    }

    /**
     * @param mOrderField 要设置的 mOrderField
     */

    public void setmOrderField(int mOrderField)
    {
        this.mOrderField = mOrderField;
    }

    /**
     * @return mModifyDate
     */

    public int getmModifyDate()
    {
        return mModifyDate;
    }

    /**
     * @param mModifyDate 要设置的 mModifyDate
     */

    public void setmModifyDate(int mModifyDate)
    {
        this.mModifyDate = mModifyDate;
    }

    /**
     * @return mModifyDate
     */

    public String getmModifyDateStr()
    {
        return mModifyDateStr;
    }

    /**
     * @param mModifyDate 要设置的 mModifyDate
     */

    public void setmModifyDateStr(String mModifyDateStr)
    {
        this.mModifyDateStr = mModifyDateStr;
    }

    /**
     * @return mPinyin
     */

    public String getmPinyin()
    {
        return mPinyin;
    }

    /**
     * @param mPinyin 要设置的 mPinyin
     */

    public void setmPinyin(String mPinyin)
    {
        this.mPinyin = mPinyin;
    }

    /**
     * @return mData
     */

    public String getmData()
    {
        if (mParentPath != null && mFileName != null)
        {
            return getmParentPath() + File.separator + getmFileName();
        }
        return mData;
    }

    /**
     * @param mData 要设置的 mData
     */

    public void setmData(String mData)
    {
        this.mData = mData;
    }

    public static final String MEDIA_TYPE_EXTRA = "MEDIA_TYPE_EXTRA";

    private int mDeleteModeState;

    public int getDeleteModeState()
    {
        return mDeleteModeState;
    }

    public void setDeleteModeState(int state)
    {
        this.mDeleteModeState = state;
    }

    public String getmThumbNail()
    {
        return mThumbNail;
    }

    public void setmThumbNail(String thumbNail)
    {
        this.mThumbNail = thumbNail;
    }

    public String getmAlbum()
    {
        return mAlbum;
    }

    public void setmAlbum(String album)
    {
        this.mAlbum = album;
    }

    public String getmArtist()
    {
        return mArtist;
    }

    public void setmArtist(String artist)
    {
        this.mArtist = artist;
    }

    public String getmTitle()
    {
        return mTitle;
    }

    public void setmTitle(String title)
    {
        this.mTitle = title;
    }

    public int getmDeviceType()
    {
        return mDeviceType;
    }

    public void setmDeviceType(int mDeviceType)
    {
        this.mDeviceType = mDeviceType;
    }

    public String getmPhysicId()
    {
        return mPhysicId;
    }

    public void setmPhysicId(String mPhysicId)
    {
        this.mPhysicId = mPhysicId;
    }

    public String getmMimeType()
    {
        return mMimeType;
    }

    public void setmMimeType(String mMimeType)
    {
        this.mMimeType = mMimeType;
    }

    public String getmResUri()
    {
        return mResUri;
    }

    public void setmResUri(String mResUri)
    {
        this.mResUri = mResUri;
    }

    public String getmObjectId()
    {
        return mObjectId;
    }

    public void setmObjectId(String mObjectId)
    {
        this.mObjectId = mObjectId;
    }


    public String getmResoulution()
    {
        return mResoulution;
    }

    public void setmResoulution(String sResoulution)
    {
        this.mResoulution = sResoulution;
    }
    
    public String getUrl()
    {
        switch (mDeviceType)
        {
            case ConstData.DeviceType.DEVICE_TYPE_DMS:
                return mResUri;
            case ConstData.DeviceType.DEVICE_TYPE_U:
            case ConstData.DeviceType.DEVICE_TYPE_SD:
            case ConstData.DeviceType.DEVICE_TYPE_NFS:
            case ConstData.DeviceType.DEVICE_TYPE_SMB:
            	return getmData();
            case ConstData.DeviceType.DEVICE_TYPE_OTHER:
            	return mUrl;
            default:
                return getmData();
        }
    }
    
    
    public void setUrl(String url){
    	mUrl = url;
    }
    
    public String getmDuration()
    {
        return mDuration;
    }

    public void setmDuration(String mDuration)
    {
        this.mDuration = mDuration;
    }
	
	public String getmDescription() {
		return mDescription;
	}
    
    public void setmDescription(String mDescription) {
    	this.mDescription = mDescription;
	}

    
    public String getFirstPhotoUrl() {
		return firstPhotoUrl;
	}
    
    public void setFirstPhotoUrl(String firstPhotoUrl) {
		this.firstPhotoUrl = firstPhotoUrl;
	}
    
	@Override
	public String toString() {
		return "LocalMediaInfo [mFileName=" + mFileName + ", mParentPath="
				+ mParentPath + ", mFolders=" + mFolders + ", mFiles=" + mFiles
				+ ", mFileType=" + mFileType + ", mFileSize=" + mFileSize
				+ ", mOrderField=" + mOrderField + ", mModifyDate="
				+ mModifyDate + ", mModifyDateStr=" + mModifyDateStr
				+ ", mPinyin=" + mPinyin + ", mData=" + mData + ", mThumbNail="
				+ mThumbNail + ", mDeviceType=" + mDeviceType + ", mAlbum="
				+ mAlbum + ", mArtist=" + mArtist + ", mTitle=" + mTitle
				+ ", mDuration=" + mDuration + ", mPhysicId=" + mPhysicId
				+ ", mMimeType=" + mMimeType + ", mResUri=" + mResUri
				+ ", mObjectId=" + mObjectId + ", mResoulution=" + mResoulution
				+ ", mDescription=" + mDescription + ", mDeleteModeState="
				+ mDeleteModeState + "]";
	}

    @Override
    public boolean equals(Object o) {
    	
    	if(o == null)
    		return false;
    	if(!(o instanceof LocalMediaInfo))
    		return false;
    	
    	LocalMediaInfo otherInfo = (LocalMediaInfo)o;
    	if(this.mFileName.equals(otherInfo.getmFileName()) && this.mParentPath.equals(otherInfo.getmParentPath()))
    		return true;
    	return false;
    }
    
}