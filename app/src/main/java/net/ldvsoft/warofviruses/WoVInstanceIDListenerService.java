package net.ldvsoft.warofviruses;

import android.content.Intent;

import com.google.android.gms.iid.InstanceIDListenerService;

/**
 * Created by ldvsoft on 21.10.15.
 */
public class WoVInstanceIDListenerService extends InstanceIDListenerService {
    @Override
    public void onTokenRefresh() {
        //TODO write something right
        //Maybe send message to server?
    }
}
