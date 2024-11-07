package com.hui.quickfile.service;

import com.aspose.pdf.*;
import com.aspose.pdf.devices.*;
import com.hui.quickfile.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;


@Service
public class PDFConvertService extends DocumentConvertService {
    private static final Logger log = LoggerFactory.getLogger(PDFConvertService.class);

    public PDFConvertService(ResourceLoader resourceLoader) {
        super(resourceLoader);
    }

    // 存储文件扩展名与对应的 ImageDevice 实例
    private static final Map<String, Device> IMAGE_DEVICE_MAP = new HashMap<>() {{
        put("jpg", new JpegDevice());
        put("png", new PngDevice());
        put("tiff", new TiffDevice());
    }};

    private static final Map<String, SaveFormat> FORMAT_MAP = new HashMap<>() {{
        put("docx", SaveFormat.DocX);
        put("doc", SaveFormat.Doc);
        put("html", SaveFormat.Html);
    }};


    public byte[] convertFile(MultipartFile file, String format) throws Exception {
        String srcFilename = file.getOriginalFilename();
        String convertedFilename = FileUtils.getBaseName(srcFilename) + "." + format;

        try (InputStream docStream = file.getInputStream();
             ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {

            // 设置许可证
            Resource licenseResource = resourceLoader.getResource("classpath:static/license.xml");
            License license = new License();
            license.setLicense(licenseResource.getInputStream());

            // 加载文档
            Document pdfDocument = new Document(docStream);

            if ("txt".equalsIgnoreCase(format)) {
                // pdf to txt 处理
                // 创建 TextAbsorber 用于提取文本
                TextAbsorber textAbsorber = new TextAbsorber();
                pdfDocument.getPages().accept(textAbsorber);

                // 获取提取的文本
                String extractedText = textAbsorber.getText();
                byteArrayOutputStream.write(extractedText.getBytes());
                return byteArrayOutputStream.toByteArray();
            }

            // 根据指定的格式保存为字节流
            SaveFormat saveFormat = FORMAT_MAP.get(format.toLowerCase());
            Device device = IMAGE_DEVICE_MAP.get(format.toLowerCase());

            if (device == null) {
                if (saveFormat == SaveFormat.Html) {
                    HtmlSaveOptions saveOptions = new HtmlSaveOptions();
                    // 将所有资源嵌入 HTML 中
                    saveOptions.setPartsEmbeddingMode(HtmlSaveOptions.PartsEmbeddingModes.EmbedAllIntoHtml);
                    // 图像以嵌入式 PNG 保存
                    saveOptions.setRasterImagesSavingMode(HtmlSaveOptions.RasterImagesSavingModes.AsEmbeddedPartsOfPngPageBackground);
                    saveOptions.setFixedLayout(true);  // 保持页面布局
                    pdfDocument.save(byteArrayOutputStream, saveOptions);
                } else {
                    pdfDocument.save(byteArrayOutputStream, saveFormat);
                }
            } else {
                // 图片都只转首页
                if (device instanceof ImageDevice imageDevice) {
                    imageDevice.process(pdfDocument.getPages().get_Item(1), byteArrayOutputStream);
                } else if (device instanceof TiffDevice tiffDevice) {
                    tiffDevice.process(pdfDocument, 1, 1, byteArrayOutputStream);
                }
            }
            log.info("{} ==> {}, 文件转换成功，返回字节流", srcFilename, convertedFilename);
            return byteArrayOutputStream.toByteArray();

        } catch (Exception e) {
            log.error("文件转换失败：{}", e.getMessage());
            throw e;  // 抛出异常供控制器捕获
        }
    }
}
