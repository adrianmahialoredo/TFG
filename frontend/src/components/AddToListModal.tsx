import React from 'react';
import { ActivityIndicator } from 'react-native';
import { Check } from 'lucide-react-native';
import ItemListPickerModal from './ItemListPickerModal';
import { COLORS } from '../constants/Theme';
import { useAddToList } from '../hooks/useAddToList';

/** Tiempo que se ve el tic en la lista elegida antes de cerrar el modal. */
const CLOSE_DELAY_MS = 500;

type AddToListModalProps = {
  /** Item que se va a añadir; null = modal cerrado. */
  item: { id: string; title: string } | null;
  onClose: () => void;
};

/**
 * Modal para elegir a qué lista del usuario se añade un item.
 * Al pulsar una lista se añade, se ve el tic un momento y se cierra. Las listas que ya
 * contienen el item salen con el tic y no se pueden pulsar.
 */
export default function AddToListModal({ item, onClose }: AddToListModalProps) {
  const { lists, loading, addingListId, error, contains, addToList } = useAddToList(item?.id ?? null);
  const closeTimer = React.useRef<ReturnType<typeof setTimeout> | null>(null);

  React.useEffect(() => () => {
    if (closeTimer.current) clearTimeout(closeTimer.current);
  }, []);

  const handleSelect = async (listId: string) => {
    if (await addToList(listId)) {
      closeTimer.current = setTimeout(onClose, CLOSE_DELAY_MS);
    }
  };

  return (
    <ItemListPickerModal
      visible={!!item}
      title="Añadir a una lista"
      subtitle={item?.title}
      lists={lists}
      loading={loading}
      emptyText="Aún no tienes listas. Puedes crearlas desde tu perfil."
      error={error}
      onSelect={handleSelect}
      onClose={onClose}
      getAccessibilityLabel={(name) => `Añadir a ${name}`}
      isDisabled={(listId) => contains(listId) || !!addingListId}
      renderTrailing={(listId) =>
        addingListId === listId ? (
          <ActivityIndicator size="small" color={COLORS.primary} />
        ) : contains(listId) ? (
          <Check size={22} color={COLORS.primary} />
        ) : null
      }
    />
  );
}
