import { View, Text } from "react-native";
import { useNavigation, NavigationProp } from "@react-navigation/native";
import ListItem from "./ListItem";
import { RootStackParamList } from "../types";
import { SearchSection } from "../hooks/useCatalogSearch";
import { SearchField } from "../utils/search.utils";
import { navigateToItem } from "../utils/navigation.utils";
import { itemToListItem } from "../utils/mappers.utils";

const SECTION_TITLES: Record<SearchField, string> = {
  title: 'Por título',
  artist: 'Por artista',
  genre: 'Por género',
  instrument: 'Por instrumento',
  decade: 'Por década',
};

type SearchResultsProps = {
  sections: SearchSection[];
  /** Texto si no hay ninguna coincidencia. */
  emptyText: string;
};

/**
 * Resultados del buscador: una lista vertical por cada campo con coincidencias,
 * en orden de prioridad.
 */
export default function SearchResults({ sections, emptyText }: SearchResultsProps) {
  const navigation = useNavigation<NavigationProp<RootStackParamList>>();

  if (sections.length === 0) {
    return (
      <View className="flex-1 justify-center items-center py-20">
        <Text className="text-secondary text-center">{emptyText}</Text>
      </View>
    );
  }

  return (
    <View>
      {sections.map((section) => (
        <View key={section.field} className="mb-8">
          <Text className="text-2xl font-semibold text-secondary mb-4">{SECTION_TITLES[section.field]}</Text>
          <View className="gap-3">
            {section.items.map((item, index) => (
              <ListItem
                key={item.id}
                item={itemToListItem(item)}
                inverted={index % 2 !== 0}
                onPress={() => navigateToItem(navigation, item)}
              />
            ))}
          </View>
        </View>
      ))}
    </View>
  );
}
