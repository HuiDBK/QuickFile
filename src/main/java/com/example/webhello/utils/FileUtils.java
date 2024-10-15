package com.example.webhello.utils;

public class FileUtils {

    /**
     * 从给定的文件名中提取基本名称（不包括扩展名）。
     *
     * @param originalFilename 原始文件名
     * @return 文件的基本名称，或 null 如果原始文件名为 null
     */
    public static String getBaseName(String originalFilename) {
        if (originalFilename != null && originalFilename.lastIndexOf('.') > 0) {
            return originalFilename.substring(0, originalFilename.lastIndexOf('.'));
        }
        return originalFilename; // 如果没有扩展名，返回原始文件名
    }

    /**
     * 从给定的文件名中提取文件扩展名。
     *
     * @param originalFilename 原始文件名
     * @return 文件的扩展名，或 null 如果原始文件名为 null
     */
    public static String getFileExtension(String originalFilename) {
        if (originalFilename != null && originalFilename.lastIndexOf('.') > 0) {
            return originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase();
        }
        return null; // 没有扩展名时返回 null
    }

}