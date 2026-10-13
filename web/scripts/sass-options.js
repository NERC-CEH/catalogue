/*
 * The Sass options every build of the catalogue stylesheets uses: gulp (dev, prod and watch) and the
 * test build for the contrast specs.
 *
 * Bootstrap and Font Awesome are loaded through loadPaths, so quietDeps silences their warnings and
 * only the project's own are reported. The project's @imports are silenced until they move to
 * @use/@forward (NERC-CEH/dri-one#365); any other warning means something to fix (dri-one #463).
 */
const path = require('path')

const web = path.join(__dirname, '..')

module.exports = {
  loadPaths: [path.join(web, 'scss'), path.join(web, 'node_modules')],
  quietDeps: true,
  silenceDeprecations: ['import']
}
