package com.uo289390.toco.dto;

import com.uo289390.toco.model.Item;

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
     * On update, {@code null} (field omitted) keeps the current genres and an empty list removes them.
     */
    private List<Long> genreIds;

    /**
     * List of IDs of the instruments associated with the song.
     * On update, {@code null} (field omitted) keeps the current instruments and an empty list removes them.
     */
    private List<Long> instrumentIds;

    /**
     * List of IDs of the song lessons associated with the song.
     * On update, {@code null} (field omitted) keeps the current lessons; otherwise the lessons
     * not included are removed (and deleted, as they cannot exist without their song).
     */
    private List<Long> lessonIds;

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
