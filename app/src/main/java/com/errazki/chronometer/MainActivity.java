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
    private Button initbtn;
    private Runnable refreshRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainText = (TextView) findViewById(R.id.mainTextView);
        stopbtn = (Button) findViewById(R.id.stopbtn);
        startbtn = (Button) findViewById(R.id.startbtn);
        initbtn = (Button) findViewById(R.id.initbtn);
        this.handler = new Handler();
        state = new State();
        //runnableInitUi();
        runnableInitHandler();
    }

    public Runnable getRefreshRunnable() {
        return refreshRunnable;
    }

    public void setRefreshRunnable(Runnable refreshRunnable) {
        this.refreshRunnable = refreshRunnable;
    }

    static class State {
        long cumulatedTime = 0; // cumulated time from the previous runs
        long startTime = -1; // startTime for the current run
        String readabletime = "00:00:00:00";

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


        public String getReadabletime() {
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

    private String getReadableTimeMain(Long nanos) {
        long nanotemp = nanos;
        long tempSec = nanos / (1000 * 1000 * 1000);
        long sec = tempSec % 60;
        long min = (tempSec / 60) % 60;
        long hour = (tempSec / (60 * 60)) % 24;
        long day = (tempSec / (24 * 60 * 60)) % 24;
        nanotemp = nanotemp - tempSec;
        String numberStr = "0";
        if (nanotemp > 0) {
            numberStr = String.valueOf(nanotemp).substring(0, 3);

        }
        return String.format("%d:%d:%d", hour, min, sec) + ":" + numberStr;
    }

    public void startChrono() {

        startbtn.setEnabled(false);
        stopbtn.setEnabled(true);
        state.setStartTime(System.nanoTime());
        updateChronoOnHandlerMethod();
        //updateChronoOnUiThreadMethod();
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

    public void refreshChrono() {
        String chrono = timeformat(state.getElapsedTime());
        Log.w("chrono", chrono + "the time in nano: " + state.getElapsedTime());
        state.setReadabletime(chrono);
        mainText.setText(state.getReadabletime());
    }



    public String timeformat(long nano) {
        return getReadableTimeMain(nano);
    }


    /*******************************************************Handler Thread Method****************************************/

    public void startRefresh(){
        handler.post(getRefreshRunnable());
    }
    public void stopRefresh(){
        handler.removeCallbacks(getRefreshRunnable());
    }
    public void runnableInitHandler(){
        refreshRunnable=()->{
            refreshChrono();
        };
      setRefreshRunnable(refreshRunnable);
    }

 public void updateChronoOnHandlerMethod() {
        handler.postDelayed(getRefreshRunnable(),500);
 }
/**************************************************************OnUiThreadMethod******************************************/
    public void runnableInitUi() {
        refreshRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    while (!Thread.interrupted()) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                refreshChrono();
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
        setRefreshRunnable(refreshRunnable);

    }
    public void updateChronoOnUiThreadMethod() {
        timerThread = new Thread(getRefreshRunnable());
        timerThread.start();

    }
}
