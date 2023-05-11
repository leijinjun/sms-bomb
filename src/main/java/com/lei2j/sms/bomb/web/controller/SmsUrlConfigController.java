package com.lei2j.sms.bomb.web.controller;

import com.lei2j.idgen.core.IdGenerator;
import com.lei2j.sms.bomb.base.entity.Pager;
import com.lei2j.sms.bomb.entity.SmsUrlConfig;
import com.lei2j.sms.bomb.repository.SmsUrlConfigRepository;
import com.lei2j.sms.bomb.util.HttpUtils;
import com.lei2j.sms.bomb.util.IgnoreEmptyStringValueTransformer;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

/**
 * @author leijinjun
 * @date 2021/1/16
 **/
@RestController
@RequestMapping("/url/config")
public class SmsUrlConfigController {

    private final SmsUrlConfigRepository smsUrlConfigRepository;

    private final IdGenerator idGenerator;

    @Autowired
    public SmsUrlConfigController(SmsUrlConfigRepository smsUrlConfigRepository,
                                  IdGenerator idGenerator) {
        this.smsUrlConfigRepository = smsUrlConfigRepository;
        this.idGenerator = idGenerator;
    }

    @GetMapping("/page")
    public ResponseEntity<Object> urlConfig(SmsUrlConfig params,
                            @RequestParam(value = "pageNo",required = false,defaultValue = "1")Integer pageNo,
                            @RequestParam(value = "pageSize",required = false,defaultValue = "20")Integer pageSize){
        PageRequest pageRequest = PageRequest.of(Math.max(pageNo - 1, 0), pageSize, Sort.by("id").descending());
        ExampleMatcher exampleMatcher = ExampleMatcher.matching().withIgnoreNullValues()
                .withMatcher("websiteName",
                        ExampleMatcher.GenericPropertyMatcher.of(ExampleMatcher.StringMatcher.CONTAINING, true).transform(IgnoreEmptyStringValueTransformer.IGNORE_EMPTY));
        Example<SmsUrlConfig> example = Example.of(params, exampleMatcher);
        Page<SmsUrlConfig> page = smsUrlConfigRepository.findAll(example, pageRequest);
        return ResponseEntity.ok(Pager.convert(page));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SmsUrlConfig> get(@PathVariable("id")Long id){
        return ResponseEntity.of(smsUrlConfigRepository.findById(id));
    }

    @PostMapping("/updateStatus")
    public ResponseEntity<Void> update(Long id,
                                       @RequestParam(value = "normal") Boolean normal) {
        return smsUrlConfigRepository.findById(id).map(c -> {
            c.setNormal(normal);
            smsUrlConfigRepository.saveAndFlush(c);
            return ResponseEntity.noContent().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/updateAllStatus")
    public ResponseEntity<Void> update(@RequestParam(value = "status") Boolean normal) {
        smsUrlConfigRepository.updateAllStatus(normal);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/save")
    public ResponseEntity<Void> update(SmsUrlConfig smsUrlConfig) {
        return Optional.of(smsUrlConfig)
                .map(c -> {
                    Long id = c.getId();
                    final boolean isCreated = Objects.isNull(id);
                    Optional<SmsUrlConfig> configOptional = isCreated ? Optional.of(c) : smsUrlConfigRepository.findById(id);
                    return configOptional.map(t -> {
                        BeanUtils.copyProperties(c, t, "createAt", "updateAt", "lastUsedTime","normal");
                        if (isCreated) {
                            t.setId(((Long) idGenerator.next()));
                            t.setNormal(true);
                        }
                        if (StringUtils.isNotBlank(t.getWebsite())&&StringUtils.isBlank(t.getWebsiteName())) {
                            try {
                                final Connection connect = Jsoup.connect(t.getWebsite());
                                final Document document = connect.get();
                                final Elements titleEle = document.getElementsByTag("title");
                                if (!titleEle.isEmpty()) {
                                    String text = titleEle.get(0).text();
                                    text = text.substring(0, Math.min(text.length(), 255));
                                    t.setWebsiteName(text);
                                }
                                Elements elements = document.getElementsByAttributeValueContaining("href", "favicon");
                                if (elements.isEmpty()) {
                                    elements = document.getElementsByAttributeValueContaining("href", "logo");
                                }
                                if (!elements.isEmpty()) {
                                    final String href = elements.get(0).attr("href");
                                    if (href.startsWith("http")) {
                                        t.setIcon(href);
                                    } else {
                                        final int index = href.indexOf("/");
                                        String iconUrl = t.getWebsite() + "" + href.substring(index);
                                        t.setIcon(iconUrl);
                                    }
                                }
                            } catch (Exception ignore) {

                            }
                        }
                        if (Objects.isNull(t.getCreateAt())) {
                            t.setCreateAt(LocalDateTime.now());
                        }
                        t.setUpdateAt(LocalDateTime.now());
                        smsUrlConfigRepository.save(t);
                        return ResponseEntity.ok().<Void>build();
                    }).orElse(ResponseEntity.notFound().build());
                }).get();
    }
}
