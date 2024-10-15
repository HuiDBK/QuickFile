package com.example.webhello.controller;

import com.example.webhello.service.DocumentConvertService;
import com.example.webhello.utils.FileUtils;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/convert/")
public class FileConvertController {

    private static final Logger log = LoggerFactory.getLogger(FileConvertController.class);
    @Autowired
    private DocumentConvertService documentConvertService;

    @RequestMapping("/docx/to/pdf")
    public String docxToPdf() {
        boolean result = documentConvertService.docxToPdf();
        return result ? "success" : "fail";
    }

    @PostMapping("/upload")
    public ResponseEntity<byte[]> uploadAndConvert(@RequestParam("file") MultipartFile file,
                                                   @RequestParam("format") String format,
                                                   HttpServletResponse response) {
        try {
            byte[] result = documentConvertService.convertFile(file, format);
            String baseName = FileUtils.getBaseName(file.getOriginalFilename());
            String saveFilename = baseName + "." + format; // 根据格式生成保存的文件名

            // 创建响应头
            HttpHeaders headers = getFileResponseHeader(saveFilename);
            return new ResponseEntity<>(result, headers, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            // 捕获格式一致的异常，返回400状态码和错误信息
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage().getBytes());
        } catch (Exception e) {
            // 处理其他异常
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("文件转换失败".getBytes());
        }
    }

    private static HttpHeaders getFileResponseHeader(String saveFilename, MediaType mediaType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(mediaType);
        headers.setContentDispositionFormData("attachment", saveFilename);
        return headers;
    }

    private static HttpHeaders getFileResponseHeader(String saveFilename) {
        return getFileResponseHeader(saveFilename, MediaType.APPLICATION_OCTET_STREAM);
    }

    private static void setFileResponseHeader(HttpServletResponse response, String saveFilename) {
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=" + saveFilename);
    }

    @RequestMapping("/spring")
    public String spring() {
        return "Hello Spring";
    }
}
