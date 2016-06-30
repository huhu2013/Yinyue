package com.yangjiahua.yinyue;

import java.util.List;


public interface ILrcView {

	void init();
	void setLrcRows(List<LrcRow> lrcRows);
	void seekTo(int progress, boolean fromSeekBar, boolean fromSeekBarByUser);
	void setLrcScalingFactor(float scalingFactor);
	void reset();
}
