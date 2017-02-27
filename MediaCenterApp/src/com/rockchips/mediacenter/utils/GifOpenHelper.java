package com.rockchips.mediacenter.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;

/**
 * Handler for read & extract Bitmap from *.gif
 * @author s00211113
 * 
 */
public class GifOpenHelper
{
    /**
     * to store *.gif data, Bitmap & delay
     * @author s00211113
     * 
     */
    class GifFrame
    {
        // to access image & delay w/o interface
        public Bitmap image;

        public int delay;

        public GifFrame(Bitmap im, int del)
        {
            image = im;
            delay = del;
        }

    }

    // to define some error type
    public static final int STATUS_OK = 0;

    public static final int STATUS_FORMAT_ERROR = 1;

    public static final int STATUS_OPEN_ERROR = 2;

    protected int mStatus;

    protected InputStream mInputStream;

    protected int mWidth; // full image width

    protected int mHeight; // full image height

    protected boolean mGctFlag; // global color table used

    protected int mGctSize; // size of global color table

    protected int mLoopCount = 1; // iterations; 0 = repeat forever

    protected int[] mGct; // global color table

    protected int[] mLct; // local color table

    protected int[] mAct; // active color table

    protected int mBgIndex; // background color index

    protected int mBgColor; // background color

    protected int mLastBgColor; // previous bg color

    protected int mPixelAspect; // pixel aspect ratio

    protected boolean mLctFlag; // local color table flag

    protected boolean mInterlace; // interlace flag

    protected int mLctSize; // local color table size

    protected int mIx, mIy, mIw, mIh; // current image rectangle

    protected int mLrx, mLry, mLrw, mLrh;

    protected Bitmap mImage; // current frame

    protected Bitmap mLastImage; // previous frame

    protected int mFrameindex;

    public int getFrameindex()
    {
        return mFrameindex;
    }

    public void setFrameindex(int frameindex)
    {
        this.mFrameindex = frameindex;
        if (frameindex > mFrames.size() - 1)
        {
            frameindex = 0;
        }
    }

    private static final int BLOCK_SIZE_MAX = 256;

    protected byte[] mBlock = new byte[BLOCK_SIZE_MAX]; // current data block

    protected int mBlockSize; // block size

    // last graphic control extension info
    protected int mDispose;

    // 0=no action; 1=leave in place; 2=restore to bg; 3=restore to prev

    private static final int DISPOSE_NO_ACTION = 0;

    private static final int DISPOSE_LEAVE_IN_PLACE = 1;

    private static final int DISPOSE_RESTORE_TO_BG = 2;

    private static final int DISPOSE_RESTORE_TO_PREV = 3;

    protected int mLastDispose;

    protected boolean mTtransparency; // use transparent color

    protected int mDelay; // delay in milliseconds

    protected int mTransIndex; // transparent color index

    protected static final int MAXSTACKSIZE = 4096;

    // max decoder pixel stack size

    // LZW decoder working arrays
    protected short[] mPrefix;

    protected byte[] mSuffix;

    protected byte[] mPixelStack;

    protected byte[] mPixels;

    protected Vector<GifFrame> mFrames; // frames read from current file

    protected int mFrameCount;

    // to get its Width / Height
    public int getWidth()
    {
        return mWidth;
    }

    public int getHeigh()
    {
        return mHeight;
    }

    /**
     * Gets display duration for specified frame.
     * 
     * @param n int index of frame
     * @return delay in milliseconds
     */
    public int getDelay(int n)
    {
        mDelay = -1;
        if ((n >= 0) && (n < mFrameCount))
        {
            mDelay = ((GifFrame) mFrames.elementAt(n)).delay;
        }
        return mDelay;
    }

    public int getFrameCount()
    {
        return mFrames.size();
    }

    public Bitmap getImage()
    {
        return getFrame(0);
    }

    public int getLoopCount()
    {
        return mLoopCount;
    }

    private static final int INC_2 = 2;

    private static final int INC_4 = 4;

    private static final int INC_8 = 8;

    private static final int PASS_1 = 1;

    private static final int PASS_2 = 2;

    private static final int PASS_3 = 3;

    private static final int PASS_4 = 4;

    private static final int PIXEL_MASK = 0xff;

    private static final int ILINE_4 = 4;

