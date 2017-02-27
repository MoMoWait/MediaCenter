package com.rockchips.mediacenter.utils;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

import android.util.Log;

public class JpegHeaders
{
    // markers
    public static final int M_SOF0 = 0xC0; // Start Of Frame N

    public static final int M_SOF1 = 0xC1; // N indicates which compression
                                           // process

    public static final int M_SOF2 = 0xC2; // Only SOF0-SOF2 are now in common
                                           // use

    public static final int M_SOF3 = 0xC3; //

    public static final int M_DHT = 0xC4; // Huffman table

    public static final int M_SOF5 = 0xC5; // NB: codes C4 and CC are NOT SOF
                                           // markers

    public static final int M_SOF6 = 0xC6;

    public static final int M_SOF7 = 0xC7;

    public static final int M_SOF9 = 0xC9;

    public static final int M_SOF10 = 0xCA;

    public static final int M_SOF11 = 0xCB;

    public static final int M_SOF13 = 0xCD;

    public static final int M_SOF14 = 0xCE;

    public static final int M_SOF15 = 0xCF;

    public static final int M_SOI = 0xD8; // Start of image (beginning of
                                          // datastream)

    public static final int M_EOI = 0xD9; // End of image (end of datastream)

    public static final int M_SOS = 0xDA; // Start of scan (begins compressed
                                          // data)

    public static final int M_DQT = 0xDB; // Quantization table - 69 bytes

    public static final int M_JFIF = 0xE0; // Jfif marker

    public static final int M_EXIF = 0xE1; // Exif marker

    public static final int M_COM = 0xFE; // Image Title

    public static final int M_XFF = 0xFF; // starts all markers

    private int m_width = 0;

    private int m_height = 0;

    private int m_bpp = 0;

    private String m_filename = null;

    /**
     * JPEG file type.
     * 
     * UNKNOWN = not a recognized JPEG file<br>
     * EXIF = Exif file (contains an APP1 header)<br>
     * JFIF = JFIF file (contains an APP0 header)<br>
     * 
     * If the file contains an APP1 and APP2 header it is considered EXIF.
     */
    // public enum FileType {UNKNOWN, EXIF, JFIF}
    static final int UNKNOWN = 0;

    static final int EXIF = 1;

    static final int JFIF = 2;

    /**
     * Byte order within the file (high byte first, low byte first).
     */
    // public enum ByteOrder {MOTOROLA, INTEL}
    static final int MOTOROLA = 0;

    static final int INTEL = 1;

    /**
     * Stores an unparsed section from the JPEG file.
     * 
     * <p>
     * This is only needed if you call getSections(). Mostly you will not want to.
     * </p>
     */
    public class Section
    {
        public int type = 0;

        public int offset = 0;

        public int[] data = null;

        public byte[] asBytes()
        {
            byte[] b = new byte[data.length];
            for (int i = 0; i < b.length; ++i)
                b[i] = (byte) (data[i] & 0xff);
            return b;
        }

        public Section()
        {
        }

        public Section(int t, int o, int[] d)
        {
            type = t;
            offset = o;
            data = d;
        }
    }

    private int m_format = UNKNOWN;

    public Vector m_sectionList = new Vector();// Section

    private boolean m_noParsing = false;

    public JpegHeaders(String filename) throws IOException, FileNotFoundException
    {
        loadFromFile(filename);
    }

    private void loadFromFile(String filename) throws IOException, FileNotFoundException
    {
        m_filename = filename;
        FileInputStream stream = new FileInputStream(filename);
        try
        {
            loadFromStream(stream);
        }
        finally
        {
            stream.close();
        }
    }

    public JpegHeaders(InputStream stream) throws IOException
    {
        loadFromStream(stream);
    }

    private void loadFromStream(InputStream istream) throws IOException
    {
        DataInputStream stream = new DataInputStream(istream);
        validateHeader(stream);
        loadSections(stream);
        stream.close();
    }

    private void validateHeader(DataInputStream stream) throws IOException
    {
        try
        {
            int firstByte = stream.readUnsignedByte();
            int secondByte = stream.readUnsignedByte();
            if (firstByte != M_XFF || secondByte != M_SOI)
                throw new IOException("Not a JPEG file");
        }
        catch (EOFException e)
        {
        }
    }

