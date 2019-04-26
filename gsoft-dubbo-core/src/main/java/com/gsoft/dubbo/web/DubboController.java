package com.gsoft.dubbo.web;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.gsoft.dubbo.service.DubboWebRequestRouter;
import com.gsoft.file.entity.FileStore;
import com.gsoft.file.service.FileStoreService;
import com.gsoft.framework.core.exception.BusException;
import com.gsoft.framework.core.orm.Pager;
import com.gsoft.framework.core.orm.PagerRecords;
import com.gsoft.framework.core.web.controller.BaseDataController;
import com.gsoft.framework.core.web.export.WebExportService;
import com.gsoft.framework.remote.data.ResContext;
import com.gsoft.framework.upload.service.FileUploadInfoWebManager;
import com.gsoft.framework.util.FileUtils;
import com.gsoft.framework.util.StringUtils;

/**
 * 
 * @author LiuPeng
 * 
 */
@Controller
@RequestMapping("/dubbo")
public class DubboController extends BaseDataController {

	@Autowired
	private DubboWebRequestRouter requestRouter;

	@Autowired(required = false)
	private WebExportService webExportService;

	@Autowired(required = false)
	private FileStoreService fileStoreService;

	@Autowired(required = false)
	private FileUploadInfoWebManager fileUploadInfoWebManager;

	/**
	 * 
	 * @param channel
	 * @param serviceName
	 * @param methodName
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/{channel}/{serviceName}/{methodName}.json")
	@ResponseBody
	public ResContext<?> exchange(@PathVariable("channel") String channel,
			@PathVariable("serviceName") String serviceName, @PathVariable("methodName") String methodName,
			HttpServletRequest request, HttpServletResponse response) {
		Map<String, String[]> params = request.getParameterMap();
		if (params.size() == 0) {
			// 防止参数为空，导致map不能添加
			params = new HashMap<>();
		}

		// 处理导出中文编码
		String export = request.getParameter("pager:export");
		if (export != null && webExportService != null) {
			params = decodeExportParams(params);
		}

		uploadFiles(request, response, channel, params);
		ResContext<?> res = requestRouter.exchange(channel, serviceName, methodName, params);
		// 处理导出
		if (export != null && webExportService != null) {
			PagerRecords pagerRecords = buildPagerRecords(res, request);
			if (pagerRecords != null) {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				try {
					HttpHeaders httpHeaders = this.webExportService.writePagerRecords(out, export, pagerRecords);
					String exportTitle = request.getParameter("pager:exportTitle");
					if (httpHeaders != null) {
						String contentDisposition = httpHeaders.getFirst("Content-disposition");
						if ((contentDisposition != null) && (exportTitle != null)) {
							try {
								exportTitle = decode(exportTitle);
							} catch (Exception e) {
							}
							String userAgent = request.getHeader("User-Agent");
							// 针对IE或者以IE为内核的浏览器：
							if (userAgent.contains("MSIE") || userAgent.contains("Trident")) {
								exportTitle = java.net.URLEncoder.encode(exportTitle, "UTF-8");
							} else {
								// 非IE浏览器的处理：
								exportTitle = new String(exportTitle.getBytes("UTF-8"), "ISO-8859-1");
							}
							contentDisposition = contentDisposition.replaceAll("grid", exportTitle);
						}
						response.addHeader("Content-disposition", contentDisposition);
					} else {
						out.write(("no suite " + export + " exporter found.").getBytes());
					}
					FileCopyUtils.copy(out.toByteArray(), response.getOutputStream());
				} catch (IOException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return res;
	}

	/**
	 * 处理导出中文编码
	 * 
	 * @param params
	 */
	private Map<String, String[]> decodeExportParams(Map<String, String[]> params) {
		Map<String, String[]> paramsMap = new HashMap<>();
		for (Map.Entry<String, String[]> entry : params.entrySet()) {
			String[] values = entry.getValue();
			for (int i = 0; i < values.length; i++) {
				String v = values[i];
				if (StringUtils.isNotEmpty(v)) {
					values[i] = decode(v);
				}
			}
			paramsMap.put(entry.getKey(), values);
		}
		return paramsMap;
	}

	private boolean uploadFiles(HttpServletRequest request, HttpServletResponse response, String channel,
			Map<String, String[]> params) {
		if (request instanceof MultipartHttpServletRequest) {
			// convertParams(params,request.getCharacterEncoding());
			MultipartHttpServletRequest req = (MultipartHttpServletRequest) request;
			Map<String, MultipartFile> files = req.getFileMap();
			for (Map.Entry<String, MultipartFile> entry : files.entrySet()) {
				MultipartFile file = entry.getValue();
				String fileName = file.getOriginalFilename();
				fileName = FileUtils.getSimpleFileName(fileName);
				if (fileStoreService == null) {
					throw new BusException("未配置文件服务,上传文件失败");
				}
				try {
					FileStore fileStore = fileStoreService.storeFile(fileName, file.getInputStream());
					params.put(entry.getKey(), new String[] { fileStore.getFilePath() });
					try {
						if (fileUploadInfoWebManager != null) {
							fileUploadInfoWebManager.saveFileUploadInfo("local", fileStore);
						}
					} catch (Exception e) {
						logger.error("文件信息保存失败," + e.getMessage(), e);
					}
				} catch (Exception e) {
					throw new BusException("文件上传失败:" + fileName, e);
				}
			}
			return true;
		}
		return false;
	}

	/**
	 * 有文件上传时,编码集修改
	 * 
	 * @param params
	 */
	// private void convertParams(Map<String, String[]> params,String
	// characterEncoding) {
	// for(Map.Entry<String, String[]> entry:params.entrySet()){
	// String key = entry.getKey();
	// String[] values = entry.getValue();
	// for(int i=0;i<values.length;i++){
	// String value = values[i];
	// try {
	// values[i] = new String(value.getBytes("iso-8859-1"),characterEncoding);
	// } catch (UnsupportedEncodingException e) {
	// }
	// }
	// params.put(key, values);
	// }
	// }

	private PagerRecords buildPagerRecords(ResContext<?> res, HttpServletRequest request) {
		List<?> domains = res.getData();
		if (domains == null) {
			domains = new ArrayList<>();
		}
		PagerRecords pagerRecords = new PagerRecords(domains, domains.size());
		Pager pager = new Pager(0, 10);

		String[] exportProperties = request.getParameterValues("pager:property");
		String[] exportHeaders = request.getParameterValues("pager:header");
		String[] exportConverts = request.getParameterValues("pager:convert");

		for (int i = 0; i < exportHeaders.length; i++) {
			try {
				exportHeaders[i] = decode(exportHeaders[i]);
			} catch (Exception e) {
			}
		}

		pager.setExportHeaders(exportHeaders);
		pager.setExportProperties(exportProperties);
		if (exportConverts != null) {
			pager.setExportConverts(exportConverts);
		}

		pagerRecords.setPager(pager);

		return pagerRecords;
	}

	/**
	 * 转码
	 * 
	 * @param value
	 * @return
	 */
	private String decode(String value) {
		try {
			value = java.net.URLDecoder.decode(value, "utf-8");
		} catch (Exception e) {
		}
		return value;
	}
}
