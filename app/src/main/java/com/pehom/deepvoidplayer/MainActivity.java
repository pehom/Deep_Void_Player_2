package com.pehom.deepvoidplayer;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private static final int MY_PERMISSION_REQUEST = 1;
    private static final String fileName = "MainActivityState.txt";
    private PlaylistsDatabase playlistsDatabase;
    private MainActivityState mainActivityState;
    private MediaPlayer mediaPlayer;
    private ImageView playPauseIcon;
    private SeekBar seekbar;
    private ArrayList<Track> queueArrayList;
    private RecyclerView.LayoutManager queueLayoutManager;
    private RecyclerView queueRecyclerView;
    private RecyclerView choosePlaylistRecyclerView;
    private PlaylistAdapter choosePlaylistAdapter;
    private TextView currentTrackTextView;
    private TrackAdapter queueAdapter;
    private Handler currentTrackPositionHandler;

    private int currentQueueItemPosition;
    private String currentTrackProgress;
    private String currentTrackTitle;
    private LinearLayout choosePlaylistLinearLayout;
    private ImageView shuffleImageView;
    private boolean isShuffleModeOn = false;
    private boolean playPauseMode = false;
    private ArrayList<Integer> shuffledTracks;
    private ImageView loopImageView;

    private int loopMode = 0;
    Random random;
    boolean choosePlaylistIsVisible =  false;
    private  float startx, stopx;
    private float dX;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        random = new Random();
        setContentView(R.layout.alternative_activity_main);

        shuffledTracks = new ArrayList<>();
        currentQueueItemPosition = 0;
        currentTrackTextView = findViewById(R.id.currentTrackTextView);
        currentTrackTextView.setSelected(true);
        currentQueueItemPosition = 0;
        loopImageView = findViewById(R.id.loopImageView);
        seekbar = findViewById(R.id.seekBar);
        queueArrayList = new ArrayList<Track>();
        currentTrackPositionHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                int min = msg.what/1000/60;
                int sec = msg.what/1000 - min*60;
                if (sec < 10) {currentTrackProgress = ""+ min + ":0" + sec;}
                else {currentTrackProgress = ""+ min + ":" + sec;}

                currentTrackTextView.setText(currentTrackTitle + "  " + currentTrackProgress);

            }
        };

        verifyStoragePermissions(this);

        if (ContextCompat.checkSelfPermission(
       MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
          if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                 Manifest.permission.READ_EXTERNAL_STORAGE)){
             ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSION_REQUEST);
          }else {
             ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSION_REQUEST);

         }

        } else {
            doStuff();
        }

        playPauseIcon = findViewById(R.id.imageViewPlay);
        playlistsDatabase = Room.databaseBuilder(getApplicationContext(),
                PlaylistsDatabase.class, "PlaylistsDB").build();

       /* mainActivityState = getMainActivityState();
        if (mainActivityState != null) {
            applyMainActivityState(mainActivityState);
        }
        String s = readFromFile(this);
        Log.d("mylog", "readFromFile: " + s);
        Log.d("mylog", "readFromFile.length() =  " + s.length());*/


    }

    public void doStuff(){
        getMusic();
        queueRecyclerView = findViewById(R.id.playlistRecyclerView);
        queueLayoutManager = new LinearLayoutManager(this);
        queueAdapter = new TrackAdapter(queueArrayList, new TrackAdapter.OnTrackTouchListener() {
            @Override
            public void onTrackTouch(View v, MotionEvent event, int position) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: // нажатие
                        startx = event.getRawX();
                        dX = v.getX() - event.getRawX();

                        break;
                    case MotionEvent.ACTION_MOVE: // движение
                        if (event.getRawX() > startx) {
                            v.animate()
                                    .x(event.getRawX() + dX)
                                    .setDuration(0)
                                    .start();
                        }
                        break;
                    case MotionEvent.ACTION_UP: // отпускание
                        stopx = event.getRawX();
                        Log.d("mylog", "startx = " + startx + "  stopx = " + stopx);

                        if (stopx - startx >  150 && stopx!=0) {
                            Log.d("mylog", "removed: " + queueArrayList.get(position).getArtist() + "  " + queueArrayList.get(position).getTitle());
                            queueArrayList.remove(position);
                            queueRecyclerView.setAdapter(queueAdapter);

                        } else if (stopx != startx) {
                            v.animate()
                                    .x(0)
                                    .setDuration(0)
                                    .start();
                        }
                        else  {


                            Log.d("mylog", "playtheposition");
                            Log.d("mylog", "prevQueueItemPosition = " + currentQueueItemPosition);
                            RecyclerView.ViewHolder prevViewHolder =  queueRecyclerView.findViewHolderForAdapterPosition(currentQueueItemPosition);
                            if (prevViewHolder != null) {
                                final TextView prevTrackTV = (TextView)prevViewHolder.itemView.findViewById(R.id.trackTitleTextView);
                                prevTrackTV.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));
                            }
                           /* final TextView prevTrackTV = (TextView)prevViewHolder.itemView.findViewById(R.id.trackTitleTextView);
                            prevTrackTV.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));*/
                            currentQueueItemPosition = position;
                            playThePosition(currentQueueItemPosition);

                        }
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        Log.d("mylog", "action canceled");

                        v.animate()
                                .x(0)
                                .setDuration(0)
                                .start();
                        break;
                }

            }
        });
        queueRecyclerView.setLayoutManager(queueLayoutManager);
        queueRecyclerView.setAdapter(queueAdapter);

    }

    public void getMusic(){

        ContentResolver contentResolver = getContentResolver();
        Uri trackUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
        Cursor cursor  = contentResolver.query(trackUri, null, selection, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int trackTitle = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int trackArtist = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int trackDuration = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION);

            do {
                Track currentTrack = new Track();
                String currentTitle = cursor.getString(trackTitle);
                String currentArtist = cursor.getString(trackArtist);
                String currentDuration = cursor.getString(trackDuration);
                int duration = Integer.parseInt(currentDuration);
                int min = duration/1000/60;
                int sec = duration/1000 - min*60;
                if (sec < 10) currentDuration = "" + min + ":0" + sec;
                else currentDuration = "" + min + ":" + sec;
                currentTrack.setArtist(currentArtist);
                currentTrack.setTitle(currentTitle);
                currentTrack.setDuration(currentDuration);
                currentTrack.setData(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)));
                queueArrayList.add(currentTrack);

            } while (cursor.moveToNext());
        }
    }

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,  String[] permissions,  int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUEST: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] ==PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(MainActivity.this,
                            Manifest.permission.READ_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED &&
                            ContextCompat.checkSelfPermission(MainActivity.this,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED) {


                        Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
                        doStuff();
                    }
                } else {
                    Toast.makeText(this, "No permission granted", Toast.LENGTH_SHORT).show();
                    finish();
                }
                return;
            }
        }
    }

    public void nextTrack(View view) {

        if (isShuffleModeOn) {
            if (shuffledTracks.size()>1) {
                switch (loopMode){
                    case 0:
                        RecyclerView.ViewHolder viewHolder =  queueRecyclerView.findViewHolderForAdapterPosition(currentQueueItemPosition);
                        final TextView currentTrackTV = (TextView)viewHolder.itemView.findViewById(R.id.trackTitleTextView);
                        currentTrackTV.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));
                        Random random = new Random();
                        currentQueueItemPosition = random.nextInt(shuffledTracks.size());
                        shuffledTracks.remove(currentQueueItemPosition);
                        playThePosition(currentQueueItemPosition);
                        for(int i = 0; i < shuffledTracks.size(); i++){
                            Log.d("shuffledTracks", "shuffled tracks[" + i + "] =" + shuffledTracks.get(i));
                        }
                        break;
                    case 1:
                        playThePosition(currentQueueItemPosition);
                        break;
                    case 2:
                        RecyclerView.ViewHolder viewHolder1 =  queueRecyclerView.findViewHolderForAdapterPosition(currentQueueItemPosition);
                        final TextView currentTrackTV1 = (TextView)viewHolder1.itemView.findViewById(R.id.trackTitleTextView);
                        currentTrackTV1.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));
                        Random random1 = new Random();
                        currentQueueItemPosition = random1.nextInt(shuffledTracks.size());

                        playThePosition(currentQueueItemPosition);
                        for(int i = 0; i < shuffledTracks.size(); i++){
                            Log.d("shuffledTracks", "shuffled tracks[" + i + "] =" + shuffledTracks.get(i));
                        }
                        break;
                }

            } else {
                if (loopMode !=1) {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    playPauseIcon.setImageResource(R.drawable.ic_play_arrow_red);
                    playPauseMode = false;
                }
            }


        } else {
            switch (loopMode){
                case 0:
                    RecyclerView.ViewHolder viewHolder =  queueRecyclerView.findViewHolderForAdapterPosition(currentQueueItemPosition);
                    final TextView currentTrackTV = (TextView)viewHolder.itemView.findViewById(R.id.trackTitleTextView);
                    currentTrackTV.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));
                    currentQueueItemPosition++;
                    playThePosition(currentQueueItemPosition);
                    break;
                case 1:
                    playThePosition(currentQueueItemPosition);
                    break;
                case 2:
                    RecyclerView.ViewHolder viewHolder1 =  queueRecyclerView.findViewHolderForAdapterPosition(currentQueueItemPosition);
                    final TextView currentTrackTV1 = (TextView)viewHolder1.itemView.findViewById(R.id.trackTitleTextView);
                    currentTrackTV1.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));
                    if (currentQueueItemPosition < queueArrayList.size()-1) {
                        currentQueueItemPosition++;
                        playThePosition(currentQueueItemPosition);
                    } else {
                        currentQueueItemPosition = 0;
                        playThePosition(currentQueueItemPosition);
                    }
                    break;
            }
        }
    }

    public void prevTrack(View view) {

        if (isShuffleModeOn) {
            if (shuffledTracks.size()>1) {
                Random random = new Random();
                RecyclerView.ViewHolder viewHolder =  queueRecyclerView.findViewHolderForAdapterPosition(currentQueueItemPosition);
                final TextView currentTrackTV = (TextView)viewHolder.itemView.findViewById(R.id.trackTitleTextView);
                currentTrackTV.setTextColor(ContextCompat.getColor(this, R.color.colorAccent));
                currentQueueItemPosition = random.nextInt(shuffledTracks.size());
                shuffledTracks.remove(currentQueueItemPosition);
                playThePosition(currentQueueItemPosition);
                for(int i = 0; i < shuffledTracks.size(); i++){
                    Log.d("shuffledTracks", "shuffled tracks[" + i + "] =" + shuffledTracks.get(i));
                }
            }
        } else {
            if (currentQueueItemPosition > 0) {
                RecyclerView.ViewHolder viewHolder =  queueRecyclerView.findViewHolderForAdapterPosition(currentQueueItemPosition);
                final TextView currentTrackTV = (TextView)viewHolder.itemView.findViewById(R.id.trackTitleTextView);
                currentTrackTV.setTextColor(ContextCompat.getColor(this, R.color.colorAccent));
                currentQueueItemPosition--;
                playThePosition(currentQueueItemPosition);

            }
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void play(View view) {
        if (mediaPlayer == null) {
            playThePosition(currentQueueItemPosition);
            if (mediaPlayer.isPlaying()) {
                playPauseIcon.setImageResource(R.drawable.ic_pause_red);
                playPauseMode = true;
            }
        } else {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                playPauseIcon.setImageResource(R.drawable.ic_play_arrow_red);
                playPauseMode = false;
            } else {
                mediaPlayer.start();
                if (mediaPlayer.isPlaying()) {
                    playPauseIcon.setImageResource(R.drawable.ic_pause_red);
                    playPauseMode = true;
                }
            }
        }
    }

    public void playThePosition(int position){
        currentQueueItemPosition = position;
        Log.d("mylog", "currentQueueItemPosition = " + currentQueueItemPosition);
        if (mediaPlayer == null || !mediaPlayer.isPlaying()) {

            mediaPlayer = new MediaPlayer();
            mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);

            // mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            Log.d("mylog", "mediaplayer got the position # "+ currentQueueItemPosition);

            try {
                Log.d("mylog", "mediaplayer have to play this data"+ queueArrayList.get(currentQueueItemPosition).getData());

                mediaPlayer.setDataSource(queueArrayList.get(currentQueueItemPosition).getData());
                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        mediaPlayer.start();
                        Log.d("mylog", "mediaplayer started the position # "+ currentQueueItemPosition);
                        seekbar.setMax(mediaPlayer.getDuration());
                        new Timer().scheduleAtFixedRate(new TimerTask() {
                            @Override
                            public void run() {
                                MainActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (mediaPlayer.isPlaying()) {
                                            seekbar.setProgress(mediaPlayer.getCurrentPosition());
                                        }
                                    }
                                });


                                //   Log.d("trackProgress", "trackProgress = " + min + ":" + sec);
                                if (mediaPlayer.isPlaying()) {
                                    currentTrackPositionHandler.sendEmptyMessage(mediaPlayer.getCurrentPosition());
                                }
                            }
                        }, 0, 200);

                        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                            @Override
                            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                if (fromUser) {
                                    mediaPlayer.seekTo(progress);
                                }
                            }

                            @Override
                            public void onStartTrackingTouch(SeekBar seekBar) {

                            }

                            @Override
                            public void onStopTrackingTouch(SeekBar seekBar) {

                            }
                        });
                        playPauseIcon.setImageResource(R.drawable.ic_pause_red);
                    }
                });
                currentTrackTitle = queueArrayList.get(currentQueueItemPosition).getArtist() + "  " + queueArrayList.get(currentQueueItemPosition).getTitle();
              //  mediaPlayer.prepare();
              // mediaPlayer.start();
                RecyclerView.ViewHolder viewHolder =  queueRecyclerView.findViewHolderForAdapterPosition(currentQueueItemPosition);
                final TextView currentTrackTV = (TextView)viewHolder.itemView.findViewById(R.id.trackTitleTextView);
                currentTrackTV.setTextColor(ContextCompat.getColor(this, R.color.colorPointer));
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        currentTrackTV.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));

                        switch (loopMode) {
                            case 0:
                                    if (isShuffleModeOn) {
                                        if (shuffledTracks.size()>1) {
                                          //  currentTrackTV.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));

                                            currentQueueItemPosition = random.nextInt(shuffledTracks.size());
                                            shuffledTracks.remove(currentQueueItemPosition);
                                            playThePosition(currentQueueItemPosition);
                                        }

                                    } else {
                                        if (currentQueueItemPosition < queueArrayList.size()-1) {
                                         //   currentTrackTV.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));

                                            currentQueueItemPosition++;
                                            playThePosition(currentQueueItemPosition);
                                        } else {
                                            playPauseIcon.setImageResource(R.drawable.ic_play_arrow_red);
                                            playPauseMode = false;
                                        }
                                    }
                                    break;
                            case 1:
                                    playThePosition(currentQueueItemPosition);
                                    break;
                            case 2:
                                    if (isShuffleModeOn) {
                                        if (shuffledTracks.size()>1) {
                                            currentQueueItemPosition = random.nextInt(shuffledTracks.size());
                                            shuffledTracks.remove(currentQueueItemPosition);
                                            playThePosition(currentQueueItemPosition);
                                        } else {
                                            for (int i = 0; i< queueArrayList.size(); i++) shuffledTracks.add(i);
                                            currentQueueItemPosition = random.nextInt(shuffledTracks.size());
                                            shuffledTracks.remove(currentQueueItemPosition);
                                            playThePosition(currentQueueItemPosition);
                                        }

                                    } else {
                                        if (currentQueueItemPosition < queueArrayList.size()-1) {
                                            currentQueueItemPosition++;
                                            playThePosition(currentQueueItemPosition);
                                        } else {
                                            currentQueueItemPosition = 0;
                                            playThePosition(currentQueueItemPosition);
                                        }
                                    }
                                    break;
                        }
                    }

                });
                mediaPlayer.prepareAsync();



            } catch (IOException | IllegalStateException e) {
                e.printStackTrace();
            }
        } else {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);

            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

            try {
                mediaPlayer.setDataSource(queueArrayList.get(currentQueueItemPosition).getData());
                currentTrackTitle = queueArrayList.get(currentQueueItemPosition).getArtist() + "  " + queueArrayList.get(currentQueueItemPosition).getTitle();
                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        mediaPlayer.start();
                        playPauseIcon.setImageResource(R.drawable.ic_pause_red);
                        seekbar.setMax(mediaPlayer.getDuration());
                        new Timer().scheduleAtFixedRate(new TimerTask() {

                            @Override
                            public void run() {
                                MainActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (mediaPlayer.isPlaying()) {
                                            seekbar.setProgress(mediaPlayer.getCurrentPosition());
                                        }
                                    }
                                });
                                if (mediaPlayer.isPlaying()) {
                                    currentTrackPositionHandler.sendEmptyMessage(mediaPlayer.getCurrentPosition());
                                }

                            }
                        }, 0, 200);
                        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                            @Override
                            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                if (fromUser) {
                                    mediaPlayer.seekTo(progress);
                                }
                            }

                            @Override
                            public void onStartTrackingTouch(SeekBar seekBar) {

                            }

                            @Override
                            public void onStopTrackingTouch(SeekBar seekBar) {

                            }
                        });

                    }
                });
                RecyclerView.ViewHolder viewHolder =  queueRecyclerView.findViewHolderForAdapterPosition(currentQueueItemPosition);
                final TextView currentTrackTV = (TextView)viewHolder.itemView.findViewById(R.id.trackTitleTextView);
                currentTrackTV.setTextColor(ContextCompat.getColor(this, R.color.colorPointer));
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        currentTrackTV.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));

                        switch (loopMode) {
                            case 0:
                                if (isShuffleModeOn) {
                                    if (shuffledTracks.size()>1) {
                                     //   currentTrackTV.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));
                                        currentQueueItemPosition = random.nextInt(shuffledTracks.size());
                                        shuffledTracks.remove(currentQueueItemPosition);
                                        playThePosition(currentQueueItemPosition);
                                    }

                                } else {
                                    if (currentQueueItemPosition < queueArrayList.size()-1) {
                                     //   currentTrackTV.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));
                                        currentQueueItemPosition++;
                                        playThePosition(currentQueueItemPosition);
                                    } else playPauseIcon.setImageResource(R.drawable.ic_play_arrow_red);
                                }
                                break;
                            case 1:
                                playThePosition(currentQueueItemPosition);
                                break;
                            case 2:
                                if (isShuffleModeOn) {
                                    if (shuffledTracks.size()>1) {
                                  //      currentTrackTV.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));
                                        currentQueueItemPosition = random.nextInt(shuffledTracks.size());
                                        shuffledTracks.remove(currentQueueItemPosition);
                                        playThePosition(currentQueueItemPosition);
                                    } else {
                                        for (int i = 0; i< queueArrayList.size(); i++) shuffledTracks.add(i);
                                  //      currentTrackTV.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));
                                        currentQueueItemPosition = random.nextInt(shuffledTracks.size());
                                        shuffledTracks.remove(currentQueueItemPosition);
                                        playThePosition(currentQueueItemPosition);
                                    }


                                } else {
                                    if (currentQueueItemPosition < queueArrayList.size()-1) {
                                   //     currentTrackTV.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));
                                        currentQueueItemPosition++;
                                        playThePosition(currentQueueItemPosition);
                                    } else {
                                  //      currentTrackTV.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));
                                        currentQueueItemPosition = 0;
                                        playThePosition(currentQueueItemPosition);
                                    }
                                }
                                break;
                        }
                    }
                });
                mediaPlayer.prepareAsync();
                //   mediaPlayer.prepare();
                //  mediaPlayer.start();



            } catch (IOException | IllegalStateException e) {
                e.printStackTrace();
            }
        }

    }

    public void changeShuffleMode(View view) {
        shuffleImageView = findViewById(R.id.shuffleImageView);
        if (!isShuffleModeOn) {
            shuffleImageView.setImageResource(R.drawable.ic_shuffle_accent_24dp);
            isShuffleModeOn = true;
            shuffledTracks = new ArrayList<>();
            for (int i = 0; i< queueArrayList.size(); i++) {
                shuffledTracks.add(i);
                Log.d("shuffledTracks", "shuffled tracks[" + i + "] =" + shuffledTracks.get(i));

            }

        } else {
            shuffleImageView.setImageResource(R.drawable.ic_shuffle_accent_faded_24dp);
            isShuffleModeOn = false;
        }
    }

    public void changeLoopMode(View view) {
        switch (loopMode) {
            case 0:
                loopImageView.setImageResource(R.drawable.ic_repeat_one_red_24dp);
                loopMode = 1;
                break;
            case 1:
                loopImageView.setImageResource(R.drawable.ic_repeat_red_24dp);
                loopMode = 2;
                for (int i = 0; i< queueArrayList.size(); i++) {
                    shuffledTracks.add(i);
                    Log.d("shuffledTracks", "shuffled tracks[" + i + "] =" + shuffledTracks.get(i));

                }
                break;
            case 2:
                loopImageView.setImageResource(R.drawable.ic_repeat_red_faded_24dp);
                loopMode = 0;
                break;


        }
    }

    public void choosePlaylist(View view) {
        choosePlaylistLinearLayout = findViewById(R.id.choosePlaylistLinearLayout);

        if (!choosePlaylistIsVisible) {
            choosePlaylistLinearLayout.setVisibility(View.VISIBLE);
            choosePlaylistIsVisible = true;
            choosePlaylistRecyclerView = findViewById(R.id.choosePlaylistRecyclerView);

            final ArrayList<Playlist> playlists = new ArrayList<>();
            new GetAllPlaylistsAsyncTask().execute(playlists);
            Log.d("mylog", "playlists.size() = " + playlists.size());
        }
        else {
            choosePlaylistLinearLayout.setVisibility(View.INVISIBLE);
            choosePlaylistIsVisible = false;
        }

    }

    private void createQueue(int position, ArrayList<Playlist> playlists) {
        queueArrayList = playlists.get(position).getTracks();
        if (queueLayoutManager == null) queueLayoutManager = new LinearLayoutManager(this);
        queueAdapter = new TrackAdapter(queueArrayList, new TrackAdapter.OnTrackTouchListener() {
            @Override
            public void onTrackTouch(View v, MotionEvent event, int position) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: // нажатие
                        startx = event.getRawX();
                        dX = v.getX() - event.getRawX();

                        break;
                    case MotionEvent.ACTION_MOVE: // движение
                        if (event.getRawX() > startx) {
                            v.animate()
                                    .x(event.getRawX() + dX)
                                    .setDuration(0)
                                    .start();
                        }
                        break;
                    case MotionEvent.ACTION_UP: // отпускание
                        stopx = event.getRawX();
                        Log.d("mylog", "startx = " + startx + "  stopx = " + stopx);
                        //   if (stopx < startx) break;

                        if (stopx - startx >  150 && stopx!=0) {
                            Log.d("mylog", "removed: " + queueArrayList.get(position).getArtist() + "  " + queueArrayList.get(position).getTitle());
                            queueArrayList.remove(position);
                            queueRecyclerView.setAdapter(queueAdapter);

                        } else if (stopx != startx) {
                            v.animate()
                                    .x(0)
                                    .setDuration(0)
                                    .start();
                        }
                        else  {


                            Log.d("mylog", "playtheposition");
                            Log.d("mylog", "prevQueueItemPosition = " + currentQueueItemPosition);

                               RecyclerView.ViewHolder prevViewHolder =  queueRecyclerView.findViewHolderForAdapterPosition(currentQueueItemPosition);
                               TextView prevTrackTV = prevViewHolder.itemView.findViewById(R.id.trackTitleTextView);
                               prevTrackTV.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));

                            playThePosition(position);
                            currentQueueItemPosition = position;


                        }
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        Log.d("mylog", "action canceled");

                        v.animate()
                                .x(0)
                                .setDuration(0)
                                .start();
                        break;
                }
            }
        });
        queueRecyclerView.setLayoutManager(queueLayoutManager);
        queueRecyclerView.setAdapter(queueAdapter);
        /*choosePlaylistLinearLayout.setVisibility(View.INVISIBLE);
        choosePlaylistIsVisible = false;*/
        currentQueueItemPosition = 0;
        TextView queueTitleTextView = findViewById(R.id.queueTitleTextView);
        queueTitleTextView.setText(playlists.get(position).getPlaylistTitle());
        Log.d("title", playlists.get(position).getPlaylistTitle());
    }

    private void deletePlaylist(Playlist playlist) {

        new DeletePlaylistAsyncTask().execute(playlist);
        choosePlaylistAdapter.notifyDataSetChanged();
    }

    private void editPlaylist(Playlist playlist) {
        Intent intent = new Intent(MainActivity.this, CreatePlaylistActivity.class);
        intent.putExtra("playlistId", playlist.getId());
        Log.d("mylog", "playlistId sent = " + playlist.getId());
        startActivity(intent);
    }

    private void addPlaylistToQueue(int position, ArrayList<Playlist> playlists) {
        Playlist pl = playlists.get(position);
        for (Track track : pl.getTracks()) {
            queueArrayList.add(track);
        }
        queueAdapter.notifyDataSetChanged();
        TextView queueTitleTextView = findViewById(R.id.queueTitleTextView);
        queueTitleTextView.setText("Mixed playlists");
        shuffledTracks = null;
        shuffledTracks = new ArrayList<>();
        for(int i = 0; i<queueArrayList.size();i++){
            shuffledTracks.add(i);
            Log.d("shuffledTracks", "shuffled tracks[" + i + "] =" + shuffledTracks.get(i));
        }

    }

    public void addPlaylist(View view) {
        Intent intent = new Intent(MainActivity.this, CreatePlaylistActivity.class);
        startActivity(intent);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        /*mainActivityState = new MainActivityState();
        mainActivityState.setPlayPauseMode(playPauseMode);
        mainActivityState.setLoopMode(loopMode);
        TextView queueTitleTextView = findViewById(R.id.queueTitleTextView);
        mainActivityState.setQueueTitle(queueTitleTextView.getText().toString());
        mainActivityState.setQueueCurrentPosition(currentQueueItemPosition);
        mainActivityState.setQueueArrayList(queueArrayList);
        mainActivityState.setChoosePlaylistRecyclerViewIsVisible(choosePlaylistIsVisible);
        mainActivityState.setShuffleMode(isShuffleModeOn);*/
      //  saveMainActivityState();
       // Log.d("mylog", "converterFromMainActivityState = " + converterFromMainActivityState(mainActivityState));
    }

    public MainActivityState getMainActivityState() {
        MainActivityState mainActivityState = new MainActivityState();
        String s = readFromFile(this);
        if (s != null || s.length()>0) {
            mainActivityState = converterToMainActivityState(s);
            return mainActivityState;
        } else return null;
    }

    public void saveMainActivityState(){
        MainActivityState mainActivityState = new MainActivityState();
        mainActivityState.setPlayPauseMode(playPauseMode);
        mainActivityState.setLoopMode(loopMode);
        TextView queueTitleTextView = findViewById(R.id.queueTitleTextView);
        mainActivityState.setQueueTitle(queueTitleTextView.getText().toString());
        mainActivityState.setQueueCurrentPosition(currentQueueItemPosition);
        mainActivityState.setQueueArrayList(queueArrayList);
        mainActivityState.setChoosePlaylistRecyclerViewIsVisible(choosePlaylistIsVisible);
        mainActivityState.setShuffleMode(isShuffleModeOn);

        String s = converterFromMainActivityState(mainActivityState);
        writeToFile(this, s);
    }
    public void applyMainActivityState(MainActivityState mainActivityState){
        isShuffleModeOn = mainActivityState.getShuffleMode();
        shuffleImageView = findViewById(R.id.shuffleImageView);
        if (isShuffleModeOn) {
            shuffleImageView.setImageResource(R.drawable.ic_shuffle_accent_24dp);
        } else {
            shuffleImageView.setImageResource(R.drawable.ic_shuffle_accent_faded_24dp);
        }
        loopMode = mainActivityState.getLoopMode();
        loopImageView = findViewById(R.id.loopImageView);
        switch (loopMode){
            case 0: loopImageView.setImageResource(R.drawable.ic_repeat_red_faded_24dp);
            case 1: loopImageView.setImageResource(R.drawable.ic_repeat_one_red_24dp);
            case 2: loopImageView.setImageResource(R.drawable.ic_repeat_red_24dp);
        }
        playPauseIcon = findViewById(R.id.imageViewPlay);
        if (mainActivityState.getPlayPauseMode()){
            playPauseIcon.setImageResource(R.drawable.ic_play_arrow_red);
        } else {
            playPauseIcon.setImageResource(R.drawable.ic_pause_red);

        }
        currentQueueItemPosition = mainActivityState.getQueueCurrentPosition();
        queueArrayList = mainActivityState.getQueueArrayList();
        queueAdapter = new TrackAdapter(queueArrayList, new TrackAdapter.OnTrackTouchListener() {
            @Override
            public void onTrackTouch(View v, MotionEvent event, int position) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: // нажатие
                        startx = event.getRawX();
                        dX = v.getX() - event.getRawX();

                        break;
                    case MotionEvent.ACTION_MOVE: // движение
                        if (event.getRawX() > startx) {
                            v.animate()
                                    .x(event.getRawX() + dX)
                                    .setDuration(0)
                                    .start();
                        }
                        break;
                    case MotionEvent.ACTION_UP: // отпускание
                        stopx = event.getRawX();
                        Log.d("mylog", "startx = " + startx + "  stopx = " + stopx);
                        //   if (stopx < startx) break;

                        if (stopx - startx >  150 && stopx!=0) {
                            Log.d("mylog", "removed: " + queueArrayList.get(position).getArtist() + "  " + queueArrayList.get(position).getTitle());
                            queueArrayList.remove(position);
                            queueRecyclerView.setAdapter(queueAdapter);

                        } else if (stopx != startx) {
                            v.animate()
                                    .x(0)
                                    .setDuration(0)
                                    .start();
                        }
                        else  {
                            Log.d("mylog", "playtheposition");
                            currentQueueItemPosition = position;
                            playThePosition(position);

                        }
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        Log.d("mylog", "action canceled");

                        v.animate()
                                .x(0)
                                .setDuration(0)
                                .start();
                        break;
                }

            }
        });
        queueRecyclerView.setLayoutManager(queueLayoutManager);
        queueRecyclerView.setAdapter(queueAdapter);
        TextView queueTitleTextView = findViewById(R.id.queueTitleTextView);
        queueTitleTextView.setText(mainActivityState.getQueueTitle());
        choosePlaylistRecyclerView = findViewById(R.id.choosePlaylistRecyclerView);
        if (mainActivityState.getChoosePlaylistRecyclerViewIsVisible()){
            choosePlaylistRecyclerView.setVisibility(View.VISIBLE);
        } else {
            choosePlaylistRecyclerView.setVisibility(View.INVISIBLE);
        }

    }

    public String converterFromMainActivityState(MainActivityState mainActivityState){
        String s;
        s ="" + mainActivityState.getShuffleMode() + ">>" +
                mainActivityState.getLoopMode() + ">>" +
                mainActivityState.getPlayPauseMode()+ ">>" +
                mainActivityState.getChoosePlaylistRecyclerViewIsVisible()+ ">>" +
                converterFromTracks(mainActivityState.getQueueArrayList()) + ">>" +
                mainActivityState.getQueueCurrentPosition() + ">>" +
                mainActivityState.getQueueTitle();
        return s;
    }

    public MainActivityState converterToMainActivityState (String s) {
        MainActivityState mainActivityState = new MainActivityState();
        String[] fields = s.split(">>");
        for (int i=0;i<7;i++){
            switch (i){
                case 0: mainActivityState.setShuffleMode(Boolean.valueOf(fields[i]));break;
                case 1: mainActivityState.setLoopMode(Integer.parseInt(fields[i]));break;
                case 2: mainActivityState.setPlayPauseMode(Boolean.valueOf(fields[i]));
                case 3: mainActivityState.setChoosePlaylistRecyclerViewIsVisible(Boolean.valueOf(fields[i]));break;
                case 4: mainActivityState.setQueueArrayList(converterToTracks(fields[i]));break;
                case 5: mainActivityState.setQueueCurrentPosition(Integer.parseInt(fields[i]));break;
                case 6: mainActivityState.setQueueTitle(fields[i]);break;

            }
        }
        return mainActivityState;
    }

    public ArrayList<Track> converterToTracks(String s){
        ArrayList<Track> arrayList = new ArrayList<>();
        for (String parts : s.split(",")){
            String[] trackString = parts.split("%>");
            Track track = new Track();
            for (int i = 0; i< 4; i++){
                switch (i) {
                    case 0: track.setArtist(trackString[i]); break;
                    case 1: track.setTitle(trackString[i]); break;
                    case 2: track.setDuration(trackString[i]);break;
                    case 3: track.setData(trackString[i]);break;
                }
            }
            arrayList.add(track);

        }
        return arrayList;
    }

    public String converterFromTracks(ArrayList<Track> tracks){
        String s = "";
        for (int i=0; i < tracks.size(); i++){
            s=s+tracks.get(i).getArtist()+"%>"+tracks.get(i).getTitle()+"%>"+tracks.get(i).getDuration()+"%>"+
                    tracks.get(i).getData()+",";
        }
        return s;
    }

    private void writeToFile(Context context, String data) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(fileName, Context.MODE_PRIVATE));

            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    private String readFromFile(Context context) {

        String ret = "";

        try {
            InputStream inputStream = context.openFileInput(fileName);

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append("\n").append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        return ret;
    }

    private class DeletePlaylistAsyncTask extends AsyncTask<Playlist, Void, Void> {

        @Override
        protected Void doInBackground(Playlist... playlists) {
            playlistsDatabase.getPlaylistDAO().deletePlaylist(playlists[0]);
            return null;
        }
    }

    private class GetAllPlaylistsAsyncTask extends AsyncTask<ArrayList<Playlist>,Void,ArrayList<Playlist>>{

        @Override
        protected ArrayList<Playlist> doInBackground(ArrayList<Playlist>... arrayLists) {
            long[] playlistIds = playlistsDatabase.getPlaylistDAO().getAllPlaylistId();
            for(long id : playlistIds){
                arrayLists[0].add(playlistsDatabase.getPlaylistDAO().getPlaylist(id));
            }
            return arrayLists[0];
        }

        @Override
        protected void onPostExecute(final ArrayList<Playlist> _playlists) {
            super.onPostExecute(_playlists);
            choosePlaylistAdapter = new PlaylistAdapter(_playlists, new PlaylistAdapter.OnPlaylistClickListener() {
                @Override
                public void onPlaylistClick(int position) {
                    createQueue(position, _playlists);
                }
            }, new PlaylistAdapter.OnAddPlaylistClickListener() {
                @Override
                public void onAddPlaylistClick(int position) {
                    addPlaylistToQueue(position, _playlists);
                }
            }, new PlaylistAdapter.OnEditPlaylistClickListener() {
                @Override
                public void onEditPlaylistClick(int position) {
                    editPlaylist(_playlists.get(position));
                }
            }, new PlaylistAdapter.OnDeletePlaylistClickListener() {
                @Override
                public void onDeletePlaylistClick(int position) {

                    deletePlaylist(_playlists.get(position));
                    _playlists.remove(position);

                }
            });
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
            choosePlaylistRecyclerView.setLayoutManager(layoutManager);
            choosePlaylistRecyclerView.setAdapter(choosePlaylistAdapter);
        }

    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        playPauseMode = false;
       // saveMainActivityState();
    }
}
