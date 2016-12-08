/**
 * Title: ILocalDeviceMgr.java<br>
 * Package: com.rockchips.android.mediacenter.api<br>
 * Description: TODO<br>
 * @author w00190739
 * @version v1.0<br>
 * Date: 2014-7-9下午3:57:12<br> 
 * Copyright © Huawei Technologies Co., Ltd. 2014. All rights reserved.
 */

package com.rockchips.mediacenter.api;

import java.util.List;

import com.rockchips.mediacenter.basicutils.bean.LocalDeviceInfo;
import com.rockchips.mediacenter.basicutils.bean.LocalMediaInfo;
import com.rockchips.mediacenter.basicutils.constant.Constant.EBrowerType;

/**
 * Description:本地设备管理接口类，用于定义对本地设备数据库操作的范围 本地设备包括：内置SD卡，外挂的U盘、移动硬盘、插入的SD卡、USB设备等<br>
 * @author w00190739
 * @version v1.0 Date: 2014-7-9 下午3:57:12<br>
 */

public interface ILocalDeviceMgr
{
    /**
     * 设置本地文件管理的监听。
     * @param: localDataListner 监听对象
     * @return
     */
    void addILocalDataListner(ILocalDataListner localDataListner);

    /**
     * 移除本地文件管理的监听。
     * @param: localDataListner 监听对象
     * @return
     */
    void removeILocalDataListner(ILocalDataListner localDataListner);

    /**
     * 获取本地设备分区列表。
     * @param: devPhysicId 目标查询的设备disk的物理分区id
     * @return 本地存储的设备分区，以列表形式返回信息。
     */
    List<LocalDeviceInfo> getLocalDevDiskInfoList(String devPhysicId);

    /**
     * 获取本地设备列表。
     * @return 本地存储的设备分区，以列表形式返回信息。
     */
    List<LocalDeviceInfo> getLocalDevInfoList();

    /**
     * 获取指定挂载设备的所有子文件（文件夹/单个文件） 信息。
     * @param: mountPath: 挂载设置路径
     * @return 指定挂载设备的所有所有子文件 信息，以列表的形式返回。
     */
    List<LocalMediaInfo> getComFileListInDisk(String mountPath, int offset, int count, EBrowerType orderType);

    /**
     * 扁平化显示当前设备的所有音频、视频、图片的 文件夹列表。
     * @param: devPhysicId 目标查询的设备disk的物理分区id
     * @param: mediaType 目标媒体类型
     * @return: 当前设备的所有音频、视频、图片的 文件夹，以列表的形式返回。
     */
    List<LocalMediaInfo> getFlatAVIFile(String devPhysicId, int mediaType, int offset, int count, EBrowerType orderType);

    /**
     * 扁平化显示当前设备的所有音频、视频、图片的 文件夹个数。
     * @param: devPhysicId 目标查询的设备disk的物理分区id
     * @param: mediaType 目标媒体类型
     * @return: 当前设备的所有音频、视频、图片的 文件夹个数。
     */
    public long getFlatAVIFileCount(String devPhysicId, int mediaType);

    /**
     * 扁平化展示指定 目标文件夹下的、指定类型的 所有子文件。
     * @param: parentPath 目标文件夹路径
     * @param: mediaType 目标媒体类型
     * @return: 当前设备下目标类型媒体的子文件，以列表的形式返回。
     */
    List<LocalMediaInfo> getFlatAVIFileSubWithType(String parentPath, int mediaType, int offset, int count, EBrowerType orderType);

    /**
     * 获取指定文件夹下所有类型的媒体子文件（文件夹/文件）。
     * @param: parentPath 目标文件夹路径
     * @return: 指定文件夹下所有类型 媒体的文件，以列表的形式返回。
     */
    List<LocalMediaInfo> getComFileListByPath(String parentPath, int offset, int count, EBrowerType orderType);

    /**
     * 获取指定文件夹下的所有文件的个数（文件夹/文件）
     * @param: parentPath 目标文件夹路径
     * @return: 当前目录下所有子文件的数目
     */
    public int getSubFilesNumByPath(String parentPath);

    /**
     * 删除选中的文件
     * @param: fullPath: 删除目标文件全路径
     * @return 成功或是失败。
     */
    void delSelectFileRecord(String fullPath);
    /**
     * 删除选中文件夹下的所有文件
     * @param: parentPath: 删除该目标文件全路径
     * @return 成功或是失败。
     */
    void delSelectAllFileRecord(String parentPath);

    /**
     * 删除选中的文件夹。
     * @param: fullPath: 删除目标文件全路径
     * @return 成功或是失败。
     */
    void delSelectFolderRecord(String fullPath);

    /**
     * 获取当前挂载的所有磁盘下的目录（扁平化）
     * @param: type: 文件类型
     * @return 媒体列表。
     */
    List<LocalMediaInfo> getAllFlatAVIFolders(int type, int offset, int count);

    /**
     * 获取指定磁盘是否有给定类型的媒体文件
     * @param: type: 文件类型 mountPath: 磁盘挂载路径
     * @return 存在与否。
     */
    public boolean isExistMultiMedia(String mountPath, int type);

    /**
     * 查询所有磁盘，匹配给出的关键字
     * @param: keyWord: 匹配的关键字
     * 
     * @return 媒体列表。
     */
    List<LocalMediaInfo> searchMediaFiles(String keyWord);

    /**
     * 查询所有磁盘，匹配给出的关键字
     * @param: physicId:查询指定设备 keyWord: 匹配的关键字
     * 
     * @return 媒体列表。
     */
    List<LocalMediaInfo> searchMediaFiles(String physicId, String keyWord);
}
