package com.lc.uid.provider.web;

import com.lc.uid.provider.feign.TestFeignUid;
import com.lc.uid.provider.service.UidGeneratorService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author: lucheng
 * @data: 2022/4/19 21:52
 * @version: 1.0
 */
@RestController
@RefreshScope
@RequestMapping("/uid")
public class ServiceController {
    private final UidGeneratorService uidGenerator;

//    private final TestFeignUid testFeignUid;
    @Value(value = "${info.name}")
    public String configName;

    public ServiceController(UidGeneratorService uidGenerator) {
        this.uidGenerator = uidGenerator;
//        this.testFeignUid = testFeignUid;
    }

    @GetMapping("/nextId")
    public long nextId() {
        return uidGenerator.nextId();
    }

    @GetMapping("/testNacos")
    public String testNacos() {
        return configName;
    }

//    @GetMapping("/test")
//    public long test(){
//        return testFeignUid.getUid();
//    }
}