    private void loadSections(DataInputStream stream) throws IOException
    {
        boolean moreSections = true;
        int offset = 0;
        while (moreSections)
        {
            // find the next marker
            boolean sofFound = false;
            int type = 0;
            while (!sofFound)
            {
                try
                {
                    type = stream.readUnsignedByte();
                    offset++;
                }
                catch (EOFException e)
                {
                    return; // no more tags
                }
                sofFound = type != M_XFF;
            }
            if (!sofFound)
                throw new IOException("Invalid JPEG - cannot find next marker");
            if (type == M_JFIF)
                m_format = JFIF;
            else if (type == M_EXIF)
                m_format = EXIF;

            switch (type)
            {
                case M_SOI:
                    break;
                case M_EOI:
                    break;

                case M_SOS:
                    moreSections = false;
                    break;

                // case M_SOF0:
                // case M_SOF1:
                // case M_SOF2:
                // case M_SOF3:
                // case M_SOF5:
                // case M_SOF6:
                // case M_SOF7:
                // case M_SOF9:
                // case M_SOF10:
                // case M_SOF11:
                // case M_SOF13:
                // case M_SOF14:
                // case M_SOF15: {
                // Section sec = readSection(stream, type, offset);
                // if (sec.data.length >= 2) {
                // offset += sec.data.length;
                // m_width = readIntLittleEndian(sec.data, 5, 2);
                // m_height = readIntLittleEndian(sec.data, 3, 2);
                // m_bpp = readIntLittleEndian(sec.data, 2, 1)
                // * readIntLittleEndian(sec.data, 7, 1);
                // m_sectionList.addElement(sec);
                // }
                // }
                // break;
                // case M_DHT:
                // case M_DQT:
                // case M_JFIF:
                case M_EXIF:
                    // case M_COM:
                    // case M_XFF:
                    Section sec = readSection(stream, type, offset);
                    Log.d("JPEG", "sec--------->" + sec);
                    if (sec != null && sec.data.length >= 2)
                    {
                        m_sectionList.addElement(sec);
                        offset += sec.data.length;
                    }
                    break;
            }
        }
    }

    private void readBytes(DataInputStream stream, int[] bytes, int n, int o) throws IOException, EOFException
    {
        for (int i = o; i < n + o; ++i)
            bytes[i] = stream.readUnsignedByte();
    }

    private Section readSection(DataInputStream stream, int type, int offset) throws IOException
    {
        try
        {
            // read the size
            Section sec = new Section();
            sec.type = type;
            int size1 = stream.readUnsignedByte();
            int size2 = stream.readUnsignedByte();
            int size = (size1 << 8) | size2;
            // read the data
            sec.data = new int[size];
            readBytes(stream, sec.data, size - 2, 2);
            if (sec.data.length < 2)
            {
                // System.err.println("Corrupt JPEG section " +
                // HexUtils.toHex(type) + " at offset " + offset +
                // " - skipping");
                return sec;
            }
            sec.data[0] = size1;
            sec.data[1] = size2;
            sec.offset = offset;
            return sec;
        }
        catch (EOFException e)
        {
        }
        return null;
    }

    /**
     * Returns the file type.
     * 
     * @return file type
     */
    public int getFileType()
    {
        return m_format;
    }

    /**
     * Returns all sections as raw data.
     * 
     * @return list of sections.
     */
    public Vector getSections()
    {
        return m_sectionList;
    }

    /**
     * Returns the actual image width (read from image data not headers).
     * 
     * @return the image width.
     */
    public int getWidth()
    {
        return m_width;
    }

    /**
     * Returns the actual image height (read from image data not headers).
     * 
     * @return the image height.
     */
    public int getHeight()
    {
        return m_height;
    }

    /**
     * Returns the number of bits per pixe; (read from image data not headers).
     * 
     * @return the bits per pixel.
     */
    public int getBitsPerPixel()
    {
        return m_bpp;
    }

    private int readIntBigEndian(int[] data, int off, int len)
    {
        int offset = 0;
        int shift = 0;
        for (int i = 0; i < len; ++i)
        {
            offset |= data[i + off] << shift;
            shift += 8;
        }
        return offset;
    }

    private int readIntLittleEndian(int[] data, int off, int len)
    {
        int offset = 0;
        int shift = 0;
        for (int i = len - 1; i >= 0; --i)
        {
            offset |= data[i + off] << shift;
            shift += 8;
        }
        return offset;
    }

    /**
     * Testing only
     */
    public static void main(String[] args)
    {
        try
        {
            // JpegHeaders headers = new JpegHeaders("121630257445991.jpg");
            JpegHeaders headers = new JpegHeaders("C:/Documents and Settings/All Users/Documents/My Pictures/示例图片/gg/5327441186556430744_1920.jpg");
            for (int i = 0; i < headers.m_sectionList.size(); i++)
            {
                Section sce = (Section) headers.m_sectionList.get(i);
                System.out.println("type: " + HexUtils.toHex(sce.type));

            }
            Section sce = (Section) headers.m_sectionList.get(0);
            // System.out.println("sce[0]: "+HexUtils.toHex(sce.asBytes()[0]));
            // System.out.println("sce[0]: "+HexUtils.toHex(sce.asBytes()[1]));
            ReadJpg.setBytes(sce.asBytes());
            ReadJpg.EXIF_process_EXIF(0, 0, 2, sce.asBytes().length);
            // System.out.println(ReadJpg.m_pExifInfo);
            System.out.println("finished");

        }
        catch (Exception e)
        {
        }
    }
}
