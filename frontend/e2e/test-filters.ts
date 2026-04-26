import { chromium } from 'playwright';

(async () => {
  const browser = await chromium.launch({ headless: true });
  const context = await browser.newContext({ viewport: { width: 1280, height: 800 } });
  const page = await context.newPage();

  // Test 1: Click category from home page
  console.log('=== TEST: Category link from home ===');
  await page.goto('http://localhost:5173/');
  await page.waitForLoadState('networkidle');
  await page.waitForTimeout(500);
  
  await page.locator('text=Microcontrollers').first().click();
  await page.waitForTimeout(1500);
  
  console.log('URL after click:', page.url());
  const heading = await page.locator('h1').textContent().catch(() => 'Not found');
  console.log('Heading:', heading);
  const productCount = await page.locator('text=Add to Cart').count();
  console.log('Product cards (by Add to Cart button):', productCount);
  
  await page.screenshot({ path: 'e2e-screenshots/category-click.png' });

  // Test 2: Sidebar checkbox filter
  console.log('\n=== TEST: Sidebar checkbox filter ===');
  await page.goto('http://localhost:5173/products');
  await page.waitForLoadState('networkidle');
  await page.waitForTimeout(1000);
  
  const beforeCount = await page.locator('text=Add to Cart').count();
  console.log('Products before filter:', beforeCount);
  
  await page.locator('label:has-text("Sensors")').first().click();
  await page.waitForTimeout(1500);
  
  const afterCount = await page.locator('text=Add to Cart').count();
  console.log('Products after filter:', afterCount);
  
  await page.screenshot({ path: 'e2e-screenshots/checkbox-filter.png' });

  // Test 3: Manufacturer filter
  console.log('\n=== TEST: Manufacturer filter ===');
  await page.goto('http://localhost:5173/products');
  await page.waitForLoadState('networkidle');
  await page.waitForTimeout(1000);
  
  await page.locator('label:has-text("Arduino")').first().click();
  await page.waitForTimeout(1500);
  
  const mfrCount = await page.locator('text=Add to Cart').count();
  console.log('Products after Arduino filter:', mfrCount);
  
  await page.screenshot({ path: 'e2e-screenshots/mfr-filter.png' });

  await browser.close();
  console.log('\nDone!');
})();
