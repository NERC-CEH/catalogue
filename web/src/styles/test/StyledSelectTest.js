import { afterAll, beforeAll, describe, expect, it } from 'vitest'
/**
 * The editor's styled selects (appearance: base-select) set each option's name with what it means
 * beneath it. An option is nowrap by default, and its text column was as wide as its longest line, so
 * the relationship picker, as wide as its select, scrolled sideways to show the long ones.
 *
 * Measured on the stylesheet compiled by scripts/build-test-css.js, as ContrastTest is.
 */
const MARKUP = `
  <div class="metadata"><div class="editor"><div class="editor-body" style="width:420px">
    <select class="styledSelect" id="relation">
      <option value="requires">Requires<span>a related resource that is required by this resource to support its function/delivery</span></option>
    </select>
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

describe('An option of a styled editor select', () => {
  let frame, win, doc

  beforeAll(async () => { frame = await loadBase(); win = frame.contentWindow; doc = frame.contentDocument })
  afterAll(() => frame.remove())

  const style = () => win.getComputedStyle(doc.querySelector('#relation option'))

  it('wraps its text', () => {
    expect(style().whiteSpace).toBe('normal')
  })

  it('keeps its text to the width the picker gives it', () => {
    expect(style().gridTemplateColumns).toMatch(/^\S+ minmax\(0px, 1fr\)$/)
  })
})
