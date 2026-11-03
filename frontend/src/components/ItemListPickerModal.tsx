import React, { useEffect, useMemo, useState } from 'react';
import { ActivityIndicator, Image, ScrollView, Text, TextInput, View, useWindowDimensions } from 'react-native';
import Touchable from './Touchable';
import { Search } from 'lucide-react-native';
import BaseModal from './BaseModal';
import { COLORS } from '../constants/Theme';
import { ItemListDTO } from '../types';
import { normalizeSearchText } from '../utils/search.utils';

/** Ancho máximo de la caja y lado de la portada de cada lista. */
const MODAL_MAX_WIDTH = 440;
const COVER_SIZE = 60;
/** Fracción del alto de la pantalla que puede ocupar la lista antes de hacer scroll. */
const LIST_MAX_HEIGHT_RATIO = 0.6;

type ItemListPickerModalProps = {
  visible: boolean;
  title: string;
  /** Texto bajo el título (p.ej. el item que se va a añadir). */
  subtitle?: string;
  lists: Pick<ItemListDTO, 'id' | 'name' | 'imageUrl'>[];
  loading?: boolean;
  emptyText: string;
  error?: string | null;
  onSelect: (listId: string) => void;
  onClose: () => void;
  /** Etiqueta de accesibilidad de cada fila. */
  getAccessibilityLabel?: (listName: string) => string;
  /** Lo que se pinta a la derecha de cada fila (tic, spinner...). */
  renderTrailing?: (listId: string) => React.ReactNode;
  isDisabled?: (listId: string) => boolean;
  /** Botones extra junto a "Cerrar". */
  footer?: React.ReactNode;
  /** Color de los bordes del buscador y de "Cerrar" (dorado en "Añadir a una lista", como "Nueva lista"). */
  borderColor?: string;
};

/**
 * Modal con las listas del usuario (portada o inicial y nombre). Lo usan "Añadir a una lista"
 * y "Mis listas" del perfil; cada uno decide qué pasa al pulsar una lista.
 *
 * Tiene un buscador por nombre (sin distinguir mayúsculas ni tildes) que se vacía al cerrarlo.
 */
export default function ItemListPickerModal({
  visible,
  title,
  subtitle,
  lists,
  loading = false,
  emptyText,
  error,
  onSelect,
  onClose,
  getAccessibilityLabel = (name) => name,
  renderTrailing,
  isDisabled,
  footer,
  borderColor = COLORS.primary,
}: ItemListPickerModalProps) {
  const { height } = useWindowDimensions();
  const [query, setQuery] = useState('');

  // Cada apertura empieza sin filtro
  useEffect(() => {
    if (!visible) setQuery('');
  }, [visible]);

  const filteredLists = useMemo(() => {
    const q = normalizeSearchText(query);
    return q ? lists.filter((list) => normalizeSearchText(list.name).includes(q)) : lists;
  }, [lists, query]);

  return (
    <BaseModal visible={visible} onClose={onClose} maxWidth={MODAL_MAX_WIDTH} fullWidth>
      <Text className="text-secondary text-xl font-bold">{title}</Text>
      {subtitle ? (
        <Text className="text-artistName text-sm mt-1" numberOfLines={1}>{subtitle}</Text>
      ) : null}

      {lists.length > 0 ? (
        <View
          style={{
            flexDirection: 'row', alignItems: 'center', marginTop: 16,
            paddingHorizontal: 12, borderRadius: 10, borderWidth: 1, borderColor,
          }}
        >
          <Search size={18} color={COLORS.secondary} />
          <TextInput
            value={query}
            onChangeText={setQuery}
            placeholder="Buscar lista"
            placeholderTextColor={COLORS.secondary}
            accessibilityLabel="Buscar lista por nombre"
            autoCorrect={false}
            autoCapitalize="none"
            returnKeyType="search"
            underlineColorAndroid="transparent"
            selectionColor={COLORS.secondary}
            style={{ flex: 1, marginLeft: 8, paddingVertical: 8, color: COLORS.secondary, fontSize: 16 }}
          />
        </View>
      ) : null}

      <View style={{ marginTop: 16 }}>
        {loading && lists.length === 0 ? (
          <View style={{ paddingVertical: 32 }}>
            <ActivityIndicator size="large" color={COLORS.primary} />
          </View>
        ) : lists.length === 0 ? (
          <Text className="text-secondary opacity-60 text-center py-8">{emptyText}</Text>
        ) : filteredLists.length === 0 ? (
          <Text className="text-secondary opacity-60 text-center py-8">Ninguna lista coincide con la búsqueda.</Text>
        ) : (
          <ScrollView
            style={{ maxHeight: height * LIST_MAX_HEIGHT_RATIO }}
            showsVerticalScrollIndicator={false}
            keyboardShouldPersistTaps="handled"
          >
            {filteredLists.map((list) => {
              const listId = list.id.toString();
              return (
                <Touchable
                  key={listId}
                  onPress={() => onSelect(listId)}
                  disabled={isDisabled?.(listId)}
                  accessibilityRole="button"
                  accessibilityLabel={getAccessibilityLabel(list.name)}
                  style={{ flexDirection: 'row', alignItems: 'center', paddingVertical: 8 }}
                >
                  {list.imageUrl ? (
                    <Image
                      source={{ uri: list.imageUrl }}
                      style={{ width: COVER_SIZE, height: COVER_SIZE, borderRadius: 10 }}
                    />
                  ) : (
                    <View
                      style={{
                        width: COVER_SIZE, height: COVER_SIZE, borderRadius: 10,
                        backgroundColor: COLORS.secondary, alignItems: 'center', justifyContent: 'center',
                      }}
                    >
                      <Text style={{ color: COLORS.primary, fontSize: 24, fontWeight: 'bold' }}>
                        {list.name.charAt(0).toUpperCase()}
                      </Text>
                    </View>
                  )}
                  <Text
                    style={{ flex: 1, marginLeft: 14, color: COLORS.secondary, fontSize: 17, fontWeight: '600' }}
                    numberOfLines={1}
                  >
                    {list.name}
                  </Text>
                  {renderTrailing?.(listId)}
                </Touchable>
              );
            })}
          </ScrollView>
        )}
      </View>

      {error ? <Text className="text-red-500 text-sm mt-2">{error}</Text> : null}

      <View style={{ flexDirection: 'row', justifyContent: 'flex-end', alignItems: 'center', gap: 12, marginTop: 16 }}>
        <Touchable
          onPress={onClose}
          style={{
            paddingHorizontal: 16, paddingVertical: 8,
            borderRadius: 9999, borderWidth: 1, borderColor,
          }}
        >
          <Text style={{ color: COLORS.secondary, fontWeight: '600' }}>Cerrar</Text>
        </Touchable>
        {footer}
      </View>
    </BaseModal>
  );
}
