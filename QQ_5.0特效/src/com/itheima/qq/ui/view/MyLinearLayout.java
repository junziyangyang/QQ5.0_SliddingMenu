package com.itheima.qq.ui.view;

import com.itheima.qq.ui.view.SlidMenuLayout.Status;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

public class MyLinearLayout extends LinearLayout {

	private SlidMenuLayout mSlidMenuLayout;

	public MyLinearLayout(Context context) {
		super(context);
	}

	public MyLinearLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public void setSlidMenuLayout(SlidMenuLayout mSlidMenuLayout){
		this.mSlidMenuLayout = mSlidMenuLayout;
		
	}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		//如果当前是关闭状态，按之前的方法处理
		if (mSlidMenuLayout.getmStatus() == Status.Close) {
			return super.onInterceptTouchEvent(ev);
		} else {
			//其他状态拦截
			return true;
		}
		
	}
	
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		//如果当前是关闭状态，按之前的方法处理
		if (mSlidMenuLayout.getmStatus() == Status.Close) {
			return super.onTouchEvent(event);
		} else {
			//状态是打开，手指抬起，执行关闭操作
			if (event.getAction() == MotionEvent.ACTION_UP) {
				mSlidMenuLayout.close();
			}
			//其他状态拦截
			return true;
		}
		
	}

}
