package com.periodic.app;
import android.app.Activity;import android.os.Bundle;
public class ElementDetailActivity extends Activity {
 @Override protected void onCreate(Bundle b){super.onCreate(b);int n=getIntent().getIntExtra("atomicNumber",getIntent().getIntExtra("element",-1));Element e=ElementData.byNumber(n);if(e==null){finish();return;}setContentView(new ElementDetailView(this,e));overridePendingTransition(R.anim.exhibit_enter,R.anim.exhibit_exit);}
}
