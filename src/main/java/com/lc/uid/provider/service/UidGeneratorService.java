package com.lc.uid.provider.service;

import com.baidu.fsg.uid.UidGenerator;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

/**
 * @author: lucheng
 * @data: 2022/4/27 15:38
 * @version: 1.0
 */
@Service
@Slf4j
public class UidGeneratorService {
    @Resource
    private  UidGenerator uidGenerator;

    public long nextId() {
        long id = this.uidGenerator.getUID();
        log.info(uidGenerator.parseUID(id));
         return id;
    }
}
