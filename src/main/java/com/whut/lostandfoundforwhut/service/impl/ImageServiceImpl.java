package com.whut.lostandfoundforwhut.service.impl;

import com.whut.lostandfoundforwhut.common.enums.ResponseCode;
import com.whut.lostandfoundforwhut.common.exception.AppException;
import com.whut.lostandfoundforwhut.common.utils.cos.COS;
import com.whut.lostandfoundforwhut.common.utils.cos.ContentRecognizer;
import com.whut.lostandfoundforwhut.common.utils.cos.ContentReviewer;
import com.whut.lostandfoundforwhut.common.utils.image.ImageValidator;
import com.whut.lostandfoundforwhut.mapper.ImageMapper;
import com.whut.lostandfoundforwhut.mapper.ItemImageMapper;
import com.whut.lostandfoundforwhut.mapper.ItemMapper;
import com.whut.lostandfoundforwhut.model.entity.Image;
import com.whut.lostandfoundforwhut.service.IImageService;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import java.util.function.BiFunction;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ImageServiceImpl extends ServiceImpl<ImageMapper, Image> implements IImageService {
    // 图片上传目录
    @Value("${app.upload.image.dir}")
    private String uploadDir;
    // 最大文件大小
    @Value("${app.upload.max-file-size}")
    private String maxFileSize;
    // 最大文件大小（字节）
    private Long maxFileSizeBytes;

    // 自动注入COS客户端
    @Autowired
    private COS cos;
    // 自动注入内容审核器
    @Autowired
    private ContentReviewer contentReviewer;
    // 自动注入内容识别器
    @Autowired
    private ContentRecognizer contentRecognizer;

    @Autowired
    private ItemImageMapper itemImageMapper;
    @Autowired
    private ItemMapper itemMapper;
    @Autowired
    private ImageMapper imageMapper;

    // 最小置信度
    private int CONTENT_RECOGNITION_MIN_CONFIDENCE = 60;

    @PostConstruct
    public void init() {
        this.maxFileSizeBytes = DataSize.parse(maxFileSize).toBytes();
    }

    /**
     * 上传并添加物品图片
     * @param itemId 物品ID
     * @param files 图片文件列表
     * @return 图片实体列表
     */
    @Override
    public List<Image> uploadAndAddItemImages(Long itemId, List<MultipartFile> files) {
        try {
            // 上传图片
            List<Image> images = uploadImages(files);
            if (images.isEmpty()) { return new ArrayList<>(); }
            // 保存图片关联到物品
            List<Long> imageIds = images.stream().map(Image::getId).collect(Collectors.toList());
            boolean success = itemImageMapper.insertItemImages(itemId, imageIds);
            // 检查关联是否成功
            if (!success) {
                throw new AppException(ResponseCode.UN_ERROR.getCode(), "图片失败");
            }
            return images;
        } catch (Exception e) {
            // 执行物理删除物品（绕过逻辑删除）
            try {
                int rowsAffected = itemMapper.deletePhysicalById(itemId);
                log.info("删除物品，itemId: {}, 受影响行数: {}", itemId, rowsAffected);
            } catch (Exception ex) {
                log.error("删除物品失败: {}", ex.getMessage());
            }
            // 处理异常
            if (e instanceof AppException) {
                throw (AppException) e;
            }
            throw new AppException(ResponseCode.UN_ERROR.getCode(), "上传图片失败: " + e.getMessage());
        }
    }

    /**
     * @description 从图片中提取标签
     * @param file 图片文件
     * @return 标签列表
     */
    @Override
    public List<String> getTabs(MultipartFile file) {
        String uniqueFilename = null;

        // 验证文件
        validateImageFile(file);
        try {
            // 上传文件到COS并获取唯一文件名
            uniqueFilename = uploadFileToCOSReturnUniqueFilename(file);
            // 审核图片
            String message = contentReviewer.reviewImageKey(uniqueFilename);
            if (message != null) {
                throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), message);
            }
            // 从COS上的图片提取标签
            List<String> tabs = contentRecognizer.getCategoriesAndNames(uniqueFilename, CONTENT_RECOGNITION_MIN_CONFIDENCE);
            
            return tabs;
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            throw new AppException(ResponseCode.UN_ERROR.getCode(), "提取标签失败: " + e.getMessage());
        } finally {
            // 确保COS上的文件被删除
            if (uniqueFilename != null && cos.hasObject(uniqueFilename)) {
                cos.deleteObject(uniqueFilename);
            }
        }
    }

    /**
     * @description 根据ID获取图片
     * @param id 图片ID
     * @return 图片实体
     */
    @Override
    public Image getImageById(Long id) {
        return imageMapper.selectById(id);
    }

    /**
     * @description 上传图片
     * @param files 图片文件列表
     * @return 图片实体列表
     */
    private List<Image> uploadImages(List<MultipartFile> files) {
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) { continue; }
            validateImageFile(file);
        }

        List<String> uniqueFilenames = new ArrayList<>(); // 存储唯一文件名
        try {
            // 上传所有文件
            for (MultipartFile file : files) {
                if (file == null || file.isEmpty()) { continue; }
                // 上传文件到COS并获取唯一文件名
                String uniqueFilename = uploadFileToCOSReturnUniqueFilename(file);
                // 添加到唯一文件名列表
                uniqueFilenames.add(uniqueFilename);
            }

            // 审核所有图片
            List<String> messages = contentReviewer.batchReviewImageKey(uniqueFilenames);
            String message = joinMessages(messages, "; ", (i, msg) -> "图片" + (i+1) + "审核失败: " + msg);
            if (message != null) {
                throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), message);
            }

            // 创建上传目录
            File uploadDirFile = createDir(uploadDir);
            // 下载并保存图片
            List<Image> images = new ArrayList<>();
            for (String uniqueFilename : uniqueFilenames) {
                File filePath = new File(uploadDirFile, uniqueFilename);
                cos.downloadFile(uniqueFilename, filePath);
                // 创建图片对象
                Image image = new Image();
                image.setUrl(uniqueFilename);
                images.add(image);
            }
            // 批量保存到数据库
            if (!images.isEmpty()) {
                this.saveBatch(images);
            }
            return images;
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            throw new AppException(ResponseCode.UN_ERROR.getCode(), "文件上传失败: " + e.getMessage());
        } finally {
            // 确保COS上的文件被删除
            cos.batchDeleteObject(uniqueFilenames);
        }
    }

    /**
     * 上传文件到COS并返回唯一文件名
     * @param file 上传的文件
     * @return 唯一文件名
     * @throws IOException 如果上传过程中发生IO错误
     */
    private String uploadFileToCOSReturnUniqueFilename(MultipartFile file) throws IOException {
        // 获取文件扩展名
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        // 生成唯一文件名
        String uniqueFilename = java.util.UUID.randomUUID().toString() + extension;
        
        // 先保存到临时文件
        File tempFile = File.createTempFile("temp", extension);
        file.transferTo(tempFile.toPath());
        // 上传到COS
        cos.uploadFile(tempFile, uniqueFilename);
        tempFile.delete();
        // 返回唯一文件名
        return uniqueFilename;
    }

    /**
     * 验证文件
     * @param file 上传的文件
     */
    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "文件不能为空");
        }
        // 验证文件是否为图片
        String errorMessage = ImageValidator.validateImageFile(file);
        if (errorMessage != null) {
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), errorMessage);
        }
        // 验证图片大小
        errorMessage = ImageValidator.validateImageSize(file, maxFileSizeBytes);
        if (errorMessage != null) {
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), errorMessage);
        }
    }

    /**
     * 创建目录文件
     * @param path 目录路径
     * @return 目录文件
     */
    private File createDir(String path) {
        File uploadDirFile = new File(path);
        if (!uploadDirFile.exists()) {
            uploadDirFile.mkdirs();
        }
        return uploadDirFile;
    }

    /**
     * 合并消息列表
     * @param messages 消息列表
     * @param separator 分隔符
     * @param mapper 映射函数
     * @return 合并后的消息字符串, 若消息列表为空则返回null
     */
    private String joinMessages(
        List<String> messages,
        String separator,
        BiFunction<Integer, String, String> mapper
    ) {
        if (messages == null || messages.isEmpty()) { return null; }

        if (separator == null) { separator = ", "; }
        if (mapper == null) { mapper = (index, message) -> message; }

        List<String> messageItems = new ArrayList<>();
        for (int i = 0; i < messages.size(); i++) {
            if (messages.get(i) == null) { continue; }
        
            String messageItem = mapper.apply(i, messages.get(i));
            messageItems.add(messageItem);
        }
        if (messageItems.isEmpty()) { return null; }

        return messageItems.stream().collect(Collectors.joining(separator));
    }
}
