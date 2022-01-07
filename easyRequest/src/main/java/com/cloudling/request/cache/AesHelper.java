package com.cloudling.request.cache;

import android.text.TextUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * 描述: Aes加解密工具
 * 联系：1966353889@qq.com
 * 日期: 2022/1/4
 */
public final class AesHelper {
    private static final String aes_key = "JpWCaGcSOMHG9U42dgAQ9wePE6NqO53b";
    private static final String aes_iv = "rju78Zz6aSofvhX6";
    private static final String encodingFormat = "UTF-8";
    private static final String algorithm = "AES/CBC/PKCS7Padding";

    /**
     * AES加密
     *
     * @param sSrc 待加密内容
     * @return 返回gzip压缩加密后的byte[]
     */
    public static byte[] encryptAsByte(String sSrc) {
        return TextUtils.isEmpty(sSrc) ? null : aesEncryptAsByte(sSrc, encodingFormat, algorithm, aes_key, aes_iv);
    }

    /**
     * AES加密
     *
     * @param sSrc 待加密内容
     * @return 返回gzip压缩加密后的byte[]
     */
    public static byte[] encryptAsByte(byte[] sSrc) {
        return sSrc == null ? null : encryptAsByte(new String(sSrc));
    }

    /**
     * AES加密
     *
     * @param sSrc 待加密内容
     * @return 返回gzip压缩加密后的String
     */
    public static String encryptAsString(String sSrc) {
        return TextUtils.isEmpty(sSrc) ? sSrc : aesEncryptAsString(sSrc, encodingFormat, algorithm, aes_key, aes_iv);
    }

    /**
     * AES加密
     *
     * @param sSrc 待加密内容
     * @return 返回gzip压缩加密后的String
     */
    public static String encryptAsString(byte[] sSrc) {
        return sSrc == null ? null : encryptAsString(new String(sSrc));
    }

    /**
     * AES解密
     *
     * @param sSrc 待解密内容
     * @return 返回gzip解压缩解密后的byte[]
     */
    public static byte[] decryptAsByte(String sSrc) {
        return TextUtils.isEmpty(sSrc) ? null : aesDecryptAsByte(sSrc, encodingFormat, algorithm, aes_key, aes_iv);
    }

    /**
     * AES解密
     *
     * @param sSrc 待解密内容
     * @return 返回gzip解压缩解密后的byte[]
     */
    public static byte[] decryptAsByte(byte[] sSrc) {
        return sSrc == null ? null : decryptAsByte(new String(sSrc));
    }

    /**
     * AES解密
     *
     * @param sSrc 待解密内容
     * @return 返回gzip解压缩解密后的String
     */
    public static String decryptAsString(String sSrc) {
        return TextUtils.isEmpty(sSrc) ? sSrc : aesDecryptAsString(sSrc, encodingFormat, algorithm, aes_key, aes_iv);
    }

    /**
     * AES解密
     *
     * @param sSrc 待解密内容
     * @return 返回gzip解压缩解密后的String
     */
    public static String decryptAsString(byte[] sSrc) {
        return sSrc == null ? null : decryptAsString(new String(sSrc));
    }

