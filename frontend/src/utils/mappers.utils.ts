/**
 * Utilidades para mapeo de DTOs a componentes visuales
 * Centraliza la lógica de transformación de datos entre capas
 */

import { AnyItemDTO, CarouselItemDTO, GenreDTO, InstrumentDTO, InstrumentGenreDTO, ItemSummaryDTO, ItemType, LessonDTO, SongDTO } from '../types';

/** IDs presentes como strings (el backend los manda como números). */
const presentIds = (...ids: (string | number | null | undefined)[]): string[] =>
  ids.filter((id) => id != null).map(String);

/**
 * Metadatos de búsqueda (géneros, instrumentos, década) que se pueden sacar del DTO completo.
 * Solo incluye los campos que conoce: una lección de un género-instrumento no trae su género
 * ni su instrumento, así que no se pisan los que ya mandó el backend en el resumen.
 */
const toSearchMetadata = (dto: AnyItemDTO, type: ItemType): Partial<ItemSummaryDTO> => {
  switch (type) {
    case ItemType.SONG: {
      const song = dto as SongDTO;
      return {
        genreIds: presentIds(...(song.genreIds ?? [])),
        instrumentIds: presentIds(...(song.instrumentIds ?? [])),
        ...(song.decade !== undefined && { decade: song.decade }),
      };
    }
    case ItemType.LESSON: {
      const lesson = dto as LessonDTO;
      return {
        ...(lesson.genreId != null && { genreIds: presentIds(lesson.genreId) }),
        ...(lesson.instrumentId != null && { instrumentIds: presentIds(lesson.instrumentId) }),
      };
    }
    case ItemType.INSTRUMENT_GENRE: {
      const instrumentGenre = dto as InstrumentGenreDTO;
      return {
        genreIds: presentIds(instrumentGenre.genreId),
        instrumentIds: presentIds(instrumentGenre.instrumentId),
      };
    }
    default:
      return {};
  }
};

/**
 * Deriva el resumen (catálogo) de un DTO completo.
 * @param dto DTO completo de cualquier subtipo
 * @param type Subtipo; por defecto el `type` del propio DTO
 * @returns ItemSummaryDTO equivalente
 */
export const toItemSummary = (dto: AnyItemDTO, type: ItemType = dto.type): ItemSummaryDTO => ({
  id: dto.id,
  title: dto.title,
  imageUrl: dto.imageUrl,
  type,
  level: dto.level,
  artist: type === ItemType.SONG ? (dto as SongDTO).artist : null,
  ...toSearchMetadata(dto, type),
});

/**
 * Mapea un ItemSummaryDTO (catálogo) a CarouselItemDTO.
 * Mantiene la descripción que tenía cada tipo: artista en canciones, título en lecciones.
 * @param item El resumen del item a mapear
 * @returns CarouselItemDTO listo para usar en carruseles
 */
export const itemToCarouselItem = (item: ItemSummaryDTO): CarouselItemDTO => ({
  id: item.id,
  title: item.title,
  imgUrl: item.imageUrl,
  ...(item.type === ItemType.SONG && { artist: item.artist ?? undefined, description: item.artist ?? undefined }),
  ...(item.type === ItemType.LESSON && { description: item.title }),
});

/**
 * Mapea múltiples ItemSummaryDTOs a CarouselItemDTOs
 * @param items Array de resúmenes de items
 * @returns Array de CarouselItemDTOs
 */
export const itemsToCarouselItems = (items: ItemSummaryDTO[]): CarouselItemDTO[] =>
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

/** Nombre legible del tipo de item. */
const getItemTypeLabel = (item: ItemSummaryDTO): string => {
  switch (item.type) {
    case ItemType.SONG:
      return 'Canción';
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

/** Géneros e instrumentos por id (sus Stores): dan nombre a lo que precede al tipo en las lecciones. */
export interface ItemNameLookup {
  genres: Record<string, GenreDTO>;
  instruments: Record<string, InstrumentDTO>;
}

/**
 * Lo que va antes del tipo en el subtítulo: el artista en canciones y el instrumento (o si no,
 * el género) en lecciones.
 */
const getItemContext = (item: ItemSummaryDTO, names?: ItemNameLookup): string | null | undefined => {
  switch (item.type) {
    case ItemType.SONG:
      return item.artist;
    case ItemType.LESSON: {
      const instrumentId = item.instrumentIds?.[0];
      const genreId = item.genreIds?.[0];
      return (instrumentId != null ? names?.instruments[String(instrumentId)]?.name : null)
        ?? (genreId != null ? names?.genres[String(genreId)]?.title : null);
    }
    default:
      return null;
  }
};

/**
 * Subtítulo de un item polimórfico: "Contexto · Tipo" (artista en canciones, instrumento o
 * género en lecciones; si no se conoce, solo el tipo).
 * @param item Cualquier item (Song, Lesson, Genre, InstrumentGenre)
 * @param names Géneros e instrumentos para nombrar los de las lecciones
 */
export const getItemSubtitle = (item: ItemSummaryDTO, names?: ItemNameLookup): string =>
  [getItemContext(item, names), getItemTypeLabel(item)]
    .filter(Boolean)
    .join(' · ');

/**
 * Mapea un ItemSummaryDTO a las props que esperan los componentes ListItem y AddListItem.
 * @param item Cualquier item polimórfico
 * @param names Géneros e instrumentos para el subtítulo de las lecciones
 * @returns Objeto con { id, title, description, imageUrl, info } (info: título y subtítulo; en
 * los géneros, en una sola línea "Título · Género")
 */
export const itemToListItem = (item: ItemSummaryDTO, names?: ItemNameLookup) => {
  const subtitle = getItemSubtitle(item, names);
  return {
    id: item.id,
    title: item.title,
    description: subtitle,
    imageUrl: item.imageUrl,
    /** @TODO mover la lógica de cálculo y propagación del resumen del item al toString del backend, mandando el campo en el DTO y que info serialzie ese campo */
    info: item.type === ItemType.GENRE ? `${item.title} · ${subtitle}` : `${item.title}\n${subtitle}`,
  };
};
