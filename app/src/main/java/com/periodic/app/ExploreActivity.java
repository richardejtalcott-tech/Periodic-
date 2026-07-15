package com.periodic.app;
import android.app.Activity;import android.os.Bundle;
public class ExploreActivity extends Activity {
 @Override protected void onCreate(Bundle b){super.onCreate(b);int n=getIntent().getIntExtra("atomicNumber",-1);Element e=ElementData.byNumber(n);if(e==null){finish();return;}setContentView(new ExploreView(this,e));overridePendingTransition(R.anim.exhibit_enter,R.anim.exhibit_exit);}
}
