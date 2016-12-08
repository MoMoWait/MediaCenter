package com.rockchips.mediacenter.portable.bean;

public class AudioInfoOfVideo
{
    private int audioformat;

    private int channelNum;

    private String lauguage;

    private int sampleRate;

    public AudioInfoOfVideo()
    {

    }

    public void finalize() throws Throwable
    {

    }

    /**
     * ��Ƶ��ʽ����Ӧһ���̶�������
     */
    public int getaudioformat()
    {
        return audioformat;
    }

    /**
     * ����Ƶ��
     */
    public int getchannelNum()
    {
        return channelNum;
    }

    public String getlauguage()
    {
        return lauguage;
    }

    /**
     * ������
     */
    public int getsampleRate()
    {
        return sampleRate;
    }

    /**
     * ��Ƶ��ʽ����Ӧһ���̶�������
     * 
     * @param newVal
     */
    public void setaudioformat(int newVal)
    {
        audioformat = newVal;
    }

    /**
     * ����Ƶ��
     * 
     * @param newVal
     */
    public void setchannelNum(int newVal)
    {
        channelNum = newVal;
    }

    /**
     * 
     * @param newVal
     */
    public void setlauguage(String newVal)
    {
        lauguage = newVal;
    }

    /**
     * ������
     * 
     * @param newVal
     */
    public void setsampleRate(int newVal)
    {
        sampleRate = newVal;
    }

}