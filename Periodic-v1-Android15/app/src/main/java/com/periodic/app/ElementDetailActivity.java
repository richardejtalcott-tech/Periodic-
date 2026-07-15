package com.periodic.app;

import android.app.Activity;
import android.os.Bundle;

/** Displays the interactive atomic detail illustration for one element. */
public class ElementDetailActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int number = getIntent().getIntExtra("element", 1);
        Element element = ElementData.byNumber(number);
        if (element == null) {
            element = ElementData.byNumber(1);
        }
        setContentView(new ElementDetailView(this, element));
    }
}
