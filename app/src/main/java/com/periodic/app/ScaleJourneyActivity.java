package com.periodic.app;
import android.app.Activity;import android.os.Bundle;
public class ScaleJourneyActivity extends Activity {
 @Override protected void onCreate(Bundle b){super.onCreate(b);setContentView(new ScaleJourneyView(this));overridePendingTransition(R.anim.exhibit_enter,R.anim.exhibit_exit);}
}
