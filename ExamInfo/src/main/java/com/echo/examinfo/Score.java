package com.echo.examinfo;

/**
 * @author Echo Pan
 * @version 1.0
 * @date 2023/12/2 8:11
 * @description 成绩类
 */
class Score {
    String uid;
    String cid;
    String cname;
    String score;
    String date;
    String note;
    public Score(){

    }

    public Score(String uid, String cid, String cname, String score, String date, String note) {
        this();
        this.uid = uid;
        this.cid = cid;
        this.cname = cname;
        this.score = score;
        this.date = date;
        this.note = note;
    }
    public String get(int i){
        return switch (i){
            case 0-> uid;
            case 1-> cid;
            case 2-> cname;
            case 3->score;
            case 4-> date;
            case 5->note;
            default ->null;
        };
    }
    @Override
    public String toString(){
        return uid+"\t"+cid+"\t"+cname+"\t"+score+"\t"+date+"\t"+note;
    }
}
