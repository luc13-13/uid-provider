package com.lc.uid.provider.feign;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author: lucheng
 * @data: 2022/5/29 0:15
 * @version: 1.0
 */
//@FeignClient(name = "uid-center")
public interface TestFeignUid {
//    @LoadBalanced
//    @GetMapping("/uid/nextId")
    long getUid();
}
