package com.nuance.speechkitsample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

/**
 * Created by Admin on 2016-02-21.
 */
public class talk extends AppCompatActivity{
    Button talk_button;
    @Override

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.talk);
        onClickTalkButton();

    }

    public void onClickTalkButton(){
        talk_button = (Button) findViewById(R.id.speak_button);
        talk_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setContentView(R.layout.activity_main);

            }
        });
    }



}
