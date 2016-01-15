package com.netease.facerecognition;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import com.netease.backend.dfs.DFSException;
import com.netease.backend.dfs.DFSManager;
import com.netease.backend.dfs.FileStream;

public class SdfsManger {

	private DFSManager dfsManager;

	public SdfsManger() {
		dfsManager = new DFSManager();
		try {
			dfsManager.launch("172.17.2.165:5559,172.17.2.122:5559", "logs/sdfs");
		} catch (DFSException e) {
			throw new Error(e);
		}
	}

	public String loadFile(String path, long dfsId, String ext) throws Exception {
		try {
			if (dfsManager.getFileSize(dfsId) <= 0) {
				return "";
			}
			FileStream stream = dfsManager.getFile(dfsId);
			String file = path + String.valueOf(dfsId);
			file += ".";
			file += ext;
			System.out.println(file);
			OutputStream writer = new FileOutputStream(new File(file));

			InputStream input = stream.getInputStream();
			byte[] bytes = new byte[1024 * 1024];
			while (true) {
				int read = input.read(bytes);
				if (read <= 0) {
					break;
				}
				writer.write(bytes, 0, read);
			}
			input.close();
			stream.close();
			writer.flush();
			writer.close();
			return file;
		} catch (Throwable t) {
			return "";
		}
	}

	public void close() {
		if (dfsManager != null) {
			dfsManager.shutdown();
		}
	}
}
