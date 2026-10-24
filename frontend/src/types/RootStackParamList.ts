// src/navigation/types.ts
export type RootStackParamList = {
  LoginScreen: undefined;
  RegisterScreen: undefined;
  BottomTab: undefined;
  SongScreen: { songId: string };
  LessonScreen: { lessonId: string };
  GenreMainScreen: {genreId: string};
  InstrumentGenreLesson: { instrumentGenreId: string; instrumentName: string };
  ListScreen: { itemListId: string };
  AddItemScreen: { itemListId: string };
  ProfileScreen: undefined;
  MetronomeScreen: undefined;
  TunerScreen: undefined;
};
