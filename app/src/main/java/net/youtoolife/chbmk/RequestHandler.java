package net.youtoolife.chbmk;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Base64;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

/**
 * Created by youtoolife on 4/29/18.
 */

public class RequestHandler {

    private Context context = null;

    private String url = null;
    private String result = "{\"id\":\"-1\",\"msg\":\"[]\"}";
    private Map<String, String> postData = null;


    public RequestHandler(String url, Map<String, String> map, Context context) {
        this.context = context;
        this.url = url;
        this.postData = map;
    }

    public void setSettings(String url, Map<String, String> map) {
        this.url = url;
        this.postData = map;
    }

    public void setResult(String result1) {
        result = result1;
    }

    public void request(final CallBack callBack) {

        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("RequestHandler: response:", response);
                        setResult(response);
                        callBack.callBackFunc(result);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String result1 = null;
                        if (error != null)
                            error.printStackTrace();
                        if (error != null && error.getMessage() != null) {
                            //Log.d("RequestHandler: Error:", error.getMessage())
                            try {
                                result1 = "{\"id\":\"-2\",\"msg\":\""
                                        + Base64.encodeToString(error.getMessage().getBytes("UTF-8"), Base64.DEFAULT) + "\"}";
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                        }
                        setResult(result1);
                        callBack.callBackFunc(result);
                    }
                }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return  postData;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(context);
        //RequestQueue requestQueue = Volley.newRequestQueue(context, new HurlStack(null, getSocketFactory()));
        requestQueue.add(stringRequest);

        // Usually getting the request queue shall be in singleton like in {@see Act_SimpleRequest}
        // Current approach is used just for brevity

        //return result;
    }


    private SSLSocketFactory getSocketFactory() {

        CertificateFactory cf = null;
        try {

            cf = CertificateFactory.getInstance("X.509");
            InputStream caInput = context.getResources().openRawResource(R.raw.localhost);
            Certificate ca;
            try {

                ca = cf.generateCertificate(caInput);
                Log.e("CERT", "ca=" + ((X509Certificate) ca).getSubjectDN());
            } finally {
                caInput.close();
            }


            String keyStoreType = KeyStore.getDefaultType();
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null, "ytlAItoor".toCharArray());
            keyStore.setCertificateEntry("ca", ca);


            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(keyStore);


            HostnameVerifier hostnameVerifier = new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {

                    Log.e("CipherUsed", session.getCipherSuite());
                    return hostname.compareTo("https://chbmk.000webhostapp.com/")==0; //The Hostname of your server.

                }
            };


            HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);
            SSLContext context = null;
            context = SSLContext.getInstance("TLS");

            context.init(null, tmf.getTrustManagers(), null);
            HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());

            SSLSocketFactory sf = context.getSocketFactory();


            return sf;

        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }

        return  null;
    }

}
