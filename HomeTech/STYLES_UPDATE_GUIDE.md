# HomeTech Futuristic Theme - Update Guide

## Overview
This guide provides a **shared CSS stylesheet** that can be applied to all pages in the HomeTech project to maintain consistent futuristic UI styling.

## Implementation Strategy

Since many pages reference `layout/base`, you should create a shared CSS file that includes all the futuristic styling from `home.html`.

### Step 1: Create Shared Stylesheet

Create a file: `src/main/resources/static/css/futuristic-theme.css`

Copy the entire `<style>` section from `home.html` (lines 15-385) into this file.

### Step 2: Include in All Pages

Add this to the `<head>` of every page template:

```html
<!-- Futuristic Theme Styles -->
<link rel="stylesheet" th:href="@{/css/futuristic-theme.css}">
```

### Step 3: Apply Premium Classes to Components

Update your templates to use the new classes:

#### Headers
```html
<header class="premium-header">
    <div class="container">
        <a class="brand-logo" href="/">HomeTech</a>
    </div>
</header>
```

#### Section Titles
```html
<h2 class="section-title">Your Title</h2>
```

#### Buttons
```html
<a class="btn-premium">Button Text</a>
<a class="btn-premium-outline">Outline Button</a>
```

#### Category Pills
```html
<a class="category-pill active">Category Name</a>
```

#### Product Cards
```html
<div class="product-card">
    <div class="product-image-container">
        <img class="product-image" src="...">
    </div>
    <div class="product-info">
        <h6 class="product-name">Product Name</h6>
        <p class="product-price">Price</p>
        <a class="btn-view-detail">View Details</a>
    </div>
</div>
```

#### Regular Cards
```html
<div class="card-premium">
    <div class="card-header-premium">Header</div>
    <div class="card-body">Content</div>
</div>
```

### Step 4: Update Key Color Variables

If you want to change colors globally, look for these values in the CSS:
- Primary: `#00BFFF` (Blue)
- Cyan: `#00FFFF` (Cyan)
- Background: `#0a0e27` (Dark Navy)
- Text: `#e0e6ed` (Light Gray)

### Step 5: Apply to Each Page Type

#### Products Pages
- Update `products/index.html`
- Replace Bootstrap cards with `.card-premium`
- Replace Bootstrap buttons with `.btn-premium`
- Replace section headings with `.section-title`

#### Cart Page
- Update `cart/index.html`
- Apply `.card-premium` to cart summary
- Style quantity controls with `.btn-premium`

#### Authentication Pages
- Update `auth/login.html`, `auth/register.html`
- Apply `.card-premium` to login/register forms
- Style inputs with futuristic borders
- Apply `.btn-premium` to submit buttons

#### Admin Pages
- Update admin product and category pages
- Apply consistent styling across tables and forms

## Key CSS Classes Reference

### Layout
- `.premium-header` - Sticky header with glassmorphism
- `.premium-footer` - Dark footer with glow

### Typography
- `.section-title` - Gradient title with underline
- `.brand-logo` - Glowing logo text
- `.text-premium-primary` - Blue text
- `.text-premium-cyan` - Cyan text
- `.text-premium-muted` - Muted text

### Buttons
- `.btn-premium` - Solid gradient button
- `.btn-premium-outline` - Outlined button
- `.btn-danger-premium` - Red danger button
- `.btn-view-detail` - Product detail button

### Cards
- `.card-premium` - Glass card with glow
- `.card-header-premium` - Dark card header
- `.product-card` - Floating product card
- `.product-grid` - Responsive grid

### Components
- `.category-pill` - Category filter pill
- `.category-pill.active` - Active category
- `.product-image-container` - Image wrapper
- `.badge-premium` - Gradient badge

## Animation Classes

- `.fadeInUp` - Fade in from bottom
- `.backgroundFloat` - Animated background
- Hover effects on cards (lift and glow)

## Responsive Design

All components are responsive and adapt to mobile screens:
- Product grid becomes single column on mobile
- Category pills wrap to multiple rows
- Section titles scale down
- Buttons become full-width when needed

## Testing Checklist

After applying the theme to each page:

- [ ] Dark background with gradient visible
- [ ] Neon blue/cyan accents present
- [ ] Glowing hover effects work
- [ ] Buttons have premium styling
- [ ] Cards have glassmorphism effect
- [ ] Typography uses Orbitron/Rajdhani fonts
- [ ] Responsive on mobile devices
- [ ] All Thymeleaf logic works correctly

## Common Issues

**Issue**: Pages still showing Bootstrap default styling
**Solution**: Ensure the CSS file is properly linked and higher specificity is used

**Issue**: Some styles not applying
**Solution**: Use `!important` on critical styles or increase selector specificity

**Issue**: JavaScript not working
**Solution**: Ensure all event listeners are properly attached and DOM is loaded

## Performance Tips

1. Minimize CSS by removing unused styles
2. Use CSS custom properties for theme colors
3. Lazy load heavy animations
4. Compress images for faster loading




