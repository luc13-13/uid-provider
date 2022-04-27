package com.lc.uid.provider.web;

import com.lc.uid.provider.service.UidGeneratorService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author: lucheng
 * @data: 2022/4/19 21:52
 * @version: 1.0
 */
@RestController
@RequestMapping("/uid")
public class ServiceController {
    private final UidGeneratorService uidGenerator;

    public ServiceController(UidGeneratorService uidGenerator) {
        this.uidGenerator = uidGenerator;
    }

    @GetMapping("/nextId")
    public long nextId() {
        return uidGenerator.nextId();
    }
}
