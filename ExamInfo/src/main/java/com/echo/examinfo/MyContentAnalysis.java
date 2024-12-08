package com.echo.examinfo;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Echo Pan
 * @version 1.0
 * @date 2023/12/1 21:01
 * @description 解析返回的内容
 */
class MyContentAnalysis {
    /**
     * 解析成绩表
     */
    public static Vector<Score> getScore(String str) {
        if (null == str) {
            return null;
        }
        Pattern score = Pattern.compile("(?is)<table.*?</table>");
        String rs;
        Matcher ma = score.matcher(str);
        Vector<Score> scoreList = null;
        String[] arr;
        if (ma.find()) {
            scoreList = new Vector<>();
            rs = ma.group().replaceAll("((<|</)table.*?>|<tr>|<(th|td).*?>|[\s\t\r\n])", "")
                    .replaceAll("</(th|td)>", "\t").replaceAll("</tr>", "\n");
            InputStream in = new ByteArrayInputStream(rs.getBytes());
            BufferedReader read = new BufferedReader(new InputStreamReader(in));
            String temp;
            try {
                while ((temp = read.readLine()) != null) {
                    arr = temp.split("\\t");
                    if (arr.length == 6) {
                        Score scoreInfo = new Score(arr[0], arr[1], arr[2], arr[3], arr[4], arr[5]);
                        scoreList.add(scoreInfo);
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return scoreList;
    }

    /**
     * 解析消息列表
     */
    public static Vector<Info> getList(String str) {
        if (null == str) {
            return null;
        }
        //?s单行模式,?i不区分大小写,*?最小匹配
        Pattern list = Pattern.compile("(?is)(?<=<ul class=\"content-list-ul news-list\">).*?(?=</ul>)");
        String rs;
        Matcher ma = list.matcher(str);
        Vector<Info> infoList = null;
        if (ma.find()) {
            infoList = new Vector<>();
            rs = ma.group().replaceAll("(<|</)li>(\r\n|\n\r|\n)", "").replaceAll("(<)a.*href=\"/{2}", "").replaceAll(" target=.*?>", "").replaceAll("[\s\t\n\r]", "")
                    .replaceAll("(\"|<span>)", "\t").replaceAll("</span>", "\n").replaceAll("</a>", "");
            InputStream in = new ByteArrayInputStream(rs.getBytes());
            BufferedReader read = new BufferedReader(new InputStreamReader(in));
            String temp;
            Pattern zk = Pattern.compile("(?s).*(自考|自学考试).*(报名|成绩|免考|毕业|准考证|考前|提醒|日程|申请|招聘|就业).*");
            try {
                while ((temp = read.readLine()) != null) {
                    ma = zk.matcher(temp);
                    if (ma.matches()) {
                        String[] arr = temp.split("\\t");
                        Info info = new Info(arr[1], arr[2], arr[0]);
                        infoList.add(info);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return infoList;
    }

    /**
     * 解析考核院校 khxx
     */
    public static String getSchollId(String content, String school) {
        if (null == content || null == school) {
            return null;
        }
        //?s单行模式,?i不区分大小写,*?最小匹配
        String reg = "(?is)(?<=<td.{0,50}?" + school.trim() + ".{1,600}?khxx=\").*?(?=\")";
        Pattern zkxx = Pattern.compile(reg);
        String rs = null;
        Matcher ma = zkxx.matcher(content);
        if (ma.find()) rs = ma.group();
        return rs;
    }

    /**
     * 解析实践报名时间
     */
    public static Map<String, String> getPracticeRegiInfo(String content) {
        if (null == content) {
            return null;
        }
        //?s单行模式,?i不区分大小写,*?最小匹配
        Map<String, String> practiceInfo = null;
        String name = "(?is)(?<=<tr>\\s{0,}<td.{0,20}?>).*?(?=</td>)";
        String date = "(?is)(?<=>)\\d.{1,20}?~.{1,20}?(?=</td>)";
        Matcher nameMa = Pattern.compile(name).matcher(content);
        Matcher dateMa = Pattern.compile(date).matcher(content);
        if (nameMa.find()) {
            practiceInfo = new LinkedHashMap<>();
            practiceInfo.put("school", nameMa.group());
            if (dateMa.find()) {
                String[] dates = dateMa.group().split("~");
                practiceInfo.put("start", dates[0].trim());
                practiceInfo.put("end", dates[1].trim());
            }
        }
        return practiceInfo;
    }
}
