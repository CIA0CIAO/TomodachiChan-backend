//package com.momo.companion.util;
//
//import com.aliyun.oss.OSS;
//import com.momo.companion.common.exception.BusinessException;
//import com.momo.companion.controller.response.Code;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.util.UUID;
//
///**
// * 阿里云 OSS 工具类
// */
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class OSSUtil {
//    private final OSS ossClient;
//
//    @Value("${momo-companion.aliyun.oss.bucket-name}")
//    private String bucketName;
//    @Value("${momo-companion.aliyun.oss.url-prefix}")
//    private String urlPrefix;
//
//    /**
//     * 文件上传
//     *
//     * @param file 待上传文件
//     * @return 文件 URL
//     */
//    public String upload(MultipartFile file) {
//        return upload(file, null);
//    }
//
//    /**
//     * 文件上传
//     *
//     * @param file   待上传文件
//     * @param folder 存储文件夹
//     * @return 文件 URL
//     */
//    public String upload(MultipartFile file, String folder) {
//        try {
//            String filename = file.getOriginalFilename();
//            if (filename == null)
//                throw new BusinessException(Code.NULL_ERROR, "上传操作异常");
//            String suffix = filename.substring(filename.lastIndexOf(".") + 1);
//            // 生成随机文件名并哈希打散
//            String newFilename = UUID.randomUUID().toString();
//            int hashCode = newFilename.hashCode();
//            int dirOne = hashCode & 0xF;
//            int dirTwo = (hashCode >> 4) & 0xF;
//            // 拼接文件夹路径及文件 URL
//            String dir = folder != null ?
//                         "%s/%s/%s".formatted(folder, dirOne, dirTwo) :
//                         "%s/%s".formatted(dirOne, dirTwo);
//            String fileUrl = "%s/%s.%s".formatted(dir, newFilename, suffix);
//            ossClient.putObject(bucketName, fileUrl, file.getInputStream());
//            return urlPrefix + fileUrl;
//        } catch (Exception e) {
//            log.error("上传文件失败: {}", e.getMessage(), e);
//            throw new BusinessException(Code.SYSTEM_ERROR, "上传文件失败, 请稍后再试");
//        }
//    }
//
//    /**
//     * 文件删除
//     *
//     * @param url 待删除文件 URL
//     */
//    public void delete(String url) {
//        try {
//            if (!url.startsWith(urlPrefix))
//                throw new BusinessException(Code.PARAMS_ERROR, "删除操作异常");
//            String filename = url.substring(urlPrefix.length());
//            ossClient.deleteObject(bucketName, filename);
//        } catch (Exception e) {
//            log.error("删除文件失败: {}", e.getMessage(), e);
//            throw new BusinessException(Code.SYSTEM_ERROR, "删除文件失败, 请稍后再试");
//        }
//    }
//}
