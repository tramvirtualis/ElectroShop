# Futuristic Theme Implementation - Quick Reference

## Summary

Your `home.html` now has a **premium futuristic dark theme** with:
- Dark gradient background (#0a0e27 to #1a1f35)
- Neon blue/cyan accents (#00BFFF, #00FFFF)
- Orbitron + Rajdhani fonts
- Glowing buttons and cards
- Smooth hover animations
- Category filtering with active states

## To Apply Theme to Other Pages

### Option 1: Copy the style block (Simplest)

For each page template, copy the entire `<style>` section from `home.html` (lines 15-385) and paste it into the `<head>` of that page.

Example structure:
```html
<!DOCTYPE html>
<html lang="vi" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>Page Title</title>
    
    <!-- Google Fonts -->
    <link href="https://fonts.googleapis.com/css2?family=Orbitron:wght@400;700;900&family=Rajdhani:wght@300;400;600;700&display=swap" rel="stylesheet">
    
    <!-- Bootstrap & FontAwesome -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css" rel="stylesheet">

    <!-- Paste style block from home.html here (lines 15-385) -->
    <style>
        /* All styles from home.html */
    </style>
</head>
```

### Option 2: Extract to shared CSS file

1. Create `src/main/resources/static/css/futuristic-theme.css`
2. Copy styles from home.html (lines 15-385) into this file
3. Add to each page:
   ```html
   <link rel="stylesheet" th:href="@{/css/futuristic-theme.css}">
   ```

## Key Classes to Use

### Layout Structure
```html
<!-- Header -->
<header class="premium-header">
    <div class="container">
        <a class="brand-logo" href="/">HomeTech</a>
        <!-- Navigation -->
    </div>
</header>

<!-- Content -->
<div class="container">
    <!-- Your content -->
</div>

<!-- Footer -->
<footer class="premium-footer">
    <p>© 2025 HomeTech</p>
</footer>
```

### Section Titles
```html
<h2 class="section-title">Your Title</h2>
```

### Buttons
```html
<!-- Primary -->
<a class="btn-premium">Button Text</a>

<!-- Outline -->
<a class="btn-premium-outline">Outline</a>

<!-- Danger -->
<a class="btn-premium btn-logout">Delete</a>
```

### Cards
```html
<div class="card-premium">
    <div class="card-header-premium">Header</div>
    <div class="card-body">Content</div>
</div>
```

### Category Pills
```html
<a class="category-pill">Category Name</a>
<a class="category-pill active">Active Category</a>
```

### Product Cards
```html
<div class="product-grid">
    <div class="product-card">
        <div class="product-image-container">
            <img class="product-image" src="...">
        </div>
        <div class="product-info">
            <h6 class="product-name">Name</h6>
            <p class="product-price">Price</p>
            <a class="btn-view-detail">View</a>
        </div>
    </div>
</div>
```

### Text Colors
```html
<span class="text-premium-primary">Blue Text</span>
<span class="text-premium-cyan">Cyan Text</span>
<span class="text-premium-muted">Muted Text</span>
```

## Pages to Update

Priority order:
1. ✅ `home.html` - **DONE**
2. ⏳ `products/index.html` - Products listing
3. ⏳ `cart/index.html` - Shopping cart
4. ⏳ `auth/login.html` - Login page
5. ⏳ `auth/register.html` - Register page
6. ⏳ Other pages as needed

## Quick Updates Per Page Type

### Products Listing
- Replace `<div class="card">` with `<div class="product-card">`
- Replace `<h1>` with `<h2 class="section-title">`
- Replace Bootstrap buttons with `.btn-premium` classes

### Cart Page
- Use `.card-premium` for cart summary
- Style quantity controls with `.btn-premium-outline`
- Update total price styling with `.text-premium-cyan`

### Auth Pages
- Wrap forms in `.card-premium`
- Style inputs like `.search-input` from home.html
- Use `.btn-premium` for submit buttons

## Color Palette

| Color | Value | Usage |
|-------|-------|-------|
| Primary Blue | `#00BFFF` | Primary actions, borders |
| Cyan | `#00FFFF` | Secondary accents, text |
| Dark Navy | `#0a0e27` | Background base |
| Light Gray | `#e0e6ed` | Text color |
| Muted | `#6c757d` | Secondary text |

## Animations

- `fadeInUp` - Fade in from bottom (used on product cards)
- `backgroundFloat` - Animated background particles
- Hover effects - Lift and glow on cards/buttons

All animations are smooth and not jarring.

## Responsive Design

The theme is fully responsive:
- Product grid: 4 columns → 2 columns → 1 column
- Category pills wrap on mobile
- Section titles scale down
- Buttons become touch-friendly

## Testing

After updating each page, test:
- ✅ Dark background visible
- ✅ Neon blue/cyan colors present
- ✅ Hover effects work
- ✅ All Thymeleaf logic works
- ✅ Mobile responsive
- ✅ No console errors

## Need Help?

Reference files created:
- `STYLES_UPDATE_GUIDE.md` - Detailed styling guide
- `FUTURISTIC_THEME_IMPLEMENTATION.md` - This file
- `home.html` - Reference implementation

## Tips

1. **Keep it consistent** - Use the same classes across all pages
2. **Don't overdo animations** - Keep them subtle
3. **Test on mobile** - Responsive design is important
4. **Preserve Thymeleaf** - Don't change `th:*` attributes
5. **Use the classes** - Leverage existing CSS rather than writing custom styles




