import { chromium } from 'playwright';

(async () => {
  const browser = await chromium.launch({ headless: true });
  const context = await browser.newContext({ viewport: { width: 1280, height: 800 } });
  const page = await context.newPage();

  const logs: string[] = [];
  page.on('console', msg => logs.push(`[${msg.type()}] ${msg.text()}`));
  page.on('request', req => {
    if (req.url().includes('/api/')) {
      console.log('REQUEST:', req.method(), req.url());
    }
  });
  page.on('response', async res => {
    if (res.url().includes('/api/')) {
      const status = res.status();
      let body = '';
      try { body = await res.text(); } catch {}
      console.log('RESPONSE:', status, res.url(), body.substring(0, 200));
    }
  });

  await page.goto('http://localhost:5173/login');
  await page.waitForLoadState('networkidle');
  await page.waitForTimeout(500);

  await page.locator('input[type="email"]').fill('admin@specsheet.com');
  await page.locator('input[type="password"]').fill('admin123');
  
  console.log('Clicking login button...');
  await page.locator('button:has-text("Login")').click();
  
  await page.waitForTimeout(3000);
  
  console.log('URL after login:', page.url());
  console.log('Console logs:', logs.join('\n'));
  
  await page.screenshot({ path: 'e2e-screenshots/login-debug.png' });
  await browser.close();
})();
