#!/usr/bin/env node
/*
 * Compiles each catalogue theme for the colour-contrast specs (src/styles/test/ContrastTest.js),
 * which Karma runs against the real stylesheets in an iframe (NERC-CEH/dri-one#462).
 *
 * Only Sass is run: the CSS libraries the production build inlines with postcss-import don't affect
 * the colours checked. Run by `npm test` (as part of the pretest step) and in CI.
 */
const fs = require('fs')
const path = require('path')
const sass = require('sass')

const root = path.join(__dirname, '..')
const out = path.join(root, 'build', 'test-css')
// The catalogue themes served by CatalogueServiceConfig (style-<id>.css per catalogue)
const THEMES = ['assist', 'datalabs', 'eidc', 'infrastructure', 'inms', 'm', 'nc', 'nm', 'pimfe', 'sa', 'ukceh', 'ukeof', 'ukscape']

fs.mkdirSync(out, { recursive: true })
for (const theme of THEMES) {
  const { css } = sass.compile(path.join(root, 'scss', `style-${theme}.scss`), {
    loadPaths: [path.join(root, 'scss'), path.join(root, 'node_modules')],
    logger: sass.Logger.silent
  })
  fs.writeFileSync(path.join(out, `style-${theme}.css`), css)
}
console.log(`Compiled ${THEMES.length} catalogue themes for the contrast specs`)
