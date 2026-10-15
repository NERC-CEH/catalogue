#!/usr/bin/env node
/*
 * Compiles the shared base stylesheet and each catalogue theme for the colour-contrast specs
 * (src/styles/test/ContrastTest.js), which the specs run against the real stylesheets in an iframe
 * (NERC-CEH/dri-one#462). A page loads base.css and then its theme (dri-one #362).
 *
 * Only Sass is run: the CSS libraries the production build inlines with postcss-import don't affect
 * the colours checked. Run by `npm test` (as part of the pretest step) and in CI.
 *
 * It also fails on any Sass warning, so the build stays clean enough for a new one to be noticed
 * (dri-one #463): sass-options.js silences only Bootstrap's and the tracked @import ones.
 */
const fs = require('fs')
const path = require('path')
const sass = require('sass')
const sassOptions = require('./sass-options')

const root = path.join(__dirname, '..')
const out = path.join(root, 'build', 'test-css')
// The catalogue themes served by CatalogueServiceConfig (style-<id>.css per catalogue)
const THEMES = ['assist', 'datalabs', 'eidc', 'infrastructure', 'inms', 'm', 'nc', 'nm', 'pimfe', 'sa', 'ukceh', 'ukeof', 'ukscape']

fs.mkdirSync(out, { recursive: true })
const warnings = new Set()
const logger = {
  warn (message, { span }) {
    const where = span && span.url ? ` (${path.relative(root, span.url.pathname)}:${span.start.line + 1})` : ''
    warnings.add(`${message.split('\n')[0]}${where}`)
  }
}
for (const name of ['base', ...THEMES.map(theme => `style-${theme}`)]) {
  const { css } = sass.compile(path.join(root, 'scss', `${name}.scss`), { ...sassOptions, logger })
  fs.writeFileSync(path.join(out, `${name}.css`), css)
}
if (warnings.size) {
  console.error(`The catalogue stylesheets have ${warnings.size} Sass warning(s):\n  ${[...warnings].join('\n  ')}`)
  process.exit(1)
}
console.log(`Compiled the base stylesheet and ${THEMES.length} catalogue themes for the contrast specs, with no Sass warnings`)
