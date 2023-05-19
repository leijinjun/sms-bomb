package com.lei2j.sms.bomb.script

import com.alibaba.fastjson.JSONObject
import com.lei2j.sms.bomb.entity.SmsUrlConfig
import com.lei2j.sms.bomb.service.impl.ScriptContext
import com.lei2j.util.Base64Util
import com.sun.imageio.plugins.gif.GIFImageReader
import com.sun.imageio.plugins.gif.GIFImageReaderSpi
import com.sun.imageio.plugins.gif.GIFImageWriter
import com.sun.imageio.plugins.gif.GIFImageWriterSpi
import groovy.json.JsonSlurper
import groovy.text.GStringTemplateEngine
import groovy.xml.XmlSlurper
import org.apache.commons.lang3.RandomStringUtils
import org.apache.commons.lang3.StringUtils
import org.apache.http.HeaderElement
import org.apache.http.message.BasicHeaderElement

import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.imageio.spi.ImageReaderSpi
import javax.imageio.spi.ImageWriterSpi
import javax.imageio.stream.FileImageInputStream
import javax.imageio.stream.FileImageOutputStream
import javax.imageio.stream.ImageOutputStream
import javax.imageio.stream.MemoryCacheImageInputStream
import javax.imageio.stream.MemoryCacheImageOutputStream
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.security.Key
import java.security.KeyFactory
import java.security.spec.X509EncodedKeySpec
import java.util.function.BiFunction

trait SmsScript {

    GStringTemplateEngine stringTemplateEngine = new GStringTemplateEngine()

    enum ResponseTypeEnum {
        JSON,
        XML,
        JSONP,
        TEXT;
    }

    void preProcess(ScriptContext scriptContext) {
        SmsUrlConfig smsUrlConfig = scriptContext.getSmsUrlConfig()
        Map<String, Object> paramsMap = scriptContext.getParamsMap()
        Map<String, String> headerMap = scriptContext.getHeaderMap()
        List<String> headerList = smsUrlConfig.getHeaderList()
        headerList.forEach((headerPair) -> {
            String[] split = headerPair.split(":", 2)
            String name = split[0]
            String value = split[1]
            if (StringUtils.isNotBlank(value)) {
                if ("cookie".equalsIgnoreCase(name)) {
                    BiFunction<String, String, String> f = (key, val) -> { (val + (val.endsWith(";") ? "" : ";") + value) }
                    String o1 = headerMap.computeIfPresent("Cookie", f)
                    String o2 = headerMap.computeIfPresent("cookie", f)
                    if (o1 == null && o2 == null) {
                        headerMap.put("Cookie", value)
                    }
                } else {
                    headerMap.put(name, value.trim())
                }
            }
        })
        if (!headerMap.containsKey("Accept") && !headerMap.containsKey("accept")) {
            headerMap.put("Accept", "*/*")
        }
        if (!headerMap.containsKey("Referer") && !headerMap.containsKey("referer")) {
            headerMap.put("Referer", scriptContext.getSmsUrlConfig().getWebsite())
        }
        //解析固定请求参数
        String bindingParams = smsUrlConfig.getBindingParams()
        if (StringUtils.isNotBlank(bindingParams)) {
            JSONObject object = JSONObject.parseObject(bindingParams)
            for (Map.Entry<String, Object> entry : object.entrySet()) {
                paramsMap.put(entry.getKey(), entry.getValue())
            }
        }
        if (ResponseTypeEnum.valueOf(scriptContext.getSmsUrlConfig().responseType.toUpperCase()) == ResponseTypeEnum.JSONP) {
            paramsMap.put(getJsonpRequestKey(), getJsonpResponseValue())
        }
        parseExpressionScriptContext(scriptContext)
    }

    /**
     * 解析表达式
     * @param scriptContext
     */
    void parseExpressionScriptContext(ScriptContext scriptContext){
        scriptContext.getQueryMap().forEach((k, v) -> {
            def template = stringTemplateEngine.createTemplate(v.toString())
            scriptContext.getQueryMap().put(k, template.make().toString())
        })
        scriptContext.getHeaderMap().forEach((k, v) -> {
            def template = stringTemplateEngine.createTemplate(v.toString())
            scriptContext.getHeaderMap().put(k, template.make().toString())
        })
        scriptContext.getParamsMap().forEach((k, v) -> {
            def template = stringTemplateEngine.createTemplate(v.toString())
            scriptContext.getParamsMap().put(k, template.make().toString())
        })
        scriptContext.getSmsUrlConfig().setSmsUrl(stringTemplateEngine.createTemplate(
                scriptContext.getSmsUrlConfig().getSmsUrl()).make(scriptContext.getParamsMap()).toString())
    }

