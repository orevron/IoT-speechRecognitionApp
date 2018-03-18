package com.example.matheus.speechrecognizersample;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Pattern;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class Sample extends AppCompatActivity implements RecognitionListener {

    Button record_btn;
    TextView speech_result;
    TextView responseView;
    EditText server_ip_textbox;
    String server_ip;

    private SpeechRecognizer speechRecognizer;
    private Intent recognizerIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample);

        record_btn = findViewById(R.id.record_btn);
        speech_result = findViewById(R.id.speech_result);
        responseView = findViewById(R.id.responseView);
        server_ip_textbox = findViewById(R.id.server_ip_textbox);

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(Sample.this);
        speechRecognizer.setRecognitionListener(Sample.this);
        // Configure the recognizer
        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, Sample.this.getPackageName()); // Replace by your package.
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en_US");
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "he_IL");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 10);
            }
            return;
        }
        record_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listen();
            }
        });
    }

    private static final Pattern PATTERN = Pattern.compile("^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");

    public static boolean validate(final String ip) {
        return PATTERN.matcher(ip).matches();
    }

    @NeedsPermission(value = {Manifest.permission.RECORD_AUDIO})
    public void listen() {
        speechRecognizer.startListening(recognizerIntent);
    }


    @Override
    public void onReadyForSpeech(Bundle bundle) {
        String ip = server_ip_textbox.getText().toString();
        server_ip_textbox.setEnabled(false);
        server_ip = validate(ip) ? ip : "192.168.10.23";
        server_ip_textbox.setText(server_ip);
    }

    @Override
    public void onBeginningOfSpeech() {

    }

    @Override
    public void onRmsChanged(float v) {

    }

    @Override
    public void onBufferReceived(byte[] bytes) {

    }

    @Override
    public void onEndOfSpeech() {

    }

    @Override
    public void onError(int i) {

    }

    @Override
    public void onResults(Bundle bundle) {
        ArrayList<String> strList = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        String data = strList.get(0);
        String cmd = data.replaceAll(" ", "_");
        sendCommandToServer(cmd);
        cmd = "sending to server:\n" + cmd;
        speech_result.setText(cmd);
    }

    @Override
    public void onPartialResults(Bundle bundle) {

    }

    @Override
    public void onEvent(int i, Bundle bundle) {

    }

    public void sendCommandToServer(String cmd) {
        String url = "http://" + server_ip + ":3000/doCommand/" + cmd;
        try {
            String res = new GET().execute(url).get();
            responseView.setText(res);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class GET extends AsyncTask<String, Integer, String> {
        protected String doInBackground(String... urls) {
            String result = "";
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                result = "received from server:\n" + readStream(in);
                in.close();
                urlConnection.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return result;
        }

        public String readStream(InputStream is) {
            try {
                byte[] bytes = new byte[1000];
                StringBuilder x = new StringBuilder();
                int numRead = 0;
                while ((numRead = is.read(bytes)) >= 0) {
                    x.append(new String(bytes, 0, numRead));
                }
                return x.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
