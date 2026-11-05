import { afterEach, beforeEach, describe, expect, it } from 'vitest'
import { sendCsrfTokenWithSsoForms } from '../src/ssoForms'

// The SSO proxy requires its CSRF token on every POST under /sso. Its token is a
// cookie the page can read, so the user menu's logout form copies it in as it goes.
describe('sendCsrfTokenWithSsoForms', () => {
  let root

  beforeEach(() => {
    root = document.createElement('div')
    document.body.appendChild(root)
    sendCsrfTokenWithSsoForms(root)
  })

  afterEach(() => {
    root.remove()
    document.cookie = 'XSRF-TOKEN=; expires=Thu, 01 Jan 1970 00:00:00 GMT; path=/'
  })

  function submit (markup) {
    root.innerHTML = markup
    const form = root.querySelector('form')
    // Submitted for real it would leave the page; the token is added before that
    form.addEventListener('submit', event => event.preventDefault())
    form.requestSubmit()
    return form
  }

  it("adds the proxy's token to the logout form", () => {
    document.cookie = 'XSRF-TOKEN=8c2f1b6a-token; path=/'

    const form = submit('<form action="/sso/logout" method="post"><button type="submit">Logout</button></form>')

    expect(form.elements._csrf.value).toBe('8c2f1b6a-token')
    expect(form.elements._csrf.type).toBe('hidden')
  })

  it('reads a token the cookie holds percent-encoded', () => {
    document.cookie = 'XSRF-TOKEN=a%2Bb%3D; path=/'

    const form = submit('<form action="/sso/logout" method="post"><button>Logout</button></form>')

    expect(form.elements._csrf.value).toBe('a+b=')
  })

  it('refreshes a token the form already carries rather than adding a second', () => {
    document.cookie = 'XSRF-TOKEN=current; path=/'

    const form = submit('<form action="/sso/logout" method="post"><input type="hidden" name="_csrf" value="stale"><button>Logout</button></form>')

    expect(form.querySelectorAll('[name=_csrf]').length).toBe(1)
    expect(form.elements._csrf.value).toBe('current')
  })

  it('leaves alone a form that does not post to /sso', () => {
    document.cookie = 'XSRF-TOKEN=8c2f1b6a-token; path=/'

    const form = submit('<form action="/documents/search" method="post"><button>Search</button></form>')

    expect(form.elements._csrf).toBeUndefined()
  })

  it('leaves alone a GET form under /sso', () => {
    document.cookie = 'XSRF-TOKEN=8c2f1b6a-token; path=/'

    const form = submit('<form action="/sso/tokens/admin" method="get"><button>Filter</button></form>')

    expect(form.elements._csrf).toBeUndefined()
  })

  it('adds nothing when there is no token to add', () => {
    const form = submit('<form action="/sso/logout" method="post"><button>Logout</button></form>')

    expect(form.elements._csrf).toBeUndefined()
  })
})
