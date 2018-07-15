package net.youtoolife.chbmk;

/**
 * Created by youtoolife on 6/11/18.
 */

public class Message {

    int index;
    String date;
    String title;
    String body;

    String hash;
    //byte[] blob = null;

    Message(int index, String title, String body, String date, String hash) {

        this.index = index;
        this.title = title;
        this.body = body;
        this.date = date;
        this.hash = hash;

        //this.blob = blob;
    }
}
