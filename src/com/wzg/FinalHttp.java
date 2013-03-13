package com.wzg;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class FinalHttp {
	static String boundary = "--------------7d226f700d0";
	static String prefix = "--";
	static String newLine = "\r\n";

	public static void main(String args[]) {
		test();
	}

	private static void test() {
		try {
			URL url = new URL("http://192.168.1.237/newucai/uploadfile.php");
			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();
			connection.setDoInput(true);
			connection.setDoOutput(true);
			// 设定HTTP协议头
			connection.setRequestProperty("Content-type",
					"multipart/form-data;boundary=" + boundary);
			AssemblyHttp(connection.getOutputStream());
			InputStream ins = connection.getInputStream();
			byte[] b = readBuffer(ins);
			System.out.println(new String(b));
		} catch (MalformedURLException e) {
			System.out.println(" URL 地址解析错误 ");
		} catch (IOException e) {
			System.out.println(" URL连接打开错误 ");
		}
	}

	private static void AssemblyHttp(OutputStream out) {
		StringBuffer params = new StringBuffer();
		// 编写分隔符
		params.append(prefix + boundary + newLine);
		// 键值说明
		params.append("Content-Disposition: form-data; name=\"ticket\"");
		// 如果内容不是文件,不用申明文件类型
		params.append(newLine + newLine);
		// 内容
		params.append("bcpmai");
		params.append(newLine);
		
		// 第二条数据 分隔符
		params.append(prefix + boundary + newLine);
		// 键值说明
		params.append("Content-Disposition: form-data; name=\"file\"; filename=\"测试.JPG\"");
		params.append(newLine);
		// 键值类型
		params.append("Content-Type: image/pjpeg");
		params.append(newLine + newLine);
		File file = new File("F:\\测试.JPG");
		try {
			InputStream in = new FileInputStream(file);
			out.write(params.toString().getBytes());
			// 第二条数据内容
			out.write(readBuffer(in));
			out.write(newLine.getBytes());
			// 协议内容结尾
			out.write((prefix + boundary + prefix + newLine).getBytes());
			out.flush();
			out.close();
		} catch (FileNotFoundException e) {
			System.out.println(" 没有找到文件 ");
		} catch (IOException e) {
			System.out.println(" 文件IO错误 ");
		}
	}

	public static byte[] readBuffer(InputStream ins) throws IOException {
		byte b[] = new byte[1024];
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		int len = 0;
		while ((len = ins.read(b)) != -1) {
			stream.write(b, 0, len);
		}
		return stream.toByteArray();
	}
}