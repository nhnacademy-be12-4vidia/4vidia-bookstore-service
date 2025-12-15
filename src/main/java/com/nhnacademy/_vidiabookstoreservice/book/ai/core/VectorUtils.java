package com.nhnacademy._vidiabookstoreservice.book.ai.core;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Base64;

public class VectorUtils {

    private VectorUtils() {}

    public static String toBase64(float[] v) {
        ByteBuffer buf = ByteBuffer.allocate(v.length * 4).order(ByteOrder.LITTLE_ENDIAN);
        for (float x : v) buf.putFloat(x);
        return Base64.getEncoder().encodeToString(buf.array());
    }

    public static float[] fromBase64ToFloat(String b64) {
        byte[] bytes = Base64.getDecoder().decode(b64);
        ByteBuffer buf = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
        int n = bytes.length / 4;
        float[] v = new float[n];
        for (int i = 0; i < n; i++) v[i] = buf.getFloat();
        return v;
    }

    public static double cosine(float[] a, float[] b) {
        double dot = 0, na = 0, nb = 0;
        int n = Math.min(a.length, b.length);
        for (int i = 0; i < n; i++) {
            double ai = a[i];
            double bi = b[i];
            dot += ai * bi;
            na += ai * ai;
            nb += bi * bi;
        }
        return dot / (Math.sqrt(na) * Math.sqrt(nb) + 1e-9);
    }
}
