package com.lei2j.sms.bomb.script

import com.lei2j.sms.bomb.ocr.OcrRequest
import com.lei2j.sms.bomb.ocr.OcrService
import com.lei2j.sms.bomb.service.impl.ScriptThreadContext
import com.lei2j.sms.bomb.util.SpringApplicationUtils

class SmsCommonScript implements SmsScript{

    protected String identifyImgCaptcha(InputStream imgStream) {
        def ocrService = getOcrService()
        OcrRequest ocrRequest = new OcrRequest()
        ocrRequest.setInputStream(imgStream)
        def text = ocrService.ocr(ocrRequest)
        return text ? text : ''
    }

    protected String identifyImgCaptcha(File imgFile) {
        def inputStream = new FileInputStream(imgFile)
        return identifyImgCaptcha(inputStream)
    }

    protected String identifyImgCaptcha(URL imgUrl) {
        return identifyImgCaptcha(imgUrl.openStream())
    }

    protected String identifyImgCaptcha(String base64) {
        def ocrService = getOcrService()
        OcrRequest ocrRequest = new OcrRequest()
        ocrRequest.setBase64(base64)
        def text = ocrService.ocr(ocrRequest)
        return text ? text : ''
    }

    private static OcrService getOcrService() {
        def scriptContext = ScriptThreadContext.get()
        SpringApplicationUtils applicationUtils = scriptContext.getSpringApplicationUtils()
        def providerName = applicationUtils.getProperty("ocr.provider.name")
        def ocrService = applicationUtils.getBean(providerName, OcrService.class)
        return ocrService
    }
}

