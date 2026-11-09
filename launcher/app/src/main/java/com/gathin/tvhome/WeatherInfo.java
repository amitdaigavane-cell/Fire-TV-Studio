package com.gathin.tvhome;
import android.content.*;
import android.os.*;
import android.view.View;
import android.widget.TextView;
import org.json.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/** Foreground-only cached weather for an explicitly selected city. */
public final class WeatherInfo {
    public static final String ATTRIBUTION="Weather: Open-Meteo (CC BY 4.0)";
    private static final Handler MAIN=new Handler(Looper.getMainLooper());
    private static final ArrayList<WeakReference<TextView>> VIEWS=new ArrayList<>();
    private static final AtomicBoolean RUNNING=new AtomicBoolean();
    private static long lastAttempt;
    private static int generation;
    private static String label="Weather loading…";
    private static String configKey="";
    private static long fetched;
    private static JSONObject card;
    /** Structured weather for the card and idle effects, or null until the first successful fetch. */
    static JSONObject card(){return card;}
    static void invalidate(){MAIN.post(()->{generation++;lastAttempt=0;fetched=0;configKey="";label="Weather loading…";card=null;publish();});}
    public static void unbind(TextView view){VIEWS.removeIf(ref->ref.get()==null||ref.get()==view);}
    public static void bind(TextView view){
        unbind(view);JSONObject weather=StudioConfig.read(view.getContext()).optJSONObject("weather");
        if(weather.optString("mode","off").equals("off")||!StudioConfig.flag(view.getContext(),"showWeather")){view.setVisibility(View.GONE);return;}view.setVisibility(View.VISIBLE);VIEWS.add(new WeakReference<>(view));
        String key=weather.toString();if(!key.equals(configKey)){configKey=key;generation++;lastAttempt=0;fetched=0;label="Weather loading…";card=null;}
        render(view);long now=System.currentTimeMillis();if((now>=fetched&&now-fetched<3600000)||(now>=lastAttempt&&now-lastAttempt<120000)||!RUNNING.compareAndSet(false,true))return;
        lastAttempt=now;int requestGeneration=generation;Context app=view.getContext().getApplicationContext();
        Thread worker=new Thread(()->{
            try {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);String next;long time=0;JSONObject nextCard=null;
            SharedPreferences cache=app.getSharedPreferences("studio_weather",Context.MODE_PRIVATE);
            try{
                if(key.equals(cache.getString("key",""))&&now>=cache.getLong("fetched",0)&&now-cache.getLong("fetched",0)<3600000){next=cache.getString("label","Weather unavailable");time=cache.getLong("fetched",0);nextCard=cached(cache);}
                else{
                    JSONObject place=weather;
                    JSONObject response=com.veyra.shared.WeatherApi.full(place,com.veyra.shared.WeatherApi::json);JSONObject data=response.getJSONObject("current");double temperature=data.getDouble("temperature_2m");cache.edit().remove("error").apply();
                    boolean fahrenheit="fahrenheit".equals(place.optString("unit","celsius"));if(fahrenheit)temperature=temperature*9/5+32;
                    next=place.optString("name","Nearby")+" · "+Math.round(temperature)+(fahrenheit?"°F":"°C")+" · "+condition(data.getInt("weather_code"));time=now;nextCard=buildCard(place,response,fahrenheit);cache.edit().putString("key",key).putString("label",next).putString("card",nextCard==null?"":nextCard.toString()).putLong("fetched",now).apply();
                }
            }catch(Exception e){cache.edit().putString("error",e.getMessage()==null?"Weather request failed":e.getMessage()).apply();long cached=cache.getLong("fetched",0);boolean known=key.equals(cache.getString("key",""))&&now>=cached&&now-cached<86400000;next=known?cache.getString("label","")+" · Last known":"Weather unavailable";if(known)nextCard=cached(cache);}
            String result=next;long resultTime=time;JSONObject resultCard=nextCard;MAIN.post(()->{if(requestGeneration!=generation){for(WeakReference<TextView> ref:new ArrayList<>(VIEWS)){TextView v=ref.get();if(v!=null)bind(v);}return;}card=resultCard;label=result;fetched=resultTime;publish();});
            } finally { RUNNING.set(false); }
        },"home-weather");
        try{worker.start();}catch(RuntimeException|Error e){RUNNING.set(false);throw e;}
    }
    static JSONObject status(Context c)throws JSONException{SharedPreferences cache=c.getSharedPreferences("studio_weather",Context.MODE_PRIVATE);return new JSONObject().put("label",cache.getString("label","")).put("fetched",cache.getLong("fetched",0)).put("error",cache.getString("error","")).put("running",RUNNING.get());}
    static void retry(){MAIN.post(()->{generation++;lastAttempt=0;fetched=0;for(WeakReference<TextView> ref:new ArrayList<>(VIEWS)){TextView v=ref.get();if(v!=null){v.getContext().getSharedPreferences("studio_weather",Context.MODE_PRIVATE).edit().remove("fetched").apply();bind(v);}}});}
    private static void render(TextView v){if(!android.text.TextUtils.equals(v.getText(),label))v.setText(label);v.setContentDescription(label+". "+ATTRIBUTION);if(v instanceof WeatherCard.Label)((WeatherCard.Label)v).refresh();}
    private static void publish(){for(Iterator<WeakReference<TextView>> it=VIEWS.iterator();it.hasNext();){TextView v=it.next().get();if(v==null)it.remove();else render(v);}}
    private static JSONObject cached(SharedPreferences cache){try{String s=cache.getString("card","");return s.isEmpty()?null:new JSONObject(s);}catch(JSONException e){return null;}}
    private static JSONObject buildCard(JSONObject place,JSONObject response,boolean fahrenheit){
        try{
            JSONObject now=response.getJSONObject("current");int code=now.getInt("weather_code");double wind=now.optDouble("wind_speed_10m",0);
            JSONObject card=new JSONObject().put("name",place.optString("name","Nearby")).put("unit",fahrenheit?"F":"C")
                .put("temp",convert(now.getDouble("temperature_2m"),fahrenheit)).put("code",code).put("wind",Math.round(wind))
                .put("condition",condition(code)).put("scene",scene(code,wind));
            JSONObject daily=response.optJSONObject("daily");JSONArray days=new JSONArray();
            if(daily!=null){
                JSONArray t=daily.getJSONArray("time"),c=daily.getJSONArray("weather_code"),hi=daily.getJSONArray("temperature_2m_max"),lo=daily.getJSONArray("temperature_2m_min");
                for(int i=0;i<Math.min(4,t.length());i++){JSONObject d=new JSONObject().put("time",t.getLong(i)).put("code",c.getInt(i)).put("scene",scene(c.getInt(i),0)).put("max",convert(hi.getDouble(i),fahrenheit)).put("min",convert(lo.getDouble(i),fahrenheit));if(i==0)card.put("max",d.getInt("max")).put("min",d.getInt("min"));else days.put(d);}
            }
            return card.put("days",days);
        }catch(JSONException e){return null;}
    }
    private static int convert(double celsius,boolean fahrenheit){return (int)Math.round(fahrenheit?celsius*9/5+32:celsius);}
    /** Visual family for icons and idle effects. Blizzard is snow with wind of 40 km/h or more. */
    static String scene(int code,double windKmh){
        if(code>=95)return "storm";
        if((code>=71&&code<=77)||code==85||code==86)return windKmh>=40?"blizzard":"snow";
        if(code==45||code==48)return "fog";
        if((code>=51&&code<=67)||(code>=80&&code<=82))return "rain";
        if(code==3)return "cloud";
        if(code==2)return "partly";
        return "clear";
    }
    private static String condition(int code) {
        switch (code) {
            case 0: return "Clear";
            case 1: return "Mostly clear";
            case 2: return "Partly cloudy";
            case 3: return "Cloudy";
            case 45: case 48: return "Fog";
            case 51: case 53: case 55: return "Drizzle";
            case 56: case 57: return "Freezing drizzle";
            case 61: case 63: return "Rain";
            case 65: return "Heavy rain";
            case 66: case 67: return "Freezing rain";
            case 71: case 73: case 75: case 77: return "Snow";
            case 80: case 81: return "Showers";
            case 82: return "Heavy showers";
            case 85: case 86: return "Snow showers";
            case 95: case 96: case 97: case 99: return "Thunderstorms";
            default: return "Conditions unavailable";
        }
    }

}
