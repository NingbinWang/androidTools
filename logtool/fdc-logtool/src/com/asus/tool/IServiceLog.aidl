package com.asus.tool;

interface IServiceLog{
	void audioLogEnable(boolean enable);
	void wifiLogEnable(boolean enable);
	void powerLogEnable(boolean enable);
	void batteryLogEnable(boolean enable);
	void meminfoLogEnable(boolean enable);
	void diskLogEnable(boolean enable);
	void topLogEnable(boolean enable);
	void procaneLogEnable(boolean enable);
	void cpuLogEnable(boolean enable);
	void cpuLoadingEnable(boolean enable);
	void cpuLoadingChangeTime();
	void activityLogEnable(boolean enable);
	void windowLogEnable(boolean enable);
	void onLogPathChange();
	void onAutoUpdateMtp(boolean enable);
	void onRefreshMtp();
	void onReStartLog();
	boolean isSystemReady();
	void setLogAllEnable(boolean enable);
}