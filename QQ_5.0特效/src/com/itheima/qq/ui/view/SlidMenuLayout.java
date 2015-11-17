package com.itheima.qq.ui.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.support.v4.widget.ViewDragHelper.Callback;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.nineoldandroids.view.ViewHelper;

/**自定义侧滑面板效果
 * @author Dell_N4110
 *
 */
public class SlidMenuLayout extends FrameLayout {

	private View mMainView;
	private View mMenuView;
	private ViewDragHelper mDragHelper;
	private int mHeight;
	private int mWidth;
	private int mRange;

	public SlidMenuLayout(Context context) {
		this(context,null);
	}

	public SlidMenuLayout(Context context, AttributeSet attrs) {
		this(context, attrs,0);
	}

	public SlidMenuLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}
	
	private void init() {
		//初始化ViewDragHelper对象
		mDragHelper = ViewDragHelper.create(this, callback);
	}

	@Override
	protected void onFinishInflate() {
		//容错性检查
		if (getChildCount() < 2) {
			throw new IllegalStateException("不能少于两个布局!");
		}
		//判断是否是ViewGroup的子类
		if (!(getChildAt(0) instanceof ViewGroup && getChildAt(1) instanceof ViewGroup)) {
			throw new IllegalArgumentException("类型异常，子View必须是ViewGroup的子类");
		}
		
		mMenuView = getChildAt(0);
		mMainView = getChildAt(1);
		
		
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		// 当尺寸有变化的时候调用
		mHeight = getMeasuredHeight();//获取高度
		mWidth = getMeasuredWidth();//获取宽度
		
		// 移动的范围
		mRange = (int) (mWidth * 0.6f);
	}
	
	//传递触摸事件
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		// 传递给mDragHelper
		return mDragHelper.shouldInterceptTouchEvent(ev);
	}
	//触摸事件
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		mDragHelper.processTouchEvent(event);
		// 返回true, 持续接受事件
		return true;
	}
	
	private Callback callback = new Callback() {
		/**
		 * 根据返回结果决定当前child是否可以拖拽
		 * child 当前被拖拽的View
		 * pointerId 区分多点触摸的id
		 */
		@Override
		public boolean tryCaptureView(View child, int pointerId) {
			
			return child == mMainView || child == mMenuView;
		}
		/**
		 * 获取view水平方向的拖拽范围, 不对拖拽进行真正的限制. 仅仅决定了动画执行速度
		 * 最好不要返回0
		 */
		public int getViewHorizontalDragRange(View child) {
			return mRange;
			
		}
		
		/**
		 * 控制child在水平方向的移动 
		 * left:表示ViewDragHelper认为你想让当前child的left改变的值,left=chile.getLeft()+dx
		 * dx:本次child水平方向移动的距离
		 * return: 表示你真正想让child的left变成的值
		 */
		public int clampViewPositionHorizontal(View child, int left, int dx) {
			
			if (child == mMainView) {
				left = fixLeft(left);
			}
			return left;
			
		}
		
		/**根据范围修正左边距的值
		 * @param left
		 * @return
		 */
		private int fixLeft(int left) {
			if (left < 0 ) {
				return 0;
			}else if (left > mRange) {
				return mRange;
			}
			return left;
		}
		
		/**
		 * 控制child在垂直方向的移动 top:
		 * 表示ViewDragHelper认为你想让当前child的top改变的值,top=chile.getTop()+dy dy:
		 * 本次child垂直方向移动的距离 return: 表示你真正想让child的top变成的值
		 */
		public int clampViewPositionVertical(View child, int top, int dy) {
			return 0;
		}
		
		/**
		 * 当child的位置改变的时候执行,处理要做的事情 (更新状态, 伴随动画, 重绘界面) 
		 * changedView：改变位置的child
		 * left：child当前最新的left 
		 * top: child当前最新的top
		 * dx: 本次水平移动的距离
		 * dy: 本次垂直移动的距离
		 */
		public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
			super.onViewPositionChanged(changedView, left, top, dx, dy);
			int newLeft = left;
			if (changedView == mMenuView) {
				// 把当前变化量传递给mMainView
				newLeft = mMainView.getLeft() + dx;
			}
			// 进行修正
			fixLeft(newLeft);
			
			if (changedView == mMenuView) {
				// 当左面板移动之后, 再强制放回去.
				mMenuView.layout(0, 0, 0 + mWidth, 0 + mHeight);
				mMainView.layout(newLeft, 0, newLeft + mWidth, 0 + mHeight);
			}
			// 更新状态,执行动画
			dispatchDragEvent(newLeft);
			// 为了兼容低版本, 每次修改值之后, 进行重绘
			invalidate();
		}
		
		/**
		 * 手指抬起的执行该方法
		 * releasedChild：当前抬起的view
		 * xvel: x方向的移动的速度 正：向右移动， 负：向左移动
		 * yvel: y方向移动的速度
		 */
		public void onViewReleased(View releasedChild, float xvel, float yvel) {
			super.onViewReleased(releasedChild, xvel, yvel);
			if (xvel == 0 && mMainView.getLeft() > mRange / 2.0f) {
				open();
			}else if(xvel > 0){
				open();
			}else{
				close();
			}
		}
	};
	
	private OnDragStatusChangeListener mListener;
	private Status mStatus = Status.Close;//默认状态为关闭
	
	/**设置状态枚举
	 * @author Dell_N4110
	 *
	 */
	public static enum Status{
		Open,Close,Draging;
	}
	
	public Status getmStatus() {
		return mStatus;
	}

	public void setmStatus(Status mStatus) {
		this.mStatus = mStatus;
	}
	/**定义接口
	 * @author Dell_N4110
	 *
	 */
	public interface OnDragStatusChangeListener{
		void onOpen();
		void onClose();
		void onDraging(float percent);
	}
	
	/**设置接口回调
	 * @param listener
	 */
	public void setOnDragStatusListener(OnDragStatusChangeListener listener){
		this.mListener = listener;
	}
	
	

	protected void dispatchDragEvent(int newLeft) {
		//获取移动的比例0.0~1
		float percent = newLeft * 1.0f / mRange;
		
		if (mListener != null) {
			mListener.onDraging(percent);
		}
		
		//更新装填，执行回调
		Status preStatus = mStatus;//存储上一次状态
		mStatus = updateStatus(percent);
		if (mStatus != preStatus) {//判断当前是否不等于上一次状态
			//状态发生变化
			if (mStatus == Status.Close) {
				//当前变为关闭
				if (mListener != null) {
					mListener.onClose();
				}
			}else if (mStatus == Status.Open) {
				//当前变为开启
				if (mListener != null) {
					mListener.onOpen();
				}
			}
		}
		
		animViews(percent);
	}

	private Status updateStatus(float percent) {
		
		if (percent == 0f) {
			return mStatus.Close;
		} else if (percent == 1.0f) {
			return mStatus.Open;
		} else {
			return mStatus.Draging;
		}
	}

	/**s设置伴随动画
	 * @param percent
	 */
	private void animViews(float percent) {
		//1. 左面板: 
		// 缩放动画 0.0 -> 1.0 >>> 0.5f -> 1.0f  >>> 0.5f * percent + 0.5f
		//mLeftContent.setScaleX(0.5f + 0.5f * percent);//低版本不兼容
		//mLeftContent.setScaleY(0.5f + 0.5f * percent);//低版本不兼容
		//nineoldandroids-2.4.0.jar中的方法，兼容低版本
		ViewHelper.setScaleX(mMenuView, evaluate(percent, 0.5f, 1.0f));
//		ViewHelper.setScaleY(mLeftContent, 0.5f + 0.5f * percent);
		ViewHelper.setScaleY(mMenuView, evaluate(percent, 0.5f, 1.0f));
		//平移动画
		ViewHelper.setTranslationX(mMenuView, evaluate(percent, -mWidth / 2.0f, 0));
		//透明动画
		ViewHelper.setAlpha(mMenuView, evaluate(percent, 0.5f, 1.0f));
		
		//2. 主面板: 缩放动画
		ViewHelper.setScaleX(mMainView, evaluate(percent, 1.0f, 0.8f));
		ViewHelper.setScaleY(mMainView, evaluate(percent, 1.0f, 0.8f));
		
		//3. 背景动画: 亮度变化 (颜色变化)
		//注意：调用getBackground()该方法获取背景时，需先在xml文件中设置背景background
		getBackground().setColorFilter((Integer)evaluateColor(percent, Color.BLACK, Color.TRANSPARENT),
					Mode.SRC_OVER);
	}
	
	
	public void close(){
		close(true);
	}
	
	public void open(){
		open(true);
	}
	
	@Override
	public void computeScroll() {
		super.computeScroll();
		// 2. 持续平滑动画 (高频率调用)
		if (mDragHelper.continueSettling(true)) {
			//  如果返回true, 动画还需要继续执行
			ViewCompat.postInvalidateOnAnimation(this);
		}
	}
	/**
	 * 关闭
	 */
	public void close(boolean isSmooth) {
		int finalLeft = 0;
		if (isSmooth) {
			//1.触发一个平滑动画
			if (mDragHelper.smoothSlideViewTo(mMainView, finalLeft, 0)) {
				// 返回true代表还没有移动到指定位置, 需要刷新界面.
				// 参数传this(child所在的ViewGroup)
				ViewCompat.postInvalidateOnAnimation(this);
			}
			
		} else {
			mMainView.layout(finalLeft, 0, finalLeft + mWidth, 0 + mHeight);
		}
	}

	/**
	 * 开启
	 */
	public void open(boolean isSmooth) {
		int finalLeft = mRange;
		if (isSmooth) {
			//1.触发一个平滑动画
			if (mDragHelper.smoothSlideViewTo(mMainView, finalLeft, 0)) {
				// 返回true代表还没有移动到指定位置, 需要刷新界面.
				// 参数传this(child所在的ViewGroup)
				ViewCompat.postInvalidateOnAnimation(this);
			}
			
		} else {
			mMainView.layout(finalLeft, 0, finalLeft + mWidth, 0 + mHeight);
		}
		
	}

