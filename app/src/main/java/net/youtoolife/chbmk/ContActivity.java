package net.youtoolife.chbmk;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContActivity extends AppCompatActivity {


    private BroadcastReceiver broadcastReceiver;

    private RVAdapter adapter;
    private volatile List<Message> msgs;
    private RecyclerView rv;

    private boolean updateFlag = true;

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("ONResume", "resume");
        MyFirebaseMessagingService.N = 0;

        if (MyFirebaseMessagingService.notificationManager != null) {
            MyFirebaseMessagingService.notificationManager.cancelAll();
        }
    }

    @Override
    protected void onPause() {
        storeMsgs();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        storeMsgs();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cont);


        rv=(RecyclerView)findViewById(R.id.rv);
        LinearLayoutManager llm  = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        rv.setLayoutManager(llm);
        //llm.setSmoothScrollbarEnabled(true);
        rv.setHasFixedSize(true);


        msgs = new ArrayList<>();
        adapter = new RVAdapter(getApplicationContext(), msgs);
        rv.setAdapter(adapter);


        responseHandler(loadMsgFromCache());
        if (updateFlag)
            responseHandler(null);

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d("breceive:", "yes");
                responseHandler(null);
            }
        };
        registerReceiver(broadcastReceiver, new IntentFilter(MyFirebaseMessagingService.TOKEN_BROADCAST));


        /*
        Button logoutBtn = (Button) findViewById(R.id.logoutBtn);
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ContActivity.this);
                builder.setTitle("Вы действительно хотите выйти?")
                        .setMessage("Если выйти из системы, уведомления приходить не будут!")
                        //.setIcon(R.drawable.ic_launcher_background)
                        .setPositiveButton("Выйти", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                dialog.cancel();

                                Map<String, String> params0 = new HashMap<>();
                                params0.put("login", SharedPrefManager.getInstance(getApplicationContext()).getLogin());
                                params0.put("invite", SharedPrefManager.getInstance(getApplicationContext()).getInvite());
                                params0.put("pwd",XA.b(XA.B));

                                JSONObject jsonObject = new JSONObject(params0);
                                Map<String, String> params = new HashMap<>();
                                String json = jsonObject.toString();
                                System.out.print("json "+json);
                                params.put("d", RSAIsa.rsaEncrypt(jsonObject.toString()));

                                String URL_SERVER = XA.b(XA.AA);

                                RequestHandler requestHandler = new RequestHandler(URL_SERVER, params, getApplicationContext());
                                requestHandler.request(new CallBack() {
                                    @Override
                                    public void callBackFunc(String response) {
                                        responseLogOut(response);
                                    }
                                });
                            }
                        })
                        .setCancelable(false)
                        .setNegativeButton("Остаться в системе",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });
        */

        Button topBtn = (Button) findViewById(R.id.topBtn);
        topBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (rv.computeVerticalScrollOffset() < 20000)
                    rv.smoothScrollToPosition(0);
                else
                    rv.scrollToPosition(0);
            }
        });

        Button mapBtn = (Button) findViewById(R.id.gpsBtn);
        mapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String login = SharedPrefManager.getInstance(getApplicationContext()).getLogin();
                String invite = SharedPrefManager.getInstance(getApplicationContext()).getInvite();

                if (login == null || invite == null)
                    return;

                Map<String, String> params0 = new HashMap<>();
                params0.put("login", login);
                params0.put("invite", RSAIsa.rsaEncrypt(invite));
                params0.put("pwd",RSAIsa.rsaEncrypt(XA.b(XA.B)));

                JSONObject jsonObject = new JSONObject(params0);
                Map<String, String> params = new HashMap<>();
                String json = jsonObject.toString();
                System.out.print("json "+json);
                //params.put("d", RSAIsa.rsaEncrypt(jsonObject.toString()));
                try {
                    params.put("d", Base64.encodeToString(jsonObject.toString().getBytes("UTF-8"), Base64.DEFAULT));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                String URL_SERVER = "http://chbmk.000webhostapp.com/chbmk/get_location.php";//XA.b(XA.A);


                RequestHandler requestHandler = new RequestHandler(URL_SERVER, params, getApplicationContext());
                requestHandler.request(new CallBack() {
                    @Override
                    public void callBackFunc(String response) {
                        responseGps(response);
                    }
                });
            }
        });

    }


    private void responseGps(String response) {

        if (response == null || response.isEmpty())
            return;

        try {
            JSONObject obj = new JSONObject(new String(Base64.decode(response, Base64.DEFAULT), "UTF-8"));

            if (obj.getInt("id") != 1)
                return;

            final JSONArray arr = obj.getJSONArray("msg");

            final ArrayList<String> items = new ArrayList<>();
            for (int i = 0; i < arr.length(); i++)
                items.add(arr.getJSONObject(i).getString("login"));
            items.add("Отмена");

            String[] sarr = new String[items.size()];
            items.toArray(sarr);

            AlertDialog.Builder builder = new AlertDialog.Builder(ContActivity.this);
            builder.setTitle("Выберите отслеживаемый телефон")
                    .setItems(sarr, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if (which == items.size()-1) {
                                dialog.cancel();
                                return;
                            }
                            Intent mapActive = new Intent(ContActivity.this, MapsActivity.class);
                            try {
                                mapActive.putExtra("loginID", which);
                                mapActive.putExtra("lat", arr.getJSONObject(which).getDouble("lat"));
                                mapActive.putExtra("long", arr.getJSONObject(which).getDouble("long"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            startActivity(mapActive);
                        }
                    });
            builder.create();
            builder.show();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }


    private void readMsgs(JSONObject obj) {
        try {

            //msgs.clear();
        JSONArray arr = obj.getJSONArray("msg");
        for (int i = 0; i < arr.length(); i++) {
            JSONObject o = arr.optJSONObject(i);
            int index = o.getInt("index");
            String msg = o.getString("msg");
            String hash = o.getString("h");
            String date = o.getString("date");

            Message msg0 = new Message(index, "SMS-INFO", msg, date, hash);
            msgs.add(0, msg0);
            //Log.d("MSGS:", msg);

            rv.getAdapter().notifyItemInserted(0);
            //rv.getAdapter().notifyDataSetChanged();
        }

        /*
        Log.d("SCROLL_POSE", String.valueOf(rv.computeVerticalScrollExtent()));
        Log.d("SCROLL_POSO", String.valueOf(rv.computeVerticalScrollOffset()));
        Log.d("SCROLL_POSR", String.valueOf(rv.computeVerticalScrollRange()));
        */

        if (arr.length() < 30 && rv.computeVerticalScrollOffset() < 20000)
            rv.smoothScrollToPosition(0);
        else
            rv.scrollToPosition(0);

        if (msgs.size() > 0) {
            SharedPrefManager.getInstance(getApplicationContext()).storeLastID(msgs.get(0).index);
            Log.d("LAST_ID", String.valueOf(msgs.get(0).index));
        }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void responseLogOut(String response) {
        SharedPrefManager.getInstance(getApplicationContext()).storeLogin(null,null);
        //SharedPrefManager.getInstance(getApplicationContext()).storeToken(null);

        Intent content = new Intent(ContActivity.this, MainActivity.class);
        startActivity(content);
        Log.d("out: ", response);
    }

    public void update(String invite, String login, int index) {

        updateFlag = false;

        Map<String, String> params0 = new HashMap<>();
        /*
        params0.put("dev", SharedPrefManager.getInstance(getApplicationContext()).getToken());
        //params0.put("i", String.valueOf(index));
        params0.put("login", login);
        params0.put("invite", invite);
        params0.put("pwd",XA.b(XA.B));*/
        String dev = SharedPrefManager.getInstance(getApplicationContext()).getToken();
        params0.put("dev", dev!=null?RSAIsa.rsaEncrypt(dev):"");
        params0.put("login", login);
        params0.put("invite", RSAIsa.rsaEncrypt(invite));
        params0.put("pwd",RSAIsa.rsaEncrypt(XA.b(XA.B)));

        JSONObject jsonObject = new JSONObject(params0);
        Map<String, String> params = new HashMap<>();
        String json = jsonObject.toString();
        System.out.print("json "+json);
        //params.put("d", RSAIsa.rsaEncrypt(jsonObject.toString()));
        try {
            params.put("d", Base64.encodeToString(jsonObject.toString().getBytes("UTF-8"), Base64.DEFAULT));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        params.put("i", String.valueOf(index));

        String URL_SERVER = XA.b(XA.A);

        RequestHandler requestHandler = new RequestHandler(URL_SERVER, params, getApplicationContext());
        requestHandler.request(new CallBack() {
            @Override
            public void callBackFunc(String response) {
                responseHandler(response);
            }
        });
    }



    private void responseHandler(String response) {

        try {
            if (response == null) {
                if (SharedPrefManager.getInstance(getApplicationContext()).getInvite() != null
                        && SharedPrefManager.getInstance(getApplicationContext()).getLogin() != null) {
                    String invite = SharedPrefManager.getInstance(getApplicationContext()).getInvite();
                    String login = SharedPrefManager.getInstance(getApplicationContext()).getLogin();
                    update(invite, login, msgs.size() > 0?msgs.get(0).index:-1);
                }
                else {
                    Intent content = new Intent(ContActivity.this, MainActivity.class);
                    startActivity(content);
                }
            } else {
                Log.d("Cont: Answ", response);
                JSONObject obj = new JSONObject(response);

                int id = obj.getInt("id");
                if (id > -1) {
                    //SharedPrefManager.getInstance(getApplicationContext()).storeMsgs(response);
                    //SharedPrefManager.getInstance(getApplicationContext()).storeLogin(login, invite);
                    readMsgs(obj);
                } else {
                    if (id != -2) {
                        Intent content = new Intent(ContActivity.this, MainActivity.class);
                        startActivity(content);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public String loadMsgFromCache() {

        Log.d("LOAD_MSG","LOADing");

        String filename = "msgs" + ".jml";
        FileInputStream inputStream;

        //JSONObject result = null;
        String result = null;
        byte[] output;
        try {
            File file = new File(getApplicationContext().getFilesDir(), filename);
            if (file.exists()) {
                inputStream = new FileInputStream(file); //openFileOutput(filename, Context.MODE_PRIVATE);
                output = new byte[(int) file.length()];
                inputStream.read(output);
                inputStream.close();

                String str = new String(output, "UTF-8");
                result = str;//new JSONObject(str);

                //if (!str.isEmpty())
                //    Log.d("LOAD_MSG:::",str);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public void storeMsgs() {
        Collections.reverse(msgs);

        try {
            Log.d("SORE_MSG","STORE");
        JSONObject obj = new JSONObject();
        JSONArray arr = new JSONArray();

        for (Message msg:msgs) {
            JSONObject o = new JSONObject();

                o.put("index", msg.index);
                o.put("msg", msg.body);
                o.put("h", msg.hash);
                o.put("date", msg.date);
                arr.put(o);
        }
        obj.put("id", 0);
        obj.put("msg", arr);

        String str = obj.toString();

        String filename = "msgs" + ".jml";
        FileOutputStream outputStream;
        try {
            File file = new File(getApplicationContext().getFilesDir(), filename);
            outputStream = new FileOutputStream(file);
            byte[] data = str.getBytes("UTF-8");
            outputStream.write(data);
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Collections.reverse(msgs);
    }


}
