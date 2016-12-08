package com.rockchips.mediacenter.imageplayer.downloader;

public interface DownloadProgressListener {
	public void onDownloadSize(int size);
	public void onDownloadSuccess();
	public void onDownloadError();
}
