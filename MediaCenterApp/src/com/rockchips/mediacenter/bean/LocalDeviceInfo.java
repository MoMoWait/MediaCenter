package com.rockchips.mediacenter.bean;
import android.os.Bundle;

import com.rockchips.mediacenter.data.ConstData;

/**
 * Description: 和设备表对应的本地设备信息结构<br>
 * @author c00224451
 * @version v1.0 Date: 2014-7-1 下午2:07:16<br>
 */

public class LocalDeviceInfo
{
    // 设备类型（SD卡，USB设备）
    private int mDeviceType;

    public static final String DEVICE_TYPE = "devices_type";

    // 设备总存储空间
    private String mTotalSize = "";

    public static final String TOTAL_SIZE = "size";

    // 设备已使用的存储空间
    private String mUsedSize = "";

    public static final String USED_SIZE = "used";

    // 设备还有的存储空间
    private String mFreeSize = "";

    public static final String FREE_SIZE = "free";

    // 设备使用情况百分比
    private String mUsedPercent = "";

    public static final String USED_PERCENT = "used_percent";

    // 设备ID
    private String mPhysicId = "";

    public static final String PHYSIC_ID = "physic_dev_id";

    // 是否物理设备
    private int mIsPhysicDev;

    public static final String IS_PHYSIC_DEV = "is_physic_dev";

    // 设备挂载路径
    private String mMountPath = "";

    public static final String MOUNT_PATH = "mount_path";

    // 设备是否已经扫描
    private int mIsScanned;

    public static final String IS_SCANNED = "has_scaned";

    // 磁盘唯一标识
    private String mUUID = "";

    // 统计意义count,数据库不存在字段
    private int mDevCount;

    public static final String DEVICE_COUNT = "dev_count";

    // 统计意义count,数据库不存在字段
    private int mDeviceId;

    public static final String DEVICE_ID = "device_id";

    public static final String DEVICE_EXTRA_NAME = "DEVICE_EXTRA_NAME";

    /**
     * 将设备信息组装至Bundle结构
     * 
     */
    public Bundle compress()
    {
        Bundle bundle = new Bundle();

        bundle.putInt(DEVICE_TYPE, getDeviceType());
        bundle.putString(TOTAL_SIZE, getTotalSize());
        bundle.putString(USED_SIZE, getUsedSize());
        bundle.putString(FREE_SIZE, getFreeSize());
        bundle.putString(USED_PERCENT, getUsedPercent());
        bundle.putString(PHYSIC_ID, getPhysicId());
        bundle.putInt(IS_PHYSIC_DEV, getIsPhysicDev());
        bundle.putString(MOUNT_PATH, getMountPath());
        bundle.putInt(IS_SCANNED, getIsScanned());
        bundle.putInt(DEVICE_COUNT, getmDevCount());
        bundle.putInt(DEVICE_ID, getmDeviceId());

        return bundle;
    }
    
    public void decompress(Bundle bundle)
    {
        setDeviceType(bundle.getInt(DEVICE_TYPE, ConstData.DeviceType.DEVICE_TYPE_UNKNOWN));
        
        setTotalSize(bundle.getString(TOTAL_SIZE));
        setUsedSize(bundle.getString(USED_SIZE));
        setFreeSize(bundle.getString(FREE_SIZE));
        setUsedPercent(bundle.getString(USED_PERCENT));
        setPhysicId(bundle.getString(PHYSIC_ID));
        setIsPhysicDev(bundle.getInt(IS_PHYSIC_DEV, 0));
        setMountPath(bundle.getString(MOUNT_PATH));
        setIsScanned(bundle.getInt(IS_SCANNED, 0));
        setmDevCount(bundle.getInt(DEVICE_COUNT, 0));
        setmDeviceId(bundle.getInt(DEVICE_ID, 0));
    }

    /**
     * @return mDeviceType
     */
    public int getDeviceType()
    {
        return mDeviceType;
    }

    /**
     * @param mDeviceType 要设置的 mDeviceType
     */
    public void setDeviceType(int mDeviceType)
    {
        this.mDeviceType = mDeviceType;
    }

    /**
     * @return mTotalSize
     */
    public String getTotalSize()
    {
        return mTotalSize;
    }

    /**
     * @param mTotalSize 要设置的 mTotalSize
     */
    public void setTotalSize(String mTotalSize)
    {
        this.mTotalSize = mTotalSize;
    }

