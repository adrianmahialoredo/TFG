import React, { createContext, useCallback, useContext, useMemo, useState } from 'react';
import AppAlertModal, { AppAlertAction } from '../components/AppAlertModal';

type ShowAlertParams = {
  title: string;
  message?: string;
  actions?: AppAlertAction[];
};

type AlertModalContextType = {
  showAlert: (params: ShowAlertParams) => void;
  hideAlert: () => void;
};

const AlertModalContext = createContext<AlertModalContextType | undefined>(undefined);

const DEFAULT_ACTIONS: AppAlertAction[] = [{ text: 'Aceptar' }];

export const AlertModalProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [visible, setVisible] = useState(false);
  const [title, setTitle] = useState('');
  const [message, setMessage] = useState('');
  const [actions, setActions] = useState<AppAlertAction[]>(DEFAULT_ACTIONS);

  const hideAlert = useCallback(() => {
    setVisible(false);
  }, []);

  const showAlert = useCallback(({ title, message, actions }: ShowAlertParams) => {
    setTitle(title);
    setMessage(message || '');
    setActions(actions && actions.length > 0 ? actions : DEFAULT_ACTIONS);
    setVisible(true);
  }, []);

  const handleActionPress = useCallback(
    (action: AppAlertAction) => {
      hideAlert();
      if (action.onPress) {
        setTimeout(() => action.onPress && action.onPress(), 0);
      }
    },
    [hideAlert]
  );

  const contextValue = useMemo(
    () => ({ showAlert, hideAlert }),
    [showAlert, hideAlert]
  );

  return (
    <AlertModalContext.Provider value={contextValue}>
      {children}
      <AppAlertModal
        visible={visible}
        title={title}
        message={message}
        actions={actions}
        onClose={hideAlert}
        onActionPress={handleActionPress}
      />
    </AlertModalContext.Provider>
  );
};

export const useAppAlert = (): AlertModalContextType => {
  const context = useContext(AlertModalContext);
  if (!context) {
    throw new Error('useAppAlert debe usarse dentro de AlertModalProvider');
  }
  return context;
};
