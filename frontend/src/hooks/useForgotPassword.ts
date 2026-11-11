import { useState } from 'react';
import * as authRepository from '../repositories/auth.repository';
import { EMAIL_REGEX, passwordError } from '../utils/password.utils';

/**
 * Pasos de "Olvidé mi contraseña":
 * - 'email': se escribe el email y se pide el código;
 * - 'code': modal del código (se comprueba en el backend sin gastarlo);
 * - 'password': con el código bueno, se elige la contraseña nueva.
 */
export type ForgotPasswordStep = 'email' | 'code' | 'password';

export const useForgotPassword = (initialEmail = '') => {
  const [step, setStep] = useState<ForgotPasswordStep>('email');
  const [email, setEmail] = useState(initialEmail);
  const [code, setCode] = useState('');
  const [codeError, setCodeError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  /** Paso 1: valida el email y pide el código. Un 429 (pedido hace < 60 s) lleva igualmente al modal. */
  const requestCode = async () => {
    const trimmed = email.trim();
    if (!EMAIL_REGEX.test(trimmed)) throw new Error('Email no válido');
    setIsLoading(true);
    try {
      await authRepository.requestPasswordResetCode(trimmed);
      setCodeError(null);
      setStep('code');
    } catch (error: any) {
      if (error?.status === 429) {
        // El código que se envió hace menos de un minuto sigue valiendo
        setCodeError('Ya te enviamos un código hace poco, revisa tu email');
        setStep('code');
        return;
      }
      throw error;
    } finally {
      setIsLoading(false);
    }
  };

  /** Reenvía el código desde el modal. */
  const resendCode = () => authRepository.requestPasswordResetCode(email.trim());

  /** Paso 2: comprueba el código; si es correcto, pasa a pedir la contraseña nueva. */
  const submitCode = async (typed: string) => {
    setCodeError(null);
    setIsLoading(true);
    try {
      await authRepository.verifyPasswordResetCode(email.trim(), typed);
      setCode(typed);
      setStep('password');
    } catch (error: any) {
      setCodeError(error?.message ?? 'El código no es correcto');
    } finally {
      setIsLoading(false);
    }
  };

  /** Paso 3: valida y guarda la contraseña nueva. */
  const resetPassword = async (password: string, confirmPassword: string) => {
    if (password !== confirmPassword) throw new Error('Las contraseñas no coinciden');
    const invalid = passwordError(password);
    if (invalid) throw new Error(invalid);
    setIsLoading(true);
    try {
      await authRepository.resetPassword(email.trim(), code, password);
    } catch (error: any) {
      // Si el código caducó mientras tanto, hay que volver a pedirlo
      if (error?.status === 400 && /código/i.test(error?.message ?? '')) {
        setCode('');
        setStep('email');
      }
      throw error;
    } finally {
      setIsLoading(false);
    }
  };

  /** Cierra el modal del código sin perder el email escrito. */
  const cancelCode = () => setStep('email');

  return {
    step,
    email,
    setEmail,
    codeError,
    isLoading,
    requestCode,
    resendCode,
    submitCode,
    resetPassword,
    cancelCode,
  };
};
