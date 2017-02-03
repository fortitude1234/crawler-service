package com.dianping.ssp.crawler.service.dao;

import com.dianping.avatar.dao.GenericDao;
import com.dianping.avatar.dao.annotation.DAOAction;
import com.dianping.avatar.dao.annotation.DAOActionType;
import com.dianping.avatar.dao.annotation.DAOParam;
import com.dianping.ssp.crawler.service.entity.DetailPageLogEntity;

public interface DetailPageLogDao extends GenericDao {

	@DAOAction(action = DAOActionType.INSERT)
	public int addLog(@DAOParam("entity") DetailPageLogEntity entity);
}
