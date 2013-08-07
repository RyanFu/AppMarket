package com.dongji.market.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

import com.dongji.market.R;


/**
 * 左右滑动按钮
 * @author RanQing
 *
 */
public class SlipSwitch extends View implements OnTouchListener {

	//从xml文件中获取的各种参数
	private Drawable switch_on_background, switch_off_background,
			switch_background;
	private String onText, offText;
	private int textSize;
	private int onTextColor, offTextColor;

	// 开关开启背景，关闭背景，滑动按钮
	private Bitmap switch_on_bg, switch_off_bg, switch_bg;
	private Rect on_rect, off_rect;

	// 是否正在滑动
	private boolean isSlipping = false;
	// 当前开关状态，true为开，false为关
	private boolean switchState = false;

	// 按下时水平坐标，当前水平坐标
	private float pressDownX, currentX;

	// "打开"与"关闭"文字
//	private String openText = "打开", closeText = "关闭";

	// 开关监听器
	private OnSwitchListener onSwitchListener;
	// 是否设置了开关监听器
	private boolean isSwitchListenerOn = false;

	public SlipSwitch(Context context) {
		super(context);
		init();
	}

	public SlipSwitch(Context context, AttributeSet attrs) {
		super(context, attrs);

		// 绑定values/attr.xml文件中的属性
		TypedArray typeArray = context.obtainStyledAttributes(attrs,
				R.styleable.SlipSwitch);
		switch_background = typeArray
				.getDrawable(R.styleable.SlipSwitch_background);
		switch_on_background = typeArray
				.getDrawable(R.styleable.SlipSwitch_onBackground);
		switch_off_background = typeArray
				.getDrawable(R.styleable.SlipSwitch_offBackground);
		
//		System.out.println("width===" + switchWidth + "     height===" + switchHeight);
//		Toast.makeText(context, "width===" + switchWidth + "     height===" + switchHeight, Toast.LENGTH_SHORT).show();
		onText = typeArray.getString(R.styleable.SlipSwitch_onText);
		offText = typeArray.getString(R.styleable.SlipSwitch_offText);
		textSize = typeArray.getDimensionPixelSize(
				R.styleable.SlipSwitch_textSize, 15);
		onTextColor = typeArray.getColor(R.styleable.SlipSwitch_onTextColor,
				0xfff);
		offTextColor = typeArray.getColor(R.styleable.SlipSwitch_offTextColor,
				0x000);
		switchState = typeArray.getBoolean(R.styleable.SlipSwitch_switchState,
				false);
		setSwitchState(switchState);
		
		typeArray.recycle();
		init();
	}
	
	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		// TODO Auto-generated method stub
		super.onLayout(changed, left, top, right, bottom);
		setImageResource(getWidth(), getHeight(), drawableToBitmap(switch_background),
				drawableToBitmap(switch_on_background),
				drawableToBitmap(switch_off_background), onText, offText,
				textSize, onTextColor, offTextColor);
	}

	/*
	 * public SlipSwitch(Context context, AttributeSet attrs, int defStyle) {
	 * super(context, attrs, defStyle); init(); }
	 */

	private void init() {
		setOnTouchListener(this);
	}

	public void setImageResource(float switchWidth, float switchHeight, Bitmap switchBg, Bitmap switchOnBg,
			Bitmap switchOffBg, String onText, String offText, int textSize,
			int onTextColor, int offTextColor) {
		switch_off_bg = drawTextAtBitmap(changeBitmapSize(switchOffBg, switchWidth/2, switchHeight),
				offText, offTextColor, textSize);
		switch_on_bg = drawTextAtBitmap(changeBitmapSize(switchOnBg, switchWidth/2, switchHeight),
				onText, onTextColor, textSize);
		switch_bg = changeBitmapSize(switchBg, switchWidth, switchHeight);

		// 右半边的 Rect,即滑动按钮在右半边时表示开关开启
		on_rect = new Rect(switch_bg.getWidth() - switch_on_bg.getWidth(), 0,
				switch_bg.getWidth(), switch_on_bg.getHeight());
		// 左半边的Rect,即滑动按钮在左半边时表示开关关闭
		off_rect = new Rect(0, 0, switch_off_bg.getWidth(),
				switch_off_bg.getHeight());
		invalidate();
	}

