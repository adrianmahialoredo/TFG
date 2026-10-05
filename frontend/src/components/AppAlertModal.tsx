import React from 'react';
import { Text, TouchableOpacity, View } from 'react-native';
import BaseModal from './BaseModal';

export type AppAlertActionStyle = 'default' | 'cancel' | 'destructive';

export type AppAlertAction = {
  text: string;
  style?: AppAlertActionStyle;
  onPress?: () => void;
};

type AppAlertModalProps = {
  visible: boolean;
  title: string;
  message?: string;
  actions: AppAlertAction[];
  onClose: () => void;
  onActionPress: (action: AppAlertAction) => void;
};

const getButtonClassName = (style: AppAlertActionStyle = 'default') => {
  if (style === 'destructive') return 'bg-red-600';
  if (style === 'cancel') return 'border border-secondary';
  return 'bg-primary';
};

const getButtonTextClassName = (style: AppAlertActionStyle = 'default') => {
  if (style === 'cancel') return 'text-secondary';
  return 'text-secondary';
};

export default function AppAlertModal({
  visible,
  title,
  message,
  actions,
  onClose,
  onActionPress,
}: AppAlertModalProps) {
  return (
    <BaseModal visible={visible} onClose={onClose}>
      <Text className="text-secondary text-lg font-bold mb-2">{title}</Text>
      {message ? <Text className="text-secondary/90 text-base">{message}</Text> : null}

      <View className="flex-row justify-end gap-3 mt-5">
        {actions.map((action, index) => (
          <TouchableOpacity
            key={`${action.text}-${index}`}
            className={`px-4 py-2 rounded-full ${getButtonClassName(action.style)}`}
            onPress={() => onActionPress(action)}
          >
            <Text className={`font-semibold ${getButtonTextClassName(action.style)}`}>{action.text}</Text>
          </TouchableOpacity>
        ))}
      </View>
    </BaseModal>
  );
}

