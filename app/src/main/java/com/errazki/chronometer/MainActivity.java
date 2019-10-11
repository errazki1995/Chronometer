package com.errazki.chronometer;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    State state;
    TextView mainText;
    private Handler handler;
    private Thread timerThread;
    private Button stopbtn;
    private Button startbtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainText = (TextView) findViewById(R.id.mainTextView);
        stopbtn = (Button) findViewById(R.id.stopbtn);
       startbtn = (Button) findViewById(R.id.startbtn);
        handler = new Handler();
        state = new State();
    }


    static class State {
        long cumulatedTime = 0; // cumulated time from the previous runs
        long startTime = -1; // startTime for the current run
        String readabletime;

        public long getElapsedTime1() {
            if (startTime > 0) {
                long elapsedTime = (System.nanoTime() - startTime);
                return cumulatedTime + elapsedTime;
            }
            return cumulatedTime;
        }

        /*ElapsedTime provided from Andrews
         */

        public long getElapsedTime() {
            //tempsavant + temps apre
            long tmp = (startTime > 0) ? (System.nanoTime() - startTime) : 0;
            Log.w("elapsed1_tmp", tmp + "");
            //Log.w("Elapsed2_cumulatedTime", cumulatedTime + "");
            return cumulatedTime + tmp;
        }


        public String getreadabletime() {
            return readabletime;
        }

        public void setReadabletime(String readabletime) {
            this.readabletime = readabletime;
        }

        public long getStartTime() {
            return startTime;
        }

        public long getCumulatedTime() {
            return cumulatedTime;
        }

        public void setStartTime(long start) {
            this.startTime = start;
        }

        public void setCumulatedTime(long timeToAdd) {
            this.cumulatedTime = timeToAdd;
        }

    }

    private String getReadableTime(Long nanos) {
        long nanotemp = nanos;
        long tempSec = nanos / (1000 * 1000 * 1000);
        long sec = tempSec % 60;
        long min = (tempSec / 60) % 60;
        long hour = (tempSec / (60 * 60)) % 24;
        long day = (tempSec / (24 * 60 * 60)) % 24;
        nanotemp = nanotemp - tempSec;
        String numberStr="";
        if(nanotemp>0){
             numberStr = String.valueOf(nanotemp).substring(0, 3);

        }
        return String.format("%d:%d:%d", hour, min, sec) + ":" + numberStr;
}

    public void startChrono() {
        /*au cas ou le temps de depart est -1(le debut) on met le temps recent
         *sinon on met comme temps de depart le (temps d'arret précédent pour continuer)
         */
        startbtn.setEnabled(false);
        stopbtn.setEnabled(true);
        state.setStartTime(System.nanoTime());
        //updateChronoOnHandlerMethod();
        updateChronoOnUiThreadMethod();
    }


    public void stopChrono() {
       startbtn.setEnabled(true);
       if (stopbtn.isEnabled()) {
            state.setCumulatedTime(state.getElapsedTime());
            state.setStartTime(-1);
            if (timerThread != null) timerThread.interrupt();
            stopbtn.setEnabled(false);
        }
    }

    public void start(View v) {
        //startbtn.setEnabled(false);
        startChrono();

    }

    public void stop(View v) {
        stopChrono();
    }

    public void init(View v) {
        state.setStartTime(-1);
        state.setCumulatedTime(0);
        mainText.setText("00:00:00:00");
    }

    public String timeformat(long nano) {
        return getReadableTime(nano);
    }

    /*
     *
     *Ce bout du code utilise le Handler , cette pratique est souvent utilisé
     */
    public void updateChronoOnHandlerMethod() {
        timerThread = new Thread(() -> {
            while (!Thread.interrupted()) {

                // le code Ui doit etre executé soit dans le runOnUIThread ou le handler
                handler.post(() -> {
                    String chrono = timeformat(state.getElapsedTime());

                    Log.w("chrono", chrono + "the time in nano: " + state.getElapsedTime());

                    state.setReadabletime(chrono);
                    mainText.setText(state.getreadabletime());
                    try {
                        Thread.sleep(500);

                    } catch (InterruptedException e) {

                    }

                });
            }


        });
        timerThread.start();

    }

    /*
     *Ce bout du code utilise le OnUiThread
     */
    public void updateChronoOnUiThreadMethod() {

        timerThread = new Thread() {
            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String chrono = timeformat(state.getElapsedTime());
                                Log.w("chronoo", chrono + "the time in nano: " + state.getElapsedTime());

                                state.setReadabletime(chrono);
                                mainText.setText(state.getreadabletime());

                            }
                        });
                        try {
                            Thread.sleep(500);

                        } catch (InterruptedException e) {

                        }
                    }
                } catch (Exception e) {
                }
            }
        };
        timerThread.start();

    }
}
