/**
 * Source: https://javadeveloperzone.com/java-basic/java-convert-int-to-byte-array/
 */

import java.nio.ByteBuffer;

public class Util {

    public static byte[] intToBytes( final int i ) {
        ByteBuffer bb = ByteBuffer.allocate(4);
        bb.putInt(i);
        return bb.array();
    }

    private int convertByteArrayToInt(byte[] intBytes){
        ByteBuffer byteBuffer = ByteBuffer.wrap(intBytes);
        return byteBuffer.getInt();
    }
}
