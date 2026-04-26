import { chromium } from 'playwright';

(async () => {
  const browser = await chromium.launch({ headless: true });
  const context = await browser.newContext({ viewport: { width: 1280, height: 800 } });
  const page = await context.newPage();

  page.on('console', msg => console.log('CONSOLE:', msg.type(), msg.text()));

  await page.goto('http://localhost:5173/products');
  await page.waitForLoadState('networkidle');
  await page.waitForTimeout(1000);
  
  console.log('Before:', await page.locator('text=Add to Cart').count(), 'products');
  
  // Try clicking the checkbox directly
  const checkbox = page.locator('[data-slot="checkbox"]').first();
  console.log('Checkbox found:', await checkbox.count() > 0);
  
  await checkbox.click();
  await page.waitForTimeout(1000);
  
  console.log('After click:', await page.locator('text=Add to Cart').count(), 'products');
  
  // Check URL
  console.log('URL:', page.url());
  
  await page.screenshot({ path: 'e2e-screenshots/checkbox-debug.png' });
  await browser.close();
})();
