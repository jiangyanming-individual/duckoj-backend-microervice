package com.jiang.duckojbackendfileservice.controller;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.io.FileUtil;
import com.jiang.duckojbackendcommon.common.BaseResponse;
import com.jiang.duckojbackendcommon.common.ErrorCode;
import com.jiang.duckojbackendcommon.common.ResultUtils;
import com.jiang.duckojbackendcommon.exception.BusinessException;
import com.jiang.duckojbackendfileservice.service.FileService;
import com.jiang.duckojbackendmodel.model.entity.User;
import com.jiang.duckojbackendmodel.model.enums.FileUploadBizEnum;
import com.jiang.duckojbackendserviceclient.service.UserOpenFeignClient;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

@RestController
public class FileController {

    @Resource
    private FileService ossService;

    @Resource
    private UserOpenFeignClient userOpenFeignClient;

    /**
     * 上传头像
     *
     * @param file
     * @return
     */
    @ApiOperation(value = "文件上传")
    @PostMapping("/upload")
    public BaseResponse<String> uploadOssFile(@RequestPart("file") MultipartFile file, HttpServletRequest request) {

//        String biz = uploadFileRequest.getBiz();
//        FileUploadBizEnum fileUploadBizEnum = FileUploadBizEnum.getEnumByValue(biz);
//        if (fileUploadBizEnum == null) {
//            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数错误");
//        }
        //校验文件
        validFile(file);
        //获取上传的文件
        if (file.isEmpty()) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "上传文件为空");
        }
        User loginUser = userOpenFeignClient.getLoginUser(request);
        // 把文件按照日期分类，获取当前日期
        String datePath = new DateTime().toString("yyyy-MM-dd");
        // 获取文件名称
        String originalFileName = file.getOriginalFilename();
        // 拼接日期、用户id、文件路径
        String fileName = String.format("%s/%s/%s", datePath, loginUser.getId(), originalFileName);
        //返回上传到oss的路径
        String url = ossService.uploadFileAvatar(file, fileName);
        //返回r对象
        return ResultUtils.success(url);
    }

    /**
     * 校验文件
     *
     * @param multipartFile
     * @param fileUploadBizEnum 业务类型
     */
    private void validFile(MultipartFile multipartFile, FileUploadBizEnum fileUploadBizEnum) {
        // 文件大小
        long fileSize = multipartFile.getSize();
        // 文件后缀
        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        final long ONE_M = 1024 * 1024L;
        if (FileUploadBizEnum.USER_AVATAR.equals(fileUploadBizEnum)) {
            //校验文件大小
            if (fileSize > ONE_M) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小不能超过 1M");
            }
            //校验文件后缀
            if (!Arrays.asList("jpeg", "jpg", "svg", "png", "webp").contains(fileSuffix)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件类型错误");
            }
        }
    }

    private void validFile(MultipartFile multipartFile) {
        // 文件大小
        long fileSize = multipartFile.getSize();
        // 文件后缀
        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        final long ONE_M = 1024 * 1024L;
        //校验文件大小
        if (fileSize > ONE_M) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小不能超过 1M");
        }
        //校验文件后缀
        if (!Arrays.asList("jpeg", "jpg", "svg", "png", "webp").contains(fileSuffix)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件类型错误");
        }

    }
}