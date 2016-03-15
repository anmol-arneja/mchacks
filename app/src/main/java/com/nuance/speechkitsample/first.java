package com.nuance.speechkitsample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

/**
 * Created by Admin on 2016-02-21.
 */
public class first extends AppCompatActivity {
    Button main_button;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        onClickMainButton();
    }

    public void onClickMainButton(){
        main_button = (Button) findViewById(R.id.mainbutton);
        main_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(first.this, talk.class));
            }
        });


    }


}
