package com.netease.facerecognition;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.netease.facedetector.jni.FaceDetector;

/**
 * Handles requests for the application face api.
 */
@Controller
public class FaceController {

	private static final Logger logger = LoggerFactory.getLogger(FaceController.class);
	private static FaceDetector faceDetector;
	private static SdfsManger sdfsManger_ob;
	private static String threadNumber = "8";
	private StringBuffer savePath = new StringBuffer("/tmp/");

	@PostConstruct
	private void init() {
		logger.info("HomeController start init");
		sdfsManger_ob = new SdfsManger();
		faceDetector = new FaceDetector();
		faceDetector.init(threadNumber);
		logger.info("HomeController init over");
	}

	/**
	 * 测试本地默认图片
	 * 
	 * @author eagle
	 * */
	@RequestMapping(value = "/faceDectectionTest", method = RequestMethod.GET)
	public @ResponseBody String faceDectectionTest() throws Exception {

		return faceDetector.detect("/home/asr/faceDetection/jni_thp/test3.jpg");
	}

	/**
	 * 
	 * 判断服务是否存在
	 * 
	 * @author eagle
	 * 
	 */
	@RequestMapping(value = "/online", method = RequestMethod.GET)
	public @ResponseBody String online() throws Exception {
		return ("server is alive!");
	}

	/**
	 * 人脸分析
	 * 
	 * @author eagle
	 * 
	 * @param pic_path
	 *            本地路径
	 * 
	 * @param pic_id
	 *            SDFS上的ID
	 * 
	 * @param web_url
	 *            网络图片链接
	 * 
	 * @param del
	 *            分析后是否删除
	 * 
	 * @throws Exception
	 */
	@RequestMapping(value = "/mail/faceDectection", method = RequestMethod.GET)
	public @ResponseBody String faceDectection(String pic_path, String pic_id, String web_url, Boolean del) {
		String image_path = null;
		if (del == null || del.equals("")) {
			del = true;
		}
		// System.out.println(del);
		String result = "{\"status\": -2 }";
		/** 获取图片路径 **/
		Date start = Calendar.getInstance().getTime();
		if (web_url != null && !web_url.equals("")) {
			String filename = UUID.randomUUID() + "_" + web_url.replace("http://nos.netease.com/", "").replace("/", "_") + ".jpg";
			logger.info("savePath:[{}]", savePath);
			if (download(web_url, filename, savePath.toString())) {
				image_path = savePath.append(filename).toString();
			} else {
				return result;
			}
		}
		// get image on sdfs id
		else if (pic_id != null && !pic_id.equals("")) {
			image_path = getPic(savePath.append(UUID.randomUUID()).append("_").toString(), pic_id);
		}
		// get image on server path
		else if (pic_path != null && !pic_path.equals("")) {
			image_path = pic_path;
		} else {
			return result;
		}

		/** 分析图片 **/
		Date end1 = Calendar.getInstance().getTime();
		if (!image_path.equals("")) {
			logger.debug("image_path:[{}]", image_path);
			analysisImage(image_path, del);
			Date end2 = Calendar.getInstance().getTime();
			long diff1 = end1.getTime() - start.getTime();
			long diff2 = end2.getTime() - start.getTime();
			logger.info("[{}]:get image time:[{}]", image_path, diff1);
			logger.info("[{}]:total time:[{}]", image_path, diff2);
		}
		return result;

	}

	/**
	 * 分析图片
	 * 
	 * @author eagle
	 * 
	 * @param pic_path
	 *            图片路径
	 * 
	 * @param del
	 *            分析图片后是否删除图片
	 *            
	 * @return {@link String} 本地路径
	 * **/
	private String analysisImage(String pic_path, Boolean del) {
		String result = "";
		result = faceDetector.detect(pic_path);
		File file = new File(pic_path);
		if (del) {
			file.delete();
		}
		return result;
	}

	/**
	 * 从SDFS下载图片保存至本地，并给出本地路径
	 * 
	 * @author eagle
	 * 
	 * @param path
	 *            计划保存图片的路径
	 * 
	 * @param sdfsId
	 *            在SDFS上的ID
	 * 
	 * @return {@link String} 本地路径
	 * **/
	private String getPic(String path, String sdfsId) {
		Long id = Long.valueOf(sdfsId);
		String pic_path = "";
		try {
			pic_path = sdfsManger_ob.loadFile(path, id, "jpg");
		} catch (Exception e) {
			logger.error("sdfsManger_ob.loadFile error " + e.toString());
		}
		// sdfsManger_ob.close();
		return pic_path;

	}

	/**
	 * 下载网络图片保存至本地，并给出本地路径
	 * 
	 * @author eagle
	 * 
	 * @param urlString
	 *            图片的网络链接
	 * 
	 * @param savePath
	 *            计划保存的图片路径
	 * 
	 * @param filename
	 *            计划保存的图片名称
	 * 
	 * @return 是否下载成功 true成功 flase 失败
	 * **/

	@SuppressWarnings("finally")
	public Boolean download(String urlString, String filename, String savePath) {
		Boolean result = false;
		InputStream is = null;
		OutputStream os = null;
		try {
			URL url = new URL(urlString);
			URLConnection con = url.openConnection();
			con.setConnectTimeout(5 * 1000);
			is = con.getInputStream();
			byte[] bs = new byte[1024];
			int len;
			File sf = new File(savePath);
			if (!sf.exists()) {
				sf.mkdirs();
			}
			os = new FileOutputStream(sf.getPath() + "//" + filename);
			while ((len = is.read(bs)) != -1) {
				os.write(bs, 0, len);
			}
			result = true;
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			logger.error("MalformedURLException error " + e.toString());

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			logger.error("FileNotFoundException error " + e.toString());

		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error("IOException error " + e.toString());

		} finally {
			if (null != is) {
				try {
					os.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					logger.error("OutputStream close error " + e.toString());
				}
			}
			if (null != os) {
				try {
					is.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					logger.error("InputStream close error " + e.toString());
				}
			}
			return result;
		}
	}
}
