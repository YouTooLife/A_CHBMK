package net.youtoolife.chbmk;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by youtoolife on 6/8/18.
 */

public class RVAdapter extends RecyclerView.Adapter<RVAdapter.MessageViewHolder>{

    List<Message> msgs;

    public volatile Map<String, ArrayList<MessageViewHolder>>  imgLoads;

    private  Context context;

    RVAdapter(Context context, List<Message> messages){
        this.context = context;
        this.msgs = messages;
        imgLoads  = new HashMap<>();
    }

    @Override
    public int getItemCount() {
        return msgs.size();
    }


    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        TextView date;
        TextView body;
        ImageView img;
        MessageViewHolder(View itemView) {
            super(itemView);
            cv = (CardView)itemView.findViewById(R.id.cv);
            date = (TextView)itemView.findViewById(R.id.msg_date);
            body = (TextView)itemView.findViewById(R.id.msg_body);
            img = (ImageView)itemView.findViewById(R.id.msg_img);
        }
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item, viewGroup, false);
        MessageViewHolder pvh = new MessageViewHolder(v);
        return pvh;
    }

    @Override
    public void onBindViewHolder(MessageViewHolder messageViewHolder, int i) {
        messageViewHolder.date.setText(msgs.get(i).date);
        messageViewHolder.body.setText(msgs.get(i).body);

        String hash = msgs.get(i).hash;

        //messageViewHolder.img.setVisibility();
        messageViewHolder.img.setImageDrawable(null);
        //if (messageViewHolder.img.getDrawable() == null)
        if (!hash.isEmpty()) {
            byte[] blob = null;
            try {
                blob = getImg(hash);
                if (blob != null) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(blob, 0, blob.length);
                    messageViewHolder.img.setImageBitmap(bitmap);
                }
            } catch (Exception e)
            {
                e.printStackTrace();
            }
            if (blob == null)
            if (!imgLoads.containsKey(hash)) {
                ArrayList<MessageViewHolder> arr = new ArrayList<>();
                arr.add(messageViewHolder);
                imgLoads.put(hash, arr);
                loadImg(hash);
            }
            else {
                ArrayList<MessageViewHolder> arr = imgLoads.get(hash);
                arr.add(messageViewHolder);
            }
        }

    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }


    public void storeImg(String hash, byte[] data) {
        String filename = hash + ".bin";
        FileOutputStream outputStream;
        try {
            File file = new File(context.getFilesDir(), filename);
            outputStream = new FileOutputStream(file); //openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(data);
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public byte[] getImg(String hash) {
        String filename = hash + ".bin";
        FileInputStream inputStream;

        byte[] output = null;
        try {

            File file = new File(context.getFilesDir(), filename);
            if (file.exists()) {
                inputStream = new FileInputStream(file); //openFileOutput(filename, Context.MODE_PRIVATE);
                output = new byte[(int) file.length()];
                inputStream.read(output);
                inputStream.close();

                /*
                Formatter formatter = new Formatter();
                for (int i = 0; i < 300; i++) {
                    formatter.format("%02x ", output[i]);
                }
                String sd = formatter.toString();
                formatter.close();
                Log.d("getSTORE+", sd);
                */
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return  output;
    }





    private void loadImg(String hash) {

        Map<String, String> params0 = new HashMap<>();
        params0.put("h", hash);
        params0.put("pwd",XA.b(XA.B));

        JSONObject jsonObject = new JSONObject(params0);
        Map<String, String> params = new HashMap<>();
        String json = jsonObject.toString();
        System.out.print("json "+json);
        params.put("d", RSAIsa.rsaEncrypt(jsonObject.toString()));

        String URL_SERVER = XA.b(XA.C);

        RequestHandler requestHandler = new RequestHandler(URL_SERVER, params, context);
        requestHandler.request(new CallBack() {
            @Override
            public void callBackFunc(String response) {
                imgResponseHandler(response);
            }
        });
    }


    public void imgResponseHandler(String response) {

        if (response == null)
            return;

        Log.d("imgResponse: resp", response);
        try {
            JSONObject obj = new JSONObject(response);

            byte[] blob = null;

            int id = obj.getInt("id");
            if (id > -1) {

                JSONArray img = obj.getJSONArray("blob");

                String hash = obj.getString("hash");

                ByteBuffer byteBuffer = ByteBuffer.allocate(img.length() * 316*1024);
                for (int j = 0; j < img.length(); j++) {
                    byte[] dec = Base64.decode(img.getString(j), Base64.DEFAULT);
                    byteBuffer.put(dec);
                }

                blob = byteBuffer.array();

                storeImg(hash, blob);

                Bitmap bitmap = BitmapFactory.decodeByteArray(blob, 0, blob.length);
                ArrayList<MessageViewHolder> arr = imgLoads.get(hash);
                if (arr != null)
                    for (MessageViewHolder mvh:arr)
                        if (mvh.img.getDrawable() != null)
                            mvh.img.setImageBitmap(bitmap);
                imgLoads.remove(hash);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }



}