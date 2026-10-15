/*
 * The Sass options every build of the catalogue stylesheets uses: Vite's (vite.config.mjs) and the test
 * build for the contrast specs.
 *
 * Bootstrap and Font Awesome are loaded through loadPaths, so quietDeps silences their warnings and
 * only the project's own are reported: any of those means something to fix (dri-one #463).
 */
const path = require('path')

const web = path.join(__dirname, '..')

module.exports = {
  loadPaths: [path.join(web, 'scss'), path.join(web, 'node_modules')],
  quietDeps: true
}
