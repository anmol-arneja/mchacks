package com.nuance.speechkitsample;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.nuance.dragon.toolkit.edr.internal.jni.StringArray;
import com.nuance.speechkit.DetectionType;
import com.nuance.speechkit.Interpretation;
import com.nuance.speechkit.Language;
import com.nuance.speechkit.Session;
import com.nuance.speechkit.Transaction;
import com.nuance.speechkit.TransactionException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import android.content.pm.PackageInfo;
import android.content.pm.ApplicationInfo;

/**
 * This Activity is built to demonstrate how to perform NLU (Natural Language Understanding) with
 * text input instead of voice.
 *
 * NLU is the transformation of text into meaning.
 *
 * When performing speech recognition with SpeechKit, you have a variety of options. Here we demonstrate
 * Context Tag and Language.
 *
 * The Context Tag is assigned in the system configuration upon deployment of an NLU model.
 * Combined with the App ID, it will be used to find the correct NLU version to query.
 *
 * Languages can be configured. Supported languages can be found here:
 * http://developer.nuance.com/public/index.php?task=supportedLanguages
 *
 * Copyright (c) 2015 Nuance Communications. All rights reserved.
 */
public class TextNLUActivity extends DetailActivity implements View.OnClickListener {
    String response;
    JSONArray arr;
    String result;
    List<String > installed_apps;
    private EditText textInput;
    private EditText nluContextTag;
    private EditText language;

    private TextView logs;
    private Button clearLogs;

    private Button toggleReco;

    private Session speechSession;
    private State state = State.IDLE;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_nlu);

        textInput = (EditText) findViewById(R.id.text_input);
        nluContextTag = (EditText)findViewById(R.id.nlu_context_tag);
        nluContextTag.setText(Configuration.CONTEXT_TAG);
        language = (EditText)findViewById(R.id.language);

        logs = (TextView)findViewById(R.id.logs);
        clearLogs = (Button)findViewById(R.id.clear_logs);
        clearLogs.setOnClickListener(this);

        toggleReco = (Button)findViewById(R.id.toggle_reco);
        toggleReco.setOnClickListener(this);

        //Create a session
        speechSession = Session.Factory.session(this, Configuration.SERVER_URI, Configuration.APP_KEY);

        setState(State.IDLE);
        installed_apps = GetInstalledAppList();
    }

    @Override
    public void onClick(View v) {
        if (v == clearLogs) {
            logs.setText("");
        } else if(v == toggleReco) {
            toggleReco();
        }
    }

    /* Reco transactions */

    private void toggleReco() {
        switch (state) {
            case IDLE:
                recognize();
                break;
            case PROCESSING:
                break;
        }
    }

    /**
     * Send user's text query to the server
     */
    private void recognize() {
        if (textInput.getText().length() > 0) {
            //Setup our Reco transaction options.
            Transaction.Options options = new Transaction.Options();
            options.setLanguage(new Language(language.getText().toString()));

            //Add properties to appServerData for use with custom service. Leave empty for use with NLU.
            JSONObject appServerData = new JSONObject();
            try {
                appServerData.put("message", textInput.getText().toString());

                speechSession.transactionWithService(nluContextTag.getText().toString(), appServerData, options, recoListener);
                Log.d("OUTPUT", "PReeeeeeetttttttttttt");
                Log.d("OUTPUT_NLU",nluContextTag.getText().toString());
                Log.d("OUTPUT",appServerData.toString());

            } catch (JSONException e) {
                e.printStackTrace();
            }
            setState(State.PROCESSING);
        }
        else {
            logs.append("\n" + getResources().getString(R.string.text_input_missing));
        }
    }

    private Transaction.Listener recoListener = new Transaction.Listener() {
        @Override
        public void onServiceResponse(Transaction transaction, JSONObject response) {
            try {
                // 2 spaces for tabulations.
                logs.append("\nonServiceResponse: " + response.toString(2));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onInterpretation(Transaction transaction, Interpretation interpretation) {
            try {
                logs.append("\nonInterpretation: " + interpretation.getResult().toString(2));
                response = interpretation.getResult().toString(2);
                JSONObject mainObject = new JSONObject(response);
                JSONArray inter = mainObject.getJSONArray("interpretations");
                List<String> allNames = new ArrayList<String>();
                for(int i =0; i< inter.length();i++) {
                    JSONObject actor = inter.getJSONObject(i);
                    String literal = actor.getString("concepts");
                    JSONObject secondObject = new JSONObject(literal);
                    String app = secondObject.getString("APP_NAME");
                    JSONArray arr = new JSONArray(app);
                    for(int j =0; j<arr.length();j++){
                        JSONObject actor1 = arr.getJSONObject(j);
                        String val = actor1.getString("literal");
                        result = val;
                        Log.d("Value is",val);
                    }
                    openRequestedApp();

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            //Log.d("Result is",result);

        }

        @Override
        public void onSuccess(Transaction transaction, String s) {
            logs.append("\nonSuccess");

            //Notification of a successful transaction. Nothing to do here.
            setState(State.IDLE);
            //openRequestedApp();
        }

        @Override
        public void onError(Transaction transaction, String s, TransactionException e) {
            logs.append("\nonError: " + e.getMessage() + s==null?"":(". " + s));

            //Something went wrong. Check Configuration.java to ensure that your settings are correct.
            //The user could also be offline, so be sure to handle this case appropriately.
            setState(State.IDLE);
        }
    };

    /* State Logic: IDLE -> PROCESSING -> repeat */

    private enum State {
        IDLE,
        PROCESSING
    }

    //Get list of all installed apps

    /**
     * Set the state and update the button text.
     */
    private void setState(State newState) {
        state = newState;
        switch (newState) {
            case IDLE:
                toggleReco.setText(getResources().getString(R.string.transaction_with_service));
                break;
            case PROCESSING:
                toggleReco.setText(getResources().getString(R.string.processing));
                break;
        }
    }


    public List<String> GetInstalledAppList()
    {
        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        final List pkgAppsList = getPackageManager().queryIntentActivities( mainIntent, 0);
        List<String> appList = new ArrayList<String>();
        List<String> pkgList = new ArrayList<String>();
        for (Object object : pkgAppsList)
        {
            ResolveInfo info = (ResolveInfo) object;
            Drawable icon    = getBaseContext().getPackageManager().getApplicationIcon(info.activityInfo.applicationInfo);
            String strAppName  	= info.activityInfo.applicationInfo.publicSourceDir.toString();
            String strPackageName  = info.activityInfo.applicationInfo.packageName.toString();
            final String title 	= (String)((info != null) ? getBaseContext().getPackageManager().getApplicationLabel(info.activityInfo.applicationInfo) : "???");
            appList.add(strAppName);
            pkgList.add(strPackageName);
        }
        Log.d("Package List",pkgList.toString());
        Log.d("appList",appList.toString());
        return pkgList;
    }



    public void openRequestedApp() {
        String paramString = result;
        String new_result = paramString.toLowerCase();
        Log.d("New Result is :",new_result);
        for (String s : installed_apps) {

            if (s.contains(new_result)) {
                startActivity(getPackageManager().getLaunchIntentForPackage(s));

            } else {
                Log.d("App Locating Error", "Cannot find such app");
            }
        }
    }
    }




