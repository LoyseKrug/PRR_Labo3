/**
 * Authors: Adrien Allemand, Loyse Krug
 *
 * Source: https://javadeveloperzone.com/java-basic/java-convert-int-to-byte-array/
 */

import java.nio.ByteBuffer;

public class Util {

    /**
     * Transform an int into a byte array
     * @param i
     * @return a byte array conting the int value
     */
    public static byte[] intToBytes( final int i ) {
        ByteBuffer bb = ByteBuffer.allocate(4);
        bb.putInt(i);
        return bb.array();
    }

    /**
     * Transform a byte array into an int
     * @param intBytes
     * @return the int created from the byte array
     */
    public static int convertByteArrayToInt(byte[] intBytes){
        ByteBuffer byteBuffer = ByteBuffer.wrap(intBytes);
        return byteBuffer.getInt();
    }

    /**
     * Fills the desitnation array with the content (or the beginning of the content) of the source array
     * @param arrayDest Destination array. the Dest array must length be smaller or equals to the source array
     * @param arraySource, Source array
     */
    public static void copyToFillByteArray(byte[] arrayDest, byte[] arraySource){
        for(int i = 0; i < arrayDest.length; ++i){
            arrayDest[i] = arraySource[i];
        }
    }

}
