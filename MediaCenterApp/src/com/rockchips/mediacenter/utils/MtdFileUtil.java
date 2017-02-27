package com.rockchips.mediacenter.utils;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.List;

public class MtdFileUtil
{
    public static void combineFileParts(File destionation, List<File> parts)
    {
        try
        {

            destionation.delete();
            RandomAccessFile outfile = null;
            try
            {
                outfile = new RandomAccessFile(destionation, "rw");

                byte[] buffer = new byte[1024];
                for (File part : parts)
                {
                    RandomAccessFile infile = new RandomAccessFile(part, "r");
                    try
                    {
                        int bytesRead = infile.read(buffer);
                        while (bytesRead != -1)
                        {
                            outfile.write(buffer, 0, bytesRead);

                            bytesRead = infile.read(buffer);
                        }
                    }
                    finally
                    {
                        infile.close();
                    }
                }

            }
            finally
            {
                if (outfile != null)
                {
                    outfile.close();
                }
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public static void combineFilePartsAndDelete(File destionation, List<File> parts)
    {
        combineFileParts(destionation, parts);
        for (File file : parts)
        {
            file.delete();
        }
    }
}
