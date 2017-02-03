package com.dianping.ssp.crawler.service.crawler.subprocessor;

import com.dianping.ed.logger.EDLogger;
import com.dianping.ed.logger.LoggerManager;
import com.dianping.ssp.crawler.common.contants.CrawlerCommonConstants;
import com.dianping.ssp.crawler.common.entity.ProStatus;
import com.dianping.ssp.crawler.common.entity.ProcessorContext;
import com.dianping.ssp.crawler.common.enums.ProMessageCode;
import com.dianping.ssp.crawler.common.pageprocessor.subprocess.CrawlerSubProcessTag;
import com.dianping.ssp.crawler.common.pageprocessor.subprocess.ICrawlerSubProcess;
import com.dianping.ssp.crawler.service.dao.DetailPageLogDao;
import com.dianping.ssp.crawler.service.entity.DetailPageLogEntity;
import com.dianping.ssp.crawler.service.log.CrawlerServiceLogEnum;
import com.dianping.ssp.file.upload.SSPUpload;
import com.dianping.ssp.file.upload.client.mss.MssS3UploadClient;
import com.dianping.ssp.file.upload.client.mss.result.MssS3UploadResult;
import com.google.common.collect.Maps;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;

import java.util.Date;
import java.util.Map;

/**
 * Created by iClaod on 10/17/16.
 */
@CrawlerSubProcessTag(name = "detailPage")
public class DetailPageDownloaderSubProcessor implements ICrawlerSubProcess {

    private static final EDLogger LOGGER = LoggerManager.getLogger(CrawlerServiceLogEnum.DETAIL_PAGE_PROCESSOR.getValue());

    private static final int RETRY_TIMES = 5;

    @Autowired
    private DetailPageLogDao detailPageLogDao;

    @Override
    public ProStatus process(Page page) {
        ProcessorContext.getContext(page).addParam(CrawlerCommonConstants.PageFieldKeys.CRAWL_TIME, new Date());
        for (int i = 0; i < RETRY_TIMES; i++) {
            try {
                MssS3UploadClient uploadClient = SSPUpload.MssS3.getMssS3UploadClient();
                MssS3UploadResult uploadResult = uploadClient.uploadFile(page.getRawText().getBytes(), "sixBing.html", null);
                if (uploadResult == null || !uploadResult.isSuccess() || StringUtils.isEmpty(uploadResult.getFilename())) {
                    continue;
                }
                String remoteFileName = uploadResult.getFilename();
                detailPageLogDao.addLog(buildLogEntity(page, remoteFileName));
                page.addTargetRequest(buildNewRequest(page, remoteFileName));
                return ProStatus.success();
            } catch (Exception e) {
                LOGGER.error("fail to process detail page, url: " + page.getRequest().getUrl(), e);
            }
        }
        return ProStatus.fail(ProMessageCode.SUB_PROCESSOR_ERROR.getCode(), "处理下载的详细页失败");
    }

    private Request buildNewRequest(Page page, String remoteFileUrl) {
        Request request = new Request();
        request.setUrl(remoteFileUrl);
        Map<String, Object> extraFromOriginRequest = page.getRequest().getExtras();
        Map<String, Object> extrasForNewRequest = request.getExtras();
        if (MapUtils.isEmpty(extrasForNewRequest)) {
            extrasForNewRequest = Maps.newHashMap();
        }
        if (MapUtils.isNotEmpty(extraFromOriginRequest)) {
            extrasForNewRequest.putAll(extraFromOriginRequest);
        }
        extrasForNewRequest.put(CrawlerCommonConstants.PageFieldKeys.ORIGIN_URL, page.getRequest().getUrl());
        extrasForNewRequest.put(CrawlerCommonConstants.PageFieldKeys.CRAWL_TIME, ProcessorContext.getContext(page).getParam(CrawlerCommonConstants.PageFieldKeys.CRAWL_TIME));
        request.setExtras(extrasForNewRequest);
        request.setMethod("S3Download");
        return request;
    }


    private DetailPageLogEntity buildLogEntity(Page page, String remoteFileUrl) {
        DetailPageLogEntity entity = new DetailPageLogEntity();
        entity.setUrl(page.getRequest().getUrl());
        entity.setHttpCode(page.getStatusCode());
        entity.setCrawlTime((Date) ProcessorContext.getContext(page).getParam(CrawlerCommonConstants.PageFieldKeys.CRAWL_TIME));
        entity.setFilePath(remoteFileUrl);
        entity.setDomainTag((String) ProcessorContext.getContext(page).getParam(CrawlerCommonConstants.ProcessorContextConstant.DOMAIN_TAG));
        Integer retryTime = (Integer) ProcessorContext.getContext(page).getParam(CrawlerCommonConstants.ProcessorContextConstant.RETRY_TIME);
        entity.setRetryTimes(retryTime == null ? 0 : retryTime);
        entity.setStatus(page.getStatusCode() == 200 ? 100 : 200);
        return entity;
    }
}
