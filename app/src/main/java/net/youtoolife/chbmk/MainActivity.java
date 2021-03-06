package net.youtoolife.chbmk;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    private EditText loginText;
    private EditText inviteText;
    private Button loginBtn;

    private RelativeLayout authActivityLayout;
    private AnimationDrawable animationDrawable;


    private String invite, login;


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

            /*
             mappings.put("sexypackage.SexyClass", buffer);
            XLoader xloa = new XLoader(mappings);
            Class sexy_cla = xloa.loadClass("sexypackage.SexyClass", decryptedBytes);

            System.out.println("class was loaded");
            System.out.println("begin object creation");
            Object sexy_ob = sexy_cla.newInstance();

            System.out.println("object was created");
            System.out.println("invoke: getFoo" + sexy_cla.getMethod("getSimpleFoo").invoke(sexy_ob));
             String modFile = "xaa.dex";
            FileOutputStream outputStream = openFileOutput(modFile, MODE_PRIVATE); //new FileOutputStream(modFile, MODE_PRIVATE);
            outputStream.write(decryptedBytes);
            outputStream.close();


            //String appDir = getApplicationInfo().dataDir;
            DexClassLoader classLoader = new DexClassLoader(modFile, appDir, null, getClass().getClassLoader());
            // Загружаем класс и создаем объект с интерфейсом ModuleInterface
            //ModuleInterface module;
            try {
                Class sexy_cla = classLoader.loadClass("sexypackage/SexyClass");

            System.out.println("class was loaded");
            System.out.println("begin object creation");
            Object sexy_ob = sexy_cla.newInstance();
            System.out.println("object was created");
            System.out.println("invoke: getFoo" + sexy_cla.getMethod("getSimpleFoo").invoke(sexy_ob));
            } catch (Exception e) {
                e.printStackTrace();
            }*/

        setContentView(R.layout.activity_main);

        String token = SharedPrefManager.getInstance(this).getToken();
        if (token != null) {
            //textView.setText(token);
            Log.d("TOKEN:", token);
        }

        loginText = findViewById(R.id.editLogin);
        inviteText = findViewById(R.id.editInvite);

        loginBtn = (Button) findViewById(R.id.loginBtn);


        authActivityLayout = (RelativeLayout) findViewById(R.id.AuthActivityLayout);
        animationDrawable = (AnimationDrawable) authActivityLayout.getBackground();
        animationDrawable.setEnterFadeDuration(1500);
        animationDrawable.setExitFadeDuration(1500);
        animationDrawable.start();


        if (SharedPrefManager.getInstance(getApplicationContext()).getInvite() != null
                && SharedPrefManager.getInstance(getApplicationContext()).getLogin() != null) {
            invite = SharedPrefManager.getInstance(getApplicationContext()).getInvite();
            login = SharedPrefManager.getInstance(getApplicationContext()).getLogin();
            login();
        }

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("button:", "onClick");
                invite = inviteText.getText().toString().trim();
                login = loginText.getText().toString().trim();
                login();
                //Intent intent = new Intent(MainActivity.this, ContActivity.class);
                //startActivity(intent);
            }
        });
    }

    private void login() {

        Map<String, String> params0 = new HashMap<>();
        params0.put("dev", RSAIsa.rsaEncrypt(SharedPrefManager.getInstance(getApplicationContext()).getToken()));
        params0.put("login", login);
        params0.put("invite", RSAIsa.rsaEncrypt(invite));
        params0.put("pwd",RSAIsa.rsaEncrypt(XA.b(XA.B)));

        JSONObject jsonObject = new JSONObject(params0);
        Map<String, String> params = new HashMap<>();
        String json = jsonObject.toString();
        System.out.print("json "+json);
        try {
            params.put("d", Base64.encodeToString(jsonObject.toString().getBytes("UTF-8"), Base64.DEFAULT));
            //params.put("d", RSAIsa.rsaEncrypt(jsonObject.toString()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        params.put("i", "i");

        RequestHandler requestHandler = new RequestHandler(XA.b(XA.A), params, getApplicationContext());
        requestHandler.request(new CallBack() {
            @Override
            public void callBackFunc(String response) {
                try {
                    if (response == null)
                    {
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle("Server Error!")
                                .setMessage("Connection Error!\n")
                                //.setIcon(R.mipmap.ic_launcher_foreground)
                                .setCancelable(false)
                                .setNegativeButton("OK",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                dialog.cancel();
                                                Intent content = new Intent(MainActivity.this, ContActivity.class);
                                                startActivity(content);
                                            }
                                        });
                        AlertDialog alert = builder.create();
                        alert.show();
                        return;
                    }

                    Log.d("Main: answer", response);
                    JSONObject obj = new JSONObject(response);
                    int id = obj.getInt("id");
                    if (id > -1) {

                        //SharedPrefManager.getInstance(getApplicationContext()).storeMsgs(response);
                        SharedPrefManager.getInstance(getApplicationContext()).storeLogin(login, invite);

                        Log.d("Translate:", "goToContent");
                        Intent content = new Intent(MainActivity.this, ContActivity.class);
                        startActivity(content);
                    }
                    else {
                        if (id == -2) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            builder.setTitle("Server Error!")
                                    .setMessage("Connection Error!\n"+
                                            new String(Base64.decode(obj.getString("msg"),Base64.DEFAULT)) )
                                    //.setIcon(R.mipmap.ic_launcher_foreground)
                                    .setCancelable(false)
                                    .setNegativeButton("OK",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    dialog.cancel();
                                                    Intent content = new Intent(MainActivity.this, ContActivity.class);
                                                    startActivity(content);
                                                }
                                            });
                            AlertDialog alert = builder.create();
                            alert.show();

                        }
                        else
                            inviteText.setError("Wrong invite code!");
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }




}

