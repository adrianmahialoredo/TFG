package com.uo289390.toco.dto.validation;

import jakarta.validation.groups.Default;

/**
 * Validation group for constraints that only apply when a resource is created.
 *
 * It extends {@link Default}, so validating with this group also checks every
 * constraint without an explicit group. Create endpoints use
 * {@code @Validated(OnCreate.class)} and update endpoints use {@code @Valid}.
 *
 * @author Adrián Mahía
 * @version 1.0
 * @since 2026-10-03
 */
public interface OnCreate extends Default {
}
