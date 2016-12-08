package com.rockchips.mediacenter.image.jpeg;


/**
 * <p>
 * Converting to and from hex values.
 * </p>
 */
public class HexUtils
{
    /**
     * Converts a hex string into an array of bytes.
     * 
     * @param str string, case-insensitive, eg 1F.
     * @return array of bytes, least significant at index 0.
     */
    static public byte[] fromHex(String str) throws NumberFormatException
    {
        char[] chars = str.toCharArray();
        byte[] bytes = new byte[chars.length / 2];
        for (int i = 0; i < chars.length; i += 2)
        {
            int j = i >> 1;
            int b = 0;
            for (int k = 0; k < 2; k++)
            {
                int ch = chars[i + k];
                switch (ch)
                {
                    case '0':
                        b += 0;
                        break;
                    case '1':
                        b += 1;
                        break;
                    case '2':
                        b += 2;
                        break;
                    case '3':
                        b += 3;
                        break;
                    case '4':
                        b += 4;
                        break;
                    case '5':
                        b += 5;
                        break;
                    case '6':
                        b += 6;
                        break;
                    case '7':
                        b += 7;
                        break;
                    case '8':
                        b += 8;
                        break;
                    case '9':
                        b += 9;
                        break;
                    case 'A':
                    case 'a':
                        b += 10;
                        break;
                    case 'B':
                    case 'b':
                        b += 11;
                        break;
                    case 'C':
                    case 'c':
                        b += 12;
                        break;
                    case 'D':
                    case 'd':
                        b += 13;
                        break;
                    case 'E':
                    case 'e':
                        b += 14;
                        break;
                    case 'F':
                    case 'f':
                        b += 15;
                        break;
                    default:
                        throw new NumberFormatException("Not a hex number");
                }
                b <<= 4 * (1 - k);
            } // for (int k...
            if (b >= 128)
                bytes[j] = (byte) (128 - b);
            else
                bytes[j] = (byte) b;
        } // for (int i...
        return bytes;
    }

    /**
     * Converts an array of bytes into a single hex number.
     * 
     * Always returns two hex digits.
     * 
     * @param bytes array of bytes, least significant at index 0.
     * @return uppercase hex string, eg 1F.
     */
    public static String toHex(byte[] bytes)
    {
        char[] chars = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; ++i)
        {
            int j = i * 2;
            for (int k = 0; k < 2; ++k)
            {
                int byte_i = bytes[i];
                if (byte_i < 0)
                    byte_i = 128 - byte_i;
                int hb = (byte_i >> ((1 - k) * 4)) % 16;
                switch (hb)
                {
                    case 0:
                        chars[j + k] = '0';
                        break;
                    case 1:
                        chars[j + k] = '1';
                        break;
                    case 2:
                        chars[j + k] = '2';
                        break;
                    case 3:
                        chars[j + k] = '3';
                        break;
                    case 4:
                        chars[j + k] = '4';
                        break;
                    case 5:
                        chars[j + k] = '5';
                        break;
                    case 6:
                        chars[j + k] = '6';
                        break;
                    case 7:
                        chars[j + k] = '7';
                        break;
                    case 8:
                        chars[j + k] = '8';
                        break;
                    case 9:
                        chars[j + k] = '9';
                        break;
                    case 10:
                        chars[j + k] = 'A';
                        break;
                    case 11:
                        chars[j + k] = 'B';
                        break;
                    case 12:
                        chars[j + k] = 'C';
                        break;
                    case 13:
                        chars[j + k] = 'D';
                        break;
                    case 14:
                        chars[j + k] = 'E';
                        break;
                    case 15:
                        chars[j + k] = 'F';
                        break;
                    default:
                        ; // can't happen
                } // switch...
            } // for (int k...
        } // for (int i...
        return new String(chars);
    }

    /**
     * Converts a single unsigned byte into a hex number.
     * 
     * Always returns two hex digits.
     * 
     * @param num an unsigned byte to convert, between 0 and 255 inclusive.
     * @return uppercase hex string, eg 1F.
     */
    public static String toHex(int num)
    {
        int ub = (num >> 4) & 0xf;
        int lb = (num & 0xf);
        char c1 = ' ';
        char c2 = ' ';
        switch (ub)
        {
            case 0:
                c1 = '0';
                break;
            case 1:
                c1 = '1';
                break;
            case 2:
                c1 = '2';
                break;
            case 3:
                c1 = '3';
                break;
            case 4:
                c1 = '4';
                break;
            case 5:
                c1 = '5';
                break;
            case 6:
                c1 = '6';
                break;
            case 7:
                c1 = '7';
                break;
            case 8:
                c1 = '8';
                break;
            case 9:
                c1 = '9';
                break;
            case 10:
                c1 = 'A';
                break;
            case 11:
                c1 = 'B';
                break;
            case 12:
                c1 = 'C';
                break;
            case 13:
                c1 = 'D';
                break;
            case 14:
                c1 = 'E';
                break;
            case 15:
                c1 = 'F';
                break;
        }
        switch (lb)
        {
            case 0:
                c2 = '0';
                break;
            case 1:
                c2 = '1';
                break;
            case 2:
                c2 = '2';
                break;
            case 3:
                c2 = '3';
                break;
            case 4:
                c2 = '4';
                break;
            case 5:
                c2 = '5';
                break;
            case 6:
                c2 = '6';
                break;
            case 7:
                c2 = '7';
                break;
            case 8:
                c2 = '8';
                break;
            case 9:
                c2 = '9';
                break;
            case 10:
                c2 = 'A';
                break;
            case 11:
                c2 = 'B';
                break;
            case 12:
                c2 = 'C';
                break;
            case 13:
                c2 = 'D';
                break;
            case 14:
                c2 = 'E';
                break;
            case 15:
                c2 = 'F';
                break;
        }
        return "" + c1 + c2;
    }

    /**
     * Converts an unsigned long value to a hex number.
     * 
     * Always returns four hex digits.
     * 
     * @param num an unsigned byte to convert, between 0 and 2^32-1 inclusive.
     * @return uppercase hex string, eg 001F.
     */
    public static String toHex(long num)
    {
        int hint = (int) ((num & 0xff00) >> 8);
        int lint = (int) (num & 0xff);
        return toHex(hint) + toHex(lint);
    }
}
