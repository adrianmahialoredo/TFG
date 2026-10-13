package com.uo289390.toco.security;

import static com.uo289390.toco.utils.EntityResolver.findItemList;

import java.util.Collection;
import java.util.Objects;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * Ownership checks used from {@code @PreAuthorize} expressions, e.g.
 * {@code @PreAuthorize("hasRole('ADMIN') or @ownership.isSelf(#id, authentication)")}.
 *
 * The authenticated principal is the user ID set by {@link JwtAuthenticationFilter}, so
 * {@link Authentication#getName()} is that ID as a string. Admins are not handled here: the
 * expressions check {@code hasRole('ADMIN')} first.
 *
 * @author Adrián Mahía
 * @version 1.0
 * @since 2026-10-13
 */
@Component("ownership")
public class OwnershipChecker {

    /**
     * Whether the given user ID is the authenticated user.
     *
     * @param userId the user the request acts on
     * @param authentication the current authentication
     * @return {@code true} if the authenticated user is {@code userId}
     */
    public boolean isSelf(Long userId, Authentication authentication) {
        return userId != null
                && authentication != null
                && Objects.equals(String.valueOf(userId), authentication.getName());
    }

    /**
     * Whether every user ID of a batch read is the authenticated user.
     *
     * @param userIds the users requested (e.g. the body of {@code POST /api/users/by-ids})
     * @param authentication the current authentication
     * @return {@code true} if the collection is empty or only contains the authenticated user
     */
    public boolean isOnlySelf(Collection<Long> userIds, Authentication authentication) {
        return userIds == null || userIds.stream().allMatch(userId -> isSelf(userId, authentication));
    }

    /**
     * Whether the authenticated user owns every item list of a batch read.
     *
     * @param itemListIds the lists requested (e.g. the body of {@code POST /api/item-lists/by-ids})
     * @param authentication the current authentication
     * @return {@code true} if every existing list belongs to the authenticated user
     */
    public boolean ownsAllItemLists(Collection<Long> itemListIds, Authentication authentication) {
        return itemListIds == null || itemListIds.stream().allMatch(id -> ownsItemList(id, authentication));
    }

    /**
     * Whether the authenticated user owns the given item list.
     *
     * A list that does not exist is allowed through so the controller answers 404 as usual
     * instead of a misleading 403.
     *
     * @param itemListId the item list the request acts on
     * @param authentication the current authentication
     * @return {@code true} if the list belongs to the authenticated user or does not exist
     */
    public boolean ownsItemList(Long itemListId, Authentication authentication) {
        return findItemList(itemListId)
                .map(itemList -> itemList.getUser() != null && isSelf(itemList.getUser().getId(), authentication))
                .orElse(true);
    }
}
