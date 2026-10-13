const fs = require('fs/promises')
const path = require('path')
const gulp = require('gulp')
const sass = require('sass')
const postcss = require('postcss')
const postcssImport = require('postcss-import')
const sassOptions = require('./scripts/sass-options')

const OUT = path.join(__dirname, 'css')

// The images the inlined library stylesheets load from css/images (url("images/..."))
const IMAGES = [
  'node_modules/leaflet-draw/dist/images',
  'node_modules/jquery-ui/dist/themes/base/images'
]

// Sass is called directly rather than through gulp-sass, which compiles each file as a string with
// no URL: Sass then treats every file as a dependency, so quietDeps hid the project's own warnings
// too (dri-one #463)
async function buildStyle (file, sourceMap) {
  const name = path.basename(file, '.scss')
  const to = path.join(OUT, `${name}.css`)
  const compiled = sass.compile(file, { ...sassOptions, sourceMap, sourceMapIncludeSources: sourceMap })
  // Inline the library stylesheets the Sass @imports as plain CSS (leaflet, select2, DataTables...)
  const result = await postcss([postcssImport()]).process(compiled.css, {
    from: file,
    to,
    map: sourceMap && { prev: compiled.sourceMap, inline: false, annotation: true }
  })
  await fs.writeFile(to, result.css)
  if (result.map) {
    await fs.writeFile(`${to}.map`, result.map.toString())
  }
}

async function copyImages () {
  const images = path.join(OUT, 'images')
  // The dev container used to link css/images to leaflet-draw's images; replace the link
  await fs.rm(images, { recursive: true, force: true })
  await fs.mkdir(images, { recursive: true })
  for (const dir of IMAGES) {
    await fs.cp(path.join(__dirname, dir), images, { recursive: true })
  }
}

async function buildStyles (sourceMap) {
  await fs.mkdir(OUT, { recursive: true })
  // One stylesheet per catalogue (style-<catalogue>.css); the other files are partials they use
  const files = (await fs.readdir(path.join(__dirname, 'scss')))
    .filter(f => f.startsWith('style-') && f.endsWith('.scss'))
    .map(f => path.join(__dirname, 'scss', f))
  await Promise.all(files.map(file => buildStyle(file, sourceMap)))
  await copyImages()
}

const buildDevStyles = () => buildStyles(true)
const buildProdStyles = () => buildStyles(false)

function watchStyles () {
  // Report a Sass error and keep watching, rather than stopping the dev container's watcher
  gulp.watch('./scss/**/*.scss', () => buildDevStyles().catch(error => console.error(error.message)))
}

exports.dev = buildDevStyles
exports.prod = buildProdStyles
exports.watch = gulp.series(buildDevStyles, watchStyles)