    /**
     * AES加密
     *
     * @param sSrc           -- 待加密内容
     * @param encodingFormat -- 字符串编码方式
     * @param algorithm      -- 使用的算法 算法/模式/补码方式, 目前支持ECB和CBC模式
     * @param sKey           -- 加密密钥
     * @param ivParameter    -- 偏移量,CBC模式时需要
     * @return gzip压缩加密后的byte[]
     */
    private static byte[] aesEncryptAsByte(String sSrc, String encodingFormat, String algorithm, String sKey, String ivParameter) {
        try {
            /*algorithm格式必须为"algorithm/mode/padding"或者"algorithm/",意为"算法/加密模式/填充方式*/
            Cipher cipher = Cipher.getInstance(algorithm);
            byte[] raw = sKey.getBytes(encodingFormat);
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            /*使用CBC模式，需要一个向量iv，可增加加密算法的强度*/
            IvParameterSpec iv = new IvParameterSpec(ivParameter.getBytes(encodingFormat));
            if (algorithm.contains("CBC")) {
                cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
            } else {
                cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
            }
            byte[] gzipResult = compressForGzip(sSrc);
            return gzipResult != null ? android.util.Base64.encodeToString(cipher.doFinal(gzipResult), android.util.Base64.DEFAULT).getBytes() : null;
        } catch (NoSuchAlgorithmException
                | NoSuchPaddingException
                | UnsupportedEncodingException
                | InvalidAlgorithmParameterException
                | InvalidKeyException
                | BadPaddingException
                | IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * AES加密
     *
     * @param sSrc           -- 待加密内容
     * @param encodingFormat -- 字符串编码方式
     * @param algorithm      -- 使用的算法 算法/模式/补码方式, 目前支持ECB和CBC模式
     * @param sKey           -- 加密密钥
     * @param ivParameter    -- 偏移量,CBC模式时需要
     * @return gzip压缩加密后的byte[]
     */
    private static String aesEncryptAsString(String sSrc, String encodingFormat, String algorithm, String sKey, String ivParameter) {
        try {
            /*algorithm格式必须为"algorithm/mode/padding"或者"algorithm/",意为"算法/加密模式/填充方式*/
            Cipher cipher = Cipher.getInstance(algorithm);
            byte[] raw = sKey.getBytes(encodingFormat);
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            /*使用CBC模式，需要一个向量iv，可增加加密算法的强度*/
            IvParameterSpec iv = new IvParameterSpec(ivParameter.getBytes(encodingFormat));
            if (algorithm.contains("CBC")) {
                cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
            } else {
                cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
            }
            byte[] gzipResult = compressForGzip(sSrc);
            return gzipResult != null ? android.util.Base64.encodeToString(cipher.doFinal(gzipResult), android.util.Base64.DEFAULT) : null;
        } catch (NoSuchAlgorithmException
                | NoSuchPaddingException
                | UnsupportedEncodingException
                | InvalidAlgorithmParameterException
                | InvalidKeyException
                | BadPaddingException
                | IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * AES解密
     *
     * @param sSrc           -- 待解密Base64字符串
     * @param encodingFormat -- 字符串编码方式
     * @param algorithm      -- 使用的算法 算法/模式/补码方式, 目前支持ECB和CBC模式
     * @param sKey           -- 加密密钥
     * @param ivParameter    -- 偏移量,CBC模式时需要
     * @return 解密后的字符串
     */
    private static String aesDecryptAsString(String sSrc, String encodingFormat, String algorithm, String sKey, String ivParameter) {
        try {
            byte[] raw = sKey.getBytes(encodingFormat);
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            /*algorithm格式必须为"algorithm/mode/padding"或者"algorithm/",意为"算法/加密模式/填充方式*/
            Cipher cipher = Cipher.getInstance(algorithm);
            IvParameterSpec iv = new IvParameterSpec(ivParameter.getBytes(encodingFormat));
            if (algorithm.contains("CBC")) {
                cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
            } else {
                /*ECB模式*/
                cipher.init(Cipher.DECRYPT_MODE, skeySpec);
            }
            /*先用base64解密*/
            byte[] encrypted1 = android.util.Base64.decode(sSrc, android.util.Base64.DEFAULT);
            byte[] original = cipher.doFinal(encrypted1);
            return decompressForGzipAsString(original);
        } catch (NoSuchAlgorithmException
                | NoSuchPaddingException
                | UnsupportedEncodingException
                | InvalidAlgorithmParameterException
                | InvalidKeyException
                | BadPaddingException
                | IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * AES解密
     *
     * @param sSrc           -- 待解密Base64字符串
     * @param encodingFormat -- 字符串编码方式
     * @param algorithm      -- 使用的算法 算法/模式/补码方式, 目前支持ECB和CBC模式
     * @param sKey           -- 加密密钥
     * @param ivParameter    -- 偏移量,CBC模式时需要
     * @return 解密后的字符串
     */
    private static byte[] aesDecryptAsByte(String sSrc, String encodingFormat, String algorithm, String sKey, String ivParameter) {
        try {
            byte[] raw = sKey.getBytes(encodingFormat);
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            /*algorithm格式必须为"algorithm/mode/padding"或者"algorithm/",意为"算法/加密模式/填充方式*/
            Cipher cipher = Cipher.getInstance(algorithm);
            IvParameterSpec iv = new IvParameterSpec(ivParameter.getBytes(encodingFormat));
            if (algorithm.contains("CBC")) {
                cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
            } else {
                /*ECB模式*/
                cipher.init(Cipher.DECRYPT_MODE, skeySpec);
            }
            /*先用base64解密*/
            byte[] encrypted1 = android.util.Base64.decode(sSrc, android.util.Base64.DEFAULT);
            byte[] original = cipher.doFinal(encrypted1);
            return decompressForGzipAsByte(original);
        } catch (NoSuchAlgorithmException
                | NoSuchPaddingException
                | UnsupportedEncodingException
                | InvalidAlgorithmParameterException
                | InvalidKeyException
                | BadPaddingException
                | IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Gzip 压缩数据
     *
     * @param unGzipStr 待压缩的字符串
     */
    private static byte[] compressForGzip(String unGzipStr) {
        if (TextUtils.isEmpty(unGzipStr)) {
            return null;
        }
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            GZIPOutputStream gzip = new GZIPOutputStream(baos);
            gzip.write(unGzipStr.getBytes());
            gzip.close();
            byte[] encode = baos.toByteArray();
            baos.flush();
            baos.close();
            return encode;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Gzip解压数据
     *
     * @param gzipStr 待解压的数据
     * @return 返回解压后的String
     */
    private static String decompressForGzipAsString(byte[] gzipStr) {
        if (gzipStr == null) {
            return null;
        }
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ByteArrayInputStream in = new ByteArrayInputStream(gzipStr);
            GZIPInputStream gzip = new GZIPInputStream(in);
            byte[] buffer = new byte[1024];
            int n = 0;
            while ((n = gzip.read(buffer, 0, buffer.length)) > 0) {
                out.write(buffer, 0, n);
            }
            gzip.close();
            in.close();
            out.close();
            return out.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Gzip解压数据
     *
     * @param gzipStr 待解压的数据
     * @return 返回解压后的byte[]
     */
    private static byte[] decompressForGzipAsByte(byte[] gzipStr) {
        if (gzipStr == null) {
            return null;
        }
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ByteArrayInputStream in = new ByteArrayInputStream(gzipStr);
            GZIPInputStream gzip = new GZIPInputStream(in);
            byte[] buffer = new byte[1024];
            int n = 0;
            while ((n = gzip.read(buffer, 0, buffer.length)) > 0) {
                out.write(buffer, 0, n);
            }
            gzip.close();
            in.close();
            out.close();
            return out.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
