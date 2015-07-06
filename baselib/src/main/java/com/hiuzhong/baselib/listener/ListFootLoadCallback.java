package com.hiuzhong.baselib.listener;

public interface ListFootLoadCallback {
	static final int LOAD_RESULT_SUCCESS=1;
	static final int LOAD_RESULT_FAIL=0;
	static final int LOAD_RESULT_NO_MORE_DATA=2;
	static final int LOAD_RESULT_ABORT=3;

	public void loadFinish(int status);
}
