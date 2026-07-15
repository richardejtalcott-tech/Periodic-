package com.periodic.app;

import android.app.Activity;
import android.os.Bundle;

/** Main periodic-table screen. */
public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new PeriodicTableView(this));
    }
}
