package uvtechsoft.shree.loggerlibrary;

import android.content.Context;
import android.provider.Settings;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;


public class WebService {
    Context context;
    // String baseUrl="http://shikuyaa.tecpool.in/service.asmx/";
    String baseUrl ="http://192.168.0.5/Logger/service.asmx/";
    RequestQueue requestQueue;
    String params[][] = new String[][]{};
    String deviceId = "";


    public WebService(Context context) {
        this.context = context;
        requestQueue = Volley.newRequestQueue(context);
        deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        //deviceId= ""+123;
    }

    public void getString(String methodName, final VolleyCallback callback) {
        String p = "&";
        for (int i = 0; i < params.length; i++) {
            p = p + params[i][0] + "=";
            p = p + (params[i][1].replace(" ", "%20").replace("\n", "%0A").replace("'", "%27%27").replace("=", "equel%20to").replace("&", "and")) + "&";
        }

        baseUrl = baseUrl + methodName + "?flag=" + deviceId + "" + (params.length <= 0 ? "" : p.substring(0, p.length() - 1));

        StringRequest stringRequest = new StringRequest(Request.Method.GET, baseUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        callback.onSuccess(response.substring(82, response.length() - 9));
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                callback.onSuccess(error.toString());
            }
        });

        requestQueue.add(stringRequest);
    }


}
