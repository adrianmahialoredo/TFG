/**
 * Utilidades para mapeo de DTOs a componentes visuales
 * Centraliza la lógica de transformación de datos entre capas
 */

import { SongDTO, LessonDTO, ItemDTO, CarouselItemDTO, GenreDTO, InstrumentDTO, AnyItemDTO, ItemType } from '../types';

/**
 * Mapea un SongDTO a CarouselItemDTO
 * @param song El SongDTO a mapear
 * @returns CarouselItemDTO listo para usar en carruseles
 */
export const songToCarouselItem = (song: SongDTO): CarouselItemDTO => ({
  id: song.id,
  title: song.title,
  imgUrl: song.imageUrl,
  artist: song.artist,
  description: song.artist,
});

/**
 * Mapea múltiples SongDTOs a CarouselItemDTOs
 * @param songs Array de SongDTOs
 * @returns Array de CarouselItemDTOs
 */
export const songsToCarouselItems = (songs: SongDTO[]): CarouselItemDTO[] =>
  songs.map(songToCarouselItem);

/**
 * Mapea un LessonDTO a CarouselItemDTO
 * @param lesson El LessonDTO a mapear
 * @returns CarouselItemDTO listo para usar en carruseles
 */
export const lessonToCarouselItem = (lesson: LessonDTO): CarouselItemDTO => ({
  id: lesson.id,
  title: lesson.title,
  imgUrl: lesson.imageUrl,
  description: lesson.title,
});

/**
 * Mapea múltiples LessonDTOs a CarouselItemDTOs
 * @param lessons Array de LessonDTOs
 * @returns Array de CarouselItemDTOs
 */
export const lessonsToCarouselItems = (lessons: LessonDTO[]): CarouselItemDTO[] =>
  lessons.map(lessonToCarouselItem);

/**
 * Mapea un ItemDTO a CarouselItemDTO
 * @param item El ItemDTO a mapear
 * @returns CarouselItemDTO listo para usar en carruseles
 */
export const itemToCarouselItem = (item: ItemDTO): CarouselItemDTO => ({
  id: item.id,
  title: item.title,
  imgUrl: item.imageUrl,
});

/**
 * Mapea múltiples ItemDTOs a CarouselItemDTOs
 * @param items Array de ItemDTOs
 * @returns Array de CarouselItemDTOs
 */
export const itemsToCarouselItems = (items: ItemDTO[]): CarouselItemDTO[] =>
  items.map(itemToCarouselItem);

/**
 * Mapea un GenreDTO a CarouselItemDTO
 * @param genre El GenreDTO a mapear
 * @returns CarouselItemDTO listo para usar en carruseles
 */
export const genreToCarouselItem = (genre: GenreDTO): CarouselItemDTO => ({
  id: genre.id,
  title: genre.title,
  imgUrl: genre.imageUrl,
  description: genre.description,
});

/**
 * Mapea múltiples GenreDTOs a CarouselItemDTOs
 * @param genres Array de GenreDTOs
 * @returns Array de CarouselItemDTOs
 */
export const genresToCarouselItems = (genres: GenreDTO[]): CarouselItemDTO[] =>
  genres.map(genreToCarouselItem);

/**
 * Mapea un InstrumentDTO a CarouselItemDTO
 * @param instrument El InstrumentDTO a mapear
 * @returns CarouselItemDTO listo para usar en carruseles
 */
export const instrumentToCarouselItem = (instrument: InstrumentDTO): CarouselItemDTO => ({
  id: instrument.id,
  title: instrument.name,
  imgUrl: instrument.imageUrl,
});

/**
 * Mapea múltiples InstrumentDTOs a CarouselItemDTOs
 * @param instruments Array de InstrumentDTOs
 * @returns Array de CarouselItemDTOs
 */
export const instrumentsToCarouselItems = (instruments: InstrumentDTO[]): CarouselItemDTO[] =>
  instruments.map(instrumentToCarouselItem);

/**
 * Devuelve un subtítulo descriptivo según el tipo de item polimórfico.
 * @param item Cualquier item (Song, Lesson, Genre, InstrumentGenre)
 * @returns Texto descriptivo del tipo
 */
export const getItemSubtitle = (item: AnyItemDTO): string => {
  switch (item.type) {
    case ItemType.SONG:
      return `Canción · ${(item as SongDTO).artist || 'Artista desconocido'}`;
    case ItemType.LESSON:
      return 'Lección';
    case ItemType.GENRE:
      return 'Género';
    case ItemType.INSTRUMENT_GENRE:
      return 'Instrumento';
    default:
      return '';
  }
};

/**
 * Mapea un AnyItemDTO a las props que espera el componente AddListItem.
 * @param item Cualquier item polimórfico
 * @returns Objeto con { id, title, description, imageUrl, info } listo para AddListItem
 */
export const itemToAddListItem = (item: AnyItemDTO) => ({
  id: item.id,
  title: item.title,
  description: getItemSubtitle(item),
  imageUrl: item.imageUrl,
  info: `${item.title}\n${getItemSubtitle(item)}`,
});