    protected void setPixels()
    {
        int[] dest = new int[mWidth * mHeight];
        // fill in starting image contents based on last image's dispose code
        if (mLastDispose > DISPOSE_NO_ACTION)
        {
            if (mLastDispose == DISPOSE_RESTORE_TO_PREV)
            {
                // use image before last
                int n = mFrameCount - 2;
                if (n > 0)
                {
                    mLastImage = getFrame(n - 1);
                }
                else
                {
                    mLastImage = null;
                }
            }
            if (mLastImage != null)
            {
                mLastImage.getPixels(dest, 0, mWidth, 0, 0, mWidth, mHeight);
                // copy pixels
                if (mLastDispose == DISPOSE_RESTORE_TO_BG)
                {
                    // fill last image rect area with background color
                    int c = 0;
                    if (!mTtransparency)
                    {
                        c = mLastBgColor;
                    }
                    for (int i = 0; i < mLrh; i++)
                    {
                        int n1 = (mLry + i) * mWidth + mLrx;
                        int n2 = n1 + mLrw;
                        for (int k = n1; k < n2; k++)
                        {
                            dest[k] = c;
                        }
                    }
                }
            }
        }

        // copy each source line to the appropriate place in the destination
        int pass = PASS_1;
        int inc = INC_8;
        int iline = 0;
        for (int i = 0; i < mIh; i++)
        {
            int line = i;
            if (mInterlace)
            {
                if (iline >= mIh)
                {
                    pass++;
                    switch (pass)
                    {
                        case PASS_2:
                            iline = ILINE_4;
                            break;
                        case PASS_3:
                            iline = 2;
                            inc = INC_4;
                            break;
                        case PASS_4:
                            iline = 1;
                            inc = INC_2;
                        default:
                            break;
                    }
                }
                line = iline;
                iline += inc;
            }
            line += mIy;
            if (line < mHeight)
            {
                int k = line * mWidth;
                int dx = k + mIx; // start of line in dest
                int dlim = dx + mIw; // end of dest line
                if ((k + mWidth) < dlim)
                {
                    dlim = k + mWidth; // past dest edge
                }
                int sx = i * mIw; // start of line in source
                while (dx < dlim)
                {
                    // map color and insert in destination
                    int index = ((int) mPixels[sx++]) & PIXEL_MASK;
                    int c = mAct[index];
                    if (c != 0)
                    {
                        dest[dx] = c;
                    }
                    dx++;
                }
            }
        }
        mImage = Bitmap.createBitmap(dest, mWidth, mHeight, Config.RGB_565);
    }

    public Bitmap getFrame(int n)
    {
        Bitmap im = null;
        if ((n >= 0) && (n < getFrameCount()))
        {
            im = ((GifFrame) mFrames.elementAt(n)).image;
        }
        return im;
    }

    public Bitmap nextBitmap()
    {
        Bitmap im = null;
        mFrameindex++;
        if (mFrameindex > mFrames.size() - 1)
        {
            mFrameindex = 0;
        }
        im = ((GifFrame) mFrames.elementAt(mFrameindex)).image;
        return im;
    }

    private static final int DELAY_TIME = 1000;

    public int nextDelay()
    {
        if (mFrameindex > mFrames.size() - 1)
        {
            mFrameindex = 0;
        }
        return (mFrames.size() != 1) ? ((GifFrame) mFrames.elementAt(mFrameindex)).delay : DELAY_TIME;
    }

    // to read & parse all *.gif stream
    public int read(InputStream is)
    {
        try
        {
            init();
            if (is != null)
            {
                mInputStream = is;
                readHeader();
                if (!err())
                {
                    readContents();
                    if (mFrameCount < 0)
                    {
                        mStatus = STATUS_FORMAT_ERROR;
                    }
                }
            }
            else
            {
                mStatus = STATUS_OPEN_ERROR;
            }
        }
        catch (OutOfMemoryError e)
        {
            free();
        }
        catch (NullPointerException e)
        {
            free();
        }
        finally
        {
            if (is != null)
            {
                try
                {
                    is.close();
                }
                catch (IOException e)
                {
                }
            }
        }

        return mStatus;
    }

    private static final int BLOCK_MASK = 0xff;

    private static final int BITS_PLUS = 8;

