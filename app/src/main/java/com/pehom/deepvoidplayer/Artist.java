package com.pehom.deepvoidplayer;

import java.util.ArrayList;

public class Artist {
    private String name;
    private String artistKey;
    private String numberOfTracks;
    private ArrayList<Track> tracks;
    public Artist(){}

    public Artist(String name, String artistKey, String numberOfTracks, ArrayList<Track> tracks) {
        this.name = name;
        this.artistKey = artistKey;
        this.numberOfTracks = numberOfTracks;
        this.tracks = tracks;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumberOfTracks() {
        return numberOfTracks;
    }

    public void setNumberOfTracks(String numberOfTracks) {
        this.numberOfTracks = numberOfTracks;
    }

    public ArrayList<Track> getTracks() {
        return tracks;
    }

    public void setTracks(ArrayList<Track> tracks) {
        this.tracks = tracks;
    }

    public String getArtistKey() {
        return artistKey;
    }

    public void setArtistKey(String artistKey) {
        this.artistKey = artistKey;
    }
}
