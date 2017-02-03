package com.dianping.ssp.crawler.service.test;

import com.dianping.ssp.crawler.common.spider.SpiderFactory;
import com.dianping.ssp.crawler.common.util.DomainTagUtils;
import com.dianping.ssp.crawler.service.base.AbstractTest;
import org.apache.commons.collections.CollectionUtils;
import org.junit.Test;

import java.util.List;

/**
 * Created by iClaod on 10/12/16.
 */
public class ServiceTest extends AbstractTest {
    @Test
    public void test() throws Exception{
        List<String> allDomainTags = DomainTagUtils.getAllDomainTags();
        if (CollectionUtils.isNotEmpty(allDomainTags)) {
            for (String domainTag : allDomainTags) {
                SpiderFactory.initSpider(domainTag);
            }
        }
        Thread.currentThread().join();
    }
}