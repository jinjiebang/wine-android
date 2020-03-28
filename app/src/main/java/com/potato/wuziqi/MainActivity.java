package com.potato.wuziqi;

import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.bugly.crashreport.CrashReport;

import java.lang.ref.WeakReference;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, AICallBack, GameCallBack {
    // 显示玩家以及ai执子
    private ImageView imgUserChess, imgAiChess;
    // 显示玩家以及ai得分
    private TextView textUserScore, textAiScore;
    // 思考时间
    private TextView textAiTime;
    private TextView textUserTime;
    private int aiTime = 0;
    private int userTime = 0;
    // 五子棋UI
    private FiveChessView fiveChessView;
    //PopUpWindow选择玩家执子
    private PopupWindow chooseChess;
    //五子棋AI
    private AI ai;
    //AI是否正在思考
    private boolean aiThinking = false;
    //用户是否正在思考
    private boolean userThinking = false;
    //难度等级按钮
    private Button btnLevel;
    //游戏模式按钮
    private Button btnGameMode;
    //handle
    private MainHandler mHandle;
    //难度等级列表
    private String[] levelList = {"初级", "中级", "高级", "大师", "特级大师"};
    private boolean isStopClock = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CrashReport.initCrashReport(getApplicationContext(), "***REMOVED***", true);

        initViews();
        mHandle = new MainHandler(this);
        mHandle.sendEmptyMessage(UPDATE_TIME);
        ai = new AI(this);
        fiveChessView.setCallBack(this);
        //view加载完成
        fiveChessView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                //初始化PopupWindow
                WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
                initPop(wm.getDefaultDisplay().getWidth(), wm.getDefaultDisplay().getHeight());
            }
        });

    }

    /**
     * 初始化控件
     */
    public void initViews() {
        fiveChessView = findViewById(R.id.id_chess_view);
        imgAiChess = findViewById(R.id.img_ai_chess);
        imgUserChess = findViewById(R.id.img_user_chess);
        textAiScore = findViewById(R.id.text_ai_score);
        textUserScore = findViewById(R.id.text_user_score);
        textAiTime = findViewById(R.id.text_ai_time);
        textUserTime = findViewById(R.id.text_user_time);
        btnLevel = findViewById(R.id.btn_level);
        btnGameMode = findViewById(R.id.btn_gameMode);

        textAiTime.setVisibility(View.INVISIBLE);
        textUserTime.setVisibility(View.INVISIBLE);
        findViewById(R.id.btn_suggest).setOnClickListener(this);
        findViewById(R.id.btn_start).setOnClickListener(this);
        findViewById(R.id.btn_undo).setOnClickListener(this);
        findViewById(R.id.btn_level).setOnClickListener(this);
        findViewById(R.id.btn_gameMode).setOnClickListener(this);
        findViewById(R.id.btn_showNum).setOnClickListener(this);
    }

    /**
     * 初始化PopWindow
     *
     * @param width  宽度
     * @param height 高度
     */
    public void initPop(int width, int height) {
        if (chooseChess == null) {
            View view = View.inflate(this, R.layout.pop_choose_chess, null);
            ImageButton white = view.findViewById(R.id.choose_white);
            ImageButton black = view.findViewById(R.id.choose_black);
            white.setOnClickListener(this);
            black.setOnClickListener(this);
            chooseChess = new PopupWindow(view, width, height);
            chooseChess.setOutsideTouchable(false);
            chooseChess.showAtLocation(fiveChessView, Gravity.CENTER, 0, 0);
        }
    }

    @Override
    public void onClick(View view) {
        if (aiThinking) {
            showToast("请等待AI思考结束！");
            return;
        }
        switch (view.getId()) {
            case R.id.btn_start:
                userRestart();
                break;
            case R.id.btn_undo:
                userUndo();
                break;
            case R.id.btn_level:
                changeLevel();
                break;
            case R.id.btn_suggest:
                aiSuggest();
                break;
            case R.id.btn_showNum:
                showChessNum();
                break;
            case R.id.btn_gameMode:
                changeGameMode();
                break;
            case R.id.choose_black:
                fiveChessView.setUserBout(true);
                fiveChessView.setUserChess(FiveChessView.BLACK_CHESS);
                ai.setAiChess(FiveChessView.WHITE_CHESS);
                updateChessPic();
                chooseChess.dismiss();
                break;
            case R.id.choose_white:
                fiveChessView.setUserBout(false);
                fiveChessView.setUserChess(FiveChessView.WHITE_CHESS);
                ai.setAiChess(FiveChessView.BLACK_CHESS);
                updateChessPic();
                //AI开始思考
                aiThink(null);
                chooseChess.dismiss();
                break;
        }
    }
    public int opp(int piece){
        return piece==FiveChessView.WHITE_CHESS?FiveChessView.BLACK_CHESS:FiveChessView.WHITE_CHESS;
    }

    public void changeGameMode() {
        if (btnGameMode.getText().equals("人人")) {
            btnGameMode.setText("人机");
            fiveChessView.setGameMode(FiveChessView.HUMAN_COMPUTER);
            ai.setAiChess(fiveChessView.getUserChess());
            fiveChessView.setUserChess(opp(ai.getAiChess()));
            fiveChessView.setUserBout(false);
            updateChessPic();
            aiTime=userTime=0;
            aiThink(null);
        } else {
            btnGameMode.setText("人人");
            aiThinking=userThinking=false;
            fiveChessView.setGameMode(FiveChessView.HUMAN_HUMAN);
        }

    }

    public void showChessNum() {
        fiveChessView.showChessNum();
    }

    public void aiSuggest() {
        if (!fiveChessView.isGameOver()) {
            fiveChessView.setUserBout(false);
            aiThinking = true;
            Point suggestPoint = ai.suggest();
            aiThinking = false;
            fiveChessView.setUserBout(true);
            fiveChessView.showSuggest(suggestPoint);
        }
    }

    public void changeLevel() {
        aiThinking = true;
        for (int i = 0; i < levelList.length; i++) {
            if (btnLevel.getText().equals(levelList[i])) {
                String newLevel = (i == levelList.length - 1) ? levelList[0] : levelList[i + 1];
                ai.setLevel(newLevel);
                btnLevel.setText(newLevel);
                break;
            }
        }
        aiThinking = false;
    }

    //改变用户执子
    public void updateChessPic() {
        if (fiveChessView.getUserChess() == FiveChessView.BLACK_CHESS) {
            imgUserChess.setBackgroundResource(R.drawable.stone_b1);
            imgAiChess.setBackgroundResource(R.drawable.stone_w2);
        } else {
            imgUserChess.setBackgroundResource(R.drawable.stone_w2);
            imgAiChess.setBackgroundResource(R.drawable.stone_b1);
        }
    }


    public void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
    public String num2Str(int num){
        if(num<10) return "0"+num;
        return ""+num;
    }
    public void showTime(final TextView textView, final int second) {
        String timeStr =num2Str(second/60)+":"+num2Str(second%60);
        if (second == 0) {
            textView.setVisibility(View.INVISIBLE);
        } else {
            textView.setVisibility(View.VISIBLE);
            textView.setText(timeStr);
        }
    }

    @Override
    public void gameOver(int winner) {
        aiThinking = false;
        userThinking = false;
        updateWinInfo();
        switch (winner) {
            case FiveChessView.BLACK_CHESS:
                showToast("黑棋胜利!");
                break;
            case FiveChessView.WHITE_CHESS:
                showToast("白棋胜利!");
                break;
            case FiveChessView.N0_CHESS:
                showToast("和棋");
                break;
        }
    }

    public void updateWinInfo() {
        textUserScore.setText("Score：" + fiveChessView.getUserScore());
        textAiScore.setText("Score：" + fiveChessView.getAiScore());
    }

    public void userUndo() {
        if (fiveChessView.getChessCount() >= 2) {
            aiThinking = true;
            ai.undo();
            aiThinking = false;
            fiveChessView.undo();
            fiveChessView.postInvalidate();
        }
    }

    public void userRestart() {
        if (fiveChessView.getGameMode() == FiveChessView.HUMAN_COMPUTER) {
            chooseChess.showAtLocation(fiveChessView, Gravity.CENTER, 0, 0);
        }
        aiTime=userTime=0;
        aiThinking = true;
        ai.restart();
        aiThinking = false;
        userThinking = false;
        fiveChessView.start();
    }
    public void aiThink(Point p){
        userThinking = false;
        aiThinking = true;
        ai.aiBout(p);
    }

    //用户落子后调用
    @Override
    public void userAtBell(Point p) {
        if (fiveChessView.getGameMode() == FiveChessView.HUMAN_COMPUTER) {
            aiThink(p);
        } else {
            ai.addChess(p);
            aiThinking = false;
        }
    }

    //ai落子结束后调用
    @Override
    public void aiAtBell(final Point point) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                aiThinking = false;
                userThinking = true;
                //更新UI
                fiveChessView.addChess(point, ai.getAiChess());
                fiveChessView.postInvalidate();
                fiveChessView.setUserBout(true);
                fiveChessView.checkGameOver();
                fiveChessView.invalidate();

            }
        });
    }

    public static final int UPDATE_TIME = 0;

    private static class MainHandler extends Handler {
        private final WeakReference<MainActivity> weakReference;

        private MainHandler(MainActivity activity) {
            this.weakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            MainActivity activity = weakReference.get();
            //isStop防止计时器叠加
            if (activity != null) {
                switch (msg.what) {
                    case UPDATE_TIME: {
                        if (activity.aiThinking) {
                            activity.showTime(activity.textAiTime, activity.aiTime);
                            activity.aiTime++;
                        }
                        if (activity.userThinking) {
                            activity.showTime(activity.textUserTime, activity.userTime);
                            activity.userTime++;
                        }
                        activity.mHandle.sendEmptyMessageDelayed(UPDATE_TIME, 1000);
                        break;
                    }
                }
            }

        }
    }
}
