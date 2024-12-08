package com.echo.examinfo;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * @author Echo Pan
 * @version 1.0
 * @date 2023/12/1 21:00
 * @description 连接网页
 */
class MyHttpRequest {
    private static final Map<String, String> headers = new HashMap<>();//请求头

    static {
        headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7");
        headers.put("Accept-Language", "zh-CN,zh;q=0.9");
        headers.put("Accept-Encoding", "gzip, deflate, br, zstd");
        headers.put("Connection", "keep-alive");
        headers.put("Cache-Control", "max-age=0");
        //headers.put("Sec-Ch-Ua", "\"Chromium\";v=\"122\", \"Not(A:Brand\";v=\"24\", \"Google Chrome\";v=\"122\"");
        headers.put("sec-ch-ua", "\"Google Chrome\";v=\"129\", \"Not=A?Brand\";v=\"8\", \"Chromium\";v=\"129\"");
        headers.put("sec-ch-ua-mobile", "?0");
        headers.put("sec-ch-ua-platform", "\"Windows\"");//返回类型
        headers.put("Sec-Fetch-Dest", "document");
        headers.put("Sec-Fetch-Mode", "navigate");
        headers.put("Sec-Fetch-Site", "same-origin");
        headers.put("Sec-Fetch-User", "?1");
        headers.put("Upgrade-Insecure-Requests", "1");
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.0.0 Safari/537.36");
    }

