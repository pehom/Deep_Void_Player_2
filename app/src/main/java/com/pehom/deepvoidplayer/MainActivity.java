package com.pehom.deepvoidplayer;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    private static final int MY_PERMISSION_REQUEST = 1;
    private MediaPlayer mediaPlayer;
    private ImageView playPauseIcon;
    private SeekBar seekbar;
    private ArrayList<Track> playlistArrayList;
    private RecyclerView.LayoutManager trackLayoutManager;
    private RecyclerView playlistRecyclerView;
    private TextView currentTrackTextView;
    private TrackAdapter playlistAdapter;
    private Handler currentTrackPositionHandler;

    private int currentPlaylistItemPosition = 0;
    private String currentTrackProgress;
    private LinearLayout choosePlaylistLinearLayout;
    private ImageView shuffleImageView;
    private boolean isShuffleModeOn = false;
    private ArrayList<Integer> shuffledTracks;
    private ImageView loopImageView;
    private TextView loopModeTextView;
    private int loopMode = 0;
    Random random;
    boolean choosePlaylistLinearLayoutIsVisible =  false;
    private  float startx, stopx;
    private float dX, dY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        random = new Random();
        setContentView(R.layout.alternative_activity_main);
        playPauseIcon = findViewById(R.id.imageViewPlay);
        currentTrackTextView = findViewById(R.id.currentTrackTextView);
        seekbar = findViewById(R.id.seekBar);
        loopImageView = findViewById(R.id.loopImageView);
        loopModeTextView = findViewById(R.id.loopModeTextView);
        loopModeTextView.setVisibility(View.INVISIBLE);
        playlistArrayList = new ArrayList<Track>();
        currentTrackPositionHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                int min = msg.what/1000/60;
                int sec = msg.what/1000 - min*60;
                if (sec < 10) {currentTrackProgress = ""+ min + ":0" + sec;}
                else {currentTrackProgress = ""+ min + ":" + sec;}
                currentTrackTextView.setText(playlistArrayList.get(currentPlaylistItemPosition).getArtist()
                        + " - " + playlistArrayList.get(currentPlaylistItemPosition).getTitle() + "  " + currentTrackProgress);

            }
        };

        if (ContextCompat.checkSelfPermission(
       MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
          if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                 Manifest.permission.READ_EXTERNAL_STORAGE)){
             ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSION_REQUEST);
          }else {
             ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSION_REQUEST);

         }

        } else {
            doStuff();
        }



    }

    public void doStuff(){
        getMusic();
        playlistRecyclerView = findViewById(R.id.playlistRecyclerView);
       // playlistRecyclerView.setHasFixedSize(true);

        trackLayoutManager = new LinearLayoutManager(this);
        playlistAdapter = new TrackAdapter(playlistArrayList, new TrackAdapter.OnTrackTouchListener() {
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
                            Log.d("mylog", "removed: " +playlistArrayList.get(position).getArtist() + "  " + playlistArrayList.get(position).getTitle());
                            playlistArrayList.remove(position);
                            playlistRecyclerView.setAdapter(playlistAdapter);

                        } else if (stopx != startx) {
                            v.animate()
                                    .x(0)
                                    .setDuration(0)
                                    .start();
                        }
                        else  {
                            Log.d("mylog", "playtheposition");
                            currentPlaylistItemPosition = position;
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
        playlistRecyclerView.setLayoutManager(trackLayoutManager);
        playlistRecyclerView.setAdapter(playlistAdapter);

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
                playlistArrayList.add(currentTrack);

            } while (cursor.moveToNext());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,  String[] permissions,  int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUEST: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(MainActivity.this,
                            Manifest.permission.READ_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED) {
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
                Random random = new Random();
                currentPlaylistItemPosition = random.nextInt(shuffledTracks.size());
                shuffledTracks.remove(currentPlaylistItemPosition);
                playThePosition(currentPlaylistItemPosition);
            }


        } else {
            if (currentPlaylistItemPosition < playlistArrayList.size()-1) {
                currentPlaylistItemPosition++;
                playThePosition(currentPlaylistItemPosition);
            } else playPauseIcon.setImageResource(R.drawable.ic_play_arrow_red);
        }
    }

    public void prevTrack(View view) {
        if (isShuffleModeOn) {
            if (shuffledTracks.size()>1) {
                Random random = new Random();
                currentPlaylistItemPosition = random.nextInt(shuffledTracks.size());
                shuffledTracks.remove(currentPlaylistItemPosition);
                playThePosition(currentPlaylistItemPosition);
            }


        } else {
            if (currentPlaylistItemPosition > 0) {
                currentPlaylistItemPosition--;
                playThePosition(currentPlaylistItemPosition);

            }
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void play(View view) {
        if (mediaPlayer == null) {
            playThePosition(currentPlaylistItemPosition);
            playPauseIcon.setImageResource(R.drawable.ic_pause_red);
        } else {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                playPauseIcon.setImageResource(R.drawable.ic_play_arrow_red);
            } else {
                mediaPlayer.start();
                playPauseIcon.setImageResource(R.drawable.ic_pause_red);
            }
        }
    }

    public void playThePosition(int position){
        currentPlaylistItemPosition = position;
        if (mediaPlayer == null || !mediaPlayer.isPlaying()) {
            mediaPlayer = new MediaPlayer();

            try {
                mediaPlayer.setDataSource(playlistArrayList.get(position).getData());
                mediaPlayer.prepare();
                mediaPlayer.start();
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        switch (loopMode) {
                            case 0:
                                    if (isShuffleModeOn) {
                                        if (shuffledTracks.size()>1) {
                                            currentPlaylistItemPosition = random.nextInt(shuffledTracks.size());
                                            shuffledTracks.remove(currentPlaylistItemPosition);
                                            playThePosition(currentPlaylistItemPosition);
                                        }

                                    } else {
                                        if (currentPlaylistItemPosition < playlistArrayList.size()-1) {
                                            currentPlaylistItemPosition++;
                                            playThePosition(currentPlaylistItemPosition);
                                        } else playPauseIcon.setImageResource(R.drawable.ic_play_arrow_red);
                                    }
                                    break;
                            case 1:
                                    playThePosition(currentPlaylistItemPosition);
                                    break;
                            case 2:
                                    if (isShuffleModeOn) {
                                        if (shuffledTracks.size()>1) {
                                            currentPlaylistItemPosition = random.nextInt(shuffledTracks.size());
                                            shuffledTracks.remove(currentPlaylistItemPosition);
                                            playThePosition(currentPlaylistItemPosition);
                                        } else {
                                            for (int i=0;i<playlistArrayList.size();i++) shuffledTracks.add(i);
                                            currentPlaylistItemPosition = random.nextInt(shuffledTracks.size());
                                            shuffledTracks.remove(currentPlaylistItemPosition);
                                            playThePosition(currentPlaylistItemPosition);
                                        }


                                    } else {
                                        if (currentPlaylistItemPosition < playlistArrayList.size()-1) {
                                            currentPlaylistItemPosition++;
                                            playThePosition(currentPlaylistItemPosition);
                                        } else {
                                            currentPlaylistItemPosition = 0;
                                            playThePosition(currentPlaylistItemPosition);
                                        }
                                    }
                                    break;
                        }
                    }

                });
                seekbar.setMax(mediaPlayer.getDuration());
                new Timer().scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        seekbar.setProgress(mediaPlayer.getCurrentPosition());

                     //   Log.d("trackProgress", "trackProgress = " + min + ":" + sec);
                        currentTrackPositionHandler.sendEmptyMessage(mediaPlayer.getCurrentPosition());
                    }
                }, 0, 1000);

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

            } catch (IOException e) {
                e.printStackTrace();
            }

         //   currentTrackTextView.setText(playlistArrayList.get(position).getArtist() + " - " + playlistArrayList.get(position).getTitle());
        } else {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = new MediaPlayer();
            try {
                mediaPlayer.setDataSource(playlistArrayList.get(position).getData());
                mediaPlayer.prepare();
                mediaPlayer.start();
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        switch (loopMode) {
                            case 0:
                                if (isShuffleModeOn) {
                                    if (shuffledTracks.size()>1) {
                                        currentPlaylistItemPosition = random.nextInt(shuffledTracks.size());
                                        shuffledTracks.remove(currentPlaylistItemPosition);
                                        playThePosition(currentPlaylistItemPosition);
                                    }

                                } else {
                                    if (currentPlaylistItemPosition < playlistArrayList.size()-1) {
                                        currentPlaylistItemPosition++;
                                        playThePosition(currentPlaylistItemPosition);
                                    } else playPauseIcon.setImageResource(R.drawable.ic_play_arrow_red);
                                }
                                break;
                            case 1:
                                playThePosition(currentPlaylistItemPosition);
                                break;
                            case 2:
                                if (isShuffleModeOn) {
                                    if (shuffledTracks.size()>1) {
                                        currentPlaylistItemPosition = random.nextInt(shuffledTracks.size());
                                        shuffledTracks.remove(currentPlaylistItemPosition);
                                        playThePosition(currentPlaylistItemPosition);
                                    } else {
                                        for (int i=0;i<playlistArrayList.size();i++) shuffledTracks.add(i);
                                        currentPlaylistItemPosition = random.nextInt(shuffledTracks.size());
                                        shuffledTracks.remove(currentPlaylistItemPosition);
                                        playThePosition(currentPlaylistItemPosition);
                                    }


                                } else {
                                    if (currentPlaylistItemPosition < playlistArrayList.size()-1) {
                                        currentPlaylistItemPosition++;
                                        playThePosition(currentPlaylistItemPosition);
                                    } else {
                                        currentPlaylistItemPosition = 0;
                                        playThePosition(currentPlaylistItemPosition);
                                    }
                                }
                                break;
                        }
                    }
                });
                playPauseIcon.setImageResource(R.drawable.ic_pause_red);
                seekbar.setMax(mediaPlayer.getDuration());
                new Timer().scheduleAtFixedRate(new TimerTask() {

                    @Override
                    public void run() {
                        seekbar.setProgress(mediaPlayer.getCurrentPosition());
                        currentTrackPositionHandler.sendEmptyMessage(mediaPlayer.getCurrentPosition());

                    }
                }, 0, 1000);
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

            } catch (IOException e) {
                e.printStackTrace();
            }
          //  currentTrackTextView.setText(playlistArrayList.get(position).getArtist() + " - " + playlistArrayList.get(position).getTitle());
        }

    }

    public void changeShuffleMode(View view) {
        shuffleImageView = findViewById(R.id.shuffleImageView);
        if (!isShuffleModeOn) {
            shuffleImageView.setImageResource(R.drawable.ic_shuffle_accent_24dp);
            isShuffleModeOn = true;
            shuffledTracks = new ArrayList<>();
            for (int i=0;i<playlistArrayList.size();i++) shuffledTracks.add(i);

        } else {
            shuffleImageView.setImageResource(R.drawable.ic_shuffle_accent_faded_24dp);
            isShuffleModeOn = false;
        }
    }

    public void changeLoopMode(View view) {
        switch (loopMode) {
            case 0:
                loopImageView.setImageResource(R.drawable.ic_repeat_one_red_24dp);
              //  loopModeTextView.setVisibility(View.VISIBLE);
              // loopModeTextView.setText("track");
                loopMode = 1;
                break;
            case 1:
                loopImageView.setImageResource(R.drawable.ic_repeat_red_24dp);
             //   loopModeTextView.setVisibility(View.VISIBLE);
             //   loopModeTextView.setText("playlist");
                loopMode = 2;
                break;
            case 2:
                loopImageView.setImageResource(R.drawable.ic_repeat_red_faded_24dp);
                loopModeTextView.setVisibility(View.INVISIBLE);
                loopModeTextView.setText("");
                loopMode = 0;
                break;


        }
    }

    public void choosePlaylist(View view) {
        choosePlaylistLinearLayout = findViewById(R.id.choosePlaylistLinearLayout);
        if (!choosePlaylistLinearLayoutIsVisible) {
            playlistRecyclerView.setVisibility(View.VISIBLE);
            choosePlaylistLinearLayoutIsVisible = true;
        }
        else {
            playlistRecyclerView.setVisibility(View.INVISIBLE);
            choosePlaylistLinearLayoutIsVisible = false;
        }

    }

    public void createPlaylist(View view) {
        Intent intent = new Intent(MainActivity.this, CreatePlaylistActivity.class);
        startActivity(intent);
    }
}
