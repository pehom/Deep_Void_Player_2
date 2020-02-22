package com.pehom.deepvoidplayer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;

public class CreatePlaylistActivity extends AppCompatActivity {
    private RecyclerView chosenTracksRecyclerView;
    private RecyclerView tracksToSelectRecyclerView;
    private EditText playlistTitleEditText;
    private ArrayList<Artist> artists;
    private ArrayList<Track> thisArtistTracksArrayList;
    private ArrayList<Track> newPlaylistArrayList;
    private TrackAdapter newPlaylistTrackAdapter;

    private LinearLayoutManager artistsLayoutManager;
    private ArtistsAdapter artistsAdapter;
    private float startx, stopx;
    private Random random;
    private float dX;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_playlist);
        random = new Random();
        chosenTracksRecyclerView = findViewById(R.id.chosenTracks);
       /* newPlaylistArrayList = new ArrayList<>();
        RecyclerView.LayoutManager trackLayoutManager = new LinearLayoutManager(this);
        newPlaylistTrackAdapter = new TrackAdapter(newPlaylistArrayList, new TrackAdapter.OnTrackTouchListener() {
            @Override
            public void onTrackTouch(View v, MotionEvent event, int position) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: // нажатие
                        startx = event.getX();
                        break;
                    case MotionEvent.ACTION_MOVE: // движение

                        break;
                    case MotionEvent.ACTION_UP: // отпускание
                        stopx = event.getX();
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        stopx = startx;
                        break;
                }
                if (stopx - startx >  80 && stopx!=0) {
                    Log.d("mylog", "startx = " + startx + "  stopx = " + stopx);
                    newPlaylistArrayList.remove(position);
                    chosenTracksRecyclerView.setAdapter(newPlaylistTrackAdapter);

                    startx=0;
                    stopx=0;
                }
            }
        });
        chosenTracksRecyclerView.setLayoutManager(trackLayoutManager);
        chosenTracksRecyclerView.setAdapter(newPlaylistTrackAdapter);*/

        showArtists();

    }

    private void showArtists() {
        artists = new ArrayList<>();

        ContentResolver mContentResolver = getContentResolver();

        String[] mProjection =
                {

                        MediaStore.Audio.Artists.ARTIST,
                        MediaStore.Audio.Artists.NUMBER_OF_TRACKS,
                        MediaStore.Audio.Artists.ARTIST_KEY
                };

        Cursor artistCursor = mContentResolver.query(
                MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI,
                mProjection,
                null,
                null,
                MediaStore.Audio.Artists.ARTIST + " ASC");

        if (artistCursor != null && artistCursor.moveToFirst()) {

            do {
                String currentArtistName = artistCursor.getString(artistCursor.getColumnIndex(MediaStore.Audio.Artists.ARTIST));
                String numberOfTracks = artistCursor.getString(artistCursor.getColumnIndex(MediaStore.Audio.Artists.NUMBER_OF_TRACKS));
                String artistKey = artistCursor.getString(artistCursor.getColumnIndex(MediaStore.Audio.Artists.ARTIST_KEY));
                Artist currentArtist  = new Artist(currentArtistName, artistKey, numberOfTracks, null);
                artists.add(currentArtist);
               // testTextView.append(currentArtist + "\n");
            } while (artistCursor.moveToNext());
        }
        tracksToSelectRecyclerView = findViewById(R.id.tracksToSelectRecyclerView);
        // playlistRecyclerView.setHasFixedSize(true);

        artistsLayoutManager = new LinearLayoutManager(this);
        artistsAdapter = new ArtistsAdapter(artists, new ArtistsAdapter.OnArtistTouchListener() {
           @Override
           public void onArtistTouch(View v, MotionEvent event, int position) {
               switch (event.getAction()) {  //
                   case MotionEvent.ACTION_DOWN: // нажатие
                       startx = event.getRawX();
                       dX = v.getX() - event.getRawX();
                       Log.d("mylog", "startx = " + startx);

                       break;
                   case MotionEvent.ACTION_MOVE: // движение
                       if (event.getRawX() < startx) {
                           v.animate()
                                   .x(event.getRawX() + dX)
                                   .setDuration(0)
                                   .start();
                       }

                       break;
                   case MotionEvent.ACTION_UP: // отпускание
                       stopx = event.getRawX();
                       Log.d("mylog", "stopx = " + stopx);

                       if (startx - stopx >  80) {
                           Log.d("mylog", "startx = " + startx + "  stopx = " + stopx);
                           addThisArtistTracks(position);
                           v.animate()
                                   .x(startx + dX)
                                   .setDuration(0)
                                   .start();

                       } else if (stopx == startx) {
                           showThisArtistTracks(position);
                           ImageView backToArtists = findViewById(R.id.backToArtistImageView);
                           backToArtists.setVisibility(View.VISIBLE);
                       } else {
                           v.animate()
                                   .x(startx + dX)
                                   .setDuration(0)
                                   .start();
                       }

                       break;
                   case MotionEvent.ACTION_CANCEL:
                       v.animate()
                               .x(startx + dX)
                               .setDuration(0)
                               .start();
                        break;

               }
           }
       });

        tracksToSelectRecyclerView.setLayoutManager(artistsLayoutManager);
        tracksToSelectRecyclerView.setAdapter(artistsAdapter);
    }

    private void addThisArtistTracks(int position) {
        ContentResolver contentResolver = getContentResolver();
        Uri trackUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA
        };

        String[] selectionArgs = {artists.get(position).getArtistKey()};
        Cursor cursor  = contentResolver.query(trackUri,
                projection,
                MediaStore.Audio.Media.ARTIST_KEY+ "=?",
                selectionArgs ,
                null);


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

                Log.d("theTrack", "" + currentArtist + "  " + currentTitle + "  " + currentDuration);
                newPlaylistArrayList.add(currentTrack);

            } while (cursor.moveToNext());
        }
        newPlaylistTrackAdapter = new TrackAdapter(newPlaylistArrayList, new TrackAdapter.OnTrackTouchListener() {
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

                        if (stopx - startx >  80 && stopx!=0) {
                            newPlaylistArrayList.remove(position);
                            chosenTracksRecyclerView.setAdapter(newPlaylistTrackAdapter);

                        }else {
                            v.animate()
                                    .x(0)
                                    .setDuration(0)
                                    .start();
                        }
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        v.animate()
                                .x(0)
                                .setDuration(0)
                                .start();
                        break;
                }
            }
        });
        chosenTracksRecyclerView.setAdapter(newPlaylistTrackAdapter);
    }

    private void showThisArtistTracks(int position) {
        thisArtistTracksArrayList = new ArrayList<>();

        ContentResolver contentResolver = getContentResolver();
        Uri trackUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA
        };

        String[] selectionArgs = {artists.get(position).getArtistKey()};
        Cursor cursor  = contentResolver.query(trackUri,
                projection,
                MediaStore.Audio.Media.ARTIST_KEY+ "=?",
                selectionArgs ,
                null);


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

                Log.d("theTrack", "" + currentArtist + "  " + currentTitle + "  " + currentDuration);
               thisArtistTracksArrayList.add(currentTrack);

            } while (cursor.moveToNext());
        }
        RecyclerView.LayoutManager trackLayoutManager = new LinearLayoutManager(this);
        TrackAdapter thisArtistTracksAdapter = new TrackAdapter(thisArtistTracksArrayList, new TrackAdapter.OnTrackTouchListener() {
            @Override
            public void onTrackTouch(View v, MotionEvent event, int position) {
                switch (event.getAction()) { //show this artist's tracks
                    case MotionEvent.ACTION_DOWN: // нажатие
                        startx = event.getRawX();
                        dX = v.getX() - event.getRawX();

                        break;
                    case MotionEvent.ACTION_MOVE: // движение
                        if (event.getRawX() < startx) {
                            v.animate()
                                    .x(event.getRawX() + dX)
                                    .setDuration(0)
                                    .start();
                        }

                        break;
                    case MotionEvent.ACTION_UP: // отпускание
                        stopx = event.getX();
                        if (startx - stopx >  80) {
                            v.animate()
                                    .x(startx + dX)
                                    .setDuration(0)
                                    .start();
                            Log.d("mylog", "startx = " + startx + "  stopx = " + stopx);
                            newPlaylistArrayList.add(thisArtistTracksArrayList.get(position));
                            newPlaylistTrackAdapter = new TrackAdapter(newPlaylistArrayList, new TrackAdapter.OnTrackTouchListener() {
                                @Override
                                public void onTrackTouch(View v, MotionEvent event, int position) {
                                    switch (event.getAction()) { // new playlist editing
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

                                            if (stopx - startx >  80 && stopx!=0) {
                                                newPlaylistArrayList.remove(position);
                                                chosenTracksRecyclerView.setAdapter(newPlaylistTrackAdapter);

                                            }else {
                                                v.animate()
                                                        .x(0)
                                                        .setDuration(0)
                                                        .start();
                                            }
                                            break;
                                        case MotionEvent.ACTION_CANCEL:
                                            v.animate()
                                                    .x(0)
                                                    .setDuration(0)
                                                    .start();
                                            break;
                                    }

                                }
                            });
                            chosenTracksRecyclerView.setAdapter(newPlaylistTrackAdapter);
                        }
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        v.animate()
                                .x(startx + dX)
                                .setDuration(0)
                                .start();
                        break;
                }


            }
        });

        tracksToSelectRecyclerView.setLayoutManager(trackLayoutManager);
        tracksToSelectRecyclerView.setAdapter(thisArtistTracksAdapter);
    }

    public void backToArtists(View view) {
        showArtists();
        ImageView backToArtists = findViewById(R.id.backToArtistImageView);
        backToArtists.setVisibility(View.INVISIBLE);

    }

    public void clearPlaylist(View view) {
        newPlaylistArrayList = new ArrayList<>();
        newPlaylistTrackAdapter = new TrackAdapter(newPlaylistArrayList, new TrackAdapter.OnTrackTouchListener() {
            @Override
            public void onTrackTouch(View v, MotionEvent event, int position) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: // нажатие
                        startx = event.getX();
                        break;
                    case MotionEvent.ACTION_MOVE: // движение

                        break;
                    case MotionEvent.ACTION_UP: // отпускание
                        stopx = event.getX();
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        stopx = startx;
                        break;
                }
                if (stopx - startx >  80 && stopx!=0) {
                    Log.d("mylog", "startx = " + startx + "  stopx = " + stopx);
                    newPlaylistArrayList.remove(position);
                    chosenTracksRecyclerView.setAdapter(newPlaylistTrackAdapter);
                    startx=0;
                    stopx=0;
                }
            }
        });
        chosenTracksRecyclerView.setAdapter(newPlaylistTrackAdapter);
    }

    public void shuffleNewPlaylist(View view) {
        ArrayList<Track> shuffledPlaylist = new ArrayList<>();
        int size = newPlaylistArrayList.size();
        for (int i = 0; i<size; i++){
            int k = random.nextInt(newPlaylistArrayList.size());
            shuffledPlaylist.add(newPlaylistArrayList.get(k));
            if (newPlaylistArrayList.get(k) != null) newPlaylistArrayList.remove(k);
        }
        newPlaylistArrayList = shuffledPlaylist;
        newPlaylistTrackAdapter = new TrackAdapter(newPlaylistArrayList, new TrackAdapter.OnTrackTouchListener() {
            @Override
            public void onTrackTouch(View v, MotionEvent event, int position) {
                switch (event.getAction()) { // new playlist editing
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

                        if (stopx - startx >  80 && stopx!=0) {
                            newPlaylistArrayList.remove(position);
                            chosenTracksRecyclerView.setAdapter(newPlaylistTrackAdapter);

                        }else {
                            v.animate()
                                    .x(0)
                                    .setDuration(0)
                                    .start();
                        }
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        v.animate()
                                .x(0)
                                .setDuration(0)
                                .start();
                        break;
                }
            }
        });
        chosenTracksRecyclerView.setAdapter(newPlaylistTrackAdapter);

    }

    public void createPlaylist(View view) {
        playlistTitleEditText = findViewById(R.id.playlistTitleEditText);

        String playlistTitle = playlistTitleEditText.getText().toString().trim();
        if (newPlaylistArrayList.size() > 0) {
            Playlist newPlaylist = new Playlist(playlistTitle, 0, newPlaylistArrayList);
            File playlistsDirectory = getApplicationContext().getFilesDir();
            File file = new File(playlistsDirectory, playlistTitle);
            file.wr
        }
    }
}
