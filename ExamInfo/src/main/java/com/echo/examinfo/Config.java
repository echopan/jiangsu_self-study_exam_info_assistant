package com.echo.examinfo;

import java.awt.*;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Echo Pan
 * @version 1.0
 * @date 2024/11/28 10:40
 * @description 基本配置
 */
class Config {
    //窗口基础样式
    final static int SCROLL_BAR_WIDTH = 6;
    final static int CORNER_SIZE = 6;//角标大小
    final static Color TRANSPARENT = new Color(0, 0, 0, 0);
    final static Color GRAY = new Color(50, 50, 50);
    final static Color BLUE = new Color(15, 102, 235);
    final static Color DEEP_BLUE = new Color(12, 62, 152);
    final static Font SongTi = new Font("SimSun", Font.ITALIC, 12);
    final static Font SongTiLarger = new Font("SimSun", Font.PLAIN, 13);
    final static Font HeiTi = new Font("SimHei", Font.PLAIN, 13);
    final static Font HeiTiSmaller = new Font("SimHei", Font.PLAIN, 12);
    final static int SCROLL_UNIT_INCREMENT = 15;
    final static int SCROLL_BLOCK_INCREMENT = 15;
    //网络连接
    final static int CONNECTION_TIMEOUT = 3000;
    final static int READ_TIMEOUT = 4000;
    final static int RETRY_WAITING = 200;
    final static int MAX_RETRIES = 3;
    //代号
    final static int ALL_PERFECT = 900;
    final static int FILE_NOT_FOUND = 901;
    final static int SCORE_TOKEN_NOT_FOUND = 902;
    final static int PRACTICE_TOKEN_NOT_FOUND = 903;
    final static int ALL_TOKEN_NOT_FOUND = 904;
    final static int UNKNOWN_ERROR = 999;
    //分辨率
    public final static DisplayMode DEFAULT_DISPLAY_MODE = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode();//默认屏幕的默认分辨率
    public final static int SYSTEM_SCREEN_HEIGHT = DEFAULT_DISPLAY_MODE.getHeight();
    public final static int SYSTEM_SCREEN_WIDTH = DEFAULT_DISPLAY_MODE.getWidth();//主屏幕显示分辨率
    public final static int SCREEN_WIDTH = Toolkit.getDefaultToolkit().getScreenSize().width;//DPI缩放后分辨率
    public final static double DPI = Math.round(100.0 * SYSTEM_SCREEN_WIDTH / SCREEN_WIDTH) / 100.0;
    public final static int SCREEN_HEIGHT = Toolkit.getDefaultToolkit().getScreenSize().height;
    //窗口默认样式
    final static int winX = SCREEN_WIDTH / 3;
    final static int winY = SCREEN_HEIGHT / 3;
    final static int winWidth = (int) (SCREEN_WIDTH / 3.5);
    final static int winHeight = SCREEN_HEIGHT / 3;
    final static float winOpacity = 0.7f;
    //本地文件
    private final static File iniSecret = new File("conf/Secret.ini");
    private final static File ini = new File("conf/ExamInfo.ini");
    private final static File tessdata = new File("tessdata");//tessdata默认路径
    final static URL icon = ExamInfo.class.getResource("/pic/jssjyksy.png");
    final static File txtPractice =new File("data/practice.txt");
    final static File txtLog =new File("log/log.txt");
    final static File txtLogOld =new File("log/log.old.txt");
    final static File txtScore =new File("data/score.txt");
    final static File txtInfoList =new File("data/infolist.txt");
    final static File txtInfoListOld =new File("data/infolist.old.txt");
    //配置信息
    static Map<String, String> iniSecretMap;
    static Map<String, String> iniMap;
    static Map<String, String> aliasMap;
    static Map<String, String> practiceMap;
    //其他
    static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd 00:00:00");//日期格式
    static{
        try {
            Files.createDirectories(Paths.get("conf"));
            Files.createDirectories(Paths.get("data"));
            Files.createDirectories(Paths.get("log"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //初始化
    static {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("name", "姓 名");
        map.put("id", "准考证");
        map.put("school", "主考学校");
        aliasMap = Collections.synchronizedMap(map);
    }

    static {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("name", "");
        map.put("id", "");
        map.put("school", "");
        iniSecretMap = Collections.synchronizedMap(map);
    }

    static {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("winX", "" + winX);
        map.put("winY", "" + winY);
        map.put("winWidth", "" + winWidth);
        map.put("winHeight", "" + winHeight);
        map.put("winOpacity", "" + winOpacity);
        map.put("autoStart", ""+0);
        map.put("alwaysOnTop", ""+0);
        map.put("tessdata", tessdata.getName());
        iniMap = Collections.synchronizedMap(map);
    }
    static {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("school", "");
        map.put("start", "");
        map.put("end", "");
        practiceMap = Collections.synchronizedMap(map);
    }
    /**
     * 获取成绩查询参数
     */
    static String getScoreQueryParams() {
        return "ksmx=" + iniSecretMap.get("name") + "&zkzh=" + iniSecretMap.get("id");
    }

    /**
     * 同步个人信息
     */
    static void syncMySecret() {
        syncSettingsFile(iniSecret, iniSecretMap);
    }

    /**
     * 同步偏好信息
     */
    static void syncMyPreference() {
        syncSettingsFile(ini, iniMap);
    }

    /**
     * 同步实践信息
     */
    static void syncPracticeEnrol() {
        syncSettingsFile(txtPractice, practiceMap);
    }

    /** 加载实践报名信息*/
    static void loadPracticeEnrol(){
        if(!txtPractice.exists()){
            syncSettingsFile(txtPractice,practiceMap);
        } else {
            readSettingsFile(practiceMap,txtPractice);
        }
    }

    /**
     * 加载偏好设置
     */
    static void loadMyPreference() {
            if (!ini.exists()) {
                syncSettingsFile(ini, iniMap);
            } else {
                readSettingsFile(iniMap,ini);
            }
    }

    /**
     * 加载个人信息, 返回:
     * ALL_PERFECT 900,
     * FILE_NOT_FOUND 901,
     * SCORE_TOKEN_NOT_FOUND 902,
     * PRACTICE_TOKEN_NOT_FOUND 903,
     * ALL_TOKEN_NOT_FOUND 904,
     * UNKNOWN_ERROR 999
     */
    static int loadMySecret() {
        int load = ALL_PERFECT;
            if (!iniSecret.exists()) {
                //文件不存在
                load = FILE_NOT_FOUND;
                syncSettingsFile(iniSecret, iniSecretMap);
            } else {
                if(readSettingsFile(iniSecretMap,iniSecret)){
                    if ("".equals(iniSecretMap.get("name")) || "".equals(iniSecretMap.get("id"))) load = SCORE_TOKEN_NOT_FOUND;
                    if ("".equals(iniSecretMap.get("school")))
                        load = load == SCORE_TOKEN_NOT_FOUND ? ALL_TOKEN_NOT_FOUND : PRACTICE_TOKEN_NOT_FOUND;
                }else{
                    load = UNKNOWN_ERROR;
                }
            }
        return load;
    }
    /** 读取本地文件*/
    private static boolean readSettingsFile( Map map,File file){
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
            String line;
            String[] arr;
            while ((line = br.readLine()) != null) {
                arr = line.trim().split("=");
                if (arr.length == 2) {
                    map.put(arr[0].trim(), arr[1].trim());
                }
            }
        }catch(IOException e) {
            ExamInfo.createLog(e);
            return false;
        }
        return true;
    }
    /**
     * 同步数据到本地文件
     */
    private static boolean syncSettingsFile(File file, Map map) {
        StringBuffer sb = new StringBuffer();
        Set<String> set = map.keySet();
        for (String t : set) {
            sb.append(t).append("=").append(map.get(t)).append("\n");
        }
        try {
            Files.createDirectories(Paths.get(file.getParentFile().getPath()));
            PrintWriter out;
            out = new PrintWriter(file, StandardCharsets.UTF_8);
            out.print(sb);
            out.flush();
            out.close();
        } catch (IOException e) {
            ExamInfo.createLog(e);
            return false;
        }
        return true;
    }
}
