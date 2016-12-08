package com.rockchips.mediacenter.retrieve;
public final class EncodeUtil
{
    public static String getRightEncodeString(String str) 
    {   
        if (null == str)
        {
            return null;
        }
                
        String newEncodeStr = null;
        String encode = "GB2312";   
        byte[] aLRC = str.getBytes();
        
        try 
        {      
            newEncodeStr = new String(str.getBytes(encode), encode);
            if (str.equals(newEncodeStr)) 
            {                          
                return new String(str.getBytes(encode), "GBK");  
            }   
        } 
        catch (Exception e) 
        {      
        }   
        
        encode = "ISO-8859-1"; 
        try
        {  
            newEncodeStr = new String(str.getBytes(encode), encode);
            if (str.equals(newEncodeStr))
            {                      
                return new String(str.getBytes(encode), "GBK");     
            } 
        } 
        catch (Exception e)
        {      
        }  
        
        encode = "UTF-8";      
        try 
        {    
            newEncodeStr = new String(str.getBytes(encode), encode);
            if (str.equals(newEncodeStr))
            {                        
                return new String(aLRC, encode);      
            }      
        } 
        catch (Exception e)
        {      
        }      
        encode = "GBK";      
        try
        { 
            newEncodeStr = new String(str.getBytes(encode), encode);
            if (str.equals(newEncodeStr))
            {                      
                return new String(aLRC, encode);     
            }
        } 
        catch (Exception e) 
        {      
        }           

        return newEncodeStr;   
    }   
}
