package com.itheima.qq.ui.activity;

import java.util.Random;

import com.itheima.qq.R;
import com.itheima.qq.R.id;
import com.itheima.qq.R.layout;
import com.itheima.qq.ui.view.MyLinearLayout;
import com.itheima.qq.ui.view.SlidMenuLayout;
import com.itheima.qq.ui.view.SlidMenuLayout.OnDragStatusChangeListener;
import com.itheima.qq.utils.Cheeses;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.view.ViewHelper;

import android.os.Bundle;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.CycleInterpolator;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends Activity {

	private MyLinearLayout mMain;
	private LinearLayout mMenu;
	private SlidMenuLayout mSmlRoot;
	private ListView mMainListview,mMenuListview;
	private ImageView mIvHead;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		initView();
		initData();
	}

	/**
	 * 初始化数据
	 */
	private void initData() {
		//设置main主面板数据
		mMainListview.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, Cheeses.NAMES));
		//设置menu侧面板数据
		mMenuListview.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, Cheeses.sCheeseStrings){
			
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View view = super.getView(position, convertView, parent);
				//simple_list_item_1底层是TextView,所以可以强转
				TextView textView = ((TextView)view);
				//设置文字颜色
				textView.setTextColor(Color.WHITE);
				return view;
				
			}
		});
		
		//设置mSmlRoot的监听事件
		mSmlRoot.setOnDragStatusListener(new OnDragStatusChangeListener() {
			
			@Override
			public void onOpen() {
				//打开侧边栏随机滚动到一个条目
				Random random = new Random();
				int nextInt = random.nextInt(50);
				mMenuListview.smoothScrollToPosition(nextInt);
			}
			
			@Override
			public void onDraging(float percent) {
				//更新图标的透明度
				ViewHelper.setAlpha(mIvHead, 1-percent);
			}
			
			@Override
			public void onClose() {
				//关闭时图标晃动
				ObjectAnimator animator = ObjectAnimator.ofFloat(mIvHead, "translationX", 15.0f);
				animator.setInterpolator(new CycleInterpolator(4));//设置动画插补器
				animator.setDuration(500);
				animator.start();
			}
		});
		//设置引用
		mMain.setSlidMenuLayout(mSmlRoot);
		
	}

	/**
	 * 初始化控件
	 */
	private void initView() {
		mSmlRoot = (SlidMenuLayout) findViewById(R.id.smlRoot);
		
		mMain = (MyLinearLayout) findViewById(R.id.ll_main);
		mMenu = (LinearLayout) findViewById(R.id.ll_menu);
		
		mMainListview = (ListView) findViewById(R.id.main_listview);
		mMenuListview = (ListView) findViewById(R.id.menu_listview);
		
		mIvHead = (ImageView) findViewById(R.id.iv_head);
		
		
	}



}
