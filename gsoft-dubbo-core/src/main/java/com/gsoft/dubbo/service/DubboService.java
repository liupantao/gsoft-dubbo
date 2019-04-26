package com.gsoft.dubbo.service;

import java.util.Map;

import com.gsoft.framework.remote.data.PubContext;
import com.gsoft.framework.remote.data.ReqContext;
import com.gsoft.framework.remote.data.ResContext;

/**
 * 
 * @author LiuPeng
 *
 */
public interface DubboService {

	public ResContext<?> exchange(Map<String, String> headers,PubContext pubContext, ReqContext<?> reqContext) throws Exception;

}