package com.pehom.deepvoidplayer;

import java.util.ArrayList;


public class MainActivityState {
    private boolean shuffleMode;
    private int loopMode;
    private boolean playPauseMode;
    private boolean choosePlaylistRecyclerViewIsVisible;
    private ArrayList<Track> queueArrayList;
    private int queueCurrentPosition;
    private String queueTitle;

    public MainActivityState(){}

    public MainActivityState(boolean shuffleMode, int loopMode, boolean playPauseMode, boolean choosePlaylistRecyclerViewIsVisible, ArrayList<Track> queueArrayList, int queueCurrentPosition, String queueTitle) {
        this.shuffleMode = shuffleMode;
        this.loopMode = loopMode;
        this.playPauseMode = playPauseMode;
        this.choosePlaylistRecyclerViewIsVisible = choosePlaylistRecyclerViewIsVisible;
        this.queueArrayList = queueArrayList;
        this.queueCurrentPosition = queueCurrentPosition;
        this.queueTitle = queueTitle;
    }

    public boolean getShuffleMode() {
        return shuffleMode;
    }

    public void setShuffleMode(boolean shuffleMode) {
        this.shuffleMode = shuffleMode;
    }

    public int getLoopMode() {
        return loopMode;
    }

    public void setLoopMode(int loopMode) {
        this.loopMode = loopMode;
    }

    public boolean getChoosePlaylistRecyclerViewIsVisible() {
        return choosePlaylistRecyclerViewIsVisible;
    }

    public void setChoosePlaylistRecyclerViewIsVisible(boolean choosePlaylistRecyclerViewIsVisible) {
        this.choosePlaylistRecyclerViewIsVisible = choosePlaylistRecyclerViewIsVisible;
    }

    public ArrayList<Track> getQueueArrayList() {
        return queueArrayList;
    }

    public void setQueueArrayList(ArrayList<Track> queueArrayList) {
        this.queueArrayList = queueArrayList;
    }

    public int getQueueCurrentPosition() {
        return queueCurrentPosition;
    }

    public void setQueueCurrentPosition(int queueCurrentPosition) {
        this.queueCurrentPosition = queueCurrentPosition;
    }

    public String getQueueTitle() {
        return queueTitle;
    }

    public void setQueueTitle(String queueTitle) {
        this.queueTitle = queueTitle;
    }

    public boolean getPlayPauseMode() {
        return playPauseMode;
    }

    public void setPlayPauseMode(boolean playPauseMode) {
        this.playPauseMode = playPauseMode;
    }
}
