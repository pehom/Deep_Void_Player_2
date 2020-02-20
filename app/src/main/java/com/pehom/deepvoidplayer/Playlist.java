package com.pehom.deepvoidplayer;

import java.util.ArrayList;

public class Playlist {
    private String playlistTitle;
    private int currentItemPosition;
    private ArrayList<Track> tracks;

    public Playlist(){}

    public Playlist(String playlistTitle, int currentItemPosition, ArrayList<Track> tracks) {
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
}
