# Feature Specification: 2D Effect Showcase

**Feature Branch**: `001-effect-showcase`
**Created**: 2026-02-22
**Status**: Draft
**Input**: User description: "i want a app with a simple menu where you select some 2d graphice effekt,
and then that effect renders in fullscreen. If possible the user should be able to interact in some
meningfull way with the effekt using touch or tilting the phone or some other way of inputing feedback
that affects the effekt."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Browse & Launch an Effect (Priority: P1)

A viewer opens the app and is greeted by a clean menu listing all available 2D graphic effects by
name. They tap an effect to launch it, and within one second it fills the entire screen and begins
animating. This is the core value of the app — every other feature depends on this working.

**Why this priority**: Without the ability to select and display an effect, the app has no value.
This story defines the minimum viable product.

**Independent Test**: Launch the app, tap any effect in the menu, and confirm the effect fills the
screen and animates smoothly within one second. No further interaction required to validate this story.

**Acceptance Scenarios**:

1. **Given** the app has just opened, **When** the menu is displayed, **Then** all available effects
   are listed with their names visible and the list is scrollable if there are more entries than fit
   on screen.
2. **Given** the menu is visible, **When** the viewer taps an effect, **Then** the effect fills the
   full screen within 1 second and begins animating at a smooth, consistent frame rate.
3. **Given** an effect is running fullscreen, **When** the viewer presses the device back button or a
   visible back control, **Then** the app returns to the menu.

---

### User Story 2 - Interact with a Running Effect (Priority: P2)

While an effect is running fullscreen, the viewer can influence it using their body or fingers —
touching the screen, dragging, or tilting the device. The effect reacts visibly and immediately to
their input in a way that feels intentional and satisfying. Each effect defines what kind of
interaction makes sense for its visuals (e.g., touch attracts particles, tilt shifts a field of
motion, dragging draws trails).

**Why this priority**: Interactivity transforms a passive screensaver into an engaging experience,
which is the differentiating goal called out in the requirements.

**Independent Test**: With any single effect running, perform a supported input gesture (tap/drag or
tilt). Confirm the effect responds visibly within one frame and the response is clearly linked to the
input (e.g., particles move toward a touch point).

**Acceptance Scenarios**:

1. **Given** an effect is running fullscreen, **When** the viewer touches or drags on the screen,
   **Then** the effect changes in a visible, direct way within one rendered frame.
2. **Given** an effect is running fullscreen and the device supports motion sensing, **When** the
   viewer tilts the device, **Then** the effect responds to the tilt direction in a perceptible way.
3. **Given** the viewer is actively interacting, **When** they stop providing input, **Then** the
   effect gracefully returns to or continues its default animated state without abrupt jumps.
4. **Given** two or more simultaneous touch points, **When** the viewer uses multi-touch, **Then**
   the effect responds to all active touch points.

---

### User Story 3 - Switch Effects Without Restarting the App (Priority: P3)

After watching one effect, the viewer wants to explore another without leaving the fullscreen view
all the way back to the root menu. A quick-switch gesture or overlay control lets them jump directly
to the next or previous effect, or open a compact picker that overlays the current effect.

**Why this priority**: Reduces friction for exploration. Not required for the app to function but
materially improves the experience when browsing several effects in sequence.

**Independent Test**: With an effect running, activate the quick-switch control (swipe from edge or
tap an overlay button), select a different effect, and confirm it launches without returning to the
main menu.

**Acceptance Scenarios**:

1. **Given** an effect is running fullscreen, **When** the viewer swipes from the left edge or taps
   a visible overlay control, **Then** a compact effect picker or next/previous control appears over
   the current effect.
2. **Given** the compact picker is visible, **When** the viewer selects a different effect, **Then**
   the new effect begins rendering fullscreen immediately, replacing the previous one.
3. **Given** the compact picker is visible, **When** the viewer dismisses it without selecting,
   **Then** the original effect resumes without interruption.

---

### Edge Cases

- What happens when the device does not support a motion sensor (e.g., tilt input)? The effect MUST
  still run and be fully interactive via touch; tilt-based controls silently deactivate.
- What happens if an effect fails to initialise (e.g., missing asset or unexpected error)? The app
  MUST return to the menu and display a brief, human-readable error message; it MUST NOT crash.
- What happens when the app is interrupted by a phone call or notification? The effect MUST pause
  while the app is not in the foreground and resume when focus returns.
- What happens when the effect list is empty (e.g., no effects bundled)? The menu MUST display a
  clear "No effects available" message rather than an empty or broken screen.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The app MUST display a menu listing all 29 available 2D effects, grouped by category,
  when launched. (See Effect Catalogue entity for the full list and groupings.)
- **FR-002**: The menu MUST support scrolling when the number of effects exceeds the visible area.
- **FR-003**: Users MUST be able to select an effect from the menu to launch it fullscreen.
- **FR-004**: A selected effect MUST begin rendering in fullscreen within 1 second of selection.
- **FR-005**: The effect MUST animate continuously at a smooth frame rate while fullscreen.
- **FR-006**: Users MUST be able to return to the menu from any running effect.
- **FR-007**: Each effect MUST respond to touch input (single tap and drag) in a visually meaningful way.
- **FR-008**: Each effect MUST declare whether it supports tilt input; effects that support tilt MUST
  respond to device orientation changes.
