package com.ai.algorithmqa.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

public interface FileStorageService {

    /**
     * 上传文件到 MinIO
     * 
     * @param file       文件
     * @param objectName 对象名称（路径）
     * @return 文件访问 URL
     */
    String uploadFile(MultipartFile file, String objectName) throws Exception;

    /**
     * 删除文件
     * 
     * @param objectName 对象名称
     */
    void deleteFile(String objectName) throws Exception;

    /**
     * 获取文件流
     * 
     * @param objectName 对象名称
     * @return 文件输入流
     */
    InputStream getFile(String objectName) throws Exception;

    /**
     * 获取文件访问 URL
     * 
     * @param objectName 对象名称
     * @return 访问 URL
     */
    String getFileUrl(String objectName);
}
