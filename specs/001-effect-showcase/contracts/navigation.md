# Contract: Navigation Routes

**Feature**: `001-effect-showcase` | **Date**: 2026-02-22

---

## Route Definitions

```kotlin
// AppNavGraph.kt
sealed class Screen(val route: String) {
    object Menu   : Screen("menu")
    object Effect : Screen("effect/{effectId}") {
        fun withId(effectId: String) = "effect/$effectId"
    }
}
```

| Route | Screen | Description |
|-------|--------|-------------|
| `menu` | `MenuScreen` | Category-grouped list of all 29 effects |
| `effect/{effectId}` | `EffectScreen` | Fullscreen GL render for the given effect |

---

## Route Parameters

### `effect/{effectId}`

| Parameter | Type | Constraints |
|-----------|------|-------------|
| `effectId` | `String` (path segment) | Must match an `EffectDescriptor.id` in `EffectCatalogue.effects` |

**Validation**:
- On navigation to `effect/{effectId}`, `EffectViewModel` looks up the ID in
  `EffectCatalogue`.
- If the ID is **not found**:
  1. Navigation pops back to `menu`.
  2. A `Snackbar` is shown: *"Unknown effect"*.
  3. No crash; no blank screen.
- If the ID **is found**: the corresponding `EffectRenderer` is initialised and
  the render loop starts.

---

## Navigation Rules

### Forward navigation

```
menu  ──tap effect──►  effect/{effectId}
```

- Triggered by tapping any item in `MenuScreen`.
- Uses `navController.navigate(Screen.Effect.withId(descriptor.id))`.
- The `menu` destination remains on the back stack.

### Back navigation

```
effect/{effectId}  ──Back press──►  menu
```

- Standard `BackHandler` / system back.
- Always returns to `menu`; the effect screen is NOT added to the back stack
  with `launchSingleTop = true` to prevent double entries.

### Quick-switch (US3)

```
effect/{effectId}  ──swipe left edge──►  QuickSwitchOverlay (Compose overlay)
                   ──tap new effect──►   effect/{newEffectId}  (replaces current)
                   ──dismiss overlay──►  effect/{effectId}     (no navigation)
```

- The overlay is a Compose `Box` drawn on top of `EffectScreen`; it is **not a
  separate navigation destination**.
- Selecting a new effect calls:
  ```kotlin
  navController.navigate(Screen.Effect.withId(newId)) {
      popUpTo(Screen.Effect.route) { inclusive = true }
  }
  ```
  This replaces the current effect entry without adding to the back stack, so
  Back still returns to `menu`.
- Dismissing the overlay (tap outside or swipe right) hides it without
  navigating.

---

## Start Destination

```kotlin
NavHost(
    navController = navController,
    startDestination = Screen.Menu.route
) { ... }
```

The app always starts at `menu`. Deep links are not supported in this feature.

---

## Back Stack Invariant

At any point, the back stack contains at most two entries:

```
[menu]  or  [menu, effect/{effectId}]
```

- Quick-switch replaces `effect/{effectId}` in-place; the stack never grows
  beyond two entries.
- This invariant prevents unbounded back-stack growth during rapid effect switching.

---

## Error Snackbar

When an invalid `effectId` is detected:

```kotlin
// In EffectViewModel or EffectScreen
LaunchedEffect(invalidEffect) {
    snackbarHostState.showSnackbar("Unknown effect")
    navController.popBackStack(Screen.Menu.route, inclusive = false)
}
```

The snackbar is shown on the `menu` screen after navigation completes.
