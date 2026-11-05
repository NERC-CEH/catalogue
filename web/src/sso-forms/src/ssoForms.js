/* global URL */
// The SSO proxy in front of the catalogue requires its CSRF token on every POST under /sso, such as the
// user menu's logout (skeleton.ftlh). The token is the proxy's XSRF-TOKEN cookie, which it leaves
// readable for exactly this, so a form posting there copies the cookie into its _csrf field as it is
// submitted. Read then rather than when the page is built, so the form always carries the current one.

const COOKIE = 'XSRF-TOKEN'
const FIELD = '_csrf'

function csrfToken () {
  const cookie = document.cookie.split('; ').find(c => c.startsWith(`${COOKIE}=`))
  return cookie ? decodeURIComponent(cookie.substring(COOKIE.length + 1)) : null
}

function postsToSso (form) {
  return form.method.toLowerCase() === 'post' &&
    new URL(form.action, document.baseURI).pathname.startsWith('/sso/')
}

export function sendCsrfTokenWithSsoForms (root) {
  root.addEventListener('submit', event => {
    const form = event.target
    const token = csrfToken()
    if (!(form instanceof HTMLFormElement) || !postsToSso(form) || !token) {
      return
    }
    let field = form.querySelector(`input[name="${FIELD}"]`)
    if (!field) {
      field = document.createElement('input')
      field.type = 'hidden'
      field.name = FIELD
      form.appendChild(field)
    }
    field.value = token
  })
}
