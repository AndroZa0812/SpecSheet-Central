import { chromium } from 'playwright';

(async () => {
  const browser = await chromium.launch({ headless: true });
  const context = await browser.newContext({ viewport: { width: 1280, height: 800 } });
  const page = await context.newPage();

  page.on('console', msg => console.log('CONSOLE:', msg.type(), msg.text()));
  page.on('request', req => {
    if (req.url().includes('/api/')) console.log('REQ:', req.method(), req.url());
  });
  page.on('response', async res => {
    if (res.url().includes('/api/')) {
      const body = await res.text().catch(() => '');
      console.log('RES:', res.status(), res.url(), body.substring(0, 300));
    }
  });

  await page.goto('http://localhost:5173/products?categoryId=1');
  await page.waitForLoadState('networkidle');
  await page.waitForTimeout(2000);
  
  const html = await page.locator('main').innerHTML();
  console.log('Main HTML length:', html.length);
  console.log('Main HTML preview:', html.substring(0, 500));
  
  // Check what elements exist
  const cards = await page.locator('div[class*="cursor-pointer"][class*="overflow"]').count();
  console.log('Card-like divs:', cards);
  
  const allDivs = await page.locator('div').count();
  console.log('Total divs:', allDivs);
  
  await page.screenshot({ path: 'e2e-screenshots/debug-catalog.png' });
  await browser.close();
})();
