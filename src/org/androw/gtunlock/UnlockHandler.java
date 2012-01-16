package org.androw.gtunlock;

import android.os.Handler;
import android.os.Message;

public class UnlockHandler extends Handler
{
	public static final int MSG_REBOOT = 1;
	public static final int MSG_QUIT = 2;
	
	private GalaxyTabUnlock primInstance = null;
	
	public UnlockHandler (GalaxyTabUnlock gtu) {
		this.primInstance = gtu;
	}
	
	@Override
	public void handleMessage (Message msg) {
		switch (msg.what) {
			case MSG_REBOOT:primInstance.reboot();
			case MSG_QUIT:primInstance.quit();
			default:
		}
	}
}
