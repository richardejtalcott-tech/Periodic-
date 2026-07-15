package com.periodic.app;

import android.content.Context;
import android.graphics.*;
import android.view.*;

public class ElementDetailView extends View {
    private final Element e;
    private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.DITHER_FLAG);
    private final ScaleGestureDetector scaler;
    private float zoom=1,panX=0,panY=0,lastX,lastY,phase=0;

    public ElementDetailView(Context c,Element e){
        super(c);this.e=e;p.setTypeface(Typeface.create("sans",Typeface.BOLD));
        scaler=new ScaleGestureDetector(c,new ScaleGestureDetector.SimpleOnScaleGestureListener(){public boolean onScale(ScaleGestureDetector d){zoom=Math.max(.7f,Math.min(3.5f,zoom*d.getScaleFactor()));invalidate();return true;}});
    }
    @Override public boolean onTouchEvent(MotionEvent m){scaler.onTouchEvent(m);if(m.getAction()==MotionEvent.ACTION_DOWN){lastX=m.getX();lastY=m.getY();}else if(m.getAction()==MotionEvent.ACTION_MOVE&&m.getPointerCount()==1){panX+=m.getX()-lastX;panY+=m.getY()-lastY;lastX=m.getX();lastY=m.getY();invalidate();}return true;}
    @Override protected void onDraw(Canvas c){c.drawColor(Color.rgb(1,5,13));stars(c);c.save();c.translate(getWidth()/2+panX,getHeight()/2+panY);c.scale(zoom,zoom);c.translate(-800,-450);drawContent(c);c.restore();phase=(phase+.9f)%360f;postInvalidateOnAnimation();}

    private void stars(Canvas c){
        for(int i=0;i<120;i++){float tw=.55f+.45f*(float)Math.sin(Math.toRadians(phase*1.5f+i*29));p.setColor(Color.argb((int)(45+145*tw),180,205,255));c.drawCircle((i*131%997)/997f*getWidth(),(i*239%991)/991f*getHeight(),.8f+(i%3)*.45f,p);}
        p.setShader(new RadialGradient(getWidth()*.18f,getHeight()*.35f,getWidth()*.45f,new int[]{Color.argb(42,38,66,130),Color.TRANSPARENT},null,Shader.TileMode.CLAMP));c.drawRect(0,0,getWidth(),getHeight(),p);p.setShader(null);
    }

    private void drawContent(Canvas c){
        p.setTextAlign(Paint.Align.LEFT);p.setColor(Color.WHITE);p.setTextSize(43);c.drawText("A "+e.name+" Atom",35,58,p);p.setColor(Color.rgb(221,191,93));p.setTextSize(22);c.drawText("inside matter — from atom to quarks",38,90,p);
        atom(c,205,330,150);
        drawConnector(c,340,330,445,270);
        nucleusLens(c,535,255,112);
        drawConnector(c,645,265,725,255);
        protonLens(c,835,255,112);
        drawConnector(c,945,265,1025,255);
        neutronLens(c,1135,255,112);
        drawConnector(c,1245,280,1320,390);
        quarkLens(c,1415,470,102);
        electronLens(c,310,690,103);
        drawConnector(c,240,460,300,585);
        profilePanel(c);
        scalePanel(c);
        factPanel(c);
        p.setColor(Color.argb(190,0,0,0));c.drawRoundRect(32,785,285,850,18,18,p);p.setColor(Color.WHITE);p.setTextSize(16);c.drawText("Pinch to zoom  •  Drag to explore",52,825,p);
    }

    private void drawConnector(Canvas c,float x1,float y1,float x2,float y2){p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2);p.setColor(Color.argb(95,225,215,190));c.drawLine(x1,y1,x2,y2,p);p.setStyle(Paint.Style.FILL);}

    private void atom(Canvas c,float x,float y,float r){
        p.setTextAlign(Paint.Align.CENTER);p.setColor(Color.WHITE);p.setTextSize(25);c.drawText(e.name+" Atom",x,y-r-43,p);p.setColor(Color.rgb(222,194,99));p.setTextSize(18);c.drawText("("+e.symbol+")",x,y-r-18,p);
        float[] tilts={-24,27,86};int[] cols={Color.rgb(89,151,225),Color.rgb(197,78,102),Color.rgb(221,190,91)};
        p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2.6f);for(int k=0;k<3;k++){p.setColor(cols[k]);c.save();c.rotate(tilts[k],x,y);c.drawOval(x-r*1.02f,y-r*.38f,x+r*1.02f,y+r*.38f,p);c.restore();}
        int total=Math.max(2,Math.min(38,e.number+e.neutrons()));p.setStyle(Paint.Style.FILL);for(int i=0;i<total;i++){double a=i*2.399963;float rr=(float)Math.sqrt(i/(float)total)*r*.36f;float px=x+(float)Math.cos(a)*rr,py=y+(float)Math.sin(a)*rr;int col=i%2==0?Color.rgb(190,63,91):Color.rgb(113,118,153);p.setShader(new RadialGradient(px-3,py-3,r*.08f,Visuals.lighten(col,.4f),col,Shader.TileMode.CLAMP));c.drawCircle(px,py,r*.065f,p);p.setShader(null);}
        int electrons=Math.min(e.number,12);for(int i=0;i<electrons;i++){int k=i%3;double a=Math.toRadians(phase*(.65f+k*.22f)+i*360f/electrons);drawEllipseElectron(c,x,y,r,tilts[k],a,cols[k]);}
        p.setTextAlign(Paint.Align.LEFT);p.setColor(Color.WHITE);p.setTextSize(17);c.drawText("Atomic number: "+e.number,55,525,p);p.setColor(Color.rgb(231,128,153));c.drawText(e.number+" protons",55,553,p);p.setColor(Color.rgb(170,177,211));c.drawText(e.neutrons()+" neutrons (estimated)",55,581,p);p.setColor(Color.rgb(121,181,239));c.drawText(e.number+" electrons",55,609,p);
    }

    private void drawEllipseElectron(Canvas c,float cx,float cy,float r,float tilt,double a,int orbitColor){float ex=(float)Math.cos(a)*r*1.02f,ey=(float)Math.sin(a)*r*.38f;double t=Math.toRadians(tilt);float x=cx+ex*(float)Math.cos(t)-ey*(float)Math.sin(t),y=cy+ex*(float)Math.sin(t)+ey*(float)Math.cos(t);p.setShader(new RadialGradient(x,y,16,new int[]{Color.argb(190,135,195,255),Color.argb(40,Color.red(orbitColor),Color.green(orbitColor),Color.blue(orbitColor)),Color.TRANSPARENT},null,Shader.TileMode.CLAMP));c.drawCircle(x,y,16,p);p.setShader(null);p.setColor(Color.WHITE);c.drawCircle(x,y,5.5f,p);}

    private void lensBase(Canvas c,float x,float y,float r,String title,String scale,int accent){p.setShadowLayer(18,7,11,Color.BLACK);p.setColor(Visuals.darken(accent,.38f));c.drawCircle(x,y,r+9,p);p.clearShadowLayer();p.setShader(new RadialGradient(x-r*.28f,y-r*.32f,r*1.15f,new int[]{Color.rgb(50,76,117),Color.rgb(8,18,34),Color.rgb(1,4,10)},null,Shader.TileMode.CLAMP));c.drawCircle(x,y,r,p);p.setShader(null);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(4);p.setColor(Visuals.lighten(accent,.35f));c.drawCircle(x,y,r,p);p.setStyle(Paint.Style.FILL);p.setTextAlign(Paint.Align.CENTER);p.setColor(accent);p.setTextSize(21);c.drawText(title,x,y-r-22,p);p.setTextSize(15);c.drawText(scale,x,y-r-3,p);}

    private void nucleusLens(Canvas c,float x,float y,float r){int ac=Color.rgb(224,190,91);lensBase(c,x,y,r,"NUCLEUS","~10⁻¹⁵ m",ac);for(int i=0;i<14;i++){double a=i*2.4;float rr=(float)Math.sqrt(i/14f)*r*.45f;float px=x+(float)Math.cos(a)*rr,py=y+(float)Math.sin(a)*rr;int col=i%2==0?Color.rgb(199,67,99):Color.rgb(141,146,179);p.setColor(col);c.drawCircle(px,py,r*.13f,p);p.setColor(Color.argb(210,255,255,255));p.setTextSize(16);p.setTextAlign(Paint.Align.CENTER);c.drawText(i%2==0?"+":"",px,py+5,p);}caption(c,x,y+r+28,"Contains protons + neutrons","Holds nearly all atomic mass");}

    private void protonLens(Canvas c,float x,float y,float r){int ac=Color.rgb(234,105,139);lensBase(c,x,y,r,"PROTON","~10⁻¹⁵ m",ac);p.setShader(new RadialGradient(x-r*.25f,y-r*.28f,r*.65f,new int[]{Color.rgb(255,156,183),Color.rgb(176,50,91),Color.rgb(62,11,31)},null,Shader.TileMode.CLAMP));c.drawCircle(x,y,r*.58f,p);p.setShader(null);p.setColor(Color.WHITE);p.setTextSize(49);p.setTextAlign(Paint.Align.CENTER);c.drawText("+",x,y+17,p);drawMiniQuarks(c,x,y,r*.38f,true);caption(c,x,y+r+28,"Positive charge: +1","2 up quarks + 1 down quark");}

    private void neutronLens(Canvas c,float x,float y,float r){int ac=Color.rgb(184,154,213);lensBase(c,x,y,r,"NEUTRON","~10⁻¹⁵ m",ac);p.setShader(new RadialGradient(x-r*.25f,y-r*.28f,r*.65f,new int[]{Color.rgb(205,211,227),Color.rgb(96,108,140),Color.rgb(26,30,45)},null,Shader.TileMode.CLAMP));c.drawCircle(x,y,r*.58f,p);p.setShader(null);p.setColor(Color.WHITE);p.setTextSize(38);p.setTextAlign(Paint.Align.CENTER);c.drawText("0",x,y+14,p);drawMiniQuarks(c,x,y,r*.38f,false);caption(c,x,y+r+28,"No net electric charge","1 up quark + 2 down quarks");}

    private void drawMiniQuarks(Canvas c,float x,float y,float rr,boolean proton){String[] q=proton?new String[]{"u","u","d"}:new String[]{"u","d","d"};int[] col={Color.rgb(212,68,78),Color.rgb(70,132,207),Color.rgb(93,176,83)};for(int i=0;i<3;i++){double a=-Math.PI/2+i*2*Math.PI/3;float px=x+(float)Math.cos(a)*rr,py=y+(float)Math.sin(a)*rr;p.setColor(col[i]);c.drawCircle(px,py,15,p);p.setColor(Color.WHITE);p.setTextSize(15);p.setTextAlign(Paint.Align.CENTER);c.drawText(q[i],px,py+5,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2);p.setColor(Color.rgb(238,196,60));c.drawLine(x, y, px, py,p);p.setStyle(Paint.Style.FILL);}}

    private void quarkLens(Canvas c,float x,float y,float r){int ac=Color.rgb(131,207,88);lensBase(c,x,y,r,"QUARK","<10⁻¹⁸ m",ac);String[] qs={"u","u","d"};int[] cols={Color.rgb(211,69,78),Color.rgb(70,132,210),Color.rgb(95,181,83)};for(int i=0;i<3;i++){double a=-Math.PI/2+i*2*Math.PI/3;float px=x+(float)Math.cos(a)*r*.38f,py=y+(float)Math.sin(a)*r*.38f;p.setShader(new RadialGradient(px-4,py-4,r*.25f,Visuals.lighten(cols[i],.45f),cols[i],Shader.TileMode.CLAMP));c.drawCircle(px,py,r*.2f,p);p.setShader(null);p.setColor(Color.WHITE);p.setTextSize(22);p.setTextAlign(Paint.Align.CENTER);c.drawText(qs[i],px,py+7,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(3);p.setColor(Color.rgb(235,190,50));Path wave=new Path();wave.moveTo(x,y);for(int s=1;s<=12;s++){float t=s/12f;wave.lineTo(x+(px-x)*t+(float)Math.sin(t*12*Math.PI)*4,y+(py-y)*t);}c.drawPath(wave,p);p.setStyle(Paint.Style.FILL);}caption(c,x,y+r+28,"Fundamental particle","Gluons bind quarks together");}

    private void electronLens(Canvas c,float x,float y,float r){int ac=Color.rgb(100,170,235);lensBase(c,x,y,r,"ELECTRON","<10⁻¹⁸ m",ac);p.setShader(new RadialGradient(x,y,r*.75f,new int[]{Color.argb(220,140,211,255),Color.argb(85,40,110,235),Color.argb(10,20,70,160),Color.TRANSPARENT},null,Shader.TileMode.CLAMP));c.drawCircle(x,y,r*.82f,p);p.setShader(null);for(int i=0;i<42;i++){double a=i*2.399+phase*.01;float rr=(float)Math.sqrt(i/42f)*r*.68f;p.setColor(Color.argb(35+(i%4)*25,100,180,255));c.drawCircle(x+(float)Math.cos(a)*rr,y+(float)Math.sin(a)*rr,2+(i%3),p);}p.setColor(Color.WHITE);c.drawCircle(x,y,10,p);p.setColor(Color.rgb(110,184,245));p.setTextSize(28);p.setTextAlign(Paint.Align.CENTER);c.drawText("−",x,y+9,p);caption(c,x,y+r+28,"Negative charge: −1","Occupies a probability cloud");}

    private void caption(Canvas c,float x,float y,String a,String b){p.setTextAlign(Paint.Align.CENTER);p.setColor(Color.WHITE);p.setTextSize(14);c.drawText(a,x,y,p);p.setColor(Color.LTGRAY);p.setTextSize(13);c.drawText(b,x,y+20,p);}

    private void profilePanel(Canvas c){panel(c,485,510,1115,705);p.setColor(Color.rgb(226,195,96));p.setTextSize(23);p.setTextAlign(Paint.Align.LEFT);c.drawText("ATOMIC PROFILE",515,548,p);line(c,"Atomic number",String.valueOf(e.number),515,582);line(c,"Atomic mass",e.mass+" u",515,615);line(c,"Protons / electrons",e.number+" / "+e.number,515,648);line(c,"Estimated neutrons",String.valueOf(e.neutrons()),515,681);line(c,"Classification",e.category,810,582);line(c,"Period / group",e.period+" / "+e.group,810,615);line(c,"Electron shells",ScienceInfo.electronConfigurationSummary(e),810,648);line(c,"Room-temperature state",ScienceInfo.state(e),810,681);}

    private void scalePanel(Canvas c){panel(c,1190,620,1535,850);p.setColor(Color.rgb(226,195,96));p.setTextSize(21);p.setTextAlign(Paint.Align.CENTER);c.drawText("THE SCALE",1362,657,p);p.setTextAlign(Paint.Align.LEFT);scaleLine(c,"Atom","~10⁻¹⁰ m",1220,700,1f);scaleLine(c,"Nucleus","~10⁻¹⁵ m",1220,738,.72f);scaleLine(c,"Proton / Neutron","~10⁻¹⁵ m",1220,776,.52f);scaleLine(c,"Quark","<10⁻¹⁸ m",1220,814,.28f);}

    private void factPanel(Canvas c){panel(c,485,725,1148,850);p.setColor(Color.rgb(226,195,96));p.setTextSize(20);p.setTextAlign(Paint.Align.LEFT);c.drawText("WHY THIS ELEMENT MATTERS",515,760,p);p.setColor(Color.rgb(231,235,241));p.setTextSize(15);drawWrapped(c,ScienceInfo.fact(e),515,790,600,23);p.setColor(Color.rgb(160,175,195));p.setTextSize(13);c.drawText(ScienceInfo.origin(e)+"  •  "+ScienceInfo.state(e)+" at room temperature",515,838,p);}

    private void scaleLine(Canvas c,String a,String b,float x,float y,float ratio){p.setColor(Color.WHITE);p.setTextSize(15);c.drawText(a,x,y,p);p.setColor(Color.rgb(200,205,216));c.drawText(b,x+190,y,p);p.setColor(Color.rgb(218,188,91));c.drawRoundRect(x+102,y-9,x+102+70*ratio,y-5,3,3,p);}
    private void line(Canvas c,String a,String b,float x,float y){p.setColor(Color.LTGRAY);p.setTextSize(14);c.drawText(a,x,y,p);p.setColor(Color.WHITE);p.setTextSize(17);c.drawText(b,x+165,y,p);}
    private void panel(Canvas c,float l,float t,float r,float b){p.setColor(Color.argb(205,4,10,22));p.setStyle(Paint.Style.FILL);c.drawRoundRect(l,t,r,b,20,20,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2);p.setColor(Color.rgb(145,111,47));c.drawRoundRect(l,t,r,b,20,20,p);p.setStyle(Paint.Style.FILL);}
    private void drawWrapped(Canvas c,String text,float x,float y,float width,float leading){String[] words=text.split(" ");StringBuilder line=new StringBuilder();for(String word:words){String test=line.length()==0?word:line+" "+word;if(p.measureText(test)>width){c.drawText(line.toString(),x,y,p);y+=leading;line=new StringBuilder(word);}else line=new StringBuilder(test);}if(line.length()>0)c.drawText(line.toString(),x,y,p);}
}
