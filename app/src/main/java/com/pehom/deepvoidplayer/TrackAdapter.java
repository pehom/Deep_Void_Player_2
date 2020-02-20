package com.pehom.deepvoidplayer;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class TrackAdapter  extends RecyclerView.Adapter<TrackAdapter.TrackViewHolder> {
    private ArrayList<Track> tracks;
    private OnTrackTouchListener listener;

    public TrackAdapter(ArrayList<Track> tracks, OnTrackTouchListener listener) {
        this.tracks = tracks;
        this.listener = listener;
    }

    public interface OnTrackTouchListener {
        void onTrackTouch(View v,MotionEvent event, int position);
    }

    public void setOnTrackTouchListener(OnTrackTouchListener listener) {

        this.listener = listener;
    }


    @NonNull
    @Override
    public TrackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.track_item, parent, false);
        TrackViewHolder viewHolder = new TrackViewHolder(view, listener);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull TrackViewHolder holder, int position) {
        Track currentTrack = tracks.get(position);
        holder.trackTitleTextView.setText(currentTrack.getArtist() + " - " + currentTrack.getTitle());

        holder.durationTextView.setText(currentTrack.getDuration());

    }

    @Override
    public int getItemCount() {
        return tracks.size();
    }

    public static class TrackViewHolder extends RecyclerView.ViewHolder {
        public TextView trackTitleTextView;
        public TextView durationTextView;

        public TrackViewHolder(@NonNull View itemView, final OnTrackTouchListener touchListener) {
            super(itemView);
            trackTitleTextView = itemView.findViewById(R.id.trackTitleTextView);
            durationTextView = itemView.findViewById(R.id.durationTextView);

            itemView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (touchListener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            touchListener.onTrackTouch(v, event, position);
                        }
                    }
                    return true;
                }
            });
        }
    }
}
