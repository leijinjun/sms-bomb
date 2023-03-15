package com.lei2j.sms.bomb.ocr;

import java.io.IOException;

/**
 * 图片识别服务
 * @author leijinjun
 * @date 2023/3/15
 **/
public interface OcrService {

    /**
     * ocr识别服务
     * @param ocrRequest
     * @return
     */
    String ocr(OcrRequest ocrRequest) throws IOException;

    /**
     * 获取服务提供商名称
     * @return
     */
    String getProviderName();
}
