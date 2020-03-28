package com.potato.wuziqi;

import android.graphics.Point;

import java.util.HashMap;

public class AI implements Runnable {
    private AICallBack callBack;
    private int aiChess;
    private long aiObject;
    private Point lastPoint;
    HashMap<String,Integer> timeMap = new HashMap<String, Integer>() ;

    static {
        System.loadLibrary("native-lib");
    }

    public native long getAiObject();

    public native int getBestPoint(long aiObject, int move);

    public native void aiRestart(long aiObject);

    public native void aiUndo(long aiObject);

    public native void aiMove(long aiObject,int move);

    public native void setStepTime(long aiObject, int time);

    public native int getSuggest(long aiObject);

    public AI(AICallBack callBack) {
        this.callBack = callBack;
        this.aiObject = getAiObject();
        initTimeMap();
    }
    private void initTimeMap(){
        timeMap.put("初级",2000);
        timeMap.put("中级",5000);
        timeMap.put("高级",10000);
        timeMap.put("大师",30000);
        timeMap.put("特级大师",60000);
    }

    //当前回合轮到AI
    public void aiBout(Point p) {
        lastPoint = p;
        new Thread(this).start();
    }

    public void addChess(Point p){
        lastPoint = p;
        aiMove(aiObject,lastPoint.x * 16 + lastPoint.y);
    }

    //AI建议
    public Point suggest(){
        int p = getSuggest(aiObject);
        return new Point(getPointX(p),getPointY(p));
    }

    public void restart(){
        aiRestart(aiObject);
    }

    public void undo(){
        aiUndo(aiObject);
    }

    public void setLevel(String level){
        setStepTime(aiObject,timeMap.get(level));
    }

    @Override
    public void run() {
        int lastMove;
        if (null == lastPoint) {
            lastMove = -1;
        } else {
            lastMove = lastPoint.x * 16 + lastPoint.y;
        }

        int best = getBestPoint(aiObject, lastMove);
        Point bestPoint = new Point(getPointX(best),getPointY(best));
        callBack.aiAtBell(bestPoint);
    }

    private int getPointX(int p) {
        return p >> 4;
    }

    private int getPointY(int p) {
        return p & 15;
    }

    public void setAiChess(int aiChess) {
        this.aiChess = aiChess;
    }

    public int getAiChess() {
        return aiChess;
    }
}
