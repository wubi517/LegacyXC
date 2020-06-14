package com.newlegacyxc.async;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import com.newlegacyxc.utils.JsonHelper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class GetArrayAsyncTask extends AsyncTask<String, Void, Map> {

    private OnGetArrayResultsListener listener;

    private Context context;
    private int request_code;

    public GetArrayAsyncTask(Context context, int request_code) {
        this.context = context;
        this.request_code = request_code;
    }

    public void onGetArrayResultsData(OnGetArrayResultsListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onPreExecute() {


    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected Map doInBackground(String... params) {
        StringBuffer buffer = new StringBuffer();
        String res = null;
        try {
            URL url = new URL(params[0]);
            Log.e("url", params[0]);
            HttpURLConnection httpUrlConn = (HttpURLConnection) url.openConnection();
            httpUrlConn.setRequestMethod("GET");
            httpUrlConn.connect();

            InputStream inputStream = httpUrlConn.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "utf-8");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String str = null;
            while ((str = bufferedReader.readLine()) != null) {
                buffer.append(str);
            }
            bufferedReader.close();
            inputStreamReader.close();
            inputStream.close();
            inputStream = null;
            httpUrlConn.disconnect();
            res = buffer.toString();
//            Log.e("result", res);
            String dec_str;
            if (res.contains("host") || res.contains("version") || res.contains("http"))
                dec_str = res;
            JSONArray array = new JSONArray(res);
            List maps = JsonHelper.toList(array);
            Map map = new HashMap();
            map.put("data", maps);
            return map;
        } catch (MalformedURLException e) {
            Map mp = new HashMap();
            mp.put("error", "error");
            return mp;
        } catch (ProtocolException e) {
            Map mp = new HashMap();
            mp.put("error", "error");
            return mp;
        } catch (IOException e) {
            Map mp = new HashMap();
            mp.put("error", "error");
            return mp;
        } catch (JSONException e) {
//            Map mp = JsonUtils.stringObject(res, Map.class);
            try{
                Map<String, Object> retMap = new Gson().fromJson(
                        res, new TypeToken<HashMap<String, Object>>() {}.getType()
                );
                return retMap;
            }catch (Exception e1){
                Map mp = new HashMap();
                mp.put("error", "error");
                return mp;
            }
        }
    }

    @Override
    protected void onPostExecute(Map response) {

        if (response != null && response.size() > 0) {
            listener.onGetArrayResultsData(response, request_code);
        } else {
//            Toast.makeText(context, "Data format error!", Toast.LENGTH_LONG).show();
            listener.onGetArrayResultsData(response, request_code);
        }
    }

    public interface OnGetArrayResultsListener {
        public void onGetArrayResultsData(Map map, int request_code);
    }
}
