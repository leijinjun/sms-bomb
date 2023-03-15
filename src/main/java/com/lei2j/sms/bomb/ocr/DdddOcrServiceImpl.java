package com.lei2j.sms.bomb.ocr;

import com.lei2j.sms.bomb.util.HttpUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @author leijinjun
 * @date 2023/3/15
 **/
@Service(DdddOcrServiceImpl.PROVIDER_NAME)
public class DdddOcrServiceImpl implements OcrService {

    static final String PROVIDER_NAME = "ddddOcr";

    @Value("${ocr.dddd.url:}")
    private String url;

    @Value("${ocr.dddd.base64.url:}")
    private String base64Url;

    @Override
    public String ocr(OcrRequest ocrRequest) throws IOException {
        if (ocrRequest.getInputStream() != null) {
            if (StringUtils.isBlank(url)) {
                throw new RuntimeException("url is not configured");
            }
            HttpUtils.MultiPart multiPart =
                    new HttpUtils.MultiPart.Builder().setName("image").setFileName("image_" + System.currentTimeMillis()).setContentType("image/jpeg").setInputStream(ocrRequest.getInputStream()).build();
            return HttpUtils.postWithFormData(url, multiPart);
        } else if (ocrRequest.getBase64() != null) {
            if (StringUtils.isBlank(base64Url)) {
                throw new RuntimeException("base64Url is not configured");
            }
            return HttpUtils.postForUpload(base64Url, new ByteArrayInputStream(ocrRequest.getBase64().getBytes(StandardCharsets.US_ASCII)));
        }
        throw new IllegalArgumentException("parameter is empty");
    }

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }
}
