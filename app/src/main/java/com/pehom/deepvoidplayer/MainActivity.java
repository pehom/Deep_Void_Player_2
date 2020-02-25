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
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
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
    private ArrayList<Track> queueArrayList;
    private RecyclerView.LayoutManager queueLayoutManager;
    private RecyclerView queueRecyclerView;
    private RecyclerView choosePlaylistRecyclerView;
    private PlaylistAdapter choosePlaylistAdapter;
    private TextView currentTrackTextView;
    private TrackAdapter queueAdapter;
    private Handler currentTrackPositionHandler;

    private int currentQueueItemPosition = 0;
    private String currentTrackProgress;
    private String currentTrackTitle;
    private LinearLayout choosePlaylistLinearLayout;
    private ImageView shuffleImageView;
    private boolean isShuffleModeOn = false;
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
        playPauseIcon = findViewById(R.id.imageViewPlay);
        currentTrackTextView = findViewById(R.id.currentTrackTextView);
        seekbar = findViewById(R.id.seekBar);
        loopImageView = findViewById(R.id.loopImageView);
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
                currentQueueItemPosition = random.nextInt(shuffledTracks.size());
                shuffledTracks.remove(currentQueueItemPosition);
                playThePosition(currentQueueItemPosition);
            }


        } else {
            if (currentQueueItemPosition < queueArrayList.size()-1) {
                currentQueueItemPosition++;
                playThePosition(currentQueueItemPosition);
            } else playPauseIcon.setImageResource(R.drawable.ic_play_arrow_red);
        }
    }

    public void prevTrack(View view) {
        if (isShuffleModeOn) {
            if (shuffledTracks.size()>1) {
                Random random = new Random();
                currentQueueItemPosition = random.nextInt(shuffledTracks.size());
                shuffledTracks.remove(currentQueueItemPosition);
                playThePosition(currentQueueItemPosition);
            }


        } else {
            if (currentQueueItemPosition > 0) {
                currentQueueItemPosition--;
                playThePosition(currentQueueItemPosition);

            }
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void play(View view) {
        if (mediaPlayer == null) {
            playThePosition(currentQueueItemPosition);
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
        currentQueueItemPosition = position;
        if (mediaPlayer == null || !mediaPlayer.isPlaying()) {
            mediaPlayer = new MediaPlayer();

            try {
                mediaPlayer.setDataSource(queueArrayList.get(position).getData());
                currentTrackTitle = queueArrayList.get(position).getArtist() + "  " + queueArrayList.get(position).getTitle();
                mediaPlayer.prepare();
                mediaPlayer.start();
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        switch (loopMode) {
                            case 0:
                                    if (isShuffleModeOn) {
                                        if (shuffledTracks.size()>1) {
                                            currentQueueItemPosition = random.nextInt(shuffledTracks.size());
                                            shuffledTracks.remove(currentQueueItemPosition);
                                            playThePosition(currentQueueItemPosition);
                                        }

                                    } else {
                                        if (currentQueueItemPosition < queueArrayList.size()-1) {
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

         //   currentTrackTextView.setText(queueArrayList.get(position).getArtist() + " - " + queueArrayList.get(position).getTitle());
        } else {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = new MediaPlayer();
            try {
                mediaPlayer.setDataSource(queueArrayList.get(position).getData());
                currentTrackTitle = queueArrayList.get(position).getArtist() + "  " + queueArrayList.get(position).getTitle();

                mediaPlayer.prepare();
                mediaPlayer.start();
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        switch (loopMode) {
                            case 0:
                                if (isShuffleModeOn) {
                                    if (shuffledTracks.size()>1) {
                                        currentQueueItemPosition = random.nextInt(shuffledTracks.size());
                                        shuffledTracks.remove(currentQueueItemPosition);
                                        playThePosition(currentQueueItemPosition);
                                    }

                                } else {
                                    if (currentQueueItemPosition < queueArrayList.size()-1) {
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
          //  currentTrackTextView.setText(queueArrayList.get(position).getArtist() + " - " + queueArrayList.get(position).getTitle());
        }

    }

    public void changeShuffleMode(View view) {
        shuffleImageView = findViewById(R.id.shuffleImageView);
        if (!isShuffleModeOn) {
            shuffleImageView.setImageResource(R.drawable.ic_shuffle_accent_24dp);
            isShuffleModeOn = true;
            shuffledTracks = new ArrayList<>();
            for (int i = 0; i< queueArrayList.size(); i++) shuffledTracks.add(i);

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
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
            PlaylistsDatabase playlistsDatabase = Room.databaseBuilder(getApplicationContext(),
                    PlaylistsDatabase.class, "PlaylistsDB").allowMainThreadQueries().build();
            final long[] playlistIds = playlistsDatabase.getPlaylistDAO().getAllPlaylistId();
            final ArrayList<Playlist> playlists = new ArrayList<>();
            for (long id : playlistIds){
                playlists.add(playlistsDatabase.getPlaylistDAO().getPlaylist(id));
            }
            choosePlaylistAdapter = new PlaylistAdapter(playlists, new PlaylistAdapter.OnPlaylistClickListener() {
                @Override
                public void onPlaylistClick(int position) {
                    createQueue(position, playlists);
                }
            }, new PlaylistAdapter.OnAddPlaylistClickListener() {
                @Override
                public void onAddPlaylistClick(int position) {
                    addPlaylistToQueue(position, playlists);
                }
            }, new PlaylistAdapter.OnEditPlaylistClickListener() {
                @Override
                public void onEditPlaylistClick(int position) {
                    editPlaylist(playlistIds[position]);
                }
            }, new PlaylistAdapter.OnDeletePlaylistClickListener() {
                @Override
                public void onDeletePlaylistClick(int position) {
                    deletePlaylist(position, playlists, playlistIds);
                }
            });
            choosePlaylistRecyclerView.setLayoutManager(layoutManager);
            choosePlaylistRecyclerView.setAdapter(choosePlaylistAdapter);

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
        choosePlaylistLinearLayout.setVisibility(View.INVISIBLE);
        choosePlaylistIsVisible = false;
        currentQueueItemPosition = 0;
        TextView queueTitleTextView = findViewById(R.id.queueTitleTextView);
        queueTitleTextView.setText(playlists.get(position).getPlaylistTitle());
        Log.d("title", playlists.get(position).getPlaylistTitle());
    }

    private void deletePlaylist(int position, final ArrayList<Playlist> playlists, final long[] playlistIds) {
        PlaylistsDatabase playlistsDatabase = Room.databaseBuilder(getApplicationContext(),
                PlaylistsDatabase.class, "PlaylistsDB").allowMainThreadQueries().build();
        playlistsDatabase.getPlaylistDAO().deletePlaylist(playlists.get(position));
        playlists.remove(position);
        choosePlaylistAdapter.notifyDataSetChanged();
    }

    private void editPlaylist(long playlistId) {
        Intent intent = new Intent(MainActivity.this, CreatePlaylistActivity.class);
        intent.putExtra("playlistId", playlistId);
        Log.d("mylog", "playlistId sent = " + playlistId);
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

    }

    public void addPlaylist(View view) {
        Intent intent = new Intent(MainActivity.this, CreatePlaylistActivity.class);
        startActivity(intent);
    }

    /*@Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.p
    }*/
}
