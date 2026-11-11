/**
 * Reglas de email y contraseña compartidas por el registro y la recuperación de contraseña.
 * Son las mismas que valida el backend (UserDto / PasswordResetDto), para avisar antes de enviar.
 */

/**
 * Comprobación de formato antes de pedir un código: algo@dominio.tld, sin espacios.
 * Que el buzón existe y es del usuario lo demuestra el código que se le envía.
 */
export const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/;

/**
 * Primer motivo por el que la contraseña no es válida, o null si cumple la política:
 * al menos 8 caracteres, sin espacios, con una mayúscula, un número y un símbolo
 * (cualquier carácter que no sea letra, número o espacio).
 */
export const passwordError = (password: string): string | null => {
  if (password.length < 8) return 'La contraseña debe tener al menos 8 caracteres';
  if (/\s/.test(password)) return 'La contraseña no puede contener espacios';
  if (!/[A-Z]/.test(password)) return 'La contraseña necesita al menos una mayúscula';
  if (!/[0-9]/.test(password)) return 'La contraseña necesita al menos un número';
  if (!/[^A-Za-z0-9\s]/.test(password)) return 'La contraseña necesita al menos un símbolo';
  return null;
};
