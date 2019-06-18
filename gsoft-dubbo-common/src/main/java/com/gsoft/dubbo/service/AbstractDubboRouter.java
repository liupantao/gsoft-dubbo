package com.gsoft.dubbo.service;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.gsoft.framework.remote.RemoteConstants;
import com.gsoft.framework.remote.service.RemoteRequestRouter;
import com.gsoft.framework.remote.service.RemoteSequenceService;
import com.gsoft.framework.util.IpUtils;

/**
 * 
 * @author LiuPantao
 * 
 */
public abstract class AbstractDubboRouter implements RemoteRequestRouter {

	@Autowired
	private RemoteSequenceService remoteSequenceService;
	
	/**
	 * 构建dubbo请求头
	 * 
	 * @param serviceName
	 * @param methodName
	 * @return
	 */
	public Map<String, String> buildHeader(String serviceName, String methodName) {
		Map<String, String> header = new HashMap<String, String>();
		header.put(RemoteConstants.HEADER_SERVICE_NAME, serviceName);
		header.put(RemoteConstants.HEADER_METHOD_NAME, methodName);
		// 加入客户端用户IP地址
		header.put(RemoteConstants.HEADER_REMOTE_IP, getRemoteIp());

		// 加入流程号
		header.put(RemoteConstants.HEADER_FLOW_NO, this.remoteSequenceService.generate().toString());

		return header;
	}

	/**
	 * @return
	 */
	private String getRemoteIp() {
		String remoteIp = "";
		try {
			RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
			if (requestAttributes instanceof ServletRequestAttributes) {
				HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
				remoteIp = IpUtils.getIpAddr(request);
			}
		} catch (Exception e) {
			// 获取IP失败
		}
		return remoteIp;
	}
}