package com.echo.examinfo;

import java.util.Vector;

/**
 * @author Echo Pan
 * @version 1.0
 * @date 2023/12/2 1:20
 * @description 通知信息类
 */
class Info {
    String content;
    String date;
    String link;

    public Info() {
    }

    public Info(String content, String date) {
        this();
        this.content = content;
        this.date = date;
    }

    public Info(String content, String date, String link) {
        this();
        this.content = content;
        this.date = date;
        this.link = link;
    }

    /**
     * 去重(保留最新的一项)
     */
    public static void dedupInfo(Vector<Info> v) {
        for (int i = 0; i < v.size() - 1; i++) {
            for (int j = i + 1; j < v.size(); j++) {
                if (v.get(j).content.equals(v.get(i).content)) {
                    v.remove(j);
                    j--;
                }
            }
        }
    }

    @Override
    public String toString() {
        return content + "\t" + date;
    }
}
