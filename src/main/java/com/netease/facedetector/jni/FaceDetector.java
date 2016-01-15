package com.netease.facedetector.jni;

import java.io.File;

public class FaceDetector {
	static {
		try {
			String path_online = "/home/asr/lib/libfaceDetector.so";
			String path_offline = "/styx/home/hzliuhaiwei/faceSolution/faceDetection/jni/libfaceDetector.so";
			File file_online = new File(path_online);
			File file_offline = new File(path_offline);
			// System.loadLibrary("faceDetector");
			if (!file_offline.exists()) {
				if (!file_online.exists()) {
					System.out.println(" not load .so !");
				} else {
					System.load(path_online);
				}
			} else {
				System.load(path_offline);
			}
		} catch (Exception e) {
			System.out.println("load .so error : " + e.toString());
		}
	}

	public native boolean init(String threadNumber);

	public native String detect(String url);
}
