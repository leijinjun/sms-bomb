package com.lei2j.sms.bomb.script

import com.lei2j.sms.bomb.util.HttpUtils

import java.nio.charset.StandardCharsets

class SmsCommonScript implements SmsScript{

    String identifyUrl = "http://192.168.100.1:9898/ocr/file"

    String identifyBase64Url = 'http://192.168.100.1:9898/ocr/b64/text'

    protected String identifyImgCaptcha(InputStream imgStream) {
        def multiPart = new HttpUtils.MultiPart.Builder().setName("image").setFileName("image_" + System.currentTimeMillis()).setContentType("image/jpeg").setInputStream(imgStream).build()
        def data = HttpUtils.postWithFormData(identifyUrl, multiPart)
        return data ? data : ''
    }

    protected String identifyImgCaptcha(File imgFile) {
        def inputStream = new FileInputStream(imgFile)
        return identifyImgCaptcha(inputStream)
    }

    protected String identifyImgCaptcha(URL imgUrl) {
        return identifyImgCaptcha(imgUrl.openStream())
    }

    protected String identifyImgCaptcha(String base64) {
        return HttpUtils.postForUpload(identifyBase64Url, new ByteArrayInputStream(base64.getBytes(StandardCharsets.US_ASCII)))
    }

}

