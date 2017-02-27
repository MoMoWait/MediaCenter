package com.rockchips.mediacenter.utils;

public class TagExifInfo
{

    public TagExifInfo()
    {
        // TODO Auto-generated constructor stub
    }

    long dwExifType; // 取值为 ET_NOT_CLOSE_FILE|ET_MALLOC_THUMBNAIL, ....

    long dwExifType2;

    String Version; // EXIF 信息版本

    String CameraMake; // DC 制造商

    String CameraModel; // DC 型号

    String DateTime; // JPG 文件日期

    String DateTimeDigitized; // JPG 文件被其它软件修改日期

    int Height, Width; // 图像高度、宽度

    public int Orientation; // 拍摄方向，例如相机向左手方向旋转后拍摄的

    int IsColor; //

    int Process; // 被处理

    String FlashUsed; // 是否使用闪光灯

    float FocalLength; // 焦距

    float ExposureTime; // 曝光时间(快门速度)

    float ApertureFNumber; // 光圈数

    float Distance; // 拍摄物体距离

    float CCDWidth; // CCD 大小

    float ExposureBias; // 曝光补偿

    int Whitebalance; // 白平衡

    int MeteringMode; // 测光模式

    int ExposureProgram; // 曝光

    int ISOequivalent; // ISO

    int CompressionLevel; // 压缩

    float FocalplaneXRes; // 焦平面X轴分辨率

    float FocalplaneYRes; // 焦平面Y轴分辨率

    float FocalplaneUnits; // 焦平面分辨率单位

    float Xresolution; // X 轴分辨率

    float Yresolution; // Y 轴分辨率

    float ResolutionUnit; // 分辨率单位

    float Brightness; // 亮度

    long ThumbnailPointer; // 缩略图数据。

    // if(dwExifType&ET_MALLOC_THUMBNAIL == TRUE) 这个数值保存了缩略图的数据

    // 否则为一个 long(需要强制转换) 表示缩略图在JPG文件中的偏移值(相对于文件起始0处)

    long ThumbnailSize; // 缩略图的大小(字节流 ThumbnailPointer 的长度)

    // 如果<=0表示该 JPG 文件没有缩略图
    public String toString()
    {
        String result = new String();
        result += "dwExifType:  " + dwExifType + "\n";
        result += "dwExifType2:  " + dwExifType2 + "\n";
        result += "Version:  " + Version + "\n";
        result += "CameraMake:  " + CameraMake + "\n";
        result += "CameraModel " + CameraModel + "\n";
        result += "DateTime " + DateTime + "\n";
        result += "DateTimeDigitized " + DateTimeDigitized + "\n";
        result += "Height " + Height + "\n";
        result += "Width " + Width + "\n";
        result += "IsColor " + IsColor + "\n";
        result += "Process " + Process + "\n";
        result += "FlashUsed " + FlashUsed + "\n";
        result += "FocalLength " + FocalLength + "\n";
        result += "ExposureTime " + ExposureTime + "\n";
        result += "ApertureFNumber " + ApertureFNumber + "\n";
        result += "Distance " + Distance + "\n";
        result += "CCDWidth " + CCDWidth + "\n";
        result += "ExposureBias " + ExposureBias + "\n";
        result += "Whitebalance " + Whitebalance + "\n";
        result += "MeteringMode " + MeteringMode + "\n";
        result += "ExposureProgram " + ExposureProgram + "\n";
        result += "ISOequivalent " + ISOequivalent + "\n";
        result += "CompressionLevel " + CompressionLevel + "\n";
        result += "FocalplaneXRes " + FocalplaneXRes + "\n";
        result += "FocalplaneYRes " + FocalplaneYRes + "\n";
        result += "FocalplaneUnits " + FocalplaneUnits + "\n";
        result += "Xresolution " + Xresolution + "\n";
        result += "Yresolution " + Yresolution + "\n";
        result += "ResolutionUnit " + ResolutionUnit + "\n";
        result += "Brightness " + Brightness + "\n";
        result += "ThumbnailPointer " + ThumbnailPointer + "\n";
        result += "ThumbnailSize " + ThumbnailSize + "\n";
        return result;
    }
}
