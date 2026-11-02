import { useState } from "react";
import { View, Text } from "react-native";
import { useNavigation, NavigationProp } from "@react-navigation/native";
import ListItem, { LIST_ITEM_SCALE } from "./ListItem";
import AddToListModal from "./AddToListModal";
import { ItemSummaryDTO, RootStackParamList } from "../types";
import { SearchSection } from "../hooks/useCatalogSearch";
import { SearchField } from "../utils/search.utils";
import { navigateToItem } from "../utils/navigation.utils";
import { useItemToListItem } from "../hooks/useItemToListItem";

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
 * en orden de prioridad. Cada resultado tiene un botón + que abre el modal para añadirlo
 * a una de las listas del usuario.
 */
export default function SearchResults({ sections, emptyText }: SearchResultsProps) {
  const navigation = useNavigation<NavigationProp<RootStackParamList>>();
  // Item que se está añadiendo a una lista (null = modal cerrado)
  const [itemToAdd, setItemToAdd] = useState<ItemSummaryDTO | null>(null);
  const toListItem = useItemToListItem();

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
                item={toListItem(item)}
                inverted={index % 2 !== 0}
                scale={LIST_ITEM_SCALE}
                onPress={() => navigateToItem(navigation, item)}
                onAdd={() => setItemToAdd(item)}
              />
            ))}
          </View>
        </View>
      ))}

      <AddToListModal
        item={itemToAdd ? { id: itemToAdd.id.toString(), title: itemToAdd.title } : null}
        onClose={() => setItemToAdd(null)}
      />
    </View>
  );
}
