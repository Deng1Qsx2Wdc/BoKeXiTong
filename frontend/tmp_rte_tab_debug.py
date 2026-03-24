from playwright.sync_api import sync_playwright

URL = 'http://127.0.0.1:5173/article/new'

with sync_playwright() as p:
    browser = p.chromium.launch(headless=True)
    context = browser.new_context()
    page = context.new_page()

    page.add_init_script("""
        localStorage.setItem('auth_token', 'debug-token');
        localStorage.setItem('user_info', JSON.stringify({
          id: '1',
          username: 'debug',
          role: 'USER'
        }));
    """)

    page.goto(URL, wait_until='networkidle')
    page.wait_for_selector('[contenteditable="true"]')

    editor = page.locator('[contenteditable="true"]').first
    editor.click()
    editor.type('abc')

    before = page.evaluate("""
      () => {
        const el = document.querySelector('[contenteditable="true"]');
        return {
          html: el?.innerHTML,
          text: el?.textContent,
        };
      }
    """)

    editor.press('Tab')

    after_tab = page.evaluate("""
      () => {
        const el = document.querySelector('[contenteditable="true"]');
        return {
          html: el?.innerHTML,
          text: el?.textContent,
        };
      }
    """)

    editor.type('x')

    after_x = page.evaluate("""
      () => {
        const el = document.querySelector('[contenteditable="true"]');
        return {
          html: el?.innerHTML,
          text: el?.textContent,
        };
      }
    """)

    print('BEFORE:', before)
    print('AFTER_TAB:', after_tab)
    print('AFTER_X:', after_x)

    browser.close()
