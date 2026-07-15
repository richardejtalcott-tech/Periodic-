package com.periodic.app;
import android.content.Context;import android.graphics.*;import android.view.*;
public final class ScaleJourneyView extends View{
 private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);private float phase=0;
 private final String[] labels={"Human","Cell","Molecule","Atom","Nucleus","Proton","Quark scale"};
 private final String[] sizes={"~1 m","~10⁻⁵ m","~10⁻⁹ m","~10⁻¹⁰ m","~10⁻¹⁵ m","~10⁻¹⁵ m","<10⁻¹⁸ m"};
 public ScaleJourneyView(Context c){super(c);p.setTypeface(Typeface.create("sans",Typeface.BOLD));}
 @Override protected void onDraw(Canvas c){c.drawColor(Color.rgb(3,8,12));float w=getWidth(),h=getHeight();p.setTextAlign(Paint.Align.CENTER);p.setColor(Color.WHITE);p.setTextSize(h*.065f);c.drawText("A JOURNEY INTO MATTER",w/2,h*.11f,p);p.setColor(Color.rgb(139,202,216));p.setTextSize(h*.026f);c.drawText("Each step changes scale by enormous powers of ten",w/2,h*.16f,p);float start=w*.09f,end=w*.91f,y=h*.51f;p.setStrokeWidth(4);p.setColor(Color.rgb(54,104,116));c.drawLine(start,y,end,y,p);for(int i=0;i<labels.length;i++){float x=start+(end-start)*i/(labels.length-1f);float pulse=1+.07f*(float)Math.sin(Math.toRadians(phase*2+i*31));p.setColor(Color.rgb(55+i*18,137+i*8,164+i*7));c.drawCircle(x,y,h*.035f*pulse,p);p.setColor(Color.WHITE);p.setTextSize(h*.025f);c.drawText(labels[i],x,y-h*.075f,p);p.setColor(Color.rgb(163,192,198));p.setTextSize(h*.021f);c.drawText(sizes[i],x,y+h*.095f,p);}p.setColor(Color.rgb(191,209,213));p.setTextSize(h*.021f);c.drawText("Scale values are orders of magnitude; illustrations are not drawn to one common physical scale.",w/2,h*.84f,p);phase=(phase+.7f)%360;postInvalidateOnAnimation();}
}
