# TASK: Replace HomeScreen mascot with Canvas-drawn Zenith logo

## READ FIRST
Read the **full content** of:
- `app/src/main/java/com/example/prtracker/ui/screens/HomeScreen.kt`

---

## STEP 1 — LOCATE THE MASCOT DRAWING BLOCK

In `HomeScreen.kt`, find the block responsible for drawing:
- The cyberpunk stick figure doing a pull-up on parallel bars (rectangular limbs, joint circles, cable details)
- The ambient glow circles drawn **behind** the figure

The HUD panels, dashed connector lines, and any `LocalAppearance`-colored UI elements that are part of the same Canvas block **must remain untouched**.

Identify which drawing API pattern is used (native `android.graphics.Canvas` via `drawIntoCanvas {}`, or Compose `DrawScope` directly). The new mascot code must use **the exact same pattern** already present in the file.

---

## STEP 2 — DECLARE ZENITH BRAND COLORS

Declare these colors as local `val`s inside the same `remember {}` block where the other Paint/Color objects live — or alongside the existing local color declarations. These are **hardcoded and never read from `LocalAppearance`**, so they look identical in every theme:

```kotlin
// ZENITH BRAND COLORS — hardcoded, never from LocalAppearance
val zenithNavy          = Color(0xFF0A1628)    // dark face-mask interior
val zenithCobalt        = Color(0xFF1A4A8A)    // main helmet dome body
val zenithMidBlue       = Color(0xFF2B6CB8)    // dome mid-tone shading
val zenithLightBlue     = Color(0xFF4AACDF)    // highlights, eye shapes, chin grill
val zenithBrightBlue    = Color(0xFF6BD5F5)    // brightest dome gloss spot
val zenithPurple        = Color(0xFF7B35C0)    // cat-ear fill, face-mask frame trim
val zenithDeepPurple    = Color(0xFF4E1E9E)    // ear secondary / shadow
val zenithWhiteSilver   = Color(0xFFDCEEFF)    // ear inner highlight
val zenithGold          = Color(0xFFFFD700)    // flower centers
val zenithBlueFlower    = Color(0xFF6BBCE8)    // blue 6-petal flowers
val zenithPurpleFlower  = Color(0xFF9B50CC)    // purple 5-petal flowers
val zenithGreen         = Color(0xFF2D8B3A)    // leaves
val zenithGreenDark     = Color(0xFF1B5A24)    // leaf center vein
val zenithOutline       = Color(0xFF050F1E)    // dark outline strokes
```

If native Paint objects are used in the file, create a corresponding `Paint` for each color as needed (reuse the same `paint` instance by reassigning `.color` if the existing code already does this).

---

## STEP 3 — DRAW THE ZENITH MASCOT

The mascot is a **fox/kitsune-style cyberpunk mask with a flower crown**. It replaces the pull-up robot figure. Draw it centered in the Canvas. Establish coordinates:

```
cx = canvasWidth / 2
cy = canvasHeight / 2  (or slightly above center if the existing figure was above center)
r  = min(canvasWidth, canvasHeight) * 0.36f   // base radius unit for all proportional math
```

