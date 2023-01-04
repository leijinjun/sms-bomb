package com.lei2j.sms.bomb.script

import com.lei2j.sms.bomb.util.HttpUtils

class SmsCommonScript implements SmsScript{

    String identifyUrl = "http://192.168.100.1:9898/ocr/file"

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

}

