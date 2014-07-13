package com.wanghao.uhfdemo;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class MyPagerAdapter extends FragmentPagerAdapter{
	
	final String[] TITLES = getTitles();
	
	public String[] getTitles(){
		String[] titles = new String[8];
		for(int title=1;title<=8;title++)
			titles[title-1] = Integer.toString(title);
		return titles;
	}
	
	public MyPagerAdapter(FragmentManager fm) {
		super(fm);
	}
	
	@Override
	public CharSequence getPageTitle(int position) {
		return TITLES[position];
	}
	
	@Override
	public int getCount() {
		
		return TITLES.length;
	}
	
	@Override
	public Fragment getItem(int position) {
		return SuperAwesomeCardFragment.newInstance(position);
	}

}
