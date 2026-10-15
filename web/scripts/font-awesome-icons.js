/*
 * The Font Awesome icons the catalogue uses, so the build can drop the CSS for the other ~2,400
 * (NERC-CEH/dri-one#362). Font Awesome's Sass emits a `.fa-<name>::before` rule for every icon and
 * can't be configured with a subset, so vite.config.mjs removes the unused rules after compiling.
 *
 * An icon counts as used when its class, `fa-<name>`, appears anywhere in the templates, the
 * JavaScript or the Sass, or when a template passes its bare name to a macro that adds the `fa-`
 * itself (`icon="check"`, in upload/hubbub/upload.ftlh). Over-counting is harmless: a word like
 * `fa-solid` simply isn't an icon. An icon whose name is only ever assembled at run time from a
 * variable is invisible to this scan and must be listed in DYNAMIC.
 *
 * The Java names no icons. If it ever does, add it to SOURCES, and copy it into the Dockerfile's
 * build-web stage beside the templates.
 */
const fs = require('fs')
const path = require('path')

const repo = path.join(__dirname, '..', '..')

// Icons whose names are built at run time, out of the scan's sight
const DYNAMIC = []

const SOURCES = [
  { dir: 'templates', extensions: ['.ftl', '.ftlh', '.ftlx', '.html'] },
  { dir: 'web/src', extensions: ['.js'], skip: 'test' },
  { dir: 'web/scss', extensions: ['.scss'] }
]

function files (dir, extensions, skip) {
  return fs.readdirSync(dir, { withFileTypes: true }).flatMap(entry => {
    const full = path.join(dir, entry.name)
    if (entry.isDirectory()) return entry.name === skip ? [] : files(full, extensions, skip)
    return extensions.includes(path.extname(entry.name)) ? [full] : []
  })
}

module.exports = function usedIcons () {
  const used = new Set(DYNAMIC)
  for (const { dir, extensions, skip } of SOURCES) {
    for (const file of files(path.join(repo, dir), extensions, skip)) {
      const source = fs.readFileSync(file, 'utf8')
      for (const [, name] of source.matchAll(/\bfa-([a-z0-9]+(?:-[a-z0-9]+)*)/g)) used.add(name)
      if (file.endsWith('.ftlh') || file.endsWith('.ftl')) {
        for (const [, name] of source.matchAll(/\bicon="([a-z0-9]+(?:-[a-z0-9]+)*)"/g)) used.add(name)
      }
    }
  }
  return used
}
