package uvtechsoft.shree.loggerlibrary;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.IBinder;
import android.provider.Settings;
import android.text.TextUtils;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

public class LoggerService extends Service {

    private static SharedPreferences pref;
    private static SharedPreferences.Editor editor;
    private static final String PREF_NAME = "Logger";
    private static Context context;
    private static int PRIVATE_MODE = 0;
    private static String deviceID;
    private static String wsResult;
    private static Timer mTimer;

    public LoggerService() {
    }

    public LoggerService(Context mContext) {
        context = mContext;
        pref = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
        deviceID = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        mTimer = new Timer();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");
    return null;
    }


    @Override
    public void onCreate() {
        super.onCreate();
    }


    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        new Logger(context).uploadLogs();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    public static boolean isInitialized() {
        String visitorID = pref.getString("VisitiorID","" );
        if (TextUtils.equals(visitorID, ""))
            return false;
        else
            return true;
    }

    public static boolean init(String appkey) {


        WebService ws = new WebService(context);
        String param[][] = {{"key",appkey},{"deviceId",deviceID}};
        ws.params = param;

        ws.getString("initService", new VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                if(result.startsWith("OK"))
                {
                    editor.putString("VisitorId", result.substring(result.indexOf("-")+1,result.lastIndexOf("-")));
                    editor.putString("SupportKey", result.substring(result.lastIndexOf("-")+1));
                    editor.commit();
                }
                else
                {
                    Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show();
                }
            }
        });


        return true;
    }



    public static boolean reInit(String appkey, String userKey) {

        WebService ws = new WebService(context);
        String param[][] = {{"key",appkey},{"deviceId",deviceID},{"userKey",userKey} };
        ws.params = param;

        ws.getString("reInitService", new VolleyCallback() {
            @Override
            public void onSuccess(String result) {

                if(result.startsWith("OK"))
                {
                    editor.putString("VisitorId", result.substring(result.indexOf("-")+1,result.lastIndexOf("-")));
                    editor.putString("SupportKey", result.substring(result.lastIndexOf("-")+1));
                    editor.commit();
                }
                else
                {
                    Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show();
                }
            }
        });
        //uploadLogs(pref.getString("LogEntries",""));
        return true;
    }



    public static void setInterval(long interval) {


        editor.putString("INTERVAL", String.valueOf(interval));
        editor.commit();

    }



    public static boolean log(String type, String title, String description) {

        String vId = String.valueOf(Logger.getVisitorId());

        //JSONArray jLogList;
        // String logEntry[][]={{"VisitorId",vId},{"type",type},{"title",title},{"description",description}};
        JSONObject jo = new JSONObject();
        try {
            jo.put("visitorId", vId);
            jo.put("type", type);
            jo.put("title", title);
            jo.put("description", description);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String oldLogs = pref.getString("LogEntries", "");
        if (oldLogs.equals("")) {

            try {
                JSONArray j = new JSONArray("["+jo.toString()+"]");

                editor.putString("LogEntries", j.toString());
                editor.commit();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            try {
                JSONArray j = new JSONArray(oldLogs);
                j.put(jo);

                editor.putString("LogEntries", j.toString());
                editor.commit();

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        //uploadLogs(pref.getString("LogEntries",""));
        return true;
    }



    public Boolean uploadLogs( ){

        int interval = Integer.parseInt(pref.getString("INTERVAL",""));
        mTimer.schedule(timerTask,interval,24*60*60*1000);

        return true;

    }
    TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
          new AsyncLoggerUploads().execute();
        }
    };




    public static String getVisitorId() {
        String visitorID = pref.getString("VisitorId","" );
        return visitorID;

    }



    private static class AsyncLoggerUploads extends AsyncTask<String,String,String>
    {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {

            WebService ws = new WebService(context);

            String json = pref.getString("LogEntries","");
            String param[][] = {{"json",json}};
            ws.params = param;

            ws.getString("logBulkEntry", new VolleyCallback() {
                @Override
                public void onSuccess(String result) {

                    if(result.startsWith("OK"))
                    {
                        //editor.putString("",);
                        // editor.commit();
                        wsResult = "OK";
                    }
                    else
                    {
                        //return error
                        wsResult="false";
                    }
                }
            });
            return "OK";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if(s.startsWith("OK"))
                editor.remove("LogEntries");
            else
                Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show();
        }

    }

}
