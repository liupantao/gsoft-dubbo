package com.gsoft.dubbo.service.impl;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.gsoft.dubbo.service.AbstractDubboWebRequestRouter;
import com.gsoft.dubbo.service.DubboService;
import com.gsoft.framework.core.exception.BusException;
import com.gsoft.framework.core.log.ConfigRegisterLog;
import com.gsoft.framework.core.log.LogInfo;
import com.gsoft.framework.remote.RemoteConstants;
import com.gsoft.framework.remote.data.ExceptionResContext;
import com.gsoft.framework.remote.data.PubContext;
import com.gsoft.framework.remote.data.ReqContext;
import com.gsoft.framework.remote.data.ResContext;
import com.gsoft.framework.remote.service.RemoteLog;

/**
 * 
 * @author LiuPeng
 * 
 */
public class DefaultDubboWebRequestRouter extends AbstractDubboWebRequestRouter implements ApplicationContextAware {

	private Log logger = LogFactory.getLog(getClass());

	private Map<String, DubboService> dubboServices;

	@Autowired(required = false)
	private RemoteLog remoteLog;

	@Override
	public ResContext<?> exchange(String channel, String serviceName, String methodName, Map<String, String[]> params) {
		Map<String, String> headers = null;
		PubContext pubContext = null;
		ReqContext<Object> reqContext = null;
		ResContext<?> resContext = null;
		LogInfo logInfo = null;
		try {
			DubboService dubboService = dubboServices.get(channel);
			if (dubboService == null) {
				throw new BusException("获取dubbo remoteService [" + channel + "] 失败!");
			}
			headers = buildHeader(serviceName, methodName);
			pubContext = buildPubContext();
			reqContext = buildParams(params);
			resContext = dubboService.exchange(headers, pubContext, reqContext);
			logInfo = resContext.getLogInfo();
		} catch (Exception e) {
			// 调用失败记录日志
			logInfo = new LogInfo(headers.get(RemoteConstants.HEADER_FLOW_NO).toString(), pubContext.getUsername(),
					headers.get(RemoteConstants.HEADER_REMOTE_IP).toString(), null, null);
			logInfo.setServiceName(serviceName);
			logInfo.setMethodName(methodName);
			logInfo.setLog(true);
			resContext = new ExceptionResContext(new BusException("调用远程dubbo服务失败,header[" + headers + "]"));
			logger.error("调用远程dubbo服务失败,header[" + headers + "]", e);
		} finally {
			if (remoteLog != null) {
				remoteLog.writeLog(logInfo, reqContext, resContext);
			}
		}
		return resContext;
	}

	public void initDubboServices(ApplicationContext applicationContext) {
		if (this.dubboServices == null) {
			dubboServices = BeanFactoryUtils.beansOfTypeIncludingAncestors(applicationContext, DubboService.class, true,
					true);
			ConfigRegisterLog.registeAdapters("DubboServices", dubboServices.keySet().toString(), this);
		}
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		initDubboServices(applicationContext);
	}

}