    /**
     * @param reqs 格式: {header.property = [value]}
     */
    public static WebPage doPost(String httpurl, String param, Map<String, String> reqs) {
        System.out.println("POST:" + httpurl);
        HttpURLConnection conn = null;
        InputStream in = null;
        OutputStream out = null;
        BufferedReader read = null;
        WebPage site = new WebPage();
        StringBuffer strbf;
        String line;
        URL url;
        Map<String, String> headers = new HashMap<>(MyHttpRequest.headers);
        if (null != reqs) headers.putAll(reqs);
        int retryCounter = 0;
        boolean ifRetry = true;
        while (retryCounter < Config.MAX_RETRIES && ifRetry) {
            System.out.println("第" + (retryCounter + 1) + "次连接");
            ifRetry = false;
            site = new WebPage();
            site.setCookies(WebPage.cutCookies(reqs));
            try {
                //trustAllHosts();
                url = new URL(httpurl);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setInstanceFollowRedirects(false);
                conn.setConnectTimeout(Config.CONNECTION_TIMEOUT);
                conn.setReadTimeout(Config.READ_TIMEOUT);
                conn.setDoOutput(true);
                conn.setDoInput(true);
                for (String key : headers.keySet()) {
                    conn.setRequestProperty(key, headers.get(key));
                    // System.out.println(key + ":" + headers.get(key));
                }
                System.out.println("post请求头:" + headers);
                if (null != param) {
                    out = conn.getOutputStream();
                    out.write(param.getBytes(StandardCharsets.UTF_8));
                    out.flush();
                }
                //获取header
                site.setHeader(conn.getHeaderFields());
                int status = conn.getResponseCode();
                System.out.println("Post响应头:" + conn.getHeaderFields());
                System.out.println("状态码:" + status);
                //
                if (status == 302) {
                    System.out.println("URL重定向");
                    String location = url.getProtocol() + "://" + url.getHost() + conn.getHeaderField("Location");
                    return handleRedirects(new URL(location), site.getCooStrMap());
                } else {
                    if (status == 200) in = conn.getInputStream();
                    else in = conn.getErrorStream();
                    // byte[] data = in.readAllBytes();
                    // site.setContent(testEncoding(data,StandardCharsets.UTF_8));
                    // testEncoding(data, Charset.forName("GBK"));
                    if ("gzip".equalsIgnoreCase(conn.getHeaderField("Content-Encoding"))) {
                        read = new BufferedReader(new InputStreamReader(new GZIPInputStream(in), StandardCharsets.UTF_8));//指定后不再受VM参数影响乱码
                    } else {
                        read = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
                    }
                    strbf = new StringBuffer();
                    while ((line = read.readLine()) != null) {
                        strbf.append(line);
                        strbf.append("\r\n");
                    }
                    site.setContent(strbf.toString());
                }
            } catch (Exception e) {
                ifRetry = true;
                retryCounter++;
                if (retryCounter >= Config.MAX_RETRIES) {
                    ExamInfo.createLog(e);
                } else {
                    try {
                        Thread.sleep(Config.RETRY_WAITING);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            } finally {
                if (null != read) {
                    try {
                        read.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (null != out) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (null != in) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (null != conn) conn.disconnect();
            }
        }
        return site;
    }

    /**
     * @param reqs 格式: {header.property = [value]}
     */
    public static WebPage handleRedirects(URL url, Map<String, String> reqs) {
        BufferedReader read = null;
        HttpURLConnection conn = null;
        String line;
        StringBuilder response;
        InputStream in = null;
        String origin = url.getProtocol() + "://" + url.getHost();
        String currentUrl = url.toString();
        WebPage site = new WebPage();
        System.out.println("current:" + currentUrl);
        int counter = 1;
        try {
            Map<String, String> headers = new HashMap<>(MyHttpRequest.headers);
            while (true) {
                System.out.println("重定向" + counter++ + "次");

                conn = (HttpURLConnection) new URL(currentUrl).openConnection();
                conn.setInstanceFollowRedirects(false); // 关闭自动重定向
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(Config.CONNECTION_TIMEOUT);
                conn.setReadTimeout(Config.READ_TIMEOUT);
                conn.setDoOutput(true);
                conn.setDoInput(true);
                if (null != reqs) {
                    headers.putAll(reqs);
                    site.setCookies(WebPage.cutCookies(reqs));
                }
                //headers.put("Referer", currentUrl);
                // 设置请求头
                for (String key : headers.keySet()) {
                    conn.setRequestProperty(key, headers.get(key));
                }
                System.out.println("redirect头:" + headers);
                int status = conn.getResponseCode();
                site.setHeader(conn.getHeaderFields());
                System.out.println("响应头:" + site.getHeader());
                System.out.println("状态码:" + status);
                if (status == 302) {
                    // 处理重定向
                    String location = conn.getHeaderField("Location");
                    if (null != location) {
                        currentUrl = origin + location; // 确保 URL 是完整的
                        System.out.println("target:" + currentUrl);
                        reqs = site.getCooStrMap();
                        site = new WebPage();
                    }
                } else {
                    // 读取响应内容
                    if (status == 200) in = conn.getInputStream();
                    else in = conn.getErrorStream();
                    if ("gzip".equalsIgnoreCase(conn.getHeaderField("Content-Encoding"))) {
                        read = new BufferedReader(new InputStreamReader(new GZIPInputStream(in), StandardCharsets.UTF_8));
                    } else {
                        read = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
                    }
                    response = new StringBuilder();
                    while ((line = read.readLine()) != null) {
                        response.append(line);
                        response.append("\r\n");
                    }
                    site.setContent(response.toString());
                    break;
                }
            }
        } catch (Exception e) {
            ExamInfo.createLog(e);
        } finally {
            if (null != read) {
                try {
                    read.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (null != in) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (null != conn) conn.disconnect();
        }
        return site;
    }

    private static String testEncoding(byte[] data, java.nio.charset.Charset charset) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(data), charset))) {
            StringBuffer strbf = new StringBuffer();
            String line;
            while ((line = reader.readLine()) != null) {
                strbf.append(line);
                strbf.append("\r\n");
            }
            System.out.println("Decoded with " + charset.name() + ":\n" + strbf.toString());
            return "Decoded with " + charset.name() + ":\n" + strbf.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @param reqs 格式: {header.property = [value]}
     */
    public static WebPage doGet(String httpurl, Map<String, String> reqs, String type) {
        System.out.println("GET:" + httpurl);
        Map<String, String> headers = new HashMap<>(MyHttpRequest.headers);
        HttpURLConnection conn = null;
        InputStream in = null;
        BufferedReader read = null;
        WebPage site = new WebPage();
        StringBuffer strbf;
        String line;
        URL url;
        type = null == type ? "document" : type;
        int retryCounter = 0;
        boolean ifRetry = true;
        if (null != reqs) headers.putAll(reqs);
        while (retryCounter < Config.MAX_RETRIES && ifRetry) {
            System.out.println("第" + (retryCounter + 1) + "次连接");
            ifRetry = false;
            site = new WebPage();
            site.setCookies(WebPage.cutCookies(reqs));
            try {
                //trustAllHosts();
                url = new URL(httpurl);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(Config.CONNECTION_TIMEOUT);
                conn.setReadTimeout(Config.READ_TIMEOUT);
                for (String key : headers.keySet()) {
                    conn.setRequestProperty(key, headers.get(key));
                    // System.out.println(key + ":" + headers.get(key));
                }
                System.out.println("get请求头:" + headers);
                conn.connect();
                //获取header
                site.setHeader(conn.getHeaderFields());
                System.out.println("状态码:" + conn.getResponseCode());
                System.out.println("get响应头" + conn.getHeaderFields());
                if (conn.getResponseCode() == 200) in = conn.getInputStream();
                else in = conn.getErrorStream();
                if ("document".equals(type)) {
                    if ("gzip".equalsIgnoreCase(conn.getHeaderField("Content-Encoding"))) {
                        read = new BufferedReader(new InputStreamReader(new GZIPInputStream(in), StandardCharsets.UTF_8));
                    } else {
                        read = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
                    }
                    strbf = new StringBuffer();
                    while ((line = read.readLine()) != null) {
                        strbf.append(line);
                        strbf.append("\r\n");
                    }
                    site.setContent(strbf.toString());
                } else if ("file".equals(type)) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = in.read(buffer)) != -1) {
                        baos.write(buffer, 0, len);
                    }
                    site.setFileData(baos.toByteArray());
                }
            } catch (Exception e) {
                ifRetry = true;
                retryCounter++;
                if (retryCounter >= Config.MAX_RETRIES) {
                    ExamInfo.createLog(e);
                    site.setNote(WebPage.TIMEOUT);
                } else {
                    try {
                        Thread.sleep(Config.RETRY_WAITING);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            } finally {
                if (null != read) {
                    try {
                        read.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (null != in) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (null != conn) conn.disconnect();
            }
        }
        return site;
    }

    /*跳过https证书验证*/
    private static void trustAllHosts() {
        /*用于验证X.509证书的接口,它可以用于覆盖证书验证逻辑*/
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                }
        };
        try {
            SSLContext sc = SSLContext.getInstance("SSL", "SunJSSE");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            /*确保在进行SSL连接时,对主机名进行正确的验证,以防止中间人攻击*/
            HttpsURLConnection.setDefaultHostnameVerifier((s, sslSession) -> true);
        } catch (NoSuchProviderException | NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
            ExamInfo.createLog(e);
        }
    }
}