    protected void decodeImageData()
    {
        int nullCode = -1;
        int npix = mIw * mIh;
        int available, clear, codeMask, codeSize, endOfInformation, inCode, oldCode, bits, code, count, i, datum, dataSize, first, top, bi, pi;

        if ((mPixels == null) || (mPixels.length < npix))
        {
            mPixels = new byte[npix]; // allocate new pixel array
        }
        if (mPrefix == null)
        {
            mPrefix = new short[MAXSTACKSIZE];
        }
        if (mSuffix == null)
        {
            mSuffix = new byte[MAXSTACKSIZE];
        }
        if (mPixelStack == null)
        {
            mPixelStack = new byte[MAXSTACKSIZE + 1];
        }
        // Initialize GIF data stream decoder.
        dataSize = read();
        clear = 1 << dataSize;
        endOfInformation = clear + 1;
        available = clear + 2;
        oldCode = nullCode;
        codeSize = dataSize + 1;
        codeMask = (1 << codeSize) - 1;
        for (code = 0; code < clear; code++)
        {
            mPrefix[code] = 0;
            mSuffix[code] = (byte) code;
        }

        // Decode GIF pixel stream.
        datum = bits = count = first = top = pi = bi = 0;
        for (i = 0; i < npix; )
        {
            if (top == 0)
            {
                if (bits < codeSize)
                {
                    // Load bytes until there are enough bits for a code.
                    if (count == 0)
                    {
                        // Read a new data block.
                        count = readBlock();
                        if (count <= 0)
                        {
                            break;
                        }
                        bi = 0;
                    }
                    datum += (((int) mBlock[bi]) & BLOCK_MASK) << bits;
                    bits += BITS_PLUS;
                    bi++;
                    count--;
                    continue;
                }
                // Get the next code.
                code = datum & codeMask;
                datum >>= codeSize;
                bits -= codeSize;

                // Interpret the code
                if ((code > available) || (code == endOfInformation))
                {
                    break;
                }
                if (code == clear)
                {
                    // Reset decoder.
                    codeSize = dataSize + 1;
                    codeMask = (1 << codeSize) - 1;
                    available = clear + 2;
                    oldCode = nullCode;
                    continue;
                }
                if (oldCode == nullCode)
                {
                    mPixelStack[top++] = mSuffix[code];
                    oldCode = code;
                    first = code;
                    continue;
                }
                inCode = code;
                if (code == available)
                {
                    mPixelStack[top++] = (byte) first;
                    code = oldCode;
                }
                while (code > clear)
                {
                    mPixelStack[top++] = mSuffix[code];
                    code = mPrefix[code];
                }
                first = ((int) mSuffix[code]) & BLOCK_MASK;
                // Add a new string to the string table,
                if (available >= MAXSTACKSIZE)
                {
                    break;
                }
                mPixelStack[top++] = (byte) first;
                mPrefix[available] = (short) oldCode;
                mSuffix[available] = (byte) first;
                available++;
                if (((available & codeMask) == 0) && (available < MAXSTACKSIZE))
                {
                    codeSize++;
                    codeMask += available;
                }
                oldCode = inCode;
            }

            // Pop a pixel off the pixel stack.
            top--;
            mPixels[pi++] = mPixelStack[top];
            i++;
        }
        for (i = pi; i < npix; i++)
        {
            mPixels[i] = 0; // clear missing pixels
        }
    }

    protected boolean err()
    {
        return mStatus != STATUS_OK;
    }

    // to initia variable
    protected void init()
    {
        mStatus = STATUS_OK;
        mFrameCount = 0;
        mFrames = new Vector<GifFrame>();
        mGct = null;
        mLct = null;
    }

    protected int read()
    {
        int curByte = 0;
        try
        {
            curByte = mInputStream.read();
        }
        catch (IOException e)
        {
            mStatus = STATUS_FORMAT_ERROR;
        }
        return curByte;
    }

    protected int readBlock()
    {
        // 先读取一个字节，即8位，小于等于255
        mBlockSize = read();
        int n = 0;
        if (mBlockSize > 0)
        {
            try
            {
                int count = 0;
                int bytesCount = 0;
                while (n < mBlockSize)
                {
                    bytesCount =  mBlockSize - n;
                    if (bytesCount <= mBlockSize)
                    {
                        // mBlockSize是小于BLOCK_SIZE_MAX的数，不会引起溢出
                        count = mInputStream.read(mBlock, n, bytesCount);
                    }
                    
                    if (count == -1)
                    {
                        break;
                    }
                    n += count;
                }
            }
            catch (IndexOutOfBoundsException e)
            {
            }
            catch (IOException e)
            {
            }
            if (n < mBlockSize)
            {
                mStatus = STATUS_FORMAT_ERROR;
            }
        }
        return n;
    }

