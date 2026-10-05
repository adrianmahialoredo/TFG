import * as itemService from '../services/item.service';
import * as itemRepository from './item.repository';
import * as songRepository from './song.repository';
import * as lessonRepository from './lesson.repository';
import * as genreRepository from './genre.repository';
import * as instrumentGenreRepository from './instrumentGenre.repository';
import { AnyItemDTO, GenreDTO, InstrumentGenreDTO, ItemType, LessonDTO, SongDTO } from '../types';
import { OnFresh } from '../utils/entityCache.utils';
import { toItemSummary } from '../utils/mappers.utils';

/**
 * Feeds mixtos de items que, por decisión de negocio, traen el DTO COMPLETO de cada item
 * (el usuario suele abrir lo que ve en ellos). Cada DTO se guarda en la Store de su subtipo
 * y su resumen en la ItemEntitiesStore, que es con lo que se pintan las tarjetas.
 *
 * Vive aparte de `item.repository` porque depende de los repositorios de cada subtipo, y
 * esos (p.ej. `song.repository`) ya dependen de `item.repository`.
 */

/** Reparte los DTOs en la Store de su subtipo y guarda sus resúmenes. Devuelve los IDs en orden. */
const hydrateMixed = (dtos: AnyItemDTO[]): string[] => {
    const ofType = <T extends AnyItemDTO>(type: ItemType) => dtos.filter((d) => d.type === type) as T[];

    songRepository.hydrate(ofType<SongDTO>(ItemType.SONG));
    lessonRepository.hydrate(ofType<LessonDTO>(ItemType.LESSON));
    genreRepository.hydrate(ofType<GenreDTO>(ItemType.GENRE));
    instrumentGenreRepository.hydrate(ofType<InstrumentGenreDTO>(ItemType.INSTRUMENT_GENRE));
    itemRepository.hydrateSummaries(dtos.map((d) => toItemSummary(d)));

    return dtos.map((d) => d.id.toString());
};

const getMixedFeed = (cacheKey: string, fetchDtos: () => Promise<AnyItemDTO[]>, onFresh?: OnFresh): Promise<string[]> =>
    itemRepository.getCustomFeed(cacheKey, async () => hydrateMixed(await fetchDtos()), onFresh);

/** Items accedidos más recientemente (Home "Recientes"). Personalizado: se borra en el logout. */
export const getRecent = (onFresh?: OnFresh): Promise<string[]> =>
    getMixedFeed('items_recent', () => itemService.getRecent(), onFresh);

/** Items recomendados ("Para ti") para añadir a una lista concreta. */
export const getRecommendedByItemList = (itemListId: string, onFresh?: OnFresh): Promise<string[]> =>
    getMixedFeed(`items_recommended_list_${itemListId}`, () => itemService.getRecommendedByItemList(itemListId), onFresh);
