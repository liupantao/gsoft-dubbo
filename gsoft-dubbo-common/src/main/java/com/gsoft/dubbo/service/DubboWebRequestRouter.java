package com.gsoft.dubbo.service;

import java.util.Map;

import com.gsoft.framework.remote.data.ResContext;

/**
 * 
 * @author LiuPeng
 *
 */
public interface DubboWebRequestRouter {
	public ResContext<?> exchange(String channel,String serviceName,String methodName,Map<String, String[]> params);
}