Draw **in this exact layer order** (painter's algorithm — back to front):

---

### LAYER A — Ambient glow (behind everything)
Two soft filled circles in very low alpha to create bloom behind the mask:
- Circle 1: center `(cx, cy)`, radius `r * 1.5f`, color `zenithCobalt` at **10% alpha**
- Circle 2: center `(cx, cy - r * 0.1f)`, radius `r * 0.9f`, color `zenithLightBlue` at **8% alpha**

---

### LAYER B — Outer fox-mask body (the wide kitsune silhouette)
Use a **Path** to draw the overall fox-face outline — this is the widest layer and the background of the entire mask. The shape is a broad rounded-top diamond that narrows to a soft pointed chin and has angular cheek wings:

```
Path points (all relative to cx, cy):
  moveTo(cx,              cy + r * 1.15f)   // bottom chin point (soft tip)
  lineTo(cx + r * 0.42f,  cy + r * 0.95f)   // lower-right cheek
  lineTo(cx + r * 1.15f,  cy + r * 0.08f)   // right cheek wing outer tip
  lineTo(cx + r * 0.72f,  cy - r * 0.62f)   // upper-right dome base
  // Rounded dome top: quadraticBezierTo or cubicBezierTo
  quadTo(cx,              cy - r * 1.12f,    // arc apex (top of dome)
         cx - r * 0.72f,  cy - r * 0.62f)   // upper-left dome base
  lineTo(cx - r * 1.15f,  cy + r * 0.08f)   // left cheek wing outer tip
  lineTo(cx - r * 0.42f,  cy + r * 0.95f)   // lower-left cheek
  close()
```

Fill: `zenithCobalt`. Stroke: `zenithOutline`, stroke width `2dp`.

---

### LAYER C — Upper dome gloss overlay
Simulate the glossy 3D highlight on the top of the dome:
- Oval centered at `(cx - r*0.06f, cy - r*0.68f)`, width `r * 0.88f`, height `r * 0.52f`
- Fill: `zenithBrightBlue` at **28% alpha**

---

### LAYER D — Inner dark face-mask panel
A darker hexagonal/octagonal region sitting inside the lower-center of the helmet — this is the "face" area with the eyes and mouth:

```
Path points:
  moveTo(cx,              cy - r * 0.38f)   // top center
  lineTo(cx + r * 0.54f,  cy - r * 0.08f)   // top-right
  lineTo(cx + r * 0.64f,  cy + r * 0.40f)   // right
  lineTo(cx + r * 0.38f,  cy + r * 0.82f)   // bottom-right
  lineTo(cx,              cy + r * 0.92f)   // bottom center
  lineTo(cx - r * 0.38f,  cy + r * 0.82f)   // bottom-left
  lineTo(cx - r * 0.64f,  cy + r * 0.40f)   // left
  lineTo(cx - r * 0.54f,  cy - r * 0.08f)   // top-left
  close()
```

Fill: `zenithNavy`. Stroke: `zenithPurple`, stroke width `2dp`.

---

### LAYER E — Face-mask purple trim ring
Re-draw the same LAYER D path but **stroke only** (no fill), slightly inflated (~`* 1.025f` from cx/cy), color `zenithPurple`, stroke width `3.5dp`. This creates a visible purple frame between the dark face panel and the blue dome.

---

### LAYER F — Left eye (crescent / drooped half-lidded)
On the dark face mask, the left eye is a horizontal crescent — it looks like a sleepy/squinting eye with the lower arc visible:

1. Draw a horizontal rounded-rect at: left `cx - r*0.44f`, top `cy - r*0.16f`, right `cx - r*0.12f`, bottom `cy - r*0.02f`, corner radius `r*0.05f`, fill `zenithLightBlue`
2. Immediately overdraw a slightly smaller rounded-rect shifted upward to mask the top portion and create the "lid": left `cx - r*0.44f`, top `cy - r*0.16f`, right `cx - r*0.12f`, bottom `cy - r*0.09f`, corner radius `r*0.03f`, fill `zenithNavy`
   (This trims the top of the eye shape, leaving only the lower crescent arc visible in light blue)

---

### LAYER G — Right eye (angular chevron / battle-worn style)
The right eye is two straight strokes meeting at a sharp corner, like a stylized `>` bent downward:

- Segment 1 (horizontal): from `(cx + r*0.12f, cy - r*0.14f)` to `(cx + r*0.40f, cy - r*0.14f)`, color `zenithLightBlue`, stroke width `4dp`, stroke cap ROUND
- Segment 2 (angled down-left): from `(cx + r*0.40f, cy - r*0.14f)` to `(cx + r*0.20f, cy + r*0.04f)`, same color and width

---

### LAYER H — Chin breathing mask / filter grill
Below the eyes at the bottom of the face panel:

1. Draw a rounded-rect for the grill body: left `cx - r*0.29f`, top `cy + r*0.58f`, right `cx + r*0.29f`, bottom `cy + r*0.88f`, corner radius `r*0.07f`, fill `zenithLightBlue`
2. Over it, draw **3 vertical dark slit lines** (respirator slots):
   - Slit L: from `(cx - r*0.16f, cy + r*0.64f)` to `(cx - r*0.16f, cy + r*0.83f)`
   - Slit C: from `(cx,           cy + r*0.64f)` to `(cx,           cy + r*0.83f)`
   - Slit R: from `(cx + r*0.16f, cy + r*0.64f)` to `(cx + r*0.16f, cy + r*0.83f)`
   Each slit: color `zenithNavy`, stroke width `3.5dp`, stroke cap SQUARE

---

### LAYER I — Left cat ear
The left ear protrudes from the upper-left area of the dome:

**Outer triangle** (purple):
```
  point A (tip):        (cx - r*0.27f, cy - r*1.12f)
  point B (base-left):  (cx - r*0.62f, cy - r*0.66f)
  point C (base-right): (cx - r*0.07f, cy - r*0.74f)
```
Fill `zenithPurple`, stroke `zenithOutline` `2dp`

**Inner highlight triangle** (white/silver, slightly inset):
```
  tip:        (cx - r*0.28f, cy - r*1.00f)
  base-left:  (cx - r*0.52f, cy - r*0.69f)
  base-right: (cx - r*0.13f, cy - r*0.75f)
```
Fill `zenithWhiteSilver`

---

### LAYER J — Right cat ear (mirror of Layer I)

**Outer triangle** (purple):
```
  point A (tip):        (cx + r*0.27f, cy - r*1.12f)
  point B (base-left):  (cx + r*0.07f, cy - r*0.74f)
  point C (base-right): (cx + r*0.62f, cy - r*0.66f)
```
Fill `zenithPurple`, stroke `zenithOutline` `2dp`

**Inner highlight triangle**:
```
  tip:        (cx + r*0.28f, cy - r*1.00f)
  base-left:  (cx + r*0.13f, cy - r*0.75f)
  base-right: (cx + r*0.52f, cy - r*0.69f)
```
Fill `zenithWhiteSilver`

---

### LAYER K — Left flower crown cluster
Draw at position base `(cx - r*0.75f, cy - r*0.65f)`. Draw leaves first (behind flowers).

**Leaves** (two elongated leaf ovals, fanning outward-downward):
For each leaf, `canvas.save()`, `canvas.rotate(angle, leafCx, leafCy)`, draw oval, `canvas.restore()` (or equivalent rotate block):
- Leaf 1: center `(cx - r*0.90f, cy - r*0.52f)`, rotation **-35°**, oval size `r*0.26f × r*0.10f`, fill `zenithGreen`; then draw a thin line down its center axis color `zenithGreenDark`, width `1.5dp`
- Leaf 2: center `(cx - r*0.95f, cy - r*0.38f)`, rotation **-55°**, oval size `r*0.22f × r*0.09f`, fill `zenithGreen`; same center vein

**Purple 5-petal flower** (draw before blue flower — sits slightly behind and right of it):
Center: `(cx - r*0.57f, cy - r*0.70f)`. For i in 0..4: rotate `i * 72°` around center, draw an oval of size `r*0.13f × r*0.07f` with its top edge at center (petal extends outward), fill `zenithPurpleFlower`. Then draw center circle radius `r*0.05f` fill `zenithGold`.

**Blue 6-petal flower** (in front of and slightly left of the purple one):
Center: `(cx - r*0.80f, cy - r*0.75f)`. For i in 0..5: rotate `i * 60°` around center, draw oval `r*0.11f × r*0.06f`, fill `zenithBlueFlower`. Center circle radius `r*0.045f` fill `zenithGold`.

---

### LAYER L — Right flower crown cluster (mirror of Layer K)

**Leaves**:
- Leaf 1: center `(cx + r*0.90f, cy - r*0.52f)`, rotation **+35°**, same size/color as left
- Leaf 2: center `(cx + r*0.95f, cy - r*0.38f)`, rotation **+55°**, same size/color as left

**Blue 6-petal flower** (right side — blue flower is on the outer/right position):
Center: `(cx + r*0.80f, cy - r*0.75f)`. Same petals as left blue flower, mirrored rotation.

**Purple 5-petal flower** (inner position on right side):
Center: `(cx + r*0.57f, cy - r*0.70f)`. Same petals as left purple flower, mirrored rotation.

---

### LAYER M — Final silhouette stroke
Re-draw the outer fox-mask body Path from **LAYER B** as **stroke only** (no fill), color `zenithOutline`, stroke width `3dp`. This sharpens the outer edge of the mask.

---

## STEP 4 — REMOVE OLD MASCOT CODE

Delete **only** the old stick figure drawing code. This includes:
- All old figure body-part draw calls (head oval, torso/limbs rectangles, joint circles, bar structure)
- Cable/wire detail drawing
- Any ambient glow circles that existed solely for the old robot

Do **not** delete or modify:
- The HUD panel drawing block (left/right floating panels with tick marks)
- Dashed connector line drawing
- The Canvas composable/block wrapper itself
- Any `remember {}` blocks shared with the HUD panels
- Any `Paint` objects or state outside the mascot drawing

---

## STEP 5 — CONNECT HUD PANELS TO THE NEW MASCOT
The existing dashed connector lines run from the HUD panels to the old figure's shoulders. Update the **endpoints** of those two connector lines only — the line endpoint that was attached to the robot's shoulder should now attach to approximately:
- Left connector endpoint: `(cx - r*0.80f, cy - r*0.10f)` (left cheek-wing area of the mask)
- Right connector endpoint: `(cx + r*0.80f, cy - r*0.10f)` (right cheek-wing area of the mask)

The HUD panel origin points and visual style must remain unchanged.

---

## CONVENTIONS
- **No new imports** unless the Canvas drawing API being used requires something not yet imported
- **No new dependencies**
- All Zenith color `val`s are declared **locally** inside the drawing lambda or `remember {}` block — do NOT add them to `Color.kt`
- Do **not** modify any file other than `HomeScreen.kt`
- Do **not** change stats display, navigation buttons, `GridBackground()`, ViewModel reads, or anything outside the Canvas mascot drawing area
- The Zenith colors are **always hardcoded** — they must not reference `LocalAppearance`, `AppearanceSettings`, or any dynamic color system; the logo must look identical in every theme
- All coordinate math uses the `r`-based proportional system defined in STEP 3
- If the existing code uses `android.graphics.Paint` + `nativeCanvas`, keep using that pattern; if it uses Compose `DrawScope`, keep using that — do not switch APIs
- Run the `prtracker-builder` skill after making changes to verify clean compilation
