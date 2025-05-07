package com.demo.demo.controller;

import org.springframework.web.bind.annotation.*;

import com.demo.demo.dto.SongDto;
import com.demo.demo.model.Item;
import com.demo.demo.service.ItemService;

import lombok.RequiredArgsConstructor;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/items")
public class ItemController {
    
    private final ItemService itemService;

    /*
     * GET	
     * /api/items/ordered/all	
     * Obtiene todos los items ordenados alfabéticamente.	
     * ❌ Ninguno	
     * 200 OK: Lista de items ordenados	
     * 500 Internal Server Error: Error del servidor.
     */
    @GetMapping("/ordered/all")
    public List<Item> getAllItemsOrdered() {
        return itemService.getAllItemsOrdered();
    }

    /*
     * GET	
     * /api/items/ordered/songs	
     * Obtiene solo las canciones ordenadas alfabéticamente.	
     * ❌ Ninguno	
     * 200 OK: Lista de canciones ordenadas	
     * 500 Internal Server Error: Error del servidor.
     */
    @GetMapping("ordered/songs")
    public List<Item> getAllSongsOrdered() {
        return itemService.getAllSongsOrdered();
    }

    /*
     * POST	
     * /api/items	
     * Crea un nuevo item (canción, ejercicio, etc.).	
     * Cuerpo: Item (JSON)	
     * 201 Created: Item creado con éxito	
     * 400 Bad Request: Datos inválidos.
     */
    @PostMapping("/songs")
    public Item createSong(@RequestBody SongDto song) {
        return itemService.saveSong(song);
    }

    /*
     * DELETE	
     * /api/items/{id}	
     * Elimina un item por su ID.	
     * Path Variable: id (Long)	
     * 204 No Content: Item eliminado exitosamente	
     * 404 Not Found: Item no encontrado.

     */
    @DeleteMapping("/{id}")
    public void deleteItem(@PathVariable Long id) {
        itemService.deleteItem(id);
    }

}
