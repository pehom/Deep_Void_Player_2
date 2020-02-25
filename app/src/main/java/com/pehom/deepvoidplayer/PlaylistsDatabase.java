package com.pehom.deepvoidplayer;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Playlist.class}, version = 1, exportSchema = false)
public abstract class PlaylistsDatabase extends RoomDatabase {
    public abstract PlaylistDAO getPlaylistDAO();
}
