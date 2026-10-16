import { afterAll, beforeAll, describe, expect, it } from 'vitest'
/**
 * The relationship picker says when it has nothing to offer (no match, a failed search, rules it
 * couldn't load) in a menu item that can't be chosen. jQuery UI fades a disabled item to .35, which
 * left the message too faint to read.
 *
 * Measured on the stylesheet compiled by scripts/build-test-css.js, as ContrastTest is.
 */
function loadBase () {
  return new Promise((resolve, reject) => {
    const frame = document.createElement('iframe')
    frame.onload = () => resolve(frame)
    frame.onerror = reject
    frame.srcdoc = `<!doctype html><html><head>
      <link rel="stylesheet" href="/build/test-css/base.css">
      </head><body>
      <ul class="ui-menu ui-widget ui-widget-content ui-autocomplete relationship-autocomplete">
        <li class="ui-state-disabled relationship-notice"><div>No matching records</div></li>
      </ul></body></html>`
    document.body.appendChild(frame)
  })
}

describe('A relationship picker notice', () => {
  let frame, win, doc

  beforeAll(async () => { frame = await loadBase(); win = frame.contentWindow; doc = frame.contentDocument })
  afterAll(() => frame.remove())

  it('is shown at full strength, to be read', () => {
    expect(win.getComputedStyle(doc.querySelector('.relationship-notice')).opacity).toBe('1')
  })

  it('is set in a dark grey that reads on the menu\'s white', () => {
    expect(win.getComputedStyle(doc.querySelector('.relationship-notice div')).color).toBe('rgb(74, 74, 74)')
  })
})
