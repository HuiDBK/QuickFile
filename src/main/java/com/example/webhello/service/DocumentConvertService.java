package com.example.webhello.service;

import com.aspose.words.Document;
import com.aspose.words.SaveFormat;
import com.example.webhello.utils.FileUtils;
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

@Service
public class DocumentConvertService {
    private static final Logger log = LoggerFactory.getLogger(DocumentConvertService.class);

    private final ResourceLoader resourceLoader;

    // 使用构造函数注入 ResourceLoader
    public DocumentConvertService(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
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

    public byte[] convertFile(MultipartFile file, String format) throws Exception {
        String srcFilename = file.getOriginalFilename();
        String fileExtension = FileUtils.getFileExtension(srcFilename);
        String convertedFilename = FileUtils.getBaseName(srcFilename) + "." + format;
        log.info("文件转换 {} ==> {}", srcFilename, convertedFilename);

        // 校验：如果上传文件的格式与目标格式一致，则返回提示
        assert fileExtension != null;
        if (fileExtension.equals(format.toLowerCase())) {
            String tip = "上传文件的格式与目标转换格式一致，无需转换！";
            log.info(tip);
            throw new IllegalArgumentException(tip);
        }

        try (InputStream docStream = file.getInputStream();
             ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {

            // 加载文档
            Document doc = new Document(docStream);

            // 根据指定的格式保存为字节流
            if ("pdf".equalsIgnoreCase(format)) {
                doc.save(byteArrayOutputStream, SaveFormat.PDF);  // 保存为 PDF
            } else if ("docx".equalsIgnoreCase(format)) {
                doc.save(byteArrayOutputStream, SaveFormat.DOCX); // 保存为 DOCX
            } else if ("doc".equalsIgnoreCase(format)) {
                doc.save(byteArrayOutputStream, SaveFormat.DOC); // 保存为 DOCX
            } else if ("txt".equalsIgnoreCase(format)) {
                doc.save(byteArrayOutputStream, SaveFormat.TEXT); // 保存为 Text
            } else if ("jpg".equalsIgnoreCase(format)) {
                    doc.save(byteArrayOutputStream, SaveFormat.JPEG); // 保存为 JPEG
            } else {
                throw new UnsupportedOperationException("Unsupported format: " + format);
            }

            log.info("{} ==> {}, 文件转换成功，返回字节流", srcFilename, convertedFilename);
            return byteArrayOutputStream.toByteArray();
        } catch (Exception e) {
            log.error("文件转换失败：{}", e.getMessage());
            throw e;  // 抛出异常供控制器捕获
        }
    }
}
