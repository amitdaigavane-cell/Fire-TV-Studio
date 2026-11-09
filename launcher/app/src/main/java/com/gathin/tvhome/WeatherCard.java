package com.gathin.tvhome;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Glass weather card: icon, temperature, condition with today's high and low, and the next three days.
 * It binds through WeatherInfo like the old one-line label: label() is the TextView that WeatherInfo
 * updates, and the card redraws from WeatherInfo.card() whenever that label changes or is hidden.
 */
final class WeatherCard extends LinearLayout {
    /** The TextView WeatherInfo binds to. Never shown itself; it drives the card's text and visibility. */
    static final class Label extends TextView {
        private WeatherCard card;
        Label(Context c){super(c);}
        @Override public void setVisibility(int visibility){super.setVisibility(visibility);if(card!=null)card.setVisibility(visibility);}
        void refresh(){if(card!=null)card.refresh();}
    }

    private final Label label;
    private final boolean large;
    private final WeatherIcon nowIcon=new WeatherIcon();
    private final ImageView icon;
    private final TextView temp, sub;
    private final View divider;
    private final LinearLayout days;

    WeatherCard(Context context,boolean large){
        super(context);this.large=large;
        setOrientation(HORIZONTAL);setGravity(Gravity.CENTER_VERTICAL);
        setBackground(new GlassDrawable(dp(16),getResources().getDisplayMetrics().density));
        setPadding(dp(12),dp(large?10:6),dp(14),dp(large?10:6));
        label=new Label(context);label.card=this;
        icon=new ImageView(context);icon.setImageDrawable(nowIcon);
        int iconSize=dp(large?34:26);addView(icon,new LayoutParams(iconSize,iconSize));
        temp=text(large?30:22,0xffffffff);temp.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL));
        LayoutParams tp=new LayoutParams(-2,-2);tp.leftMargin=dp(8);addView(temp,tp);
        sub=text(large?13:11,0xd9ffffff);sub.setLineSpacing(0,1.1f);sub.setMaxLines(2);
        LayoutParams sp=new LayoutParams(-2,-2);sp.leftMargin=dp(8);addView(sub,sp);
        divider=new View(context);divider.setBackgroundColor(0x4dffffff);
        LayoutParams dv=new LayoutParams(Math.max(1,dp(1)),dp(large?40:30));dv.leftMargin=dp(12);dv.rightMargin=dp(12);addView(divider,dv);
        days=new LinearLayout(context);days.setOrientation(HORIZONTAL);addView(days,new LayoutParams(-2,-2));
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);setFocusable(false);
        refresh();
    }

    /** Bind this to WeatherInfo exactly as the old one-line label was bound. */
    TextView label(){return label;}

    void refresh(){
        JSONObject d=WeatherInfo.card();
        setContentDescription(label.getContentDescription());
        if(d==null){
            icon.setVisibility(GONE);temp.setVisibility(GONE);divider.setVisibility(GONE);days.setVisibility(GONE);
            sub.setText(label.getText());return;
        }
        icon.setVisibility(VISIBLE);temp.setVisibility(VISIBLE);
        nowIcon.scene(d.optString("scene","clear"));
        temp.setText(d.optInt("temp")+"°");
        String hiLo=d.has("max")?"H "+d.optInt("max")+"° · L "+d.optInt("min")+"°":"";
        String stale=String.valueOf(label.getText()).endsWith("Last known")?" · Last known":"";
        sub.setText(d.optString("name")+" · "+d.optString("condition")+(hiLo.isEmpty()?"":"\n"+hiLo)+stale);
        days.removeAllViews();
        JSONArray list=d.optJSONArray("days");
        boolean any=list!=null&&list.length()>0;
        divider.setVisibility(any?VISIBLE:GONE);days.setVisibility(any?VISIBLE:GONE);
        if(!any)return;
        SimpleDateFormat dow=new SimpleDateFormat("EEE",Locale.getDefault());
        for(int i=0;i<Math.min(3,list.length());i++){
            JSONObject day=list.optJSONObject(i);if(day==null)continue;
            LinearLayout col=new LinearLayout(getContext());col.setOrientation(VERTICAL);col.setGravity(Gravity.CENTER_HORIZONTAL);
            TextView name=text(large?12:10,0xffffffff);name.setText(dow.format(new Date(day.optLong("time")*1000L)));name.setGravity(Gravity.CENTER);
            col.addView(name,new LayoutParams(-2,-2));
            WeatherIcon small=new WeatherIcon();small.scene(day.optString("scene","clear"));ImageView img=new ImageView(getContext());img.setImageDrawable(small);
            int s=dp(large?20:16);LayoutParams ip=new LayoutParams(s,s);ip.topMargin=dp(2);ip.bottomMargin=dp(2);col.addView(img,ip);
            TextView range=text(large?12:10,0xe0ffffff);range.setText(day.optInt("max")+"° / "+day.optInt("min")+"°");range.setGravity(Gravity.CENTER);
            col.addView(range,new LayoutParams(-2,-2));
            LayoutParams cp=new LayoutParams(-2,-2);if(i>0)cp.leftMargin=dp(large?16:12);days.addView(col,cp);
        }
    }

    private TextView text(float size,int color){
        TextView t=new TextView(getContext());t.setTextSize(size);t.setTextColor(color);t.setIncludeFontPadding(false);
        t.setTypeface(Typeface.create("sans-serif",Typeface.NORMAL));t.setShadowLayer(dp(2),0,dp(1),0x8c000000);t.setSingleLine(false);return t;
    }
    private int dp(float v){return Math.round(v*getResources().getDisplayMetrics().density);}
}
