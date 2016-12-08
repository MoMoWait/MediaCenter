/**
 * 
 * com.rockchips.iptv.stb.dlna.util
 * CharsetDetector.java
 * 
 * 2012-4-11-����2:59:08
 * Copyright 2012 Huawei Technologies Co., Ltd
 * 
 */
package com.rockchips.mediacenter.basicutils.util;

import org.mozilla.intl.chardet.nsDetector;
import org.mozilla.intl.chardet.nsICharsetDetectionObserver;
import org.mozilla.intl.chardet.nsPSMDetector;

import android.util.Log;

/**
 * 
 * CharsetDetector
 * 
 * @version 1.0.0
 * 
 */
public class CharsetDetector
{
    private static final String LOGTAG = "MusicPlayerActivity";
    
    private boolean found;
    private String result;
    
    public String detectCharset(byte[] bytes){
        
        nsDetector det = new nsDetector(nsPSMDetector.ALL);
        
        // Set an observer...
        // The Notify() will be called when a matching charset is found.
        det.Init(new nsICharsetDetectionObserver() {
            public void Notify(String charset) {
                found = true;
                Log.i(LOGTAG, "charset----------------------"+charset);
                result = charset;
            }
        });
        
        boolean isAscii = true;
        if (isAscii)
            isAscii = det.isAscii(bytes, bytes.length);
        // DoIt if non-ascii and not done yet.
        if (!isAscii) {
            det.DoIt(bytes, bytes.length, false);
        }
        
        det.DataEnd();
        
        if (isAscii) {
            found = true;
            result = "ASCII";
        } else if (!found) {
            Log.i(LOGTAG, "Default encoding-----------------------------UTF-8");
            result = "UTF-8";
        }
        
        if(result.equals("windows-1252")){
            result = "UTF-16";
        }
        
        if(result.equals("UTF-16LE")){
            result = "UTF-8";
        }
        
        //Charset.availableCharsets()
        
        Log.i(LOGTAG, "Final encoding-----------------------------"+result);
        return result;
    }
    
}
