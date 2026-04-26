import { chromium } from 'playwright';

(async () => {
  const browser = await chromium.launch({ headless: true });
  const context = await browser.newContext({
    viewport: { width: 1280, height: 800 },
  });
  const page = await context.newPage();

  // Collect console errors
  const errors = [];
  const warnings = [];
  page.on('console', msg => {
    if (msg.type() === 'error') errors.push(msg.text());
    if (msg.type() === 'warning') warnings.push(msg.text());
  });

  // Collect failed requests
  const failedRequests = [];
  page.on('requestfailed', request => {
    failedRequests.push({ url: request.url(), error: request.failure()?.errorText });
  });

  // 1. Home page
  console.log('=== HOME PAGE ===');
  await page.goto('http://localhost:5173/');
  await page.waitForLoadState('networkidle');
  await page.waitForTimeout(1000);
  
  const homeTitle = await page.title();
  console.log('Page title:', homeTitle);
  
  const h1Text = await page.locator('h1').first().textContent();
  console.log('Hero heading:', h1Text?.trim());
  
  const categoryCards = await page.locator('[class*="category-card"], [class*="CategoryCard"]').count();
  console.log('Category cards found:', categoryCards);
  
  const navLinks = await page.locator('nav a, nav button').count();
  console.log('Nav links:', navLinks);
  
  // Check for visible errors on page
  const errorOverlay = await page.locator('vite-error-overlay').count();
  console.log('Vite error overlay:', errorOverlay > 0 ? 'YES - ERROR' : 'No');

  // Take screenshot
  await page.screenshot({ path: 'e2e-screenshots/home.png', fullPage: true });
  console.log('Screenshot saved: home.png');

  // 2. Login page
  console.log('\n=== LOGIN PAGE ===');
  await page.goto('http://localhost:5173/login');
  await page.waitForLoadState('networkidle');
  await page.waitForTimeout(1000);
  
  console.log('Current URL:', page.url());
  
  // Check if app mounted
  const appDiv = await page.locator('#app').innerHTML();
  console.log('App innerHTML length:', appDiv.length);
  console.log('App innerHTML preview:', appDiv.substring(0, 200));
  
  const loginHeading = await page.locator('h1, h2, [class*="CardTitle"]').first().textContent().catch(() => 'Not found');
  console.log('Login heading:', loginHeading);
  
  const emailInput = await page.locator('input[type="email"]').count();
  const passwordInput = await page.locator('input[type="password"]').count();
  console.log('Email inputs:', emailInput, '| Password inputs:', passwordInput);
  
  const loginButton = await page.locator('button:has-text("Login")').count();
  console.log('Login button:', loginButton > 0 ? 'Found' : 'Not found');
  
  await page.screenshot({ path: 'e2e-screenshots/login.png', fullPage: true });
  console.log('Screenshot saved: login.png');

  // 3. Try logging in
  console.log('\n=== LOGIN ATTEMPT ===');
  const emailField = page.locator('input[type="email"]').first();
  const passwordField = page.locator('input[type="password"]').first();
  
  await emailField.fill('admin@specsheet.com');
  await passwordField.fill('admin123');
  
  // Click the form submit button specifically (inside main, not navbar)
  const submitBtn = page.locator('main button:has-text("Login")').first();
  await submitBtn.click();
  
  await page.waitForTimeout(2000);
  
  const currentUrl = page.url();
  console.log('After login URL:', currentUrl);
  
  const authUser = await page.locator('[class*="user-email"], [class*="user"]').textContent().catch(() => 'Not found');
  console.log('User email visible:', authUser?.trim() || 'Not found');
  
  await page.screenshot({ path: 'e2e-screenshots/after-login.png', fullPage: true });
  console.log('Screenshot saved: after-login.png');

  // 4. Catalog page
  console.log('\n=== CATALOG PAGE ===');
  await page.goto('http://localhost:5173/products');
  await page.waitForLoadState('networkidle');
  await page.waitForTimeout(1000);
  
  const catalogHeading = await page.locator('h1').first().textContent().catch(() => 'Not found');
  console.log('Catalog heading:', catalogHeading);
  
  const productCards = await page.locator('[class*="product-card"], [class*="ProductCard"]').count();
  console.log('Product cards:', productCards);
  
  const filterSidebar = await page.locator('aside, [class*="filter"], [class*="sidebar"]').count();
  console.log('Filter sidebar:', filterSidebar > 0 ? 'Found' : 'Not found');
  
  await page.screenshot({ path: 'e2e-screenshots/catalog.png', fullPage: true });
  console.log('Screenshot saved: catalog.png');

  // 5. Admin page (should be accessible as admin)
  console.log('\n=== ADMIN PAGE ===');
  await page.goto('http://localhost:5173/admin');
  await page.waitForLoadState('networkidle');
  await page.waitForTimeout(1000);
  
  const adminHeading = await page.locator('h1').first().textContent().catch(() => 'Not found');
  console.log('Admin heading:', adminHeading);
  
  const metricCards = await page.locator('[class*="metric"], [class*="Card"]').count();
  console.log('Metric cards:', metricCards);
  
  const dataTable = await page.locator('table, [class*="table"]').count();
  console.log('Data table:', dataTable > 0 ? 'Found' : 'Not found');
  
  await page.screenshot({ path: 'e2e-screenshots/admin.png', fullPage: true });
  console.log('Screenshot saved: admin.png');

  // 6. Cart page
  console.log('\n=== CART PAGE ===');
  await page.goto('http://localhost:5173/cart');
  await page.waitForLoadState('networkidle');
  await page.waitForTimeout(1000);
  
  const cartHeading = await page.locator('h1').first().textContent().catch(() => 'Not found');
  console.log('Cart heading:', cartHeading);
  
  await page.screenshot({ path: 'e2e-screenshots/cart.png', fullPage: true });
  console.log('Screenshot saved: cart.png');

  // Summary
  console.log('\n=== SUMMARY ===');
  console.log('Console errors:', errors.length > 0 ? errors.join('\n  - ') : 'None');
  console.log('Failed requests:', failedRequests.length > 0 ? failedRequests.map(r => r.url).join('\n  - ') : 'None');

  await browser.close();
  console.log('\nDone!');
})();
