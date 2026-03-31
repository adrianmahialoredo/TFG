// src/navigation/types.ts
export type RootStackParamList = {
  LoginScreen: undefined;
  RegisterScreen: undefined;
  BottomTab: undefined;
  SongScreen: { songId: string };
  GenreMainScreen: {genreId: string};
  ListScreen: { itemListId: string };
  ProfileScreen: undefined;
  MetronomeScreen: undefined;
};
