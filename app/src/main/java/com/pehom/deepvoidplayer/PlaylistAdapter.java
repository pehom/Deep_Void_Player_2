package com.pehom.deepvoidplayer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;



public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder> {
    private ArrayList<Playlist> playlists;
    private OnPlaylistClickListener onPlaylistClickListener;
    private OnAddPlaylistClickListener onAddPlaylistClickListener;
    private OnEditPlaylistClickListener onEditPlaylistClickListener;
    private OnDeletePlaylistClickListener onDeletePlaylistClickListener;


    public PlaylistAdapter(ArrayList<Playlist> playlists, OnPlaylistClickListener onPlaylistClickListener, OnAddPlaylistClickListener onAddPlaylistClickListener, OnEditPlaylistClickListener onEditPlaylistClickListener, OnDeletePlaylistClickListener onDeletePlaylistClickListener) {
        this.playlists = playlists;
        this.onPlaylistClickListener = onPlaylistClickListener;
        this.onAddPlaylistClickListener = onAddPlaylistClickListener;
        this.onEditPlaylistClickListener = onEditPlaylistClickListener;
        this.onDeletePlaylistClickListener = onDeletePlaylistClickListener;
    }

    public interface OnPlaylistClickListener {
        void onPlaylistClick(int position);
    }

    public void setOnPlaylistClickListener(OnPlaylistClickListener listener) {

        this.onPlaylistClickListener = listener;
    }

    public interface OnAddPlaylistClickListener{
        void onAddPlaylistClick(int position);
    }

    public void setOnAddPlaylistClickListener(OnAddPlaylistClickListener listener) {

        this.onAddPlaylistClickListener = listener;
    }

    public interface OnEditPlaylistClickListener{
        void onEditPlaylistClick(int position);
    }

    public void setOnEditPlaylistClickListener(OnEditPlaylistClickListener listener) {

        this.onEditPlaylistClickListener = listener;
    }

    public interface OnDeletePlaylistClickListener{
        void onDeletePlaylistClick(int position);
    }

    public void setOnDeletePlaylistClickListener(OnDeletePlaylistClickListener listener) {

        this.onDeletePlaylistClickListener = listener;
    }


    @NonNull
    @Override
    public PlaylistAdapter.PlaylistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.playlist_item, parent, false);
        PlaylistViewHolder viewHolder = new PlaylistViewHolder(view, onPlaylistClickListener, onAddPlaylistClickListener,
                onEditPlaylistClickListener, onDeletePlaylistClickListener);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull PlaylistAdapter.PlaylistViewHolder holder, int position) {
        Playlist currentPlaylist = playlists.get(position);
        holder.playlistTitleTextView.setText(currentPlaylist.getPlaylistTitle());
    }

    @Override
    public int getItemCount() {
       return playlists.size();
    }

    public static class PlaylistViewHolder extends RecyclerView.ViewHolder {
        public TextView playlistTitleTextView;
        public ImageView addPlaylistToQueueImageView;
        public ImageView editPlaylistImageView;
        public ImageView deletePlaylistImageView;
        public ImageView morePlaylistActionImageView;
        public boolean isMoreActionMode;


        public PlaylistViewHolder(@NonNull View itemView, final OnPlaylistClickListener onPlaylistClickListener,
                                  final OnAddPlaylistClickListener onAddPlaylistClickListener,
                                  final OnEditPlaylistClickListener onEditPlaylistClickListener,
                                  final OnDeletePlaylistClickListener onDeletePlaylistClickListener) {
            super(itemView);
            playlistTitleTextView = itemView.findViewById(R.id.playlistTitleTextView);
            isMoreActionMode = false;
            playlistTitleTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                        int position = getAdapterPosition();
                        onPlaylistClickListener.onPlaylistClick(position);
                    }
            });
            addPlaylistToQueueImageView = itemView.findViewById(R.id.addPlaylistToQueueImageView);
            addPlaylistToQueueImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    onAddPlaylistClickListener.onAddPlaylistClick(position);
                }
            });
            editPlaylistImageView = itemView.findViewById(R.id.editPlaylistImageView);
            editPlaylistImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    onEditPlaylistClickListener.onEditPlaylistClick(position);
                }
            });
            deletePlaylistImageView = itemView.findViewById(R.id.deletePlaylistImageView);
            deletePlaylistImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    onDeletePlaylistClickListener.onDeletePlaylistClick(position);
                }
            });
            morePlaylistActionImageView = itemView.findViewById(R.id.playlistMoreActionsImageView);
            morePlaylistActionImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!isMoreActionMode) {
                        addPlaylistToQueueImageView.setVisibility(View.VISIBLE);
                        editPlaylistImageView.setVisibility(View.VISIBLE);
                        deletePlaylistImageView.setVisibility(View.VISIBLE);
                        isMoreActionMode=true;
                    } else {
                        addPlaylistToQueueImageView.setVisibility(View.GONE);
                        editPlaylistImageView.setVisibility(View.GONE);
                        deletePlaylistImageView.setVisibility(View.GONE);
                        isMoreActionMode=false;
                    }
                }
            });

        }
    }
}
