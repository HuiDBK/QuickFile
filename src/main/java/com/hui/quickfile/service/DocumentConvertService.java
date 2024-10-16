package com.hui.quickfile.service;

import com.aspose.words.Document;
import com.aspose.words.SaveFormat;
import com.hui.quickfile.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class DocumentConvertService {
    private static final Logger log = LoggerFactory.getLogger(DocumentConvertService.class);

    private final ResourceLoader resourceLoader;

    // 使用构造函数注入 ResourceLoader
    public DocumentConvertService(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    private static final Map<String, Integer> FORMAT_MAP = new HashMap<>() {{
        put("pdf", SaveFormat.PDF);
        put("docx", SaveFormat.DOCX);
        put("doc", SaveFormat.DOC);
        put("txt", SaveFormat.TEXT);
        put("jpg", SaveFormat.JPEG);
        put("png", SaveFormat.PNG);
        put("tiff", SaveFormat.TIFF);
        put("md", SaveFormat.MARKDOWN);
        put("html", SaveFormat.HTML);
    }};

    private static final long MAX_FILE_SIZE = 100 * 1024 * 1024; // 100MB

    public byte[] convertFile(MultipartFile file, String format) throws Exception {
        String srcFilename = file.getOriginalFilename();
        String convertedFilename = FileUtils.getBaseName(srcFilename) + "." + format;
        log.info("文件转换 {} ==> {}", srcFilename, convertedFilename);

        // 校验文件上传信息
        verifyFileInfo(format, file);

        try (InputStream docStream = file.getInputStream();
             ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {

            // 加载文档
            Document doc = new Document(docStream);

            // 根据指定的格式保存为字节流
            Integer saveFormat = FORMAT_MAP.get(format.toLowerCase());
            if (saveFormat == null) {
                throw new UnsupportedOperationException("Unsupported format: " + format);
            }
            doc.save(byteArrayOutputStream, saveFormat);
            log.info("{} ==> {}, 文件转换成功，返回字节流", srcFilename, convertedFilename);
            return byteArrayOutputStream.toByteArray();

        } catch (Exception e) {
            log.error("文件转换失败：{}", e.getMessage());
            throw e;  // 抛出异常供控制器捕获
        }
    }

    private static void verifyFileInfo(String format, MultipartFile file) {

        String fileExtension = FileUtils.getFileExtension(file.getOriginalFilename());
        assert fileExtension != null;
        if (fileExtension.equals(format.toLowerCase())) {
            String tip = "上传文件的格式与目标转换格式一致，无需转换！";
            log.info(tip);
            throw new IllegalArgumentException(tip);
        }

        if (fileExtension.equals("pdf")) {
            String tip = "Java API 暂不支持PDF转换为其他格式！";
            log.info(tip);
            throw new IllegalArgumentException(tip);
        }

        // 文件大小校验
        if (file.getSize() > MAX_FILE_SIZE) {
            String tip = "文件大小超过限制，最大支持100MB！";
            log.info(tip);
            throw new IllegalArgumentException(tip);
        }
    }

    public boolean docxToPdf() {
        try {
            // 加载位于 resources/static 下的 .docx 文件
            Resource resource = resourceLoader.getResource("classpath:static/input_01.docx");

            // 使用 InputStream 读取文件
            InputStream docStream = resource.getInputStream();

            // 加载文档
            Document doc = new Document(docStream);

            // 获取项目根目录
            String projectRootDir = System.getProperty("user.dir");

            // 输出目录路径，位于项目根目录的 output 文件夹下
            String outputDir = projectRootDir + File.separator + "output";

            // 创建文件夹（如果不存在）
            File directory = new File(outputDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // 保存文档
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            String saveFilename = outputDir + File.separator + "output_" + sdf.format(new Date()) + ".pdf";
            doc.save(saveFilename);
            log.info("convert successful {}", saveFilename);
        } catch (Exception e) {
            log.error("转换文件失败：{}", e.getMessage());
            return false;
        }
        return true;
    }
}
