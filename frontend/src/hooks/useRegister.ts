import { useState } from 'react';
import * as authRepository from '../repositories/auth.repository';

export const useRegister = () => {
    const [isLoading, setIsLoading] = useState(false);

    const validate = (data: any) => {
        if (!data.name?.trim()) throw new Error('El nombre es obligatorio');
        if (!data.email?.includes('@')) throw new Error('Email no válido');
        if (data.username?.length < 3) throw new Error('El usuario debe tener 3+ caracteres');
        if (data.password !== data.confirmPassword) throw new Error('Las contraseñas no coinciden');

        const passRegex = /^(?=.*[A-Z])(?=.*[0-9])(?=.*[@$!%*?&])[A-Za-z0-9@$!%*?&]{8,}$/;
        if (!passRegex.test(data.password)) {
            throw new Error('Contraseña débil: requiere Mayúscula, Número y Símbolo');
        }
    };

    const register = async (formData: any) => {
        setIsLoading(true);
        try {
            validate(formData);
            // El repositorio se encargará de hacer el registro y el login automático
            return await authRepository.register(formData);
        } finally {
            setIsLoading(false);
        }
    };

    return {
        register,
        isLoading
    };
};