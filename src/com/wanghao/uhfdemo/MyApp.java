package com.wanghao.uhfdemo;

import android.app.Application;

public class MyApp extends Application{

	private  boolean loopFlag;
	private boolean hasPlayed;
	private boolean isPlaying;
	private String play_content;
	
	public String getPlay_content() {
		return play_content;
	}

	public void setPlay_content(String play_content) {
		this.play_content = play_content;
	}

	public boolean isHasPlayed() {
		return hasPlayed;
	}

	public void setHasPlayed(boolean hasPlayed) {
		this.hasPlayed = hasPlayed;
	}
	
	public boolean isPlaying() {
		return isPlaying;
	}

	public void setPlaying(boolean isPlaying) {
		this.isPlaying = isPlaying;
	}

	public boolean isLoopFlag() {
		return loopFlag;
	}

	public void setLoopFlag(boolean loopFlag) {
		this.loopFlag = loopFlag;
	}

	
}
