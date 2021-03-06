package com.github.anastr.speedviewlib;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.animation.DecelerateInterpolator;

import java.util.Locale;
import java.util.Random;

public class SpeedView extends Speed {

    private Path indicatorPath, markPath;
    private Paint circlePaint, paint, speedometerPaint, markPaint;
    private TextPaint speedTextPaint, textPaint;
    private RectF speedometerRect;

    private boolean canceled = false;
    private final int MIN_DEGREE = 135, MAX_DEGREE = 135+270;
    /** to rotate indicator */
    private float degree = MIN_DEGREE;
    private int maxSpeed = 100;
    private int speed = 0;
    private ValueAnimator speedAnimator, trembleAnimator;

    public SpeedView(Context context) {
        super(context);
        init();
    }

    public SpeedView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
        initAttributeSet(context, attrs);
    }

    public SpeedView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
        initAttributeSet(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        int size = (width > height) ? height : width;
        setMeasuredDimension(size, size);
    }

    private void initAttributeSet(Context context, AttributeSet attrs) {
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.SpeedView, 0, 0);

        setIndicatorColor(a.getColor(R.styleable.SpeedView_indicatorColor, getIndicatorColor()));
        setCenterCircleColor(a.getColor(R.styleable.SpeedView_centerCircleColor, getCenterCircleColor()));
        setMarkColor(a.getColor(R.styleable.SpeedView_markColor, getMarkColor()));
        setLowSpeedColor(a.getColor(R.styleable.SpeedView_lowSpeedColor, getLowSpeedColor()));
        setMediumSpeedColor(a.getColor(R.styleable.SpeedView_mediumSpeedColor, getMediumSpeedColor()));
        setHighSpeedColor(a.getColor(R.styleable.SpeedView_highSpeedColor, getHighSpeedColor()));
        setTextColor(a.getColor(R.styleable.SpeedView_textColor, getTextColor()));
        setBackgroundCircleColor(a.getColor(R.styleable.SpeedView_backgroundCircleColor, getBackgroundCircleColor()));
        setSpeedometerWidth(a.getDimension(R.styleable.SpeedView_speedometerWidth, getSpeedometerWidth()));
        setMaxSpeed(a.getInt(R.styleable.SpeedView_maxSpeed, getMaxSpeed()));
        setWithTremble(a.getBoolean(R.styleable.SpeedView_withTremble, isWithTremble()));
        setWithBackgroundCircle(a.getBoolean(R.styleable.SpeedView_withBackgroundCircle, isWithBackgroundCircle()));
        setSpeedTextSize(a.getDimension(R.styleable.SpeedView_speedTextSize, getSpeedTextSize()));
        String unit = a.getString(R.styleable.SpeedView_unit);
        a.recycle();
        setUnit( (unit != null) ? unit : getUnit() );
    }

    private void init() {
        indicatorPath = new Path();
        markPath = new Path();

        circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        speedometerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        markPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        speedTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);

        speedometerRect = new RectF();

        speedometerPaint.setStyle(Paint.Style.STROKE);
        markPaint.setStyle(Paint.Style.STROKE);
        speedTextPaint.setTextAlign(Paint.Align.CENTER);

        speedAnimator = ValueAnimator.ofFloat(0f, 1f);
        trembleAnimator = ValueAnimator.ofFloat(0f, 1f);
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        super.onSizeChanged(w, h, oldW, oldH);

        float risk = getSpeedometerWidth()/2f;
        speedometerRect.set(risk, risk, w -risk, h -risk);

        float indW = w/32f;

        indicatorPath.moveTo(w/2f, 0f);
        indicatorPath.lineTo(w/2f -indW, h*2f/3f);
        indicatorPath.lineTo(w/2f +indW, h*2f/3f);
        RectF rectF = new RectF(w/2f -indW, h*2f/3f -indW, w/2f +indW, h*2f/3f +indW);
        indicatorPath.addArc(rectF, 0f, 180f);
        indicatorPath.moveTo(0f, 0f);

        float markH = h/28f;
        markPath.moveTo(w/2f, 0f);
        markPath.lineTo(w/2f, markH);
        markPath.moveTo(0f, 0f);
        markPaint.setStrokeWidth(markH/3f);
    }

    private void initDraw() {
        speedometerPaint.setStrokeWidth(getSpeedometerWidth());
        markPaint.setColor(getMarkColor());
        speedTextPaint.setColor(getTextColor());
        speedTextPaint.setTextSize(getSpeedTextSize());
        textPaint.setColor(getTextColor());
        circlePaint.setColor(getBackgroundCircleColor());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        initDraw();

        if (isWithBackgroundCircle())
            canvas.drawCircle(getWidth()/2f, getHeight()/2f, getWidth()/2f, circlePaint);

        speedometerPaint.setColor(getLowSpeedColor());
        canvas.drawArc(speedometerRect, 135f, 160f, false, speedometerPaint);
        speedometerPaint.setColor(getMediumSpeedColor());
        canvas.drawArc(speedometerRect, 135f+160f, 75f, false, speedometerPaint);
        speedometerPaint.setColor(getHighSpeedColor());
        canvas.drawArc(speedometerRect, 135f+160f+75f, 35f, false, speedometerPaint);

        canvas.save();
        canvas.rotate(135f+90f, getWidth()/2f, getHeight()/2f);
        for (int i=135; i <= 345; i+=30) {
            canvas.rotate(30f, getWidth()/2f, getHeight()/2f);
            canvas.drawPath(markPath, markPaint);
        }
        canvas.restore();

        paint.setColor(getIndicatorColor());
        canvas.save();
        canvas.rotate(90f +degree, getWidth()/2f, getHeight()/2f);
        canvas.drawPath(indicatorPath, paint);
        canvas.restore();
        paint.setColor(getCenterCircleColor());
        canvas.drawCircle(getWidth()/2f, getHeight()/2f, getWidth()/12f, paint);

        textPaint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("00", getWidth()/6f, getHeight()*7/8f, textPaint);
        textPaint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(String.format(Locale.getDefault(), "%d", maxSpeed), getWidth()*5/6f, getHeight()*7/8f, textPaint);
        canvas.drawText(String.format(Locale.getDefault(), "%.1f"
                , (degree-MIN_DEGREE) * maxSpeed/(MAX_DEGREE-MIN_DEGREE)) +getUnit()
                , getWidth()/2f, speedometerRect.bottom, speedTextPaint);
    }

    private void cancel() {
        cancelSpeedMove();
        cancelTremble();
    }

    private void cancelTremble() {
        canceled = true;
        trembleAnimator.cancel();
        canceled = false;
    }

    private void cancelSpeedMove() {
        canceled = true;
        speedAnimator.cancel();
        canceled = false;
    }

    @Override
    public void speedPercentTo(int percent) {
        percent = (percent > 100) ? 100 : (percent < 0) ? 0 : percent;
        speedTo(percent * maxSpeed / 100);
    }

    @Override
    public void speedToDef() {
        speedTo(speed, 2000);
    }

    @Override
    public void speedTo(int speed) {
        speedTo(speed, 2000);
    }

    @Override
    public void speedTo(int speed, long moveDuration) {
        speed = (speed > maxSpeed) ? maxSpeed : (speed < 0) ? 0 : speed;
        this.speed = speed;

        float newDegree = (float)speed * (MAX_DEGREE - MIN_DEGREE) /maxSpeed +MIN_DEGREE;
        if (newDegree == degree)
            return;

        cancel();
        speedAnimator = ValueAnimator.ofFloat(degree, newDegree);
        speedAnimator.setInterpolator(new DecelerateInterpolator());
        speedAnimator.setDuration(moveDuration);
        speedAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                degree = (float) speedAnimator.getAnimatedValue();
                postInvalidate();
            }
        });
        speedAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (!canceled)
                    tremble();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        speedAnimator.start();
    }

    private void tremble() {
        cancelTremble();
        if (!isWithTremble())
            return;
        Random random = new Random();
        float mad = 4*random.nextFloat() * ((random.nextBoolean()) ? -1 :1);
        float originalDegree = (float)speed * (MAX_DEGREE - MIN_DEGREE) /maxSpeed +MIN_DEGREE;
        mad = (originalDegree+mad > MAX_DEGREE) ? MAX_DEGREE - originalDegree
                : (originalDegree+mad < MIN_DEGREE) ? MIN_DEGREE - originalDegree : mad;
        trembleAnimator = ValueAnimator.ofFloat(degree, originalDegree +mad);
        trembleAnimator.setInterpolator(new DecelerateInterpolator());
        trembleAnimator.setDuration(1000);
        trembleAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                degree = (float) trembleAnimator.getAnimatedValue();
                postInvalidate();
            }
        });
        trembleAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (!canceled)
                    tremble();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        trembleAnimator.start();
    }

    @Override
    public int getPercentSpeed() {
        return speed * 100 / maxSpeed;
    }

    @Override
    public void setSpeedometerWidth(float speedometerWidth) {
        super.setSpeedometerWidth(speedometerWidth);
        float risk = speedometerWidth/2f;
        speedometerRect.set(risk, risk, getWidth() -risk, getHeight() -risk);
        invalidate();
    }

    @Override
    public void setWithTremble(boolean withTremble) {
        super.setWithTremble(withTremble);
        tremble();
    }

    @Override
    public int getSpeed() {
        return speed;
    }

    @Override
    public int getMaxSpeed() {
        return maxSpeed;
    }

    @Override
    public void setMaxSpeed(int maxSpeed) {
        if (maxSpeed <= 0)
            return;
        this.maxSpeed = maxSpeed;
        speedTo(speed);
        invalidate();
    }
}
