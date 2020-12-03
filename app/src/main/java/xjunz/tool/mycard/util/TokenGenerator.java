/*
 * Copyright (c) xjunz 2020.
 */

package xjunz.tool.mycard.util;

import android.util.Base64;

public class TokenGenerator {
    public static String generate(int id) {
        String[] buffer = new String[]{"d0", "30", "00", "00", "00", "00"};
        int r = id % 0xffff + 1;
        for (int t = 0; t < buffer.length; t += 2) {
            String le1 = buffer[t + 1];
            String le2 = buffer[t];
            int i = Integer.parseInt(le1 + le2, 16);
            String k = String.format("%04x", i ^ r);
            buffer[t] = k.substring(2, 4);
            buffer[t + 1] = k.substring(0, 2);
        }
        byte[] bytes = new byte[6];
        for (int i = 0; i < buffer.length; i++) {
            int b = Integer.parseInt(buffer[i], 16) & 0xff;
            bytes[i] = (byte) b;
        }
        return Base64.encodeToString(bytes, Base64.NO_WRAP);
    }
}
