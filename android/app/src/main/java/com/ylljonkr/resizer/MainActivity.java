package com.ylljonkr.resizer;

import android.os.Bundle;

import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        // Our own plugin (GallerySaverPlugin.java) must be registered before super.onCreate
        registerPlugin(GallerySaverPlugin.class);
        super.onCreate(savedInstanceState);
    }
}
