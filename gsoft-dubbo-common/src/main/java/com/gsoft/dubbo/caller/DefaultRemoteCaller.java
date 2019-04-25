package com.gsoft.dubbo.caller;

import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.gsoft.dubbo.service.AbstractDubboRouter;
import com.gsoft.dubbo.service.DubboService;
import com.gsoft.framework.core.exception.BusException;
import com.gsoft.framework.remote.caller.RemoteCaller;
import com.gsoft.framework.remote.data.PubContext;
import com.gsoft.framework.remote.data.ReqContext;
import com.gsoft.framework.remote.data.ResContext;
import com.gsoft.framework.util.StringUtils;

/**
 * 
 * @author LiuPeng
 * 
 */
public class DefaultRemoteCaller extends AbstractDubboRouter implements RemoteCaller, ApplicationContextAware {

	@Value("${remote.default.channel}")
	private String defaultChannel;

	private Map<String, DubboService> dubboServices;

	@Override
	public ResContext<?> callRemoteService(String channel, String adapterChannel, String servicesName, String tranName,
			ReqContext<?> req) {
		return callRemoteService(channel, servicesName, tranName, req);
	}

	@Override
	public ResContext<?> callRemoteService(String channel, String serviceName, String tranName, ReqContext<?> req) {
		channel = StringUtils.isEmpty(channel)?defaultChannel:channel;
		Map<String, String> headers = null;
		PubContext pubContext = null;
		try {
			DubboService dubboService = dubboServices.get(channel);
			if (dubboService == null) {
				throw new BusException("获取dubbo remoteService [" + channel + "] 失败!");//1
			}
			headers = buildHeader(serviceName, tranName);
			pubContext = new PubContext();
			return dubboService.exchange(headers, pubContext, req);
		} catch (Exception e) {
			throw new BusException("调用远程dubbo服务失败,header[" + headers + "]", e);
		}
	}

	@Override
	public ResContext<?> callRemoteService(String servicesName, String tranName, ReqContext<?> req) {
		return callRemoteService(defaultChannel, servicesName, tranName, req);
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		initDubboServices(applicationContext);
	}

	public void initDubboServices(ApplicationContext applicationContext) {
		if (this.dubboServices == null) {
			dubboServices = BeanFactoryUtils.beansOfTypeIncludingAncestors(applicationContext, DubboService.class, true, true);
		}
	}
}
