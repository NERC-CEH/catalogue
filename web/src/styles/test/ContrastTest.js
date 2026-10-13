/* global beforeAll, afterAll */
/**
 * Every catalogue theme must give text a contrast of at least 4.5:1 and controls 3:1 (WCAG 2.1 AA).
 * Several fell short: an invisible search button on white-header catalogues, #999 headings, links
 * and status alerts on the brand water blue, grey input text (NERC-CEH/dri-one#462).
 *
 * Each theme, compiled by scripts/build-test-css.js, is loaded into an iframe with representative
 * markup, and the computed colours are measured. Hover states can't be triggered from a spec, so the
 * link hover colour is read from its custom property instead.
 */
const THEMES = ['assist', 'datalabs', 'eidc', 'infrastructure', 'inms', 'm', 'nc', 'nm', 'pimfe', 'sa', 'ukceh', 'ukeof', 'ukscape']

const MARKUP = `
  <div id="search">
    <form class="search-form"><input type="text" class="form-control" value="soil"><button class="btn" type="submit">Search</button></form>
    <div class="results__footer"><ul class="pagination">
      <li class="page-item active"><span class="page-link">1</span></li>
      <li class="page-item"><a class="page-link" href="#p2">2</a></li>
    </ul></div>
  </div>
  <h2>Heading 2</h2><h3>Heading 3</h3><h4>Heading 4</h4>
  <p><a id="body-link" href="#x">A link</a></p>
  <div class="metadata"><div class="alert alert-Embargoed">Embargoed <a href="#y">a link</a></div></div>
  <input class="form-check-input" type="checkbox" checked>`

function loadTheme (theme) {
  return new Promise((resolve, reject) => {
    const frame = document.createElement('iframe')
    frame.style.width = '1200px'
    frame.onload = () => resolve(frame)
    frame.onerror = reject
    frame.srcdoc = `<!doctype html><html><head><link rel="stylesheet" href="/base/build/test-css/style-${theme}.css"></head><body>${MARKUP}</body></html>`
    document.body.appendChild(frame)
  })
}

// Any CSS colour (including color-mix() and color()) is normalised by painting it on a canvas
const canvas = document.createElement('canvas').getContext('2d', { willReadFrequently: true })
function rgba (colour) {
  canvas.clearRect(0, 0, 1, 1)
  canvas.fillStyle = '#000'
  canvas.fillStyle = colour
  canvas.fillRect(0, 0, 1, 1)
  const [r, g, b, a] = canvas.getImageData(0, 0, 1, 1).data
  return { r, g, b, a: a / 255 }
}

function luminance ({ r, g, b }) {
  const channel = v => { v /= 255; return v <= 0.03928 ? v / 12.92 : Math.pow((v + 0.055) / 1.055, 2.4) }
  return 0.2126 * channel(r) + 0.7152 * channel(g) + 0.0722 * channel(b)
}

function contrast (a, b) {
  const [light, dark] = [luminance(a), luminance(b)].sort((x, y) => y - x)
  return Math.round(((light + 0.05) / (dark + 0.05)) * 100) / 100
}

describe('Catalogue theme contrast', () => {
  THEMES.forEach(theme => {
    describe(theme, () => {
      let frame, win, doc

      beforeAll(async () => {
        frame = await loadTheme(theme)
        win = frame.contentWindow
        doc = frame.contentDocument
      })

      afterAll(() => frame.remove())

      const background = el => {
        for (let e = el; e; e = e.parentElement) {
          const colour = rgba(win.getComputedStyle(e).backgroundColor)
          if (colour.a > 0.5) return colour
        }
        return { r: 255, g: 255, b: 255, a: 1 }
      }
      const text = selector => {
        const el = doc.querySelector(selector)
        return contrast(rgba(win.getComputedStyle(el).color), background(el))
      }

      it('loads its stylesheet', () => {
        expect(doc.styleSheets.length).toBe(1)
        expect(doc.styleSheets[0].cssRules.length).toBeGreaterThan(100)
      })

      it('gives text at least 4.5:1', () => {
        const ratios = {
          searchButton: text('.search-form .btn'),
          searchInput: text('.search-form input'),
          activePage: text('.pagination .active .page-link'),
          pageLink: text('.pagination a.page-link'),
          h2: text('h2'),
          h3: text('h3'),
          h4: text('h4'),
          link: text('#body-link'),
          statusAlert: text('.alert-Embargoed'),
          statusAlertLink: text('.alert-Embargoed a')
        }
        const failing = Object.entries(ratios).filter(([, ratio]) => ratio < 4.5)
        expect(failing).toEqual([])
      })

      it('gives a hovered link at least 4.5:1', () => {
        const hover = win.getComputedStyle(doc.documentElement).getPropertyValue('--link-hover-colour').trim()
        expect(contrast(rgba(hover), background(doc.getElementById('body-link')))).toBeGreaterThanOrEqual(4.5)
      })

      it('gives a checked checkbox at least 3:1', () => {
        const box = doc.querySelector('.form-check-input')
        expect(contrast(rgba(win.getComputedStyle(box).backgroundColor), background(box.parentElement))).toBeGreaterThanOrEqual(3)
      })
    })
  })
})