    private static final int BYTES_MULTIPLE = 3;

    private static final int TAB_SIZE = 256;

    private static final int COLOR_MASK = 0xff;

    private static final int TAB_MASK = 0xff000000;

    private static final int R_DISPLACEMENT = 16;

    private static final int G_DISPLACEMENT = 8;

    // Global Color Table
    protected int[] readColorTable(int ncolors)
    {
        int nbytes = BYTES_MULTIPLE * ncolors;
        int[] tab = null;
        byte[] c = new byte[nbytes];
        int n = 0;
        try
        {
            n = mInputStream.read(c);
        }
        catch (IOException e)
        {
        }
        if (n < nbytes)
        {
            mStatus = STATUS_FORMAT_ERROR;
        }
        else
        {
            tab = new int[TAB_SIZE]; // max size to avoid bounds checks
            int i = 0;
            int j = 0;
            while (i < ncolors)
            {
                int r = ((int) c[j++]) & COLOR_MASK;
                int g = ((int) c[j++]) & COLOR_MASK;
                int b = ((int) c[j++]) & COLOR_MASK;
                tab[i++] = TAB_MASK | (r << R_DISPLACEMENT) | (g << G_DISPLACEMENT) | b;
            }
        }
        return tab;
    }

    private static final int CODE_IMAGE_SEPARATOR = 0x2C;

    private static final int CODE_EXTENSION = 0x21;

    private static final int CODE_GRAPHICS_CONTROL_EXTENSION = 0xf9;

    private static final int CODE_APPLICATION_EXTENSION = 0xff;

    private static final int CODE_TERMINATOR = 0x3b;

    private static final int CODE_BAD_BYTE = 0x00;
    
    private static final int APPLICATION_EXTENSION_READ_SIZE = 11;
    
    private static final String NETSCAPE2_0 = "NETSCAPE2.0";

    // Image Descriptor
    protected void readContents()
    {
        // read GIF file content blocks
        boolean done = false;
        while (!(done || err()))
        {
            int code = read();
            switch (code)
            {
                case CODE_IMAGE_SEPARATOR:
                    readImage();
                    break;
                case CODE_EXTENSION:
                    code = read();
                    switch (code)
                    {
                        case CODE_GRAPHICS_CONTROL_EXTENSION:
                            readGraphicControlExt();
                            break;

                        case CODE_APPLICATION_EXTENSION:
                            readBlock();
                            String app = "";
                            for (int i = 0; i < APPLICATION_EXTENSION_READ_SIZE; i++)
                            {
                                app += (char) mBlock[i];
                            }
                            if (app.equals(NETSCAPE2_0))
                            {
                                readNetscapeExt();
                            }
                            else
                            {
                                skip(); // don't care
                            }
                            break;
                        default: // uninteresting extension
                            skip();
                    }
                    break;

                case CODE_TERMINATOR:
                    done = true;
                    break;

                case CODE_BAD_BYTE: // bad byte, but keep going and see what
                                    // happens
                    break;
                default:
                    mStatus = STATUS_FORMAT_ERROR;
            }
        }
    }

    private static final int PACK_MASK = 0x1c;

    private static final int DELAY_TIME_MULTIPLE = 10;

    protected void readGraphicControlExt()
    {
        read(); // block size
        int packed = read(); // packed fields
        mDispose = (packed & PACK_MASK) >> 2; // disposal method
        if (mDispose == 0)
        {
            mDispose = DISPOSE_LEAVE_IN_PLACE; // elect to keep old image if
                                               // discretionary
        }
        mTtransparency = (packed & 1) != 0;
        mDelay = readShort() * DELAY_TIME_MULTIPLE; // delay in milliseconds
        mTransIndex = read(); // transparent color index
        read(); // block terminator
    }

    private static final int HEAD_LENGTH = 6;

    // to get Stream - Head
    protected void readHeader()
    {
        String id = "";
        for (int i = 0; i < HEAD_LENGTH; i++)
        {
            id += (char) read();
        }
        if (!id.startsWith("GIF"))
        {
            mStatus = STATUS_FORMAT_ERROR;
            return;
        }
        readLSD();
        if (mGctFlag && !err())
        {
            mGct = readColorTable(mGctSize);
            mBgColor = mGct[mBgIndex];
        }
    }

    private static final int LCTFLAG_MASK = 0x80;

    private static final int INTERLACE_MASK = 0x40;