    Boolean postProcess(ScriptContext scriptContext) {
        SmsUrlConfig smsUrlConfig = scriptContext.getSmsUrlConfig()
        String response = scriptContext.getResponse()
        if (response == null || response.isEmpty()) {
            return Boolean.TRUE
        }
        def successCode = smsUrlConfig.getSuccessCode()
        if (successCode == null || successCode.isEmpty()) {
            return Boolean.FALSE
        }
        def responseType = smsUrlConfig.getResponseType()
        try {
            return parseResponse(scriptContext, smsUrlConfig.getSuccessCode(), response, ResponseTypeEnum.valueOf(responseType.toUpperCase()))
        } catch (Exception e) {
            e.printStackTrace()
            return false
        }
    }

    Boolean parseResponse(ScriptContext scriptContext, String resultFormatCode, String response, ResponseTypeEnum responseType) {
        def parseObject = null
        if (responseType == ResponseTypeEnum.XML) {
            parseObject = new XmlSlurper().parseText(response)
        } else if (responseType == ResponseTypeEnum.JSON) {
            parseObject = new JsonSlurper().parseText(response)
        } else if (responseType == ResponseTypeEnum.TEXT) {
            return parseText(scriptContext, response, resultFormatCode)
        } else if (responseType == ResponseTypeEnum.JSONP) {
            return parseJsonp(scriptContext, response, resultFormatCode)
        }
        if (parseObject) {
            def sp = resultFormatCode.split(",", 2)
            def keyPair = sp[0].trim()
            def valuePair = sp[1].trim()
            def code = keyPair.split("=", 2)[1]
            def value = valuePair.split("=", 2)[1]
            def split = code.split("\\.")
            if (Objects.nonNull(parseObject)) {
                for (int i = 0; i < split.length; i++) {
                    parseObject = parseObject.(split[i].trim())
                    if (parseObject == null) {
                        return false
                    }
                }
                parseObject = parseObject.toString()
                return parseObject == value
            }
        }
        return false
    }

    Boolean retry(ScriptContext scriptContext) {
        SmsUrlConfig smsUrlConfig = scriptContext.getSmsUrlConfig()
        String response = scriptContext.getResponse()
        if (smsUrlConfig.getEndCode()) {
            String[] split = smsUrlConfig.getEndCode().split('[\r\n]')
            for (String sp : split) {
                if (parseResponse(scriptContext, sp.trim(), response, ResponseTypeEnum.valueOf(smsUrlConfig.getResponseType().toUpperCase()))) {
                    return true
                }
            }
        }
        false
    }

    /**
     *  由子类重写
     * @param scriptContext
     * @return
     */
    Boolean parseText(ScriptContext scriptContext, String response, String resultFormatCode) {
        if (!response) {
            return true
        }
        response == resultFormatCode
    }

    Boolean parseJsonp(ScriptContext scriptContext, String response, String resultFormatCode){
        def callbackMethod = scriptContext.getParamsMap().get(getJsonpRequestKey()).toString()
        def regex = ~/${callbackMethod}\((.*)\)/
        def matcher = regex.matcher(response)
        assert matcher.find()
        parseResponse(scriptContext, resultFormatCode, matcher.group(1), ResponseTypeEnum.JSON)
    }


    void setCookie(String key, String value, Map<String, String> headerMap) {
        Map<String, List<HeaderElement[]>> responseHeaderMap = new HashMap<>()
        def list = new ArrayList<HeaderElement[]>()
        list.add(new BasicHeaderElement[]{new BasicHeaderElement(key, value)})
        responseHeaderMap.put('Set-Cookie', list)
        setCookie(responseHeaderMap, headerMap)
    }

    void setCookie(Map<String, List<HeaderElement[]>> responseHeaderMap, Map<String, String> headerMap) {
        setCookie(responseHeaderMap, headerMap, false)
    }

    void setCookie(Map<String, List<HeaderElement[]>> responseHeaderMap, Map<String, String> headerMap, boolean ifPresentReserve) {
        def headerElements = responseHeaderMap.get('Set-Cookie')?responseHeaderMap.get('Set-Cookie'):responseHeaderMap.get('set-cookie')
        if (headerElements) {
            headerElements.stream().forEach(c -> {
                String cookie = headerMap.get('Cookie')
                if (cookie) {
                    List<String> list = new ArrayList<>(Arrays.asList(cookie.split(';')))
                    boolean exist = list.stream().anyMatch(p -> {
                        String[] itemSplit = p.split('=', 2)
                        return Objects.equals(itemSplit[0].trim(), c[0].getName())
                    })
                    if (!(exist && ifPresentReserve)) {
                        list.removeIf(p -> p.trim().startsWith(c[0].getName().trim()))
                        list.add(c[0].getName() + '=' + c[0].getValue())
                    }
                    headerMap.put('Cookie', String.join(';', list))
                } else {
                    headerMap.put('Cookie', String.format('%s=%s', c[0].getName(), c[0].getValue()))
                }
            })
        }
    }

