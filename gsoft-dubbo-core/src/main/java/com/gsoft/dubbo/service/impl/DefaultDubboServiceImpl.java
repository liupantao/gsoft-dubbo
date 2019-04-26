package com.gsoft.dubbo.service.impl;

import java.lang.reflect.Method;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.gsoft.dubbo.service.DubboService;
import com.gsoft.framework.core.exception.BusException;
import com.gsoft.framework.core.log.LogInfo;
import com.gsoft.framework.remote.RemoteConstants;
import com.gsoft.framework.remote.annotation.ServiceMapping;
import com.gsoft.framework.remote.data.ExceptionResContext;
import com.gsoft.framework.remote.data.PubContext;
import com.gsoft.framework.remote.data.ReqContext;
import com.gsoft.framework.remote.data.ResContext;
import com.gsoft.framework.remote.invoke.ServiceInvoker;
import com.gsoft.framework.remote.service.RemoteLog;

/**
 * 
 * @author LiuPeng
 * 
 */
public class DefaultDubboServiceImpl implements DubboService {

	protected final Log logger = LogFactory.getLog(getClass());

	@Autowired
	private ServiceInvoker serviceInvoker;

	@Autowired
	private RemoteLog remoteLog;// 交易日志

	// @Autowired
	// private LoggerService loggerService;// 交易日志

	@Override
	public ResContext<?> exchange(Map<String, String> headers, PubContext pubContext, ReqContext<?> reqContext)
			throws Exception {
		long startTime = System.currentTimeMillis();
		if (serviceInvoker != null) {

			String serviceName = headers.get(RemoteConstants.HEADER_SERVICE_NAME);
			String methodName = headers.get(RemoteConstants.HEADER_METHOD_NAME);

			ServiceMapping mapping = null;
			ResContext<?> res;
			try {
				Object bean = serviceInvoker.getEsbServiceBean(serviceName);
				Method serviceMethod = serviceInvoker.getEsbServiceMethod(bean, methodName);
				mapping = serviceMethod.getAnnotation(ServiceMapping.class);

				res = serviceInvoker.invoke(bean, serviceMethod, reqContext, pubContext);
				// res转换，把domain对象转换为LinkedMultiValueMap对象

			} catch (BusException e) {
				res = new ExceptionResContext(e);
				this.logger.error("交易异常：" + e.getMessage());
			} catch (Throwable e) {
				res = new ExceptionResContext(e);
				logger.error("交易异常：" + e.getMessage(), e);
			}
			
			//日志
			long time = System.currentTimeMillis() - startTime;
			String flowno = headers.get(RemoteConstants.HEADER_FLOW_NO);
			String remoteIp = headers.get(RemoteConstants.HEADER_REMOTE_IP);
			LogInfo logInfo = new LogInfo(flowno, pubContext.getUsername(), remoteIp, mapping.trancode(),
					mapping.caption());
			logInfo.setServiceName(serviceName);
			logInfo.setMethodName(methodName);
			logInfo.setTime(time);
			logInfo.setLog(mapping.log());
			res.setLogInfo(logInfo);
			
			// 写交易日志
			if (remoteLog != null) {
				// 输出交易调用日志
				// TableGenerator
				remoteLog.writeLog(logInfo, reqContext, res);
			}

			return res;
		}
		logger.error("交易调用异常:" + headers);
		return null;
	}

}
