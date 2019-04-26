package com.gsoft.dubbo.service;

import java.util.Map;

import com.gsoft.framework.remote.data.PubContext;
import com.gsoft.framework.remote.data.ReqContext;
import com.gsoft.framework.remote.data.ResContext;

/**
 * dubbo通用调用接口
 * @author LiuPeng
 *
 */
public interface DubboService {

	public ResContext<?> exchange(Map<String, String> headers,PubContext pubContext, ReqContext<?> reqContext) throws Exception;

}