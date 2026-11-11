import { useState } from 'react';
import * as authRepository from '../repositories/auth.repository';
import { MIN_FAVOURITE_GENRES, MIN_FAVOURITE_INSTRUMENTS } from '../constants/Preferences';
import { EMAIL_REGEX, passwordError } from '../utils/password.utils';

export const useRegister = () => {
    const [isLoading, setIsLoading] = useState(false);

    const validate = (data: any) => {
        // Mismas reglas que UserDto en el backend, para avisar antes de enviar
        const name: string = data.name?.trim() ?? '';
        const username: string = data.username?.trim() ?? '';
        if (name.length < 2 || name.length > 100) throw new Error('El nombre debe tener entre 2 y 100 caracteres');
        if (!EMAIL_REGEX.test(data.email?.trim() ?? '')) throw new Error('Email no válido');
        if (username.length < 3 || username.length > 20) throw new Error('El usuario debe tener entre 3 y 20 caracteres');
        if (data.password !== data.confirmPassword) throw new Error('Las contraseñas no coinciden');

        const invalidPassword = passwordError(data.password ?? '');
        if (invalidPassword) throw new Error(invalidPassword);

        if ((data.favouriteGenresIds?.length ?? 0) < MIN_FAVOURITE_GENRES) {
            throw new Error(`Elige al menos ${MIN_FAVOURITE_GENRES} géneros favoritos`);
        }
        if ((data.favouriteInstrumentsIds?.length ?? 0) < MIN_FAVOURITE_INSTRUMENTS) {
            throw new Error(`Elige al menos ${MIN_FAVOURITE_INSTRUMENTS} instrumentos`);
        }
    };

    /** Paso 1: valida el formulario y envía el código de verificación al email. */
    const requestCode = async (formData: any) => {
        setIsLoading(true);
        try {
            validate(formData);
            await authRepository.requestVerificationCode(formData.email.trim());
        } finally {
            setIsLoading(false);
        }
    };

    /** Reenvía el código (el formulario ya se validó en el paso 1). */
    const resendCode = (email: string) => authRepository.requestVerificationCode(email.trim());

    /** Paso 2: registra con el código recibido. */
    const register = async (formData: any, verificationCode: string) => {
        setIsLoading(true);
        try {
            validate(formData);
            // El repositorio se encargará de hacer el registro y el login automático
            return await authRepository.register({ ...formData, email: formData.email.trim() }, verificationCode);
        } finally {
            setIsLoading(false);
        }
    };

    return {
        requestCode,
        resendCode,
        register,
        isLoading
    };
};