/*	public void setImageResource(int switchBg, int switchOnBg, int switchOffBg) {
		switch_off_bg = drawTextAtBitmap(
				changeBitmapSize(BitmapFactory.decodeResource(getResources(),
						switchOffBg), 0.5f), closeText, Color.BLACK, 20);
		switch_on_bg = drawTextAtBitmap(
				changeBitmapSize(BitmapFactory.decodeResource(getResources(),
						switchOnBg), 0.5f), openText, Color.WHITE, 20);
		switch_bg = changeBitmapSize(
				BitmapFactory.decodeResource(getResources(), switchBg), 0.5f);

		// 右半边的 Rect,即滑动按钮在右半边时表示开关开启
		on_rect = new Rect(switch_bg.getWidth() - switch_on_bg.getWidth(), 0,
				switch_bg.getWidth(), switch_on_bg.getHeight());
		// 左半边的Rect,即滑动按钮在左半边时表示开关关闭
		off_rect = new Rect(0, 0, switch_off_bg.getWidth(),
				switch_off_bg.getHeight());
		invalidate();
	}*/

	public void setSwitchState(boolean switchState) {
		this.switchState = switchState;
	}

	public boolean getSwitchState() {
		return switchState;
	}

	public void updateSwitchState(boolean switchState) {
		if (this.switchState != switchState) {
			this.switchState = switchState;
			if (onSwitchListener != null) {
				onSwitchListener.onSwitched(switchState);
			}
			invalidate();
		}
	}

	@Override
	public void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);

		Matrix matrix = new Matrix();
		Paint paint = new Paint();
		// 滑动按钮的左边坐标
		float left_slipBtn;

		canvas.drawBitmap(switch_bg, matrix, paint);

		// 判断当前是否正在滑动
		if (isSlipping) {
			if (currentX > switch_bg.getWidth()) {
				left_slipBtn = switch_bg.getWidth() - switch_on_bg.getWidth();
			} else {
				left_slipBtn = currentX - switch_on_bg.getWidth() / 2;
			}
		} else {
			// 根据当前开关的状态设置滑动按钮的位置
			if (switchState) {
				left_slipBtn = on_rect.left;

				// 打开页面尚无点击事件时，若初始为“打开”状态，则将初始坐标置为>=1/2开关长度即可，以避免初始化图标错误，因为默认点击前currentX==0;
				currentX = switch_bg.getWidth();
			} else {
				left_slipBtn = off_rect.left;

				// 打开页面直接点击文字而非开关按钮时，若初始为“关闭”状态，则将初始坐标置为<1/2开关长度即可，以避免初始化图标错误;
				currentX = 0;
			}
		}

		// 对滑动按钮的位置进行异常判断
		if (left_slipBtn < 0) {
			left_slipBtn = 0;
		} else if (left_slipBtn > switch_bg.getWidth()
				- switch_on_bg.getWidth()) {
			left_slipBtn = switch_bg.getWidth() - switch_on_bg.getWidth();
		}

		// 按钮滑动到左半边时表示开关为关闭状态，滑动到右边时表示为开启状态
		if (currentX < switch_bg.getWidth() / 2) {
			canvas.drawBitmap(switch_off_bg, left_slipBtn, 0, paint);
		} else {
			canvas.drawBitmap(switch_on_bg, left_slipBtn, 0, paint);
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec); 
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec); 
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec); 
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec); 
  
        //定义最终确定的宽度和高度 
        int width,height; 
  
        //如果在xml文件中是明确定义视图大小，那么就使用明确定义的值 
        if(widthSpecMode == MeasureSpec.EXACTLY){ 
            width = widthSpecSize; 
        }else{ 
            //否则测量需要显示的内容所占的宽度(就是分配视图占用的宽度) 
            width = switch_bg.getWidth(); 
            //如果有定义一个最大宽度， 那么视图分配的宽度不得超过最大宽度 
            if(widthSpecMode == MeasureSpec.AT_MOST){ 
                width = Math.min(width, widthSpecSize); 
            } 
        }
  
        //下面测量高度的方式跟宽度差不多 
        if(heightSpecMode == MeasureSpec.EXACTLY){ 
            height = heightSpecSize; 
        }else{ 
            height = switch_bg.getHeight(); 
            if(heightSpecMode == MeasureSpec.AT_MOST){ 
                height = Math.min(height, heightSpecSize); 
            } 
        } 
        //这个一定要调用，  告诉系统测量的最终结果 需要的宽度是width 高度是height 
        setMeasuredDimension(width, height);
	}

	/**
	 * 将指定文字绘制到位图上，合并为一张位图
	 * @param bitmap
	 * @param text
	 * @param textColor
	 * @param textSize
	 * @return
	 */
	private Bitmap drawTextAtBitmap(Bitmap bitmap, String text, int textColor,
			int textSize) {
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();

		// 创建一个和原图一样大小 的位图
		Bitmap newbit = Bitmap.createBitmap(width, height,
				Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(newbit);
		Paint paint = new Paint();

		// 在原始位置插入位图
		canvas.drawBitmap(bitmap, 0, 0, paint);

		paint.setColor(textColor);
		paint.setTextSize(textSize);
		paint.setAntiAlias(true);

		FontMetrics fontMetrics = paint.getFontMetrics();
		// 获取字的高度
		int textHeight = (int) (fontMetrics.descent - fontMetrics.ascent);
		// 获取文字宽度
		int textWidth = (int) paint.measureText(text);

		canvas.drawText(text, (width - textWidth) / 2, (height + textHeight)
				/ 2 - textSize / 4, paint);
		canvas.save(Canvas.ALL_SAVE_FLAG);

		canvas.restore();
		return newbit;
	}

	private Bitmap changeBitmapSize(Bitmap bitmap, float width, float height) {
		Bitmap newbit = Bitmap.createScaledBitmap(bitmap, Math.round(width), Math.round(height), true);
		/*Canvas canvas = new Canvas(newbit);
		Paint paint = new Paint();
		canvas.drawBitmap(bitmap, 0, 0, paint);

		canvas.save(Canvas.ALL_SAVE_FLAG);
		canvas.restore();*/
		return newbit;
	}

	/**
	 * 将drawable转换为bitmap
	 * @param drawable
	 * @return
	 */
	private Bitmap drawableToBitmap(Drawable drawable) {
		Bitmap bitmap = Bitmap
				.createBitmap(
						drawable.getIntrinsicWidth(),
						drawable.getIntrinsicHeight(),
						drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
								: Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
				drawable.getIntrinsicHeight());
		drawable.draw(canvas);
		return bitmap;
	}
	
	public Drawable getSwitch_on_background() {
		return switch_on_background;
	}

	public void setSwitch_on_background(Drawable switch_on_background) {
		this.switch_on_background = switch_on_background;
	}

	public Drawable getSwitch_off_background() {
		return switch_off_background;
	}

	public void setSwitch_off_background(Drawable switch_off_background) {
		this.switch_off_background = switch_off_background;
	}

	public Drawable getSwitch_background() {
		return switch_background;
	}

	public void setSwitch_background(Drawable switch_background) {
		this.switch_background = switch_background;
	}

	public String getOnText() {
		return onText;
	}

	public void setOnText(String onText) {
		this.onText = onText;
	}

	public String getOffText() {
		return offText;
	}

	public void setOffText(String offText) {
		this.offText = offText;
	}

	public int getTextSize() {
		return textSize;
	}

	public void setTextSize(int textSize) {
		this.textSize = textSize;
	}

	public int getOnTextColor() {
		return onTextColor;
	}

	public void setOnTextColor(int onTextColor) {
		this.onTextColor = onTextColor;
	}

	public int getOffTextColor() {
		return offTextColor;
	}

	public void setOffTextColor(int offTextColor) {
		this.offTextColor = offTextColor;
	}

	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		switch (event.getAction()) {
		case MotionEvent.ACTION_MOVE:// 滑动
			currentX = event.getX();
			break;
		case MotionEvent.ACTION_DOWN:// 按下
			if (event.getX() > switch_bg.getWidth()
					|| event.getY() > switch_bg.getHeight()) {
				return false;
			}
			isSlipping = true;
			pressDownX = event.getX();
			currentX = pressDownX;
			break;
		case MotionEvent.ACTION_UP:// 松开
			isSlipping = false;
			// 松开前的开关状态
			boolean previousSwitchState = switchState;

			if (event.getX() >= switch_bg.getWidth() / 2) {
				switchState = true;
			} else {
				switchState = false;
			}

			// 如果设置了监听器，调用此方法
			if (isSwitchListenerOn && previousSwitchState != switchState) {
				onSwitchListener.onSwitched(switchState);
			}
			break;

		default:
			isSlipping = false;
			// 松开前的开关状态
			previousSwitchState = switchState;

			if (event.getX() >= switch_bg.getWidth() / 2) {
				switchState = true;
			} else {
				switchState = false;
			}

			// 如果设置了监听器，调用此方法
			if (isSwitchListenerOn && previousSwitchState != switchState) {
				onSwitchListener.onSwitched(switchState);
			}
			break;
		}

		// 重新绘制控件
		invalidate();
		return true;
	}

	public void setOnSwitchListener(OnSwitchListener listener) {
		onSwitchListener = listener;
		isSwitchListenerOn = true;
	}

	public interface OnSwitchListener {
		abstract void onSwitched(boolean switchState);
	}

}
