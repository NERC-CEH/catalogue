import { afterAll, beforeAll, describe, expect, it } from 'vitest'
/**
 * In a floating field the label sits where the placeholder is until the field is used, so Bootstrap
 * makes the placeholder transparent there. The editor's own placeholder colour, nested three classes
 * deep, outweighed that, and a new contact's "e.g., C." and "e.g., Darwin" were written over their
 * Given name and Family name labels.
 *
 * Measured on the stylesheet compiled by scripts/build-test-css.js, as ContrastTest is.
 */
const MARKUP = `
  <div class="metadata"><div class="editor"><div class="editor-body">
    <div class="form-floating">
      <input class="editor-input" id="floating" placeholder="e.g., C.">
      <label for="floating">Given name</label>
    </div>
    <input class="editor-input" id="plain" placeholder="e.g., Darwin">
  </div></div></div>`

function loadBase () {
  return new Promise((resolve, reject) => {
    const frame = document.createElement('iframe')
    frame.onload = () => resolve(frame)
    frame.onerror = reject
    frame.srcdoc = `<!doctype html><html><head>
      <link rel="stylesheet" href="/build/test-css/base.css">
      </head><body>${MARKUP}</body></html>`
    document.body.appendChild(frame)
  })
}

describe('Editor fields with a floating label', () => {
  let frame, win, doc

  beforeAll(async () => { frame = await loadBase(); win = frame.contentWindow; doc = frame.contentDocument })
  afterAll(() => frame.remove())

  const placeholderColour = id => win.getComputedStyle(doc.getElementById(id), '::placeholder').color

  // The specs run in a random order, so each sets the focus it needs
  it('hide the placeholder while the label sits over it', () => {
    doc.getElementById('floating').blur()

    expect(placeholderColour('floating')).toEqual('rgba(0, 0, 0, 0)')
  })

  it('show it once the label has moved up, as the field is used', () => {
    doc.getElementById('floating').focus()

    expect(placeholderColour('floating')).toEqual('rgb(170, 170, 170)')
  })

  it('leave the placeholder of an ordinary field as it was', () => {
    expect(placeholderColour('plain')).toEqual('rgb(170, 170, 170)')
  })
})
