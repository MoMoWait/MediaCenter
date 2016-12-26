package com.rockchips.mediacenter.bean;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

/**
 * @author GaoFei
 * Upnp设备下文件描述
 */
@Table(name = "UpnpFile")
public class UpnpFile {
	@Column(isId = true, autoGen = true, name="fileId")
	private int fileId;
	@Column(name="itemId")
	private String itemId;
	@Column(name="name")
	private String name;
	@Column(name="path")
	private String path;
	@Column(name="type")
	private int type;
	@Column(name="last_modify_date")
	private long last_modify_date;
	@Column(name="size")
	private long size;
	@Column(name="devicetype")
	private int devicetype;
	@Column(name="physic_dev_id")
	private String physic_dev_id;
	@Column(name="deviceID")
	private String deviceID;
	@Column(name="parentId")
	private String parentId;
	/**
	 * 时长(仅针对audio,video)
	 */
	@Column(name="duration")
	private String duration;
	/**daojiu
	 * 比特率，针对audio，video
	 */
	@Column(name="bitrate")
	private long bitrate;
	/**
	 * 采样率，针对audio，video
	 */
	@Column(name="sampleFrequency")
	private long sampleFrequency;
	/**
	 * 采样位数，针对audio，video
	 */
	@Column(name="bitsPerSample")
	private long bitsPerSample;
	/**
	 * 声道数，针对audio，video
	 */
	@Column(name="nrAudioChannels")
	private long nrAudioChannels;
	/**
	 * 像素长度，针对video,image
	 */
	@Column(name="width")
	private int width;
	/**
	 * 像素宽度,针对video,imgae
	 */
	@Column(name="height")
	private int height;
	/**
	 * 音乐类别，针对audio,video
	 */
	@Column(name="genre")
	private String genre;
	/**
	 * 艺术家，针对audio,video
	 */
	@Column(name="artist")
	private String artist;
	/**
	 * 演唱家（演员）,针对audio,video
	 */
	@Column(name="actor")
	private String actor;
	/**
	 * 作者,针对audio,video
	 */
	@Column(name="author")
	private String author;
	/**
	 * 唱片集
	 */
	@Column(name="album")
	private String album;
	/**
	 * 专辑封面图
	 */
	@Column(name="albumArtURI")
	private String albumArtURI;
	/**
	 * 文件日期描述yyyy-mm-dd
	 */
	@Column(name="date")
	private String date;
	
	@Column(name="resolution")
	private String resolution;
	
	/**
	 * 预览图路径
	 */
	@Column(name="previewPhotoPath")
	private String previewPhotoPath;
	/**
	 * 是否加载预览图
	 */
	@Column(name="isLoadPreviewPhoto")
	private boolean isLoadPreviewPhoto;
	public int getFileId() {
		return fileId;
	}
	public void setFileId(int fileId) {
		this.fileId = fileId;
	}
	public String getItemId() {
		return itemId;
	}
	public void setItemId(String itemId) {
		this.itemId = itemId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public long getLast_modify_date() {
		return last_modify_date;
	}
	public void setLast_modify_date(long last_modify_date) {
		this.last_modify_date = last_modify_date;
	}
	public long getSize() {
		return size;
	}
	public void setSize(long size) {
		this.size = size;
	}
	public int getDevicetype() {
		return devicetype;
	}
	public void setDevicetype(int devicetype) {
		this.devicetype = devicetype;
	}
	public String getPhysic_dev_id() {
		return physic_dev_id;
	}
	public void setPhysic_dev_id(String physic_dev_id) {
		this.physic_dev_id = physic_dev_id;
	}
	public String getDeviceID() {
		return deviceID;
	}
	public void setDeviceID(String deviceID) {
		this.deviceID = deviceID;
	}
	public String getParentId() {
		return parentId;
	}
	public void setParentId(String parentId) {
		this.parentId = parentId;
	}
	public String getDuration() {
		return duration;
	}
	public void setDuration(String duration) {
		this.duration = duration;
	}
	public long getBitrate() {
		return bitrate;
	}
	public void setBitrate(long bitrate) {
		this.bitrate = bitrate;
	}
	public long getSampleFrequency() {
		return sampleFrequency;
	}
	public void setSampleFrequency(long sampleFrequency) {
		this.sampleFrequency = sampleFrequency;
	}
	public long getBitsPerSample() {
		return bitsPerSample;
	}
	public void setBitsPerSample(long bitsPerSample) {
		this.bitsPerSample = bitsPerSample;
	}
	public long getNrAudioChannels() {
		return nrAudioChannels;
	}
	public void setNrAudioChannels(long nrAudioChannels) {
		this.nrAudioChannels = nrAudioChannels;
	}
	public int getWidth() {
		return width;
	}
	public void setWidth(int width) {
		this.width = width;
	}
	public int getHeight() {
		return height;
	}
	public void setHeight(int height) {
		this.height = height;
	}
	public String getGenre() {
		return genre;
	}
	public void setGenre(String genre) {
		this.genre = genre;
	}
	public String getArtist() {
		return artist;
	}
	public void setArtist(String artist) {
		this.artist = artist;
	}
	public String getActor() {
		return actor;
	}
	public void setActor(String actor) {
		this.actor = actor;
	}
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	public String getAlbum() {
		return album;
	}
	public void setAlbum(String album) {
		this.album = album;
	}
	public String getAlbumArtURI() {
		return albumArtURI;
	}
	public void setAlbumArtURI(String albumArtURI) {
		this.albumArtURI = albumArtURI;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public String getResolution() {
		return resolution;
	}
	public void setResolution(String resolution) {
		this.resolution = resolution;
	}
	public String getPreviewPhotoPath() {
		return previewPhotoPath;
	}
	public void setPreviewPhotoPath(String previewPhotoPath) {
		this.previewPhotoPath = previewPhotoPath;
	}
	public boolean isLoadPreviewPhoto() {
		return isLoadPreviewPhoto;
	}
	public void setLoadPreviewPhoto(boolean isLoadPreviewPhoto) {
		this.isLoadPreviewPhoto = isLoadPreviewPhoto;
	}
	@Override
	public String toString() {
		return "UpnpFile [fileId=" + fileId + ", itemId=" + itemId + ", name="
				+ name + ", path=" + path + ", type=" + type
				+ ", last_modify_date=" + last_modify_date + ", size=" + size
				+ ", devicetype=" + devicetype + ", physic_dev_id="
				+ physic_dev_id + ", deviceID=" + deviceID + ", parentId="
				+ parentId + ", duration=" + duration + ", bitrate=" + bitrate
				+ ", sampleFrequency=" + sampleFrequency + ", bitsPerSample="
				+ bitsPerSample + ", nrAudioChannels=" + nrAudioChannels
				+ ", width=" + width + ", height=" + height + ", genre="
				+ genre + ", artist=" + artist + ", actor=" + actor
				+ ", author=" + author + ", album=" + album + ", albumArtURI="
				+ albumArtURI + ", date=" + date + ", resolution=" + resolution
				+ ", previewPhotoPath=" + previewPhotoPath
				+ ", isLoadPreviewPhoto=" + isLoadPreviewPhoto + "]";
	}
	
	
}
