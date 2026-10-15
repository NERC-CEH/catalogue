/*
 * The Font Awesome icons the catalogue uses, so the build can drop the CSS for the other ~2,400
 * (NERC-CEH/dri-one#362). Font Awesome's Sass emits a `.fa-<name>::before` rule for every icon and
 * can't be configured with a subset, so vite.config.mjs removes the unused rules after compiling.
 *
 * An icon counts as used when its class, `fa-<name>`, appears anywhere in the templates, the
 * JavaScript or the Sass, or when a template passes its bare name to a macro that adds the `fa-`
 * itself (`class="fa-${icon}"`, in upload/hubbub/upload.ftlh), by keyword (`icon="check"`) or by
 * position. Over-counting is harmless: a word like `fa-solid` simply isn't an icon. An icon whose
 * name is only ever assembled at run time from a variable is invisible to this scan and must be
 * listed in DYNAMIC. A macro given anything but a quoted name fails the build, so that isn't missed,
 * unless VARIABLES says which icons it can be.
 *
 * The Java names no icons. If it ever does, add it to SOURCES, and copy it into the Dockerfile's
 * build-web stage beside the templates.
 */
const fs = require('fs')
const path = require('path')

const repo = path.join(__dirname, '..', '..')

// Icons whose names are built at run time, out of the scan's sight
const DYNAMIC = []

// What a template passes to one of the macros below when it isn't a quoted name, with the icons it can
// be: { 'section.icon': ['archive', 'lock'] }
const VARIABLES = {}

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

const ICON = /^[a-z0-9]+(?:-[a-z0-9]+)*$/

// A macro's parameters, or a call's arguments, one per item: name="a value" stays whole
function words (text) {
  return text.match(/(?:[^\s"']+|"[^"]*"|'[^']*')+/g) || []
}

// Each macro that makes an icon's class from a bare name it is given (class="fa-${icon}"), with that
// parameter's name and position, so that a call passing the name either way is seen
// (NERC-CEH/dri-one#476: three calls passed it by position, and their icons were dropped)
function iconMacros (templates) {
  const macros = new Map()
  for (const { source } of templates) {
    for (const [, name, params, body] of source.matchAll(/<#macro\s+(\w+)([^>]*)>([\s\S]*?)<\/#macro>/g)) {
      const names = words(params).map(param => param.split('=')[0])
      for (const [, param] of body.matchAll(/\bfa-\$\{(\w+)\}/g)) {
        if (names.includes(param)) macros.set(name, { param, index: names.indexOf(param) })
      }
    }
  }
  return macros
}

// The icons the templates pass to those macros
function macroIcons (templates, macros) {
  const icons = []
  for (const { file, source } of templates) {
    for (const [call, name, args] of source.matchAll(/<@(?:\w+\.)?(\w+)\b([^>]*?)\/?>/g)) {
      const macro = macros.get(name)
      if (!macro) continue
      const given = words(args)
      const positional = given.filter(arg => !/^\w+=/.test(arg))
      const keyword = given.find(arg => arg.startsWith(macro.param + '='))
      const value = keyword ? keyword.slice(macro.param.length + 1) : positional[macro.index]
      if (value === undefined) continue // left to the macro's default, which the fa- scan has seen
      if (VARIABLES[value]) {
        icons.push(...VARIABLES[value])
        continue
      }
      const icon = value.replace(/^(["'])(.*)\1$/, '$2')
      if (icon === value || !ICON.test(icon)) {
        throw new Error(`${path.relative(repo, file)}: ${call} gives its ${macro.param} as ${value}, which the ` +
          'Font Awesome scan can\'t read. Pass the name in quotes, or list the icons it can be in VARIABLES ' +
          '(web/scripts/font-awesome-icons.js).')
      }
      icons.push(icon)
    }
  }
  return icons
}

module.exports = function usedIcons () {
  const used = new Set(DYNAMIC)
  const templates = []
  for (const { dir, extensions, skip } of SOURCES) {
    for (const file of files(path.join(repo, dir), extensions, skip)) {
      const source = fs.readFileSync(file, 'utf8')
      for (const [, name] of source.matchAll(/\bfa-([a-z0-9]+(?:-[a-z0-9]+)*)/g)) used.add(name)
      if (file.endsWith('.ftlh') || file.endsWith('.ftl')) templates.push({ file, source })
    }
  }
  macroIcons(templates, iconMacros(templates)).forEach(icon => used.add(icon))
  return used
}
