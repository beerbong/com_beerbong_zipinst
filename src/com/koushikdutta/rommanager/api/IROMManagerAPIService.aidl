package com.koushikdutta.rommanager.api;

import com.koushikdutta.rommanager.api.IClockworkRecoveryScriptBuilder;

interface IROMManagerAPIService {
	boolean isPremium();
	void installZip(String path);

	// will return null if not premium
	IClockworkRecoveryScriptBuilder createClockworkRecoveryScriptBuilder();
	
	void rebootRecovery();
	String getClockworkModRecoveryVersion();
}