    /**
     * @return mUsedSize
     */
    public String getUsedSize()
    {
        return mUsedSize;
    }

    /**
     * @param mUsedSize 要设置的 mUsedSize
     */
    public void setUsedSize(String mUsedSize)
    {
        this.mUsedSize = mUsedSize;
    }

    /**
     * @return mFreeSize
     */
    public String getFreeSize()
    {
        return mFreeSize;
    }

    /**
     * @param mFreeSize 要设置的 mFreeSize
     */
    public void setFreeSize(String mFreeSize)
    {
        this.mFreeSize = mFreeSize;
    }

    /**
     * @return mUsedPercent
     */
    public String getUsedPercent()
    {
        return mUsedPercent;
    }

    /**
     * @param mUsedPercent 要设置的 mUsedPercent
     */
    public void setUsedPercent(String mUsedPercent)
    {
        this.mUsedPercent = mUsedPercent;
    }

    /**
     * @return mPhysicId
     */
    public String getPhysicId()
    {
        return mPhysicId;
    }

    /**
     * @param mPhysicId 要设置的 mPhysicId
     */
    public void setPhysicId(String mPhysicId)
    {
        this.mPhysicId = mPhysicId;
    }

    /**
     * @return mIsPhysicDev
     */
    public int getIsPhysicDev()
    {
        return mIsPhysicDev;
    }

    /**
     * @param mIsPhysicDev 要设置的 mIsPhysicDev
     */
    public void setIsPhysicDev(int mIsPhysicDev)
    {
        this.mIsPhysicDev = mIsPhysicDev;
    }

    /**
     * @return mMountPath
     */
    public String getMountPath()
    {
        return mMountPath;
    }

    /**
     * @param mMountPath 要设置的 mMountPath
     */
    public void setMountPath(String mMountPath)
    {
        this.mMountPath = mMountPath;
    }

    /**
     * @return mIsScanned
     */
    public int getIsScanned()
    {
        return mIsScanned;
    }

    /**
     * @param mIsScanned 要设置的 mIsScanned
     */
    public void setIsScanned(int mIsScanned)
    {
        this.mIsScanned = mIsScanned;
    }

    /**
     * @return mUUID
     */
    public String getmUUID()
    {
        return mUUID;
    }

    /**
     * @param mUUID 要设置的 mUUID
     */
    public void setmUUID(String mUUID)
    {
        this.mUUID = mUUID;
    }

    /**
     * @return mDevCount
     */
    public int getmDevCount()
    {
        return mDevCount;
    }

    /**
     * @param mDevCount 要设置的 mDevCount
     */
    public void setmDevCount(int mDevCount)
    {
        this.mDevCount = mDevCount;
    }

    /**
     * @return mDeviceId
     */
    public int getmDeviceId()
    {
        return mDeviceId;
    }

    /**
     * @param mDevCount 要设置的 mDevCount
     */
    public void setmDeviceId(int mDeviceId)
    {
        this.mDeviceId = mDeviceId;
    }
    
    @Override
    public int hashCode()
    {
        return super.hashCode();
    }
    
    @Override
    public boolean equals(Object o)
    {     
        if (o == null)
        {
            return false;
        }
        if (this == o)
        {
            return true;
        }
        else if (!(o instanceof LocalDeviceInfo))
        {
            return false;
        }
        LocalDeviceInfo deviceInfo = (LocalDeviceInfo)o;
        if (mDeviceType == deviceInfo.getDeviceType())
        {
            if (mDeviceType == ConstData.DeviceType.DEVICE_TYPE_DMS)
            {
                return mDeviceId == deviceInfo.getmDeviceId();
            }
            else
            {
                return mPhysicId.equals(deviceInfo.getPhysicId());
            }
        }
        else
        {
            return false;
        }        
    }




	@Override
	public String toString() {
		return "LocalDeviceInfo [mDeviceType=" + mDeviceType + ", mTotalSize="
			+ mTotalSize + ", mUsedSize=" + mUsedSize + ", mFreeSize="
			+ mFreeSize + ", mUsedPercent=" + mUsedPercent + ", mPhysicId="
			+ mPhysicId + ", mIsPhysicDev=" + mIsPhysicDev
			+ ", mMountPath=" + mMountPath + ", mIsScanned=" + mIsScanned
			+ ", mUUID=" + mUUID + ", mDevCount=" + mDevCount
			+ ", mDeviceId=" + mDeviceId + "]";
	}

	
    
}
