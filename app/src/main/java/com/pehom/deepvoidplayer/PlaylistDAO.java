package com.pehom.deepvoidplayer;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.ArrayList;
import java.util.List;

@Dao
public interface PlaylistDAO {
    @Insert
    public long addPlaylist(Playlist playlist);

    @Update
    public  void updatePlaylist(Playlist playlist);

    @Delete
    public void deletePlaylist(Playlist playlist);

    @Query("select playlistId from playlistsTable")
    public long[] getAllPlaylistId();

    @Query("select * from playlistsTable where playlistId ==:id")
    public Playlist getPlaylist(long id);
}
