package com.demo.demo.controller;

import org.springframework.web.bind.annotation.*;

import com.demo.demo.dto.ItemListDto;
import com.demo.demo.model.ItemList;
import com.demo.demo.service.ItemListService;

import lombok.RequiredArgsConstructor;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/itemlists")
public class ItemListController {

    private final ItemListService itemListService;

    /*
     * GET	
     * /api/itemlists/user/{userId}	
     * Obtiene todas las listas asociadas a un usuario.	
     * Path Variable: userId (Long)	
     * 200 OK: Lista de ItemList asociados al usuario
     * 404 Not Found: Si no se encuentran listas para el usuario.
     */
    @GetMapping("/user/{userId}")
    public List<ItemList> getListsByUser(@PathVariable Long userId) {
        return itemListService.getListsByUser(userId);
    }

    /*
     * POST	
     * /api/itemlists	
     * Crea una nueva lista de items.	
     * Cuerpo: ItemList (JSON)	
     * 201 Created: ItemList creado con éxito	
     * 400 Bad Request: Si los datos enviados son inválidos.
     */
    @PostMapping
    public ItemList createItemList(@RequestBody ItemListDto itemList) {
        return itemListService.saveItemList(itemList);
    }

    /*
     * DELETE	
     * /api/itemlists/{id}	
     * Elimina una lista de items por su id.	
     * Path Variable: id (Long)	
     * 204 No Content: Lista eliminada correctamente	
     * 404 Not Found: Si no se encuentra la lista con ese id.
     */
    @DeleteMapping("/{id}")
    public void deleteItemList(@PathVariable Long id) {
        itemListService.deleteItemList(id);
    }

    /*
     * /api/itemlists/{listId}/add/{itemId}	
     * Agrega un item a una lista existente.	
     * Path Variables: listId (Long), itemId (Long)	
     * 200 OK: ItemList actualizado con el nuevo item.	
     * 404 Not Found: Si no se encuentra la lista o el item.
     */
    @PostMapping("/{listId}/add/{itemId}")
    public ItemList addItemToList(@PathVariable Long listId, @PathVariable Long itemId) {
        return itemListService.addItemToList(listId, itemId);
    }

    /*
     * DELETE	
     * /api/itemlists/{listId}/remove/{itemId}	
     * Elimina un item de una lista existente.	
     * Path Variables: listId (Long), itemId (Long)	
     * 200 OK: ItemList actualizado sin el item eliminado.	
     * 404 Not Found: Si no se encuentra la lista o el item.
     */
    @DeleteMapping("/{listId}/remove/{itemId}")
    public ItemList removeItemFromList(@PathVariable Long listId, @PathVariable Long itemId) {
        return itemListService.removeItemFromList(listId, itemId);
    }
}
