package com.lei2j.sms.bomb.ocr;

import java.io.InputStream;

/**
 * ocr请求参数
 * @author leijinjun
 * @date 2023/3/15
 **/
public class OcrRequest {

    private InputStream inputStream;

    private String base64;

    public InputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public String getBase64() {
        return base64;
    }

    public void setBase64(String base64) {
        this.base64 = base64;
    }
}
