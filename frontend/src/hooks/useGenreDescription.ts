import { useGenreEntitiesStore } from '../stores/entities';

export const useGenreDescription = (genreId: string) => {
    // Miramos directamente a la "despensa" de entidades de género
    const genre = useGenreEntitiesStore((state) => state.genres[genreId]);

    return {
        description: genre?.description || '',
        bannerUrl: genre?.bannerUrl || '',
        welcomeVideoId: genre?.welcomeIntroVideoId || '',
        welcomeSongId: genre?.welcomeSongVideoId || '',
        isValid: !!genre
    };
};