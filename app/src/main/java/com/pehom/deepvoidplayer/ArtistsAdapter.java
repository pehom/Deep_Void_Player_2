package com.pehom.deepvoidplayer;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ArtistsAdapter extends RecyclerView.Adapter<ArtistsAdapter.ArtistViewHolder>{
    private ArrayList<Artist> artists;
   // private OnArtistClickListener clickListener;
    private OnArtistTouchListener touchListener;

   /* public ArtistsAdapter(ArrayList<Artist> artists, OnArtistClickListener clickListener) {
        this.artists = artists;
        this.clickListener = clickListener;
    } */


    public ArtistsAdapter(ArrayList<Artist> artists, OnArtistTouchListener touchListener) {
        this.artists = artists;

        this.touchListener = touchListener;
    }

    @NonNull
    @Override
    public ArtistsAdapter.ArtistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.artist_item, parent, false);
        ArtistViewHolder viewHolder = new ArtistViewHolder(view, touchListener);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ArtistsAdapter.ArtistViewHolder holder, int position) {
        Artist currentArtist = artists.get(position);

        if (Integer.parseInt(currentArtist.getNumberOfTracks()) == 1) {
            holder.artistTextView.setTextSize(14);
        } else if (Integer.parseInt(currentArtist.getNumberOfTracks()) < 5) {
            holder.artistTextView.setTextSize(18);
        } else {
            holder.artistTextView.setTextSize(25);
        }
        holder.artistTextView.setText(currentArtist.getName());
    }

    @Override
    public int getItemCount() {
        return artists.size();
    }


    public static class ArtistViewHolder extends RecyclerView.ViewHolder {
        public TextView artistTextView;

        public ArtistViewHolder(@NonNull View itemView, final OnArtistTouchListener touchListener) {
            super(itemView);
            artistTextView = itemView.findViewById(R.id.artistTextView);

           itemView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (touchListener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            touchListener.onArtistTouch(v, event, position);
                        }
                    }
                return  true;}
            });

         /*   artistTextView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                   if (touchListener != null) {
                       int position = getAdapterPosition();
                       if (position != RecyclerView.NO_POSITION) {
                           touchListener.onArtistTouch(event, position);
                       }
                   }
                    return true;
                }
            });*/
        }
    }

    public interface OnArtistClickListener {
        void onArtistClick(int position);
    }

    public interface OnArtistTouchListener {
        void onArtistTouch(View v, MotionEvent event, int position);
    }

   /* public void setOnArtistClickListener(OnArtistClickListener listener) {

        this.clickListener = listener;
    }*/

    public void setOnArtistTouchListener(OnArtistTouchListener listener) {

   //     this.touchListener = listener;
    }



}
