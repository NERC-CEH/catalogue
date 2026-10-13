#!/usr/bin/env node
/*
 * Fails when an underscore template interpolates without escaping.
 *
 * `<%= x %>` inserts x as raw HTML; `<%- x %>` escapes it. Record data, server responses and URL
 * fragments reached the page through `<%=`, which made every record field a stored-XSS sink and
 * truncated values containing a quote (NERC-CEH/dri-one#454). Use `<%-` for data. The only `<%=`
 * allowed is for values that are trusted HTML from code or editor configuration, listed below
 * per file, with the reason.
 *
 * Run by `npm test` (as the pretest step) and in CI.
 */
const fs = require('fs')
const path = require('path')

const ALLOWED = {
  // Section label and help text come from editor configuration and contain markup (<small>, <p>)
  'src/editor/src/singleTemplate.js': ['data.label', 'data.helpText'],
  'src/editor/src/templates/Parent.js': ['data.label', 'data.helpText'],
  // Hand-written <option> list from editor configuration
  'src/editor/src/InputView.js': ['data.listAttribute'],
  // Question text from editor configuration (<h3>, <p>, <strong>)
  'src/editor/src/views/RadioView.js': ['data.text'],
  'src/editor/src/views/service-agreement/TextOnlyView.js': ['data.text'],
  // Icon markup from the view's own constant symbol list
  'src/editor/src/templates/MapStyleSelector.js': ['s.icon'],
  // Output of an already-rendered (and itself checked) nested template
  'src/search/src/templates/facetsPanelTemplate.js': ['template({"facet": facet, "template": template})']
}

const root = path.join(__dirname, '..')

function sourceFiles (dir) {
  return fs.readdirSync(dir, { withFileTypes: true }).flatMap(entry => {
    const full = path.join(dir, entry.name)
    if (entry.isDirectory()) return entry.name === 'test' ? [] : sourceFiles(full)
    return entry.name.endsWith('.js') ? [full] : []
  })
}

const failures = []
for (const file of sourceFiles(path.join(root, 'src'))) {
  const relative = path.relative(root, file).split(path.sep).join('/')
  const source = fs.readFileSync(file, 'utf8')
  for (const match of source.matchAll(/<%=\s*([\s\S]*?)\s*%>/g)) {
    const expression = match[1].replace(/\s+/g, ' ')
    if ((ALLOWED[relative] || []).includes(expression)) continue
    const line = source.slice(0, match.index).split('\n').length
    failures.push(`${relative}:${line}  <%= ${expression} %>`)
  }
}

if (failures.length) {
  console.error(`Unescaped template interpolation (use <%- %>, or add trusted markup to ALLOWED in ${path.relative(root, __filename)}):`)
  failures.forEach(failure => console.error(`  ${failure}`))
  process.exit(1)
}
console.log('Template escaping check passed')
