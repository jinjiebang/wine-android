package com.potato.wuziqi;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018/6/9.
 */

public class FiveChessView extends View {
    // 棋盘面板宽度
    private int mPanelWidth;
    // 棋盘每格的行高
    private float mLineHeight;
    // 棋盘尺寸
    private int MAX_LINE = 15;
    // 棋子占行高比例
    private float ratioPieceOfLineheight = 0.9f;
    private float outLine = 10.0f;
    private float startPos;
    private float endPos;

    //黑棋数组，白棋数组，棋盘数组
    private ArrayList<Point> mWhiteArray = new ArrayList<>();
    private ArrayList<Point> mBlackArray = new ArrayList<>();
    private ArrayList<Point> chessArray = new ArrayList<>();
    private int[][] mBoard = new int[MAX_LINE][MAX_LINE];

    // 胜利玩家
    private int mWinner;
    // 连成五个的棋子
    private ArrayList<Point> fiveArray = new ArrayList<>();
    // 游戏是否结束
    private boolean mIsGameOver = false;

    public static final int BLACK_CHESS = 1;
    public static final int WHITE_CHESS = 2;
    public static final int N0_CHESS = 0;

    public static final int HUMAN_COMPUTER = 0;     //人机对战
    public static final int HUMAN_HUMAN = 1;        //人人对战
    private int gameMode = HUMAN_COMPUTER;

    //玩家以及AI得分纪录
    private int userScore, aiScore;
    //玩家执子
    private int userChess;
    //当前回合是否轮到玩家
    private boolean isUserBout;
    private boolean isDrawChessNum = false;

    private GameCallBack callBack;

    private Paint mPaint = new Paint();
    private Bitmap mWhitePiece;
    private Bitmap mBlackPiece;

    private Point suggestPoint;

    public FiveChessView(Context context) {
        this(context, null);
    }

    public FiveChessView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FiveChessView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mPaint.setColor(Color.BLACK);
        mPaint.setAntiAlias(true);//抗锯齿
        mPaint.setDither(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setTextSize(24);
        mPaint.setTextAlign(Paint.Align.CENTER);

        mWhitePiece = BitmapFactory.decodeResource(getResources(), R.drawable.stone_w2);
        mBlackPiece = BitmapFactory.decodeResource(getResources(), R.drawable.stone_b1);

        userScore = 0;
        aiScore = 0;
        start();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        int width = Math.min(widthSize, heightSize);
        if (widthMode == MeasureSpec.UNSPECIFIED) {
            width = heightSize;
        } else if (heightMode == MeasureSpec.UNSPECIFIED) {
            width = widthSize;
        }
        setMeasuredDimension(width, width);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mPanelWidth = w;
        mLineHeight = (mPanelWidth * 1.0f - 2.0f * outLine) / MAX_LINE;
        startPos = outLine;
        endPos = w - outLine;

        int pieceWidth = (int) (mLineHeight * ratioPieceOfLineheight);

        mWhitePiece = Bitmap.createScaledBitmap(mWhitePiece, pieceWidth, pieceWidth, false);
        mBlackPiece = Bitmap.createScaledBitmap(mBlackPiece, pieceWidth, pieceWidth, false);
    }

    private Point getValidPoint(int x, int y) {
        return new Point((int) (x / mLineHeight), (int) (y / mLineHeight));
    }

    public int getChessCount() {
        return chessArray.size();
    }

    public void removeLastChess() {
        Point p = chessArray.get(chessArray.size() - 1);
        chessArray.remove(chessArray.size() - 1);
        if (mBoard[p.y][p.x] == BLACK_CHESS) {
            mBlackArray.remove(mBlackArray.size() - 1);
        } else if (mBoard[p.y][p.x] == WHITE_CHESS) {
            mWhiteArray.remove(mWhiteArray.size() - 1);
        }
        mBoard[p.y][p.x] = N0_CHESS;
    }