    String getCookieValue(Map<String, String> headerMap, String key) {
        def cookie = headerMap.get('Cookie')
        if (!cookie) {
            cookie = headerMap.get('cookie')
        }
        if (cookie) {
            String[] split = cookie.split(";")
            def list = split.toList()
            for (String str : list) {
                def split1 = str.split("=")
                if (split1[0].trim() == key) {
                    return split1.length > 1 ? split1[1].trim() : null
                }
            }
        }
        return null
    }

    /**
     *  使用RSA加密
     * @param data
     * @param publicKeyBase64
     * @return
     */
    String encryptByRsa(String data, String publicKeyBase64) {
        def keySpec = new X509EncodedKeySpec(publicKeyBase64.decodeBase64())
        def keyFactory = KeyFactory.getInstance("RSA")
        def publicKey = keyFactory.generatePublic(keySpec)
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm())
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)
        return cipher.doFinal(data.getBytes(StandardCharsets.UTF_8)).encodeBase64().toString()
    }

    /**
     * AES加密
     * @param text
     * @param encryptionKey
     * @return 返回加密后的16进制字符串
     */
    String encryptAes(String text,String encryptionKey) {
        Key aesKey = new SecretKeySpec(encryptionKey.getBytes("UTF-8"), "AES")
        if (!text) return text
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, aesKey)
        String encryptedStr = cipher.doFinal(text.getBytes("UTF-8")).encodeHex()
        return encryptedStr
    }

    /**
     * AES加密
     * @param text
     * @param encryptionKey
     * @return 返回加密后的base64字符串
     */
    String encryptAesBase64(String text,String encryptionKey) {
        Key aesKey = new SecretKeySpec(encryptionKey.getBytes("UTF-8"), "AES")
        if (!text) return text
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, aesKey)
        String encryptedStr = cipher.doFinal(text.getBytes("UTF-8")).encodeBase64()
        return encryptedStr
    }

    /**
     * AES加密
     * @param text
     * @param encryptionKey
     * @return
     */
    String encryptAes(String text, String encryptionKey, String iv) {
        Key aesKey = new SecretKeySpec(encryptionKey.getBytes("UTF-8"), "AES")
        if (!text) return text
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, aesKey, new IvParameterSpec(iv.getBytes()))
        String encryptedStr = cipher.doFinal(text.getBytes("UTF-8")).encodeBase64()
        return encryptedStr
    }

    String getJsonpRequestKey(){
        'callback'
    }

    String getJsonpResponseValue(){
        'jQuery' + RandomStringUtils.randomNumeric(19) + "_" + System.currentTimeMillis()
    }

    File getGifOneFrame(InputStream inputStream,String targetName, int frame) {
        String path = System.getenv("tmp") +File.separator+ "${targetName}.gif"
        Files.copy(inputStream, Paths.get(path), StandardCopyOption.REPLACE_EXISTING)
        ImageReaderSpi readerSpi = new GIFImageReaderSpi()
        GIFImageReader gifReader = (GIFImageReader) readerSpi.createReaderInstance()
        def imageInputStream = new FileImageInputStream(new File(path))
        gifReader.setInput(imageInputStream)
        int num = gifReader.getNumImages(true)
        if (num > frame) {
            for (int i = 0; i < num; i++) {
                if (frame == i) {
                    ImageWriterSpi writerSpi = new GIFImageWriterSpi()
                    GIFImageWriter writer = (GIFImageWriter) writerSpi.createWriterInstance()
                    def file = new File(System.getenv("tmp") + File.separator + "${targetName}.jpg")
                    FileImageOutputStream out = new FileImageOutputStream(file)
                    writer.setOutput(out)
                    // 读取读取帧的图片
                    writer.write(gifReader.read(i))
                    out.flush()
                    out.close()
                    imageInputStream.close()
                    return file
                }
            }
        } else {
            imageInputStream.close()
        }
        return null
    }

/**
 *
 * @param base64 图片base64
 * @param frame 获取第几帧，等于-1时，返回所有帧
 * @return 返回帧图片的base64格式集合
 */
    List<String> getGifFrame(String base64, int frame) {
        List<String> result = new ArrayList<>()
        ImageReaderSpi readerSpi = new GIFImageReaderSpi()
        GIFImageReader gifReader = (GIFImageReader) readerSpi.createReaderInstance()
        gifReader.setInput(new MemoryCacheImageInputStream(new ByteArrayInputStream(Base64Util.decode(base64))))
        int num = gifReader.getNumImages(true)
        if (num > frame) {
            for (int i = 0; i < num; i++) {
                ImageWriterSpi writerSpi = new GIFImageWriterSpi()
                GIFImageWriter writer = (GIFImageWriter) writerSpi.createWriterInstance()
                if (frame == -1 || frame == i) {
                    def outputStream = new ByteArrayOutputStream()
                    ImageOutputStream out = new MemoryCacheImageOutputStream(outputStream)
                    writer.setOutput(out)
                    // 读取读取帧的图片
                    writer.write(gifReader.read(i))
                    out.flush()
                    result.add(Base64Util.encodeToString(outputStream.toByteArray()))
                }
            }
        }
        return result
    }

}