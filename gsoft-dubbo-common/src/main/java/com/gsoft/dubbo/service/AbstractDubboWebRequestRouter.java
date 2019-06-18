package com.gsoft.dubbo.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.gsoft.framework.remote.data.PubContext;
import com.gsoft.framework.remote.data.ReqContext;
import com.gsoft.framework.security.AccountPrincipal;
import com.gsoft.framework.security.IdUser;
import com.gsoft.framework.util.SecurityUtils;

/**
 * 
 * @author LiuPantao
 * 
 */
public abstract class AbstractDubboWebRequestRouter extends AbstractDubboRouter implements DubboWebRequestRouter {

	/**
	 * 构建dubbo请求公共参数
	 * 
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	protected PubContext buildPubContext() {
		AccountPrincipal account = SecurityUtils.getAccount();
		PubContext pubContext = new PubContext();
		if ((account != null) && (account.getPrincipalConfig() != null)) {
			pubContext.setUsername(account.getLoginName());
			for (Map.Entry entry : account.getPrincipalConfig().entrySet()) {
				pubContext.addParam(entry.getKey().toString(), entry.getValue());
			}

			if (IdUser.class.isAssignableFrom(account.getClass())) {
				pubContext.addParam("userId", ((IdUser) account).getUserId());
			}

			if (account.roleIds() != null) {
				pubContext.addParam("roleIds", account.roleIds());
			}
		}
		return pubContext;
	}

	protected ReqContext<Object> buildParams(Map<String, String[]> params) {
		ReqContext<Object> reqContext = new ReqContext<Object>();
		for (Map.Entry<String, String[]> entry : params.entrySet()) {
			List<Object> valueList = new ArrayList<Object>();
			String[] value = entry.getValue();
			if (value != null) {
				for (String v : value) {
					valueList.add(v);
				}
			}
			reqContext.put(entry.getKey(), valueList);
		}
		return reqContext;
	}

}
