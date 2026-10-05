import { useState } from 'react';
import * as authRepository from '../repositories/auth.repository';

export const useRegister = () => {
    const [isLoading, setIsLoading] = useState(false);

    const validate = (data: any) => {
        // Mismas reglas que UserDto en el backend, para avisar antes de enviar
        const name: string = data.name?.trim() ?? '';
        const username: string = data.username?.trim() ?? '';
        if (name.length < 2 || name.length > 100) throw new Error('El nombre debe tener entre 2 y 100 caracteres');
        if (!data.email?.includes('@')) throw new Error('Email no válido');
        if (username.length < 3 || username.length > 20) throw new Error('El usuario debe tener entre 3 y 20 caracteres');
        if (data.password !== data.confirmPassword) throw new Error('Las contraseñas no coinciden');

        // Cualquier carácter que no sea letra, número o espacio cuenta como símbolo
        const password: string = data.password ?? '';
        if (password.length < 8) throw new Error('La contraseña debe tener al menos 8 caracteres');
        if (/\s/.test(password)) throw new Error('La contraseña no puede contener espacios');
        if (!/[A-Z]/.test(password)) throw new Error('La contraseña necesita al menos una mayúscula');
        if (!/[0-9]/.test(password)) throw new Error('La contraseña necesita al menos un número');
        if (!/[^A-Za-z0-9\s]/.test(password)) throw new Error('La contraseña necesita al menos un símbolo');
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