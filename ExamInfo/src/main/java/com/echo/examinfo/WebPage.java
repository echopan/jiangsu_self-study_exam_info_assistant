package com.echo.examinfo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Echo Pan
 * @version 1.0
 * @date 2024/11/23 21:41
 * @description 网页信息类
 */
class WebPage {
    public final static int TIMEOUT=408;
    private String content;
    private Map<String, List<String>> header;//响应头
    private Map<String, String> cookies;
    private byte[] fileData;
    private int note;//备注

    public WebPage() {
        header = new HashMap<>();
        cookies = new HashMap<>();
        note=0;
    }

    public WebPage(String content, Map<String, List<String>> header) {
        this();
        this.content = content;
        this.header = header;
    }

    public WebPage(String content, List<String> cookies) {
        this();
        this.content = content;
        list2Map(cookies);
    }

    public int getNote() {
        return note;
    }

    public void setNote(int note) {
        this.note = note;
    }

    public byte[] getFileData() {
        return fileData;
    }

    public void setFileData(byte[] fileData) {
        this.fileData = fileData;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Map<String, List<String>> getHeader() {
        System.out.println("responsed header:" + header);
        return header;
    }

    public void setHeader(Map<String, List<String>> header) {
        this.header.putAll(header);
        /** 获取cookies数组,用于验证用户*/
        list2Map(header.get("Set-Cookie"));
        System.out.println("set responsed header.cookies(不应包含Cookie总头):" + cookies);
    }
    public static Map<String,String> cutCookies(Map<String,String> coStrMap){
        Map<String,String> cookiesMap = new HashMap<>();
        if (coStrMap!=null){
            String[] coStrArr=coStrMap.get("Cookie").split(";");
            for (String co : coStrArr) {
                String[] cookieNV = co.split("=");
                if (cookieNV.length == 2) {
                    cookiesMap.put(cookieNV[0].trim(), cookieNV[1].trim());
                }
            }
        }
        return cookiesMap;
    }
    public void list2Map(List<String> cookieList) {
        if (cookieList != null) {
            for (String cookie : cookieList) {
                String[] cookieSplit = cookie.split(";", 2);
                String[] cookieNV = cookieSplit[0].split("=");
                if (cookieNV.length == 2) {
                    cookies.put(cookieNV[0].trim(), cookieNV[1].trim());
                }
            }
        }
    }
    /** 返回 Cookies 数组, 格式: {key=[value],key=[value],...}*/
    public Map<String, String> getCookies() {
        if (null == this.cookies) this.cookies = new HashMap<>();
        System.out.println("get site.cookies:(不应包含Cookie总头)" + cookies);
        return this.cookies;
    }

    /**
     * 传入分开的Cookie条目,如果条目已存在, 则忽略
     */
    public WebPage setCookies(Map<String, String> cookies) {
        if (null == cookies) return this;
        Map<String, String> temp = new HashMap<>(cookies);
        temp.putAll(this.cookies);
        this.cookies = temp;
        return this;
    }

    /**
     * 返回 Cookies 合成值, 格式: {Cookie=[value]}
     */
    public Map<String, String> getCooStrMap() {
        Map<String, String> cookieMap = new HashMap<>();
        String str = "";
        if (cookies.size() != 0) {
            for (String cookie : cookies.keySet()) {
                str += cookie + "=" + cookies.get(cookie) + "; ";
            }
            str = str.trim().substring(0, str.length() - 2);

        }
        cookieMap.put("Cookie", str);
        System.out.println("用于网页请求的cookies(不应包含Cookie总头):" + str);
        return cookieMap;
    }

    public void saveFile(String filePath) {
        if (fileData == null) {
            return;
        }
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            baos.write(fileData);
            java.nio.file.Files.write(java.nio.file.Paths.get(filePath), baos.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
