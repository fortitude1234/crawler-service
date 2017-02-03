package com.dianping.ssp.crawler.service.log;


import com.dianping.ed.logger.LoggerConfig;
import com.dianping.ed.logger.LoggerLevel;

/**
 * Description
 * Created by yuxiang.cao on 16/5/17.
 */
public enum CrawlerServiceLogEnum {

	DETAIL_PAGE_PROCESSOR("detail_page", "subprocessor", false, LoggerLevel.INFO)

	;

	CrawlerServiceLogEnum(String name, String category, boolean isError, LoggerLevel level) {
		loggerConfig = new LoggerConfig();
		loggerConfig.setApp(APP_NAME);
		loggerConfig.setCategory(category);
		loggerConfig.setName(name);
		loggerConfig.setLevel(level);
		loggerConfig.setDaily(true);
		loggerConfig.setPerm(false);
		loggerConfig.setError(isError);
	}

	private static final String APP_NAME = "ssp-crawler-service";

	private LoggerConfig loggerConfig;

	public LoggerConfig getValue() {
		return loggerConfig;
	}
}