- **FR-009**: Multi-touch input (two or more simultaneous touch points) MUST be passed to effects
  that declare multi-touch support.
- **FR-010**: Effects MUST pause when the app loses foreground focus and resume when focus returns.
- **FR-011**: If an effect fails to start, the app MUST return to the menu and display a readable
  error message without crashing.
- **FR-012**: Users MUST be able to switch to a different effect from the fullscreen view without
  returning to the main menu (quick-switch).
- **FR-013**: The menu MUST organise effects into named category groups; each group heading MUST be
  visible and the menu MUST allow collapsing/expanding groups or scrolling past them.

### Key Entities

- **Effect**: A self-contained animated visual identified by a unique name. Declares which
  interaction types it supports (touch, multi-touch, tilt). Drives its own render loop. Belongs
  to exactly one category.
- **Interaction Event**: A discrete input signal (touch point coordinates, drag vector, or device
  tilt angles) that an Effect consumes to update its visual state.
- **Effect Catalogue**: The ordered list of all available Effects presented in the menu, grouped
  by category. Immutable at runtime for a given app version. Initial catalogue (29 effects):

  | # | Category | Effect | Brief description |
  |---|----------|--------|-------------------|
  | 1 | Raster / Plasma | Plasma | Smooth, shifting color waves generated with sine functions |
  | 2 | Raster / Plasma | Copper bars | Horizontal color gradient bars sweeping down the screen (Amiga classic) |
  | 3 | Raster / Plasma | Raster bars | Similar to copper bars, often with color cycling |
  | 4 | Raster / Plasma | Color cycling | Palette rotation to create animated effects without moving pixels |
  | 5 | Tunnel & 3D Illusions | Tunnel effect | Zooming into an infinite textured tunnel |
  | 6 | Tunnel & 3D Illusions | Voxel landscape | Ray-cast height-mapped terrain (Comanche-style) |
  | 7 | Tunnel & 3D Illusions | Dot tunnel | A tunnel made entirely of animated dot particles |
  | 8 | Tunnel & 3D Illusions | Starfield | Classic 2D or 3D scrolling star simulation |
  | 9 | Tunnel & 3D Illusions | Warp zoom | Texture zooming toward the viewer endlessly |
  | 10 | Blobs & Metaballs | Metaballs | Organic blobby shapes that merge and separate |
  | 11 | Blobs & Metaballs | Blobs | Simpler version of metaballs, often 2D |
  | 12 | Particles | Particle systems | Fountains, explosions, snow, sparks |
  | 13 | Particles | Dot explosions | Points flying outward from a center |
  | 14 | Particles | Twisted particle ribbons | Trails of particles forming spirals |
  | 15 | Distortion & Deformation | Sine scroller | Text or graphics waving along a sine path |
  | 16 | Distortion & Deformation | Rubber / jelly distortion | Meshes deforming with a wobbly sine warp |
  | 17 | Distortion & Deformation | Twister | A vertical column of polygons rotating and twisting |
  | 18 | Distortion & Deformation | Morphing | Smooth interpolation between two shapes |
  | 19 | Distortion & Deformation | Wobble / wave effect | The entire screen rippling like a flag |
  | 20 | Rotozoom | Rotozoom | A texture being rotated and scaled simultaneously in real time |
  | 21 | 2D Classics | Scrolltext | Horizontally scrolling text, often with greetings |
  | 22 | 2D Classics | Bobs | Bouncing sprites/blobs flying around the screen |
  | 23 | 2D Classics | Sprites multiplexing | Many sprites animated simultaneously beyond typical limits |
  | 24 | 2D Classics | Checkerboard zoom | A rotating/zooming tiled checkerboard pattern |
  | 25 | 2D Classics | Kaleidoscope | Mirrored, radially symmetrical patterns |
  | 26 | 2D Classics | Interference patterns | Overlapping sine-wave grids creating moiré effects |
  | 27 | Fractals | Mandelbrot / Julia set zoom | Zooming into fractal boundaries |
  | 28 | Fractals | IFS fractals | Iterated function systems such as ferns and Sierpinski triangles |
  | 29 | Fractals | Fractal landscapes | Midpoint displacement terrain generation |

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A viewer can select an effect from the menu and see it animating fullscreen in under
  1 second on a mid-range Android device.
- **SC-002**: All effects maintain a smooth, consistent frame rate with no visible stuttering during
  normal viewing (no interaction) on the target device class.
- **SC-003**: Touch or tilt input produces a visible, clearly linked response in the effect within
  one rendered frame of the input event.
- **SC-004**: A viewer can explore three different effects in under 30 seconds using only the
  quick-switch control, without returning to the main menu.
- **SC-005**: The app recovers gracefully from a failed effect launch 100% of the time — no crashes,
  always returns to a usable state.

## Assumptions

- The initial catalogue contains exactly 29 effects across 8 categories; the set is fixed at build
  time (no dynamic loading or downloads).
- "Smooth frame rate" is defined by the constitution's 60 fps target on the declared minimum Android
  API level.
- Tilt input uses the device's built-in accelerometer or gyroscope; no external hardware is required.
- The app targets portrait and landscape orientations; effects render in whichever orientation the
  device is currently held.
- No user accounts, persistence of preferences, or network connectivity are required for this feature.