//	TypeEvaluator<T>
	
    /**估值器
     * @param fraction
     * @param startValue
     * @param endValue
     * @return
     */
    public Float evaluate(float fraction, Number startValue, Number endValue) {
        float startFloat = startValue.floatValue();
        return startFloat + fraction * (endValue.floatValue() - startFloat);
    }
    /**
     * 颜色变化过度器
     * @param fraction
     * @param startValue
     * @param endValue
     * @return
     */
    public Object evaluateColor(float fraction, Object startValue, Object endValue) {
        int startInt = (Integer) startValue;
        int startA = (startInt >> 24) & 0xff;
        int startR = (startInt >> 16) & 0xff;
        int startG = (startInt >> 8) & 0xff;
        int startB = startInt & 0xff;

        int endInt = (Integer) endValue;
        int endA = (endInt >> 24) & 0xff;
        int endR = (endInt >> 16) & 0xff;
        int endG = (endInt >> 8) & 0xff;
        int endB = endInt & 0xff;

        return (int)((startA + (int)(fraction * (endA - startA))) << 24) |
                (int)((startR + (int)(fraction * (endR - startR))) << 16) |
                (int)((startG + (int)(fraction * (endG - startG))) << 8) |
                (int)((startB + (int)(fraction * (endB - startB))));
    }
}
