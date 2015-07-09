package com.hiuzhong.baselib.listener;

public interface RefreshResultNotify {
	void refreshSuccess();
	void refreshFail();
	void abort();
	int curStatus();
}
