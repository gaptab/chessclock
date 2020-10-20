package com.eldopp.chessclk;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Guideline;
import androidx.preference.PreferenceManager;

import android.animation.ValueAnimator;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    //layout
    private ImageButton button_settings;
    private ImageButton button_rate;
    private ImageButton button_reset;
    private ImageButton button_pause;
    private ImageButton button_player1;
    private ImageButton button_player2;
    private TextView text_player1;
    private TextView text_player2;
    private TextView tap_player1;
    private TextView tap_player2;
    private ImageView flag_player1;
    private ImageView flag_player2;
    private TextView moves_player1;
    private TextView moves_player2;



    private boolean sound;
    private boolean moves;
    private boolean p2;
    private boolean increment;
    private boolean orientation;
    private int increment_type;
    private int minutes_player1;
    private int seconds_player1;
    private int minutes_player2;
    private int seconds_player2;
    private int increment_value;
    private int n_moves;
    private long bronstein_memory;


    //timers
    private CountDownTimer mCountDownTimer_player1;
    private CountDownTimer mCountDownTimer_player2;
    private CountDownTimer mCountDownTimer_delay_player1;
    private CountDownTimer mCountDownTimer_delay_player2;
    private boolean mTimerRunning_player1;
    private boolean mTimerRunning_player2;
    private long mTimeLeftInMillis_player1;
    private long mTimeLeftInMillis_player2;

    //game state
    private boolean game_started = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.main_activity);
        final MediaPlayer mp_player1 = MediaPlayer.create(this, R.raw.sound_player1);
        final MediaPlayer mp_player2 = MediaPlayer.create(this, R.raw.sound_player2);

        tap_player1 = findViewById(R.id.tap_player1);
        tap_player2 = findViewById(R.id.tap_player2);
        text_player1 = findViewById(R.id.text_player1);
        text_player2 = findViewById(R.id.text_player2);
        flag_player1 = findViewById(R.id.flag_player1);
        flag_player2 = findViewById(R.id.flag_player2);
        moves_player1 = findViewById(R.id.moves_player1);
        moves_player2 = findViewById(R.id.moves_player2);

        button_settings = (ImageButton) findViewById(R.id.button_settings);
        button_rate = (ImageButton) findViewById(R.id.button_rate);
        button_reset = (ImageButton) findViewById(R.id.button_reset);
        button_pause = (ImageButton) findViewById(R.id.button_pause);
        button_player1 = (ImageButton) findViewById(R.id.button_player1);
        button_player2 = (ImageButton) findViewById(R.id.button_player2);

        button_reset.setVisibility(View.INVISIBLE);
        button_pause.setVisibility(View.INVISIBLE);

        //RESET BUTTON PRESSED
        button_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetCounters();
                game_started = false;
                button_reset.setVisibility(View.INVISIBLE);
                button_pause.setVisibility(View.INVISIBLE);
                button_settings.setVisibility(View.VISIBLE);
                button_rate.setVisibility(View.VISIBLE);
                button_player1.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                button_player2.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                tap_player1.setText("Tap to start");
                tap_player2.setText("Tap to start");
                tap_player1.setVisibility(View.VISIBLE);
                tap_player2.setVisibility(View.VISIBLE);
                button_player1.setClickable(true);
                button_player2.setClickable(true);
                flag_player1.setVisibility(View.INVISIBLE);
                flag_player2.setVisibility(View.INVISIBLE);
                text_player1.setVisibility(View.VISIBLE);
                text_player2.setVisibility(View.VISIBLE);
            }
        });

        //BUTTON PAUSE PRESSED
        button_pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                button_reset.setVisibility(View.VISIBLE);
                button_pause.setVisibility(View.INVISIBLE);
                tap_player1.setText("Tap to resume");
                tap_player2.setText("Tap to resume");
                tap_player1.setVisibility(View.VISIBLE);
                tap_player2.setVisibility(View.VISIBLE);
                if(mCountDownTimer_player1 != null){
                    stopTimer_player1();
                }
                if(mCountDownTimer_player2 != null){
                    stopTimer_player2();
                }
                if(mCountDownTimer_delay_player2 != null){
                    stopTimer_delay_player2();
                }
                if(mCountDownTimer_delay_player1 != null){
                    stopTimer_delay_player1();
                }
                button_player1.setClickable(true);
                button_player2.setClickable(true);
            }
        });

        button_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //mp_player2.start();
                openSettingsActivity();
            }
        });
        button_rate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRateMsg();
            }
        });

        //PLAYER 1 BUTTON PRESSED
        button_player1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //sound
                if(sound){mp_player1.start();}

                button_player1.setClickable(false);
                button_player2.setClickable(true);

                updateButtonsOnBeginGame();

                n_moves++;
                moves_player1.setText(Integer.toString(n_moves));
                moves_player2.setText(Integer.toString(n_moves));

                if(!game_started) {
                    game_started = true;
                } else{
                    if(increment_type==0){
                        mTimeLeftInMillis_player1 += increment_value * 1000;
                    }
                    else if(increment_type==2){
                        //bronstein case
                        if((bronstein_memory - mTimeLeftInMillis_player1) > (increment_value * 1000)){
                            mTimeLeftInMillis_player1 += increment_value * 1000;
                        }else{
                            mTimeLeftInMillis_player1 = bronstein_memory;
                        }

                    }
                    else if(increment_type==4){
                        mTimeLeftInMillis_player1 = 1000 * (minutes_player1 * 60 + seconds_player1);
                    }
                    updateCountDownText_player1();
                    if(mCountDownTimer_player1 != null){
                        stopTimer_player1();
                    }

                }
                if(mCountDownTimer_delay_player1 != null){
                    stopTimer_delay_player1();
                }
                if(increment_type==1){
                    startDelayTimer_player2();
                }else{
                    startTimer_player2();
                }
            }
        });

        //PLAYER 2 BUTTON PRESSED
        button_player2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //sound
                if(sound){mp_player2.start();}

                button_player1.setClickable(true);
                button_player2.setClickable(false);

                updateButtonsOnBeginGame();

                if(!game_started) {
                    game_started = true;
                } else{
                    if(increment_type==0){
                        //fischer case
                        mTimeLeftInMillis_player2 += increment_value * 1000;
                    }
                    else if(increment_type==2){
                        //bronstein case
                        if((bronstein_memory - mTimeLeftInMillis_player2) > (increment_value * 1000)){
                            mTimeLeftInMillis_player2 += increment_value * 1000;
                        }else{
                            mTimeLeftInMillis_player2 = bronstein_memory;
                        }
                    }
                    else if(increment_type==4){
                        mTimeLeftInMillis_player2 = 1000 * (minutes_player2 * 60 + seconds_player2);
                    }
                    updateCountDownText_player2();
                    if(mCountDownTimer_player2 != null){
                        stopTimer_player2();
                    }

                }
                if(mCountDownTimer_delay_player2 != null){
                    stopTimer_delay_player2();
                }
                if(increment_type==1){
                    startDelayTimer_player1();
                }else{
                    startTimer_player1();
                }
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        sound = prefs.getBoolean("sound", true);
        moves = prefs.getBoolean("movecount", true);
        p2 = prefs.getBoolean("same_player2", true);
        increment = prefs.getBoolean("increment", false);
        increment_type = Integer.parseInt(prefs.getString("increment_type", "0"));
        orientation = prefs.getBoolean("orientation", false);

        minutes_player1 = prefs.getInt("minutes_player1", 5);
        seconds_player1 = prefs.getInt("seconds_player1", 0);

        if(p2==false) {
            minutes_player2 = minutes_player1;
            seconds_player2 = seconds_player1;
        }else {
            minutes_player2 = prefs.getInt("minutes_player2", 5);
            seconds_player2 = prefs.getInt("seconds_player2", 0);
        }

        resetCounters();

        if(orientation){
            text_player1.setRotation(90);
            text_player2.setRotation(90);
            moves_player1.setRotation(90);
            moves_player2.setRotation(90);
            flag_player1.setRotation(90);
            flag_player2.setRotation(90);

            text_player1.setTextSize(85);
            text_player2.setTextSize(85);

        }else{
            text_player1.setRotation(0);
            text_player2.setRotation(180);
            moves_player1.setRotation(0);
            moves_player2.setRotation(180);
            flag_player1.setRotation(0);
            flag_player2.setRotation(180);

            text_player1.setTextSize(100);
            text_player2.setTextSize(100);
        }

        if(!increment){
            increment_value = 0;
        } else{
            increment_value = prefs.getInt("increment_value", 2);
        }



        resetCounters();

        if(moves){
            showMoves();
        } else{
            hideMoves();
        }

    }

    private void startTimer_player1() {
        bronstein_memory = mTimeLeftInMillis_player1;
        mCountDownTimer_player1 = new CountDownTimer(mTimeLeftInMillis_player1, 100) {
            @Override
            public void onTick(long millisUntilFinished) {
                mTimeLeftInMillis_player1 = millisUntilFinished;
                if(increment_type == 3){
                    //hourglass
                    mTimeLeftInMillis_player2 += 100;
                    updateCountDownText_player1();
                    updateCountDownText_player2();
                }
                updateCountDownText_player1();
            }
            @Override
            public void onFinish() {
                button_player1.setClickable(false);
                button_player2.setClickable(false);
                button_reset.setVisibility(View.VISIBLE);
                button_player1.setBackgroundColor(getResources().getColor(R.color.flag));
                flag_player1.setVisibility(View.VISIBLE);
                text_player1.setVisibility(View.INVISIBLE);
            }
        }.start();
    }

    private void startTimer_player2() {
        bronstein_memory = mTimeLeftInMillis_player2;
        mCountDownTimer_player2 = new CountDownTimer(mTimeLeftInMillis_player2, 100) {
            @Override
            public void onTick(long millisUntilFinished) {
                mTimeLeftInMillis_player2 = millisUntilFinished;
                if(increment_type == 3){
                    //hourglass
                    mTimeLeftInMillis_player1 += 100;
                    updateCountDownText_player1();
                    updateCountDownText_player2();
                }
                updateCountDownText_player2();
            }
            @Override
            public void onFinish() {
                button_player1.setClickable(false);
                button_player2.setClickable(false);
                button_reset.setVisibility(View.VISIBLE);
                button_player2.setBackgroundColor(getResources().getColor(R.color.flag));
                flag_player2.setVisibility(View.VISIBLE);
                text_player2.setVisibility(View.INVISIBLE);
            }
        }.start();
    }

    private void stopTimer_player1() {
        mCountDownTimer_player1.cancel();
    }

    private void stopTimer_player2() {
        mCountDownTimer_player2.cancel();
    }

    private void startDelayTimer_player1() {
        mCountDownTimer_delay_player1 = new CountDownTimer(increment_value*1000, 100) {

            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                startTimer_player1();
            }
        }.start();
    }

    private void startDelayTimer_player2() {
        mCountDownTimer_delay_player2 = new CountDownTimer(increment_value*1000, 100) {

            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                startTimer_player2();
            }
        }.start();
    }

    private void stopTimer_delay_player1() {mCountDownTimer_delay_player1.cancel();}
    private void stopTimer_delay_player2() {mCountDownTimer_delay_player2.cancel();}

    private void updateCountDownText_player1() {
        String timeLeftFormatted;
        int minutes = (int) (mTimeLeftInMillis_player1 / 1000) / 60;
        int seconds = (int) (mTimeLeftInMillis_player1 / 1000) % 60;
        int dec = (int) (mTimeLeftInMillis_player1 / 100) % 10;
        if(mTimeLeftInMillis_player1 > 10000){
            timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        } else {
            timeLeftFormatted = String.format(Locale.getDefault(), "%d.%1ds", seconds, dec);
        }
        text_player1.setText(timeLeftFormatted);
    }
    private void updateCountDownText_player2() {
        String timeLeftFormatted;
        int minutes = (int) (mTimeLeftInMillis_player2 / 1000) / 60;
        int seconds = (int) (mTimeLeftInMillis_player2 / 1000) % 60;
        int dec = (int) (mTimeLeftInMillis_player2 / 100) % 10;
        if(mTimeLeftInMillis_player2 > 10000){
            timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        } else {
            timeLeftFormatted = String.format(Locale.getDefault(), "%d.%1ds", seconds, dec);
        }
        text_player2.setText(timeLeftFormatted);
    }

    public void resetCounters() {
        mTimeLeftInMillis_player1 = 1000 * (minutes_player1 * 60 + seconds_player1);
        mTimeLeftInMillis_player2 = 1000 * (minutes_player2 * 60 + seconds_player2);

        updateCountDownText_player1();
        updateCountDownText_player2();

        n_moves = 0;
        moves_player1.setText(Integer.toString(n_moves));
        moves_player2.setText(Integer.toString(n_moves));
    }

    public void updateButtonsOnBeginGame() {
        tap_player1.setVisibility(View.INVISIBLE);
        tap_player2.setVisibility(View.INVISIBLE);
        button_reset.setVisibility(View.INVISIBLE);
        button_settings.setVisibility(View.INVISIBLE);
        button_rate.setVisibility(View.INVISIBLE);
        button_pause.setVisibility(View.VISIBLE);
    }

    public void showMoves() {
        moves_player1.setVisibility(View.VISIBLE);
        moves_player2.setVisibility(View.VISIBLE);
    }

    public void hideMoves() {
        moves_player1.setVisibility(View.INVISIBLE);
        moves_player2.setVisibility(View.INVISIBLE);
    }

    public void openSettingsActivity() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    public void showRateMsg() {
        try{
            //startActivity(new Intent(Intent.ACTION_VIEW), Uri.parse("market://details?id=com.eldopp.chessclk"));
            Toast.makeText(getApplicationContext(), "Please consider giving us positive feedback.", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("market://details?id=krishna.chess.clock"));
            startActivity(intent);
        }catch(ActivityNotFoundException e){
            //startActivity(new Intent(Intent.ACTION_VIEW), Uri.parse("https://play.google.com/store/apps/details?id=com.eldopp.chessclk"));
            Toast.makeText(getApplicationContext(), "Please consider giving us positive feedback.", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=krishna.chess.clock"));
            startActivity(intent);
        }
        //Toast.makeText(getApplicationContext(), "Please consider rating \nthe app on the PlayStore", Toast.LENGTH_LONG).show();
    }
}