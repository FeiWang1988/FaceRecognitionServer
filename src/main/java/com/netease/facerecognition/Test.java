package com.netease.facerecognition;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.UUID;

public class Test {

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		String web_url = "http://nos.netease.com/yydbact1/test";
		String filename = UUID.randomUUID() + "_" + web_url.replace("http://nos.netease.com/", "").replace("/", "_") + ".jpg";
		download(web_url, filename, "D:\\image\\");
	}

	public static void download(String urlString, String filename, String savePath) throws Exception {
		// ����URL
		URL url = new URL(urlString);
		// ������
		URLConnection con = url.openConnection();
		// ��������ʱΪ5s
		con.setConnectTimeout(5 * 1000);
		// ������
		InputStream is = con.getInputStream();

		// 1K�����ݻ���
		byte[] bs = new byte[1024];
		// ��ȡ�������ݳ���
		int len;
		// ������ļ���
		File sf = new File(savePath);
		if (!sf.exists()) {
			sf.mkdirs();
		}
		OutputStream os = new FileOutputStream(sf.getPath() + "\\" + filename);
		// ��ʼ��ȡ
		while ((len = is.read(bs)) != -1) {
			os.write(bs, 0, len);
		}
		// ��ϣ��ر���������
		os.close();
		is.close();
	}

}
