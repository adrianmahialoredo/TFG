package com.demo.demo.dto;

import com.demo.demo.model.Item;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;


/**
 * Data Transfer Object for song information.
 *
 * This DTO is used to transfer song data between the client and server
 * during API operations. It extends {@link ItemDTO} to reuse the common
 * item fields (id, title, imageUrl, level) and adds the song specific data
 * such as the artist and its related genres, instruments and lessons.
 *
 * @author Adrián Mahía
 * @version 1.0
 * @since 2025-06-06
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SongDTO extends ItemDTO {

    /**
     * The artist that performs the song.
     */
    private String artist;

    /**
     * List of IDs of the genres associated with the song.
     */
    private List<Long> genreIds = new ArrayList<>();

    /**
     * List of IDs of the instruments associated with the song.
     */
    private List<Long> instrumentIds = new ArrayList<>();

    /**
     * List of IDs of the song lessons associated with the song.
     */
    private List<Long> lessonIds = new ArrayList<>();

    /**
     * Full song lessons payload, used when creating a song together with
     * its lessons and tab sections in a single request.
     */
    private List<SongLessonDTO> lessons = new ArrayList<>();

    @Override
    public Item.Type getType() {
        return Item.Type.Song;
    }
}