    public void undo() {
        if (chessArray.size() >= 2) {
            mIsGameOver = false;
            suggestPoint = null;
            removeLastChess();
            removeLastChess();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mIsGameOver || !isUserBout) return false;
        int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            return true;
        }
        if (action == MotionEvent.ACTION_UP) {
            int x = (int) event.getX();
            int y = (int) event.getY();

            Point p = getValidPoint(x, y);
            if (mWhiteArray.contains(p) || mBlackArray.contains(p)) {
                return false;
            }
            suggestPoint = null;
            addChess(p, userChess);
            if (gameMode == HUMAN_COMPUTER) {
                setUserBout(false);
            } else if (gameMode == HUMAN_HUMAN) {
                setUserBout(true);
                userChess = (userChess == WHITE_CHESS) ? BLACK_CHESS : WHITE_CHESS;
            }

            checkGameOver();

            invalidate();

            if (!mIsGameOver) {
                callBack.userAtBell(p);
            }
            return true;
        }
        return super.onTouchEvent(event);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawBoard(canvas);
        drawPiece(canvas);
    }

    public void addChess(Point point, int chessType) {
        mBoard[point.y][point.x] = chessType;
        chessArray.add(point);
        if (chessType == BLACK_CHESS) {
            mBlackArray.add(point);
        } else if (chessType == WHITE_CHESS) {
            mWhiteArray.add(point);
        }
    }

    public void setGameMode(int gameMode) {
        this.gameMode = gameMode;
    }

    public int getGameMode() {
        return gameMode;
    }

    public void checkGameOver() {
        boolean blackWin = checkFiveInLine(mBlackArray);
        boolean whiteWin = blackWin ? false : checkFiveInLine(mWhiteArray);
        if (whiteWin || blackWin) {
            mIsGameOver = true;
            mWinner = whiteWin ? WHITE_CHESS : BLACK_CHESS;
            if (mWinner == userChess) {
                userScore++;
            } else {
                aiScore++;
            }
            callBack.gameOver(mWinner);
        } else if (isFull()) {  //和棋
            mIsGameOver = true;
            mWinner = N0_CHESS;
            callBack.gameOver(mWinner);
        }
    }

    public boolean checkFiveInLine(List<Point> points) {
        List<Point> dirArray = new ArrayList<>();
        dirArray.add(new Point(1, 0));
        dirArray.add(new Point(0, 1));
        dirArray.add(new Point(1, 1));
        dirArray.add(new Point(1, -1));
        for (Point p : points) {
            for (Point dir : dirArray) {
                if (checkFiveOneLine(dir, p.x, p.y, points)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 检查棋子在某个方向是否已经连五
     *
     * @param dir
     * @param x
     * @param y
     * @param points
     * @return
     */
    public boolean checkFiveOneLine(Point dir, int x, int y, List<Point> points) {
        int count = 1;
        fiveArray.clear();
        fiveArray.add(new Point(x, y));
        for (int i = 1; i < 5; i++) {
            Point p = new Point(x + dir.x * i, y + dir.y * i);
            if (points.contains(p)) {
                fiveArray.add(p);
                count++;
            } else {
                break;
            }
        }
        for (int i = 1; i < 5; i++) {
            Point p = new Point(x - dir.x * i, y - dir.y * i);
            if (points.contains(p)) {
                fiveArray.add(p);
                count++;
            } else {
                break;
            }
        }
        return count >= 5;
    }


    private void drawPiece(Canvas canvas) {
        mPaint.setStrokeWidth(2.0f);
        mPaint.setTextSize(24);
        Paint.FontMetricsInt fontMetrics = mPaint.getFontMetricsInt();
        for (int i = 0; i < mWhiteArray.size(); i++) {
            Point whitePoint = mWhiteArray.get(i);
            float left = startPos+(whitePoint.x + (1 - ratioPieceOfLineheight) / 2) * mLineHeight;
            float top = startPos+(whitePoint.y + (1 - ratioPieceOfLineheight) / 2) * mLineHeight;
            canvas.drawBitmap(mWhitePiece, left, top, null);
            if (isDrawChessNum) {
                mPaint.setColor(Color.BLACK);
                float textTop = startPos+whitePoint.y * mLineHeight;
                float textBottom = textTop + mLineHeight;
                float baseline = (textTop + textBottom - fontMetrics.ascent - fontMetrics.descent) / 2;
                float centerX = startPos+(whitePoint.x + 0.5f) * mLineHeight;
                canvas.drawText(String.format("%S", 2 + i * 2), centerX, baseline, mPaint);
            }
        }
        for (int i = 0; i < mBlackArray.size(); i++) {
            Point blackPoint = mBlackArray.get(i);
            float left = startPos+(blackPoint.x + (1 - ratioPieceOfLineheight) / 2) * mLineHeight;
            float top = startPos+(blackPoint.y + (1 - ratioPieceOfLineheight) / 2) * mLineHeight;
            canvas.drawBitmap(mBlackPiece, left, top, null);
            if (isDrawChessNum) {
                mPaint.setColor(Color.WHITE);
                float textTop = startPos+blackPoint.y * mLineHeight;
                float textBottom = textTop + mLineHeight;
                float baseline = (textTop + textBottom - fontMetrics.ascent - fontMetrics.descent) / 2;
                float centerX = startPos+(blackPoint.x + 0.5f) * mLineHeight;
                canvas.drawText(String.format("%S", 1 + i * 2), centerX, baseline, mPaint);
            }
        }
    }

    private void drawBoard(Canvas canvas) {
        mPaint.setColor(Color.BLACK);
        mPaint.setStrokeWidth(2.0f);
        int w = mPanelWidth;
        float lineHeight = mLineHeight;
        //画棋盘线
        for (int i = 0; i < MAX_LINE; i++) {
            int startX = (int) (startPos+lineHeight / 2);
            int endX = (int) (endPos - lineHeight / 2);
            int y = (int) (startPos+(0.5 + i) * lineHeight);
            canvas.drawLine(startX, y, endX, y, mPaint);
        }
        for (int i = 0; i < MAX_LINE; i++) {
            int startY = (int) (startPos+lineHeight / 2);
            int endY = (int) (endPos- lineHeight / 2);
            int x = (int) (startPos+(0.5 + i) * lineHeight);
            canvas.drawLine(x, startY, x, endY, mPaint);
        }
        //画棋盘坐标
        mPaint.setTextSize(20);
        mPaint.setStrokeWidth(1.7f);
        Paint.FontMetricsInt fontMetrics = mPaint.getFontMetricsInt();
        for (int i = 0; i < MAX_LINE; i++) {
            float y = startPos+(0.5f + i) * lineHeight;
            float textTop = y-0.5f*mLineHeight;
            float textBottom = y+0.5f*mLineHeight;
            float baseline = (textTop + textBottom - fontMetrics.ascent - fontMetrics.descent) / 2;
            float x = 12;
            canvas.drawText(String.format("%S",15-i), x, baseline, mPaint);
        }
        for (int i = 0; i < MAX_LINE; i++) {
            float y = w-12;
            float textTop = y-0.5f*mLineHeight;
            float textBottom = y+0.5f*mLineHeight;
            float baseline = (textTop + textBottom - fontMetrics.ascent - fontMetrics.descent) / 2;
            float x = startPos+(0.5f + i) * lineHeight;
            String text=String.valueOf((char)('A'+i));
            canvas.drawText(text, x, baseline, mPaint);
        }
        //棋盘边缘线
        mPaint.setStrokeWidth(4.0f);
        int min=(int)lineHeight/2+4,max=w-min;
        canvas.drawLine(min-2,min,max+2,min,mPaint);
        canvas.drawLine(min-2,max,max+2,max,mPaint);
        canvas.drawLine(min,min,min,max,mPaint);
        canvas.drawLine(max,min,max,max,mPaint);

        //画五个小黑点
        mPaint.setStrokeWidth(8f);
        canvas.drawCircle(startPos+3.5f*lineHeight,startPos+3.5f*lineHeight,5,mPaint);
        canvas.drawCircle(startPos+11.5f*lineHeight,startPos+3.5f*lineHeight,5,mPaint);
        canvas.drawCircle(startPos+3.5f*lineHeight,startPos+11.5f*lineHeight,5,mPaint);
        canvas.drawCircle(startPos+11.5f*lineHeight,startPos+11.5f*lineHeight,5,mPaint);
        canvas.drawCircle(startPos+7.5f*lineHeight,startPos+7.5f*lineHeight,5,mPaint);
        mPaint.setColor(Color.GREEN);
        mPaint.setStrokeWidth(4.0f);
        if (!mIsGameOver) {
            //标识最后一子
            if (chessArray.size() > 0) {
                drawCircle(canvas, chessArray.get(chessArray.size() - 1));
            }
            //标识建议位置
            if (suggestPoint != null) {
                mPaint.setColor(Color.GRAY);
                drawCircle(canvas, suggestPoint);
            }
        } else {
            //标识连五
            for (Point point : fiveArray) {
                drawCircle(canvas, point);
            }
        }
    }

    public void drawCircle(Canvas canvas, Point point) {
        float cx = startPos+(0.5f + point.x) * mLineHeight;
        float cy = startPos+(0.5f + point.y) * mLineHeight;
        float radius = ratioPieceOfLineheight * mLineHeight / 2;
        canvas.drawCircle(cx, cy, radius, mPaint);
    }

    public void showSuggest(Point point) {
        this.suggestPoint = point;
        postInvalidate();
    }

    public void showChessNum() {
        isDrawChessNum = !isDrawChessNum;
        postInvalidate();
    }

    public void start() {
        for (int i = 0; i < MAX_LINE; i++) {
            for (int j = 0; j < MAX_LINE; j++) {
                mBoard[i][j] = 0;
            }
        }
        mBlackArray.clear();
        mWhiteArray.clear();
        chessArray.clear();
        suggestPoint = null;
        mIsGameOver = false;
        mWinner = 0;
        userChess = BLACK_CHESS;
        invalidate();
    }

    public boolean isFull() {
        for (int i = 0; i < MAX_LINE; i++) {
            for (int j = 0; j < MAX_LINE; j++) {
                if (mBoard[i][j] == N0_CHESS) {
                    return false;
                }
            }
        }
        return true;
    }

    public static final String INSTANCE = "instance";
    public static final String INSTANCE_GAMEOVER = "instance_game_over";
    public static final String INSTANCE_WHITE_ARRAY = "instance_white_array";
    public static final String INSTANCE_BLACK_ARRAY = "instance_black_array";

    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable(INSTANCE, super.onSaveInstanceState());
        bundle.putBoolean(INSTANCE_GAMEOVER, mIsGameOver);
        bundle.putParcelableArrayList(INSTANCE_BLACK_ARRAY, mBlackArray);
        bundle.putParcelableArrayList(INSTANCE_WHITE_ARRAY, mWhiteArray);
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            mIsGameOver = bundle.getBoolean(INSTANCE_GAMEOVER);
            mWhiteArray = bundle.getParcelableArrayList(INSTANCE_WHITE_ARRAY);
            mBlackArray = bundle.getParcelableArrayList(INSTANCE_BLACK_ARRAY);
            super.onRestoreInstanceState(bundle.getParcelable(INSTANCE));
        } else {
            super.onRestoreInstanceState(state);
        }
    }

    public void setUserChess(int userChess) {
        this.userChess = userChess;
    }

    public void setUserBout(boolean userBout) {
        isUserBout = userBout;
    }

    public void setCallBack(GameCallBack callBack) {
        this.callBack = callBack;
    }

    public int getUserScore() {
        return userScore;
    }

    public int getAiScore() {
        return aiScore;
    }

    public boolean isGameOver() {
        return mIsGameOver;
    }

    public int getUserChess(){
        return userChess;
    }
}
