package com.pehom.deepvoidplayer;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.util.ArrayList;

@Entity(tableName = "playlistsTable")
public class Playlist {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo (name = "playlistId")
    private long id;
    @ColumnInfo
    private String playlistTitle;
    @ColumnInfo
    private int currentItemPosition;
    @ColumnInfo
    @TypeConverters({TracksConverter.class})
    private ArrayList<Track> tracks;
    @Ignore
    public Playlist(){}

    public Playlist(long id, String playlistTitle, int currentItemPosition, ArrayList<Track> tracks) {
        this.id = id;
        this.playlistTitle = playlistTitle;
        this.currentItemPosition = currentItemPosition;
        this.tracks = tracks;
    }

    public String getPlaylistTitle() {
        return playlistTitle;
    }

    public void setPlaylistTitle(String playlistTitle) {
        this.playlistTitle = playlistTitle;
    }

    public int getCurrentItemPosition() {
        return currentItemPosition;
    }

    public void setCurrentItemPosition(int currentItemPosition) {
        this.currentItemPosition = currentItemPosition;
    }

    public ArrayList<Track> getTracks() {
        return tracks;
    }

    public void setTracks(ArrayList<Track> tracks) {
        this.tracks = tracks;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
