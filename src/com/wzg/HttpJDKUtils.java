package com.wzg;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class HttpJDKUtils {
	private static HttpJDKUtils instance;

	public static HttpJDKUtils getInstance() {
		if (null == instance) {
			instance = new HttpJDKUtils();
		}
		return instance;
	}

	private HttpJDKUtils() {
		jdk_version = Float.parseFloat(System
				.getProperty("java.specification.version"));
	}

	/**
	 * jdk版本
	 */
	private float jdk_version;

	/**
	 * 连接超时
	 */
	private int connectTimeOut = 5000;

	/**
	 * 读取数据超时
	 */
	private int readTimeOut = 10000;

	/**
	 * 默认请求编码UTF-8
	 */
	private String requestEncoding = "UTF-8";

	/**
	 * 默认响应编码UTF-8
	 */
	private String responseEncoding = "UTF-8";

	/**
	 * POST请求方式
	 */
	private final String POSTMETHOD = "POST";

	/**
	 * GET请求方式
	 */
	private final String GETMETHOD = "GET";

	/**
	 * @return 连接超时(毫秒)
	 * @see com.HttpJDKUtils.common.web.HttpRequestProxy#connectTimeOut
	 */
	public int getConnectTimeOut() {
		return this.connectTimeOut;
	}

	/**
	 * 
	 * @return
	 */
	public String getResponseEncoding() {
		return responseEncoding;
	}

	/**
	 * 设置响应编码
	 * 
	 * @param responseEncoding
	 */
	public void setResponseEncoding(String responseEncoding) {
		this.responseEncoding = responseEncoding;
	}

	/**
	 * @return 读取数据超时(毫秒)
	 * @see com.HttpJDKUtils.common.web.HttpRequestProxy#readTimeOut
	 */
	public int getReadTimeOut() {
		return this.readTimeOut;
	}

	/**
	 * @return 请求编码
	 * @see com.HttpJDKUtils.common.web.HttpRequestProxy#requestEncoding
	 */
	public String getRequestEncoding() {
		return this.requestEncoding;
	}

	/**
	 * @param connectTimeOut
	 *            连接超时(毫秒)
	 * @see com.HttpJDKUtils.common.web.HttpRequestProxy#connectTimeOut
	 */
	public void setConnectTimeOut(int connectTimeOut) {
		this.connectTimeOut = connectTimeOut;
	}

	/**
	 * @param readTimeOut
	 *            读取数据超时(毫秒)
	 * @see com.HttpJDKUtils.common.web.HttpRequestProxy#readTimeOut
	 */
	public void setReadTimeOut(int readTimeOut) {
		this.readTimeOut = readTimeOut;
	}

	/**
	 * @param requestEncoding
	 *            请求编码
	 * @see com.HttpJDKUtils.common.web.HttpRequestProxy#requestEncoding
	 */
	public void setRequestEncoding(String requestEncoding) {
		this.requestEncoding = requestEncoding;
	}

	/**
	 * <pre>
	 * 发送带参数的GET的HTTP请求
	 * </pre>
	 * 
	 * @param reqUrl
	 *            HTTP请求URL
	 * @param parameters
	 *            参数映射表
	 * @return HTTP响应的字符串
	 */
	public String doGet(String reqUrl, Map<String, Object> parameters) {
		byte[] data = buildRequestData(parameters);
		return requestAndResp(reqUrl, this.GETMETHOD, data);
	}

	/**
	 * <pre>
	 * 发送不带参数的GET的HTTP请求
	 * </pre>
	 * 
	 * @param reqUrl
	 *            HTTP请求URL
	 * @return HTTP响应的字符串
	 */
	public String doGet(String reqUrl) {
		String queryUrl = reqUrl;
		String parameters = "";
		int paramIndex = reqUrl.indexOf("?");
		if (paramIndex > 0) {
			parameters = reqUrl.substring(paramIndex + 1, reqUrl.length());
			queryUrl = reqUrl.substring(0, paramIndex);
		}
		byte[] data = buildRequestData(parameters);
		return requestAndResp(queryUrl, this.GETMETHOD, data);
	}

	/**
	 * <pre>
	 * 发送带参数的POST的HTTP请求
	 * </pre>
	 * 
	 * @param reqUrl
	 *            HTTP请求URL
	 * @param parameters
	 *            参数映射表
	 * @return HTTP响应的字符串
	 */
	public String doPost(String reqUrl, Map<String, Object> parameters) {
		byte[] data = buildRequestData(parameters);
		return requestAndResp(reqUrl, this.POSTMETHOD, data);
	}

	private byte[] buildRequestData(Map<String, Object> parameters) {
		StringBuffer params = new StringBuffer();
		try {
			for (Iterator<Entry<String, Object>> iter = parameters.entrySet()
					.iterator(); iter.hasNext();) {
				Entry<String, Object> element = iter.next();
				params.append(element.getKey());
				params.append("=");
				params.append(URLEncoder.encode(element.getValue().toString(),
						this.requestEncoding));
				params.append("&");
			}
		} catch (UnsupportedEncodingException e) {
			System.out.println("不支持的编码格式:" + e.getMessage());
		}
		if (params.length() > 0) {
			params = params.deleteCharAt(params.length() - 1);
		}
		return params.toString().getBytes();
	}

	private byte[] buildRequestData(String parameters) {
		StringBuffer params = new StringBuffer();
		String[] paramArray = parameters.split("&");
		try {
			for (int i = 0; i < paramArray.length; i++) {
				String string = paramArray[i];
				int index = string.indexOf("=");
				if (index > 0) {
					String parameter = string.substring(0, index);
					String value = string.substring(index + 1, string.length());
					params.append(parameter);
					params.append("=");
					params.append(URLEncoder
							.encode(value, this.requestEncoding));
					params.append("&");
				}
			}
		} catch (UnsupportedEncodingException e) {
			System.out.println("不支持的编码格式:" + e.getMessage());
		}
		params = params.deleteCharAt(params.length() - 1);
		return params.toString().getBytes();
	}

	private String requestAndResp(String reqUrl, String method, byte[] b) {
		HttpURLConnection url_con = null;
		OutputStream os = null;
		InputStream is = null;
		String responseContent = null;
		try {
			URL url = new URL(reqUrl);
			url_con = (HttpURLConnection) url.openConnection();
			url_con.setRequestMethod(method);
			if (this.jdk_version > 1.4) {
				url_con.setConnectTimeout(this.connectTimeOut);// （单位：毫秒）jdk1.5换成这个,连接超时
				url_con.setReadTimeout(this.readTimeOut);// （单位：毫秒）jdk1.5换成这个,读操作超时
			} else {
				System.setProperty("sun.net.client.defaultConnectTimeout",
						String.valueOf(this.connectTimeOut));// 单位：毫秒）jdk1.4换成这个,连接超时
				System.setProperty("sun.net.client.defaultReadTimeout",
						String.valueOf(this.readTimeOut)); // （单位：毫秒）jdk1.4换成这个,读操作超时
			}
			url_con.setDoOutput(true);
			os = url_con.getOutputStream();
			os.write(b, 0, b.length);
			os.flush();
			os.close();

			is = url_con.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is,
					this.getResponseEncoding()));
			String tempLine = rd.readLine();
			StringBuffer tempStr = new StringBuffer();
			String crlf = System.getProperty("line.separator");
			while (tempLine != null) {
				tempStr.append(tempLine);
				tempStr.append(crlf);
				tempLine = rd.readLine();
			}
			responseContent = tempStr.toString();
			rd.close();
			is.close();
		} catch (IOException e) {
			System.out.println("网络故障" + e.getMessage());
		} finally {
			if (url_con != null) {
				url_con.disconnect();
			}
		}
		return responseContent;
	}

	private String uploadFile(String uploadUrl, File file,
			Map<String, Object> parameters) {
		String respData = null;
		String boundary = "--------------7d226f700d0";
		String prefix = "--";
		String newLine = "\r\n";
		StringBuffer params = new StringBuffer();
		if (null != parameters && parameters.size() > 0) {
			for (Iterator<Entry<String, Object>> iter = parameters.entrySet()
					.iterator(); iter.hasNext();) {
				Entry<String, Object> element = iter.next();
				// 编写分隔符
				params.append(prefix + boundary + newLine);
				// 键值说明
				params.append("Content-Disposition: form-data; name=\"");
				params.append(element.getKey());
				params.append("\"");
				// 如果内容不是文件,不用申明文件类型
				params.append(newLine + newLine);
				// 内容
				params.append(element.getValue().toString());
				params.append(newLine);
			}
		}
		// 第二条数据 分隔符
		params.append(prefix + boundary + newLine);
		// 键值说明
		params.append("Content-Disposition: form-data; name=\"file\"; filename=\"");
		params.append(file.getName());
		params.append("\"");
		params.append(newLine);
		// 键值类型
		params.append("Content-Type: image/pjpeg");
		params.append(newLine + newLine);

		HttpURLConnection httpURLConnection = null;
		try {
			URL url = new URL(uploadUrl);
			httpURLConnection = (HttpURLConnection) url.openConnection();
			// 设置每次传输的流大小，可以有效防止手机因为内存不足崩溃
			// 此方法用于在预先不知道内容长度时启用没有进行内部缓冲的 HTTP 请求正文的流。
			httpURLConnection.setChunkedStreamingMode(128 * 1024);// 128K
			// 允许输入输出流
			httpURLConnection.setDoInput(true);
			httpURLConnection.setDoOutput(true);
			httpURLConnection.setUseCaches(false);
			// 设定HTTP协议头
			httpURLConnection.setRequestProperty("Content-type",
					"multipart/form-data;boundary=" + boundary);
			OutputStream out = new DataOutputStream(
					httpURLConnection.getOutputStream());
			InputStream in = new FileInputStream(file);
			out.write(params.toString().getBytes());
			// 第二条数据内容
			out.write(readInputStreamToByteArr(in));
			out.write(newLine.getBytes());
			// 协议内容结尾
			out.write((prefix + boundary + prefix + newLine).getBytes());
			in.close();
			out.flush();
			out.close();

			InputStream iStream = httpURLConnection.getInputStream();
			byte[] resp = readInputStreamToByteArr(iStream);
			respData = new String(resp, this.getResponseEncoding());
			iStream.close();
		} catch (MalformedURLException e) {
			System.out.println("URL地址解析错误");
		} catch (FileNotFoundException e) {
			System.out.println("没有找到文件");
		} catch (IOException e) {
			System.out.println("文件IO错误");
		} finally {
			if (httpURLConnection != null) {
				httpURLConnection.disconnect();
			}
		}
		return respData;
	}

	private byte[] readInputStreamToByteArr(InputStream ins) throws IOException {
		byte b[] = new byte[1024];
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		int len = 0;
		while ((len = ins.read(b)) != -1) {
			stream.write(b, 0, len);
		}
		return stream.toByteArray();
	}

	public static void main(String[] args) throws UnsupportedEncodingException {
		HttpJDKUtils http = HttpJDKUtils.getInstance();
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("ticket", "中文");

		// post提交
		String postMsg = http.doPost("http://192.168.1.237/newucai/post.php",
				map);
		System.out.println("返回的消息是:" + postMsg);

		// get提交方式一
		String getMsg1 = http.doGet("http://192.168.1.237/newucai/post.php",
				map);
		System.out.println("返回的消息是:" + getMsg1);

		// get提交方式二
		String getMsg2 = http
				.doGet("http://192.168.1.237/newucai/post.php?ticket=中文");
		System.out.println("返回的消息是:" + getMsg2);

		// 上传文件
		String resp = http.uploadFile(
				"http://192.168.1.237/newucai/uploadfile.php", new File(
						"F:\\测试.JPG"), map);
		System.out.println(resp);
	}

}
