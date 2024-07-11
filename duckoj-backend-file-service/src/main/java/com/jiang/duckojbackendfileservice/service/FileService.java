package com.jiang.duckojbackendfileservice.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    /**
     * 上传头像到OSS
     *
     * @param file
     * @return
     */
    String uploadFileAvatar(MultipartFile file,String fileName);
}