    private static final int LCTSIZE_MASK = 7;

    protected void readImage()
    {
        // offset of X
        mIx = readShort(); // (sub)image position & size
        // offset of Y
        mIy = readShort();
        // width of bitmap
        mIw = readShort();
        // height of bitmap
        mIh = readShort();

        // Local Color Table Flag
        int packed = read();
        mLctFlag = (packed & LCTFLAG_MASK) != 0; // 1 - local color table flag

        // Interlace Flag, to array with interwoven if ENABLE, with order
        // otherwise
        mInterlace = (packed & INTERLACE_MASK) != 0; // 2 - interlace flag
        // 3 - sort flag
        // 4-5 - reserved
        mLctSize = 2 << (packed & LCTSIZE_MASK); // 6-8 - local color table size
        if (mLctFlag)
        {
            mLct = readColorTable(mLctSize); // read table
            mAct = mLct; // make local table active
        }
        else
        {
            mAct = mGct; // make global table active
            if (mBgIndex == mTransIndex)
            {
                mBgColor = 0;
            }
        }
        int save = 0;
        if (mTtransparency)
        {
            save = mAct[mTransIndex];
            mAct[mTransIndex] = 0; // set transparent color if specified
        }
        if (mAct == null)
        {
            mStatus = STATUS_FORMAT_ERROR; // no color table defined
        }
        if (err())
        {
            return;
        }
        decodeImageData(); // decode pixel data
        skip();
        if (err())
        {
            return;
        }
        // create new image to receive frame data
        // image = Bitmap.createBitmap(width, height, Config.RGB_565);
        // createImage(width, height);
        setPixels(); // transfer pixel data to image
        mFrames.addElement(new GifFrame(mImage, mDelay)); // add image to frame
        mFrameCount++;
        // list
        if (mTtransparency)
        {
            mAct[mTransIndex] = save;
        }
        resetFrame();
    }


    private static final int GCTFLAG_MASK = 0x80;

    private static final int GCTSIZE_MASK = 7;
    // Logical Screen Descriptor
    protected void readLSD()
    {
        // logical screen size
        mWidth = readShort();
        mHeight = readShort();
        // packed fields
        int packed = read();
        mGctFlag = (packed & GCTFLAG_MASK) != 0; // 1 : global color table flag
        // 2-4 : color resolution
        // 5 : gct sort flag
        mGctSize = 2 << (packed & GCTSIZE_MASK); // 6-8 : gct size
        mBgIndex = read(); // background color index
        mPixelAspect = read(); // pixel aspect ratio
    }

    private static final int DISPLACEMENT_8 = 8;
    protected void readNetscapeExt()
    {
        do
        {
            readBlock();
            if (mBlock[0] == 1)
            {
                // loop count sub-block
                int b1 = ((int) mBlock[1]) & BLOCK_MASK;
                int b2 = ((int) mBlock[2]) & BLOCK_MASK;
                mLoopCount = (b2 << DISPLACEMENT_8) | b1;
            }
        }
        while ((mBlockSize > 0) && !err());
    }

    // read 8 bit data
    protected int readShort()
    {
        // read 16-bit value, LSB first
        return read() | (read() << DISPLACEMENT_8);
    }

    protected void resetFrame()
    {
        mLastDispose = mDispose;
        mLrx = mIx;
        mLry = mIy;
        mLrw = mIw;
        mLrh = mIh;
        mLastImage = mImage;
        mLastBgColor = mBgColor;
        mDispose = DISPOSE_NO_ACTION;
        mTtransparency = false;
        mDelay = 0;
        mLct = null;
    }

    /**
     * Skips variable length blocks up to and including next zero length block.
     */
    protected void skip()
    {
        do
        {
            readBlock();
        }
        while ((mBlockSize > 0) && !err());
    }

    /**
     * 释放资源
     */
    public void free()
    {
        GifFrame gifFrame = null;
        Bitmap gifBitmap = null;
        for (int i = 0, size = mFrames.size(); i < size; i++)
        {
            gifFrame = mFrames.get(i);
            if (gifFrame != null)
            {
                gifBitmap = gifFrame.image;
                if (gifBitmap != null && !gifBitmap.isRecycled())
                {
                    gifBitmap.recycle();
                }
            }
        }

        if (mInputStream != null)
        {
            try
            {
                mInputStream.close();
            }
            catch (IOException e)
            {
            }
            mInputStream = null;
        }
    }
}
