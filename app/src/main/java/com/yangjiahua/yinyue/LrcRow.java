package com.yangjiahua.yinyue;

import android.text.TextUtils;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public class LrcRow implements Comparable<LrcRow>{

	private String timeStr;
	private int time;
	private String content;
	private int totalTime;
	public long getTotalTime() {
		return totalTime;
	}
	public void setTotalTime(int totalTime) {
		this.totalTime = totalTime;
	}
	public String getTimeStr() {
		return timeStr;
	}
	public void setTimeStr(String timeStr) {
		this.timeStr = timeStr;
	}
	public int getTime() {
		return time;
	}
	public void setTime(int time) {
		this.time = time;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}

	public LrcRow() {
		super();
	}
	public LrcRow(String timeStr, int time, String content) {
		super();
		this.timeStr = timeStr;
		this.time = time;
		this.content = content;
	}

	public static final List<LrcRow> createRows(String lrcLine){
		if(!lrcLine.startsWith("[") || lrcLine.indexOf("]") != 9){
			return null;
		}
		int lastIndexOfRightBracket = lrcLine.lastIndexOf("]");
		String content = lrcLine.substring(lastIndexOfRightBracket+1, lrcLine.length());
		String times = lrcLine.substring(0, lastIndexOfRightBracket+1).replace("[", "-").replace("]", "-");
		String[] timesArray = times.split("-");
		List<LrcRow> lrcRows = new ArrayList<LrcRow>();
		for (String tem : timesArray) {
			if(TextUtils.isEmpty(tem.trim())){
				continue;
			}
			try{
				LrcRow lrcRow = new LrcRow(tem, formatTime(tem), content);
				lrcRows.add(lrcRow);
			}catch(Exception e){
				Log.w("LrcRow", e.getMessage());
			}
		}
		return lrcRows;
	}

	private static int formatTime(String timeStr) {
		timeStr = timeStr.replace('.', ':');
		String[] times = timeStr.split(":");

		return Integer.parseInt(times[0])*60*1000
				+ Integer.parseInt(times[1])*1000 
				+ Integer.parseInt(times[2]);
	}
	@Override
	public int compareTo(LrcRow anotherLrcRow) {
		return (int) (this.time - anotherLrcRow.time);
	}
	@Override
	public String toString() {
		return "LrcRow [timeStr=" + timeStr + ", time=" + time + ", content="
				+ content + "]";
	} 


}
