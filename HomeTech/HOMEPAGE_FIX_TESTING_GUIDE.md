# HomeTech Homepage Fix - Testing Guide

## Overview
This fix ensures the HomeTech homepage always displays exactly 10 product cards with proper fallbacks, debug logging, and visual indicators.

## Changes Made

### 1. HomeController Updates
- Added comprehensive debug logging using SLF4J
- Implemented `ensureExactly10Products()` method to pad with placeholders if needed
- Added `createPlaceholderProduct()` for visual fallbacks
- Enhanced model attributes with debug information

### 2. Template Updates
- Added debug comments in HTML: `<!-- DEBUG: displayProducts size = [[${debugProductCount}]] -->`
- Implemented conditional rendering for placeholder products
- Added CSS styles for placeholder products (dashed borders, muted colors)
- Enhanced JavaScript with client-side debug logging

### 3. ProductService & Repository
- Verified `findTop10ByOrderBySalesDesc()` method exists
- Confirmed fallback logic in `getTop10BestSellingProducts()`
- Added proper null/empty validation

## Testing Steps

### 1. Server-Side Testing
```bash
# Start the application
mvn spring-boot:run

# Check server logs for debug output:
# - "HomeController.home() called with category: ..."
# - "Categories loaded: X"
# - "Top 10 products loaded: X"
# - "Final displayProducts size=10, ids=[...]"
```

### 2. Client-Side Testing

#### Test URLs:
- **Homepage**: `http://localhost:8080/`
- **Category Filter**: `http://localhost:8080/?category=Laptop`
- **All Products**: `http://localhost:8080/?category=all`

#### Browser Console Debug Output:
```javascript
// Expected console output:
"Page loaded, initializing category buttons..."
"DEBUG: Rendered product cards count: 10"
"DEBUG: Placeholder cards count: X"
"DEBUG: Card 1: 'iPhone 15' (placeholder: false)"
"DEBUG: Card 2: 'Sản phẩm sắp ra mắt 1' (placeholder: true)"
// ... etc
```

### 3. Visual Verification

#### Expected Behavior:
1. **Always 10 Cards**: Grid should show exactly 10 product cards
2. **Placeholder Indicators**: Cards with dashed borders and 🚀 icons are placeholders
3. **Debug Comments**: View page source to see `<!-- DEBUG: displayProducts size = 10 -->`
4. **Category Filtering**: Clicking categories should show filtered top 10 products
5. **Image Loading**: Real products show images, placeholders show rocket icons

#### Before/After Screenshots:
- **Before**: Shows 1-3 products inconsistently
- **After**: Always shows 10 products (real + placeholders)

### 4. Unit Tests
```bash
# Run tests
mvn test

# Specific test classes:
# - ProductServiceTest: Tests service layer logic
# - HomeControllerTest: Tests controller behavior
```

## Debug Information

### Server Logs (DEBUG level):
```
2025-01-XX XX:XX:XX DEBUG HomeController - HomeController.home() called with category: null
2025-01-XX XX:XX:XX DEBUG HomeController - Categories loaded: 4
2025-01-XX XX:XX:XX DEBUG HomeController - Top 10 products loaded: 3
2025-01-XX XX:XX:XX DEBUG HomeController - Padding with 7 placeholder products
2025-01-XX XX:XX:XX DEBUG HomeController - Final displayProducts size=10, ids=[1, 2, 3, -1, -2, -3, -4, -5, -6, -7]
```

### HTML Debug Comments:
```html
<!-- DEBUG: Server-side product count -->
<!-- DEBUG: displayProducts size = 10 -->
```

### Client Console Output:
```javascript
DEBUG: Rendered product cards count: 10
DEBUG: Placeholder cards count: 7
DEBUG: Card 1: "iPhone 15" (placeholder: false)
DEBUG: Card 2: "Samsung Galaxy S24" (placeholder: false)
DEBUG: Card 3: "Google Pixel 9" (placeholder: false)
DEBUG: Card 4: "Sản phẩm sắp ra mắt 1" (placeholder: true)
// ... etc
```

## Acceptance Criteria ✅

- [x] Homepage renders 10 product cards by default
- [x] Placeholder products visible when fewer than 10 real products
- [x] Product images load or placeholder visible
- [x] Pagination reflects correct number of pages
- [x] Category filter returns top 10 of that category
- [x] No hidden DOM nodes due to CSS
- [x] Grid visible above the fold
- [x] Server and client debug logs implemented
- [x] Unit tests added for service and controller layers

## Rollback Instructions
If issues occur, remove debug logs and placeholder logic:
1. Revert HomeController to simple version
2. Remove debug comments from template
3. Remove placeholder CSS styles
4. Remove client-side debug logging

## Commit Message
```
fix(home): ensure top-10 products render on homepage and add debug/fallback for empty lists

- Add ensureExactly10Products() method to pad with placeholders
- Implement comprehensive debug logging (server + client)
- Add placeholder product styling and visual indicators
- Create unit tests for ProductService and HomeController
- Add HTML debug comments for template verification
- Ensure consistent 10-product display across all views
```

