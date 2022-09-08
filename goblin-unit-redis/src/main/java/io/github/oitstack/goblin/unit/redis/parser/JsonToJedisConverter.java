package io.github.oitstack.goblin.unit.redis.parser;

import java.io.UnsupportedEncodingException;


public class JsonToJedisConverter {

    private static final String DEFAULT_CHARSET = "UTF-8";

    private JsonToJedisConverter() {
        super();
    }

    public static final byte[] toByteArray(Object object) {

        if (object instanceof Number) {
            Number number = (Number) object;
            byte[] numberByte = new byte[1];
            numberByte[0] = number.byteValue();

            return numberByte;
        } else {
            if (object instanceof Boolean) {
                Boolean bool = (Boolean) object;
                try {
                    return bool.toString().getBytes(DEFAULT_CHARSET);
                } catch (UnsupportedEncodingException e) {
                    throw new IllegalArgumentException(e);
                }
            } else {
                if (object instanceof String) {
                    String stringValue = (String) object;
                    try {
                        return stringValue.getBytes(DEFAULT_CHARSET);
                    } catch (UnsupportedEncodingException e) {
                        throw new IllegalArgumentException(e);
                    }
                } else {
                    throw new IllegalArgumentException("Class type " + object.getClass()
                            + " is not supported to be converted to byte[].");
                }
            }
        }
    }

    public static final double toDouble(Object object) {

        if (object instanceof Number) {
            return ((Number) object).doubleValue();
        } else {

            throw new IllegalArgumentException("Class type " + object.getClass()
                    + " is not supported to be converted to Double.");
        }
    }

   public static final byte[] string2bytes(Object object) {

           String input = (String) object;

           StringBuilder in = new StringBuilder(input);
           // 注：这里in.length() 不可在for循环内调用，因为长度在变化
           int remainder = in.length() % 8;
           if (remainder > 0) {
               for (int i = 0; i < 8 - remainder; i++) {
                   in.append("0");
               }
           }
           byte[]  bts = new byte[in.length() / 8];

           // Step 8 Apply compression
           for (int i = 0; i < bts.length; i++) {
               bts[i] = (byte) Integer.parseInt(in.substring(i * 8, i * 8 + 8), 2);
           }

       return bts;
   }
}
