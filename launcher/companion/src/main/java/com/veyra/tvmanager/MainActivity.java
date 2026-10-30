package com.veyra.tvmanager;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.net.Uri;
import android.os.*;
import android.webkit.*;
import org.json.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;

public final class MainActivity extends Activity {
    private WebView web;
    private TvController tv;
    private final ExecutorService worker=Executors.newSingleThreadExecutor();
    private String pickerRequest, exportRequest, exportData, importRequest;
    private volatile boolean destroyed;
    private static final String ORIGIN="https://appassets.androidplatform.net/";
    @Override public void onCreate(Bundle saved){
        super.onCreate(saved);web=new WebView(this);web.setBackgroundColor(0xff0b141a);setContentView(web);
        WebSettings settings=web.getSettings();settings.setJavaScriptEnabled(true);settings.setDomStorageEnabled(false);settings.setAllowFileAccess(false);settings.setAllowContentAccess(false);settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);settings.setSupportMultipleWindows(false);
        web.setWebViewClient(new WebViewClient(){
            @Override public boolean shouldOverrideUrlLoading(WebView view,WebResourceRequest r){return true;}
            @Override public WebResourceResponse shouldInterceptRequest(WebView view,WebResourceRequest request){
                String url=request.getUrl().toString();String name=url.startsWith(ORIGIN)?url.substring(ORIGIN.length()):"";
                try{if(!Arrays.asList("index.html","style.css","app.js").contains(name))return new WebResourceResponse("text/plain","UTF-8",403,"Blocked",Collections.emptyMap(),new ByteArrayInputStream(new byte[0]));
                    return new WebResourceResponse(name.endsWith("html")?"text/html":name.endsWith("css")?"text/css":"application/javascript","UTF-8",getAssets().open(name));
                }catch(IOException e){return new WebResourceResponse("text/plain","UTF-8",new ByteArrayInputStream(new byte[0]));}
            }
        });
        tv=new TvController(this,message->event("progress",message));web.addJavascriptInterface(new Bridge(),"Studio");web.loadUrl(ORIGIN+"index.html");
    }
    private void event(String type,String text){try{JSONObject value=new JSONObject().put("type",type).put("message",text);runOnUiThread(()->{if(!destroyed)web.evaluateJavascript("window.studioEvent("+value+")",null);});}catch(JSONException ignored){}}
    private void reply(String id,Object result,String error){try{JSONObject value=new JSONObject().put("id",id).put("ok",error==null).put("data",result==null?JSONObject.NULL:result).put("error",error==null?JSONObject.NULL:error);runOnUiThread(()->{if(!destroyed)web.evaluateJavascript("window.studioReply("+value+")",null);});}catch(JSONException ignored){}}
    final class Bridge {
        @JavascriptInterface public void request(String id,String action,String raw){
            if(destroyed||id==null||!id.matches("[0-9]{1,12}")||raw==null||raw.length()>100000)return;
            worker.execute(()->{try{JSONObject arg=new JSONObject(raw);Object response;
                switch(action){
                    case "init":response=new JSONObject().put("host",getSharedPreferences("studio",0).getString("lastHost","")).put("port",getSharedPreferences("studio",0).getInt("lastPort",5555));break;
                    case "connect":response=tv.connect(arg.getString("host"),arg.optInt("port",5555));break;
                    case "disconnect":tv.close();response=new JSONObject();break;
                    case "status":response=tv.status();break;
                    case "install":response=tv.install();break;
                    case "home":response=tv.home(arg.getString("choice"));break;
                    case "cleanup":response=tv.cleanup(arg.getJSONArray("packages"));break;
                    case "package":response=tv.packageAction(arg.getString("package"),arg.getString("action"));break;
                    case "updates":response=tv.updates(arg.getBoolean("restrict"));break;
                    case "repairHome":response=tv.repairHome();break;
                    case "weatherRetry":response=tv.weatherRetry();break;
                    case "config":response=tv.config(arg.getJSONObject("config"));break;
                    case "apps":response=tv.apps(arg);break;
                    case "restore":response=tv.restore();break;
                    case "pickImages":runOnUiThread(()->pick(id));return;
                    case "upload":String image=arg.getString("image");if(!image.matches("[a-zA-Z0-9_-]{1,48}"))throw new IOException("Invalid image");response=tv.wallpaper(new File(getCacheDir(),image+".jpg"),image);break;
                    case "citySearch":response=citySearch(arg.getString("query"));break;
                    case "export":String data=tv.export();runOnUiThread(()->export(id,data));return;
                    case "importRestore":runOnUiThread(()->{if(importRequest!=null){reply(id,null,"A picker is already open");return;}importRequest=id;try{startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT).setType("application/json").addCategory(Intent.CATEGORY_OPENABLE),12);}catch(RuntimeException e){importRequest=null;reply(id,null,"No document picker available");}});return;
                    case "licenses":response=new JSONObject().put("text",TvController.read(getAssets().open("THIRD-PARTY-NOTICES.txt"),30000));break;
                    default:throw new IllegalArgumentException("Unknown action");
                }
                reply(id,response,null);
            }catch(Exception e){if(!tv.connected())event("disconnected","Connection closed");reply(id,null,e.getMessage()==null?e.getClass().getSimpleName():e.getMessage());}});
        }
    }
    private JSONObject citySearch(String query)throws Exception{if(query.length()<2||query.length()>80)throw new IllegalArgumentException("Enter a city name");HttpURLConnection connection=(HttpURLConnection)new URL("https://geocoding-api.open-meteo.com/v1/search?count=6&language=en&format=json&name="+URLEncoder.encode(query,"UTF-8")).openConnection();connection.setConnectTimeout(6000);connection.setReadTimeout(8000);try{if(connection.getResponseCode()!=200)throw new IOException("City search unavailable");try(InputStream in=connection.getInputStream()){return new JSONObject(TvController.read(in,65536));}}finally{connection.disconnect();}}
    private void pick(String id){if(pickerRequest!=null){reply(id,null,"A photo picker is already open");return;}pickerRequest=id;try{startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT).setType("image/*").putExtra(Intent.EXTRA_MIME_TYPES,new String[]{"image/jpeg","image/png","image/webp"}).addCategory(Intent.CATEGORY_OPENABLE).putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true),10);}catch(RuntimeException e){pickerRequest=null;reply(id,null,"No photo picker available");}}
    private void export(String id,String data){if(exportRequest!=null){reply(id,null,"An export is already open");return;}exportRequest=id;exportData=data;try{startActivityForResult(new Intent(Intent.ACTION_CREATE_DOCUMENT).setType("application/json").addCategory(Intent.CATEGORY_OPENABLE).putExtra(Intent.EXTRA_TITLE,"TV-Home-restore-point.json"),11);}catch(RuntimeException e){exportRequest=null;exportData=null;reply(id,null,"No document picker available");}}
    @Override protected void onActivityResult(int request,int result,Intent data){super.onActivityResult(request,result,data);
        if(request==10){String id=pickerRequest;pickerRequest=null;if(result!=RESULT_OK||data==null){reply(id,new JSONArray(),null);return;}ArrayList<Uri> uris=new ArrayList<>();if(data.getClipData()!=null)for(int i=0;i<Math.min(8,data.getClipData().getItemCount());i++)uris.add(data.getClipData().getItemAt(i).getUri());else if(data.getData()!=null)uris.add(data.getData());worker.execute(()->{try{JSONArray images=new JSONArray();for(Uri uri:uris){Bitmap bitmap=ImageDecoder.decodeBitmap(ImageDecoder.createSource(getContentResolver(),uri),(decoder,info,source)->{float scale=Math.min(1f,Math.min(1920f/info.getSize().getWidth(),1080f/info.getSize().getHeight()));decoder.setTargetSize(Math.max(1,Math.round(info.getSize().getWidth()*scale)),Math.max(1,Math.round(info.getSize().getHeight()*scale)));decoder.setAllocator(ImageDecoder.ALLOCATOR_SOFTWARE);});String name="image_"+UUID.randomUUID().toString().replace("-","");File file=new File(getCacheDir(),name+".jpg");try(OutputStream out=new FileOutputStream(file)){bitmap.compress(Bitmap.CompressFormat.JPEG,88,out);}float thumbScale=Math.min(240f/bitmap.getWidth(),135f/bitmap.getHeight());Bitmap thumb=Bitmap.createScaledBitmap(bitmap,Math.max(1,Math.round(bitmap.getWidth()*thumbScale)),Math.max(1,Math.round(bitmap.getHeight()*thumbScale)),true);ByteArrayOutputStream preview=new ByteArrayOutputStream();thumb.compress(Bitmap.CompressFormat.JPEG,65,preview);images.put(new JSONObject().put("id",name).put("width",bitmap.getWidth()).put("height",bitmap.getHeight()).put("preview","data:image/jpeg;base64,"+android.util.Base64.encodeToString(preview.toByteArray(),android.util.Base64.NO_WRAP)));if(thumb!=bitmap)thumb.recycle();bitmap.recycle();}reply(id,images,null);}catch(Exception|OutOfMemoryError e){reply(id,null,"Could not import this image. Choose a smaller JPEG or PNG.");}});}
        else if(request==12){String id=importRequest;importRequest=null;if(result!=RESULT_OK||data==null||data.getData()==null){reply(id,new JSONObject(),null);return;}Uri uri=data.getData();worker.execute(()->{try(InputStream in=getContentResolver().openInputStream(uri)){if(in==null)throw new IOException("Cannot read restore point");reply(id,tv.importRestore(TvController.read(in,2*1024*1024)),null);}catch(Exception e){reply(id,null,e.getMessage());}});}
        else if(request==11){String id=exportRequest,text=exportData;exportRequest=null;exportData=null;if(result!=RESULT_OK||data==null||data.getData()==null){reply(id,new JSONObject(),null);return;}Uri uri=data.getData();worker.execute(()->{try(OutputStream out=getContentResolver().openOutputStream(uri)){if(out==null)throw new IOException("Could not save file");out.write(text.getBytes(StandardCharsets.UTF_8));reply(id,new JSONObject().put("saved",true),null);}catch(Exception e){reply(id,null,e.getMessage());}});}
    }
    @Override public void onBackPressed(){web.evaluateJavascript("window.studioBack()",null);}
    @Override public void onDestroy(){destroyed=true;tv.close();worker.shutdownNow();web.removeJavascriptInterface("Studio");web.destroy();super.onDestroy();}
}
