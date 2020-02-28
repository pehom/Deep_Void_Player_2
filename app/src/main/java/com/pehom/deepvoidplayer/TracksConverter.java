package com.pehom.deepvoidplayer;

import androidx.room.TypeConverter;

import java.util.ArrayList;

public class TracksConverter {
    @TypeConverter
    public String fromTracks(ArrayList<Track> tracks){
        String s = "";
        for (int i=0; i < tracks.size(); i++){
            s=s+tracks.get(i).getArtist()+"%>"+tracks.get(i).getTitle()+"%>"+tracks.get(i).getDuration()+"%>"+
                    tracks.get(i).getData()+",";
        }
        return s;
    }
    @TypeConverter
    public ArrayList<Track> toTracks(String s){
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



}
