package net.capps.word.crypto;

import com.google.common.io.BaseEncoding;

import java.io.IOException;

/**
 * Created by charlescapps on 12/28/14.
 */
public class CryptoUtils {
    /**
     * From a base 64 representation, returns the corresponding byte[]
     * @param data String The base64 representation
     * @return byte[]
     * @throws java.io.IOException
     */
    public static byte[] base64ToByte(String data) throws IOException {
        BaseEncoding encoder = BaseEncoding.base64();
        return encoder.decode(data);
    }

    /**
     * From a byte[] returns a base 64 representation
     * @param data byte[]
     * @return String
     * @throws java.io.IOException
     */
    public static String byteToBase64(byte[] data){
        BaseEncoding encoder = BaseEncoding.base64();
        return encoder.encode(data);
    }
}
