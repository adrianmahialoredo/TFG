/**
 * Barrel export para todos los local modules
 * Facilita las importaciones desde otros archivos
 * 
 * Uso: import * as genreLocal from '@/local/genre.local';
 *      o para user.local: import { saveSingle, getById } from '@/local/user.local';
 */

export * from './storage.local';
export * from './user.local';


