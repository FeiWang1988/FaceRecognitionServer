package com.netease.facerecognition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * server health check
 * 
 * @author eagle
 * 
 * @since 0.2.0
 * */
@Controller
public class HealthController {
	/** 服务存活状态 */
	private static boolean serverAlive = false;
	private static Map<String, String> map = new HashMap<String, String>();
	private static ArrayList<Pattern> patternArray = new ArrayList<Pattern>();

	@RequestMapping("/health/status")
	@ResponseBody
	public String getServerStatus(HttpServletResponse resp) {
		if (!serverAlive) {
			resp.setStatus(403);
		}
		return serverAlive ? "alive" : "offline";
	}

	@RequestMapping("/health/offline")
	@ResponseBody
	public String offline(HttpServletRequest req) {
		if (checkIP(req)) {
			serverAlive = false;
			return "ok";
		} else {
			return "false";
		}
	}

	@RequestMapping("/health/active")
	@ResponseBody
	public String active(HttpServletRequest req) {
		init();
		if (checkIP(req)) {
			serverAlive = true;
			return "ok";
		} else {
			return "false";
		}
	}

	@PostConstruct
	public void init() {
		String[] list = {};
		int length = list.length;
		for (int i = 0; i < length; ++i) {
			// System.out.println(list[i]);
			String line = list[i].trim();
			if (line.length() == 0) {
				continue;
			}
			if (line.indexOf("*") == -1) {
				map.put(line, null);
				continue;
			}
			line = line.replaceAll("\\*", "[0-9]{1,3}");
			line = line.replaceAll("\\.", "\\\\.");
			// System.out.println(line);
			Pattern pattern = Pattern.compile(line);
			patternArray.add(pattern);
		}
	}

	private boolean checkIP(ServletRequest request) {
		String ip = request.getRemoteAddr();
		// System.out.println(ip);
		if (map.containsKey(ip)) {
			return true;
		}
		for (int i = 0; i < patternArray.size(); ++i) {
			Matcher mat = patternArray.get(i).matcher(ip);
			if (mat.matches()) {
				return true;
			}
		}
		return false;
	}

}
