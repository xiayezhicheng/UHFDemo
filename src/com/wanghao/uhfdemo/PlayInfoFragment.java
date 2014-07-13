package com.wanghao.uhfdemo;

import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;


public class PlayInfoFragment extends DialogFragment {

	private PagerSlidingTabStrip tabs;
	private ViewPager pager;
	private PlayInfoAdapter adapter;

	public static PlayInfoFragment newInstance() {
		PlayInfoFragment f = new PlayInfoFragment();
		return f;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		if (getDialog() != null) {
			getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
			getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
		}

		View root = inflater.inflate(R.layout.fragment_play_info, container, false);

		tabs = (PagerSlidingTabStrip) root.findViewById(R.id.tabs);
		pager = (ViewPager) root.findViewById(R.id.pager);
		adapter = new PlayInfoAdapter();

		pager.setAdapter(adapter);

		tabs.setViewPager(pager);

		return root;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onStart() {
		super.onStart();

		// change dialog width
		if (getDialog() != null) {

			int fullWidth = getDialog().getWindow().getAttributes().width;

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
				Display display = getActivity().getWindowManager().getDefaultDisplay();
				Point size = new Point();
				display.getSize(size);
				fullWidth = size.x;
			} else {
				Display display = getActivity().getWindowManager().getDefaultDisplay();
				fullWidth = display.getWidth();
			}

			final int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources()
					.getDisplayMetrics());

			int w = fullWidth - padding;
			int h = getDialog().getWindow().getAttributes().height;

			getDialog().getWindow().setLayout(w, h);
		}
	}

	public class PlayInfoAdapter extends PagerAdapter{

		private final String[] TITLES = {"ƒ⁄»›","Ã·–—"};
		
		
		@Override
		public CharSequence getPageTitle(int position) {
			return TITLES[position];
		}
		

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			TextView v = new TextView(getActivity());
			v.setBackgroundResource(R.color.background_window);
			v.setText("PAGE " + (position + 1));
			final int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, getResources()
					.getDisplayMetrics());
			v.setPadding(padding, padding, padding, padding);
			v.setGravity(Gravity.CENTER);
			container.addView(v, 0);
			return v;
		}


		@Override
		public int getCount() {
			return TITLES.length;
		}
		
		@Override
		public void destroyItem(ViewGroup container, int position, Object view) {
			container.removeView((View) view);
		}
		
		@Override
		public boolean isViewFromObject(View v, Object o) {
			return v == ((View) o);
		}

	}


}
