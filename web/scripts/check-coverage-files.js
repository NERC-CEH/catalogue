#!/usr/bin/env node
/*
 * Fails when a source file is loaded by no spec at all. Istanbul only measures the files the specs
 * import, so one nothing tests is missing from the coverage figures rather than counted against
 * them (NERC-CEH/dri-one#473).
 *
 * Runs after `npm run test-coverage`, from coverage/lcov.info. NOT_YET_LOADED lists the files no spec
 * loads today, with the reason; #473 works through it, and anything new must come with a spec.
 */
const fs = require('fs')
const path = require('path')

const NOT_YET_LOADED = {
  // Every page's entry point: claims the jQuery globals and calls startApps(document), which runs on
  // import. What it starts is in startup/apps.js, which the specs cover.
  'src/index.js': 'runs the page on import',
  // The SPARQL page's start-up, which builds YASGUI on import
  'src/sparql/index.js': '#473'
}

const root = path.join(__dirname, '..')
const lcov = path.join(root, 'coverage', 'lcov.info')
if (!fs.existsSync(lcov)) {
  console.error('No coverage/lcov.info: run `npm run test-coverage` first')
  process.exit(1)
}

const loaded = new Set(fs.readFileSync(lcov, 'utf8').split('\n')
  .filter(line => line.startsWith('SF:'))
  .map(line => path.relative(root, path.resolve(root, line.slice(3).trim())).split(path.sep).join('/')))

function sourceFiles (dir) {
  return fs.readdirSync(dir, { withFileTypes: true }).flatMap(entry => {
    const full = path.join(dir, entry.name)
    if (entry.isDirectory()) return entry.name === 'test' ? [] : sourceFiles(full)
    return entry.name.endsWith('.js') ? [path.relative(root, full).split(path.sep).join('/')] : []
  })
}

const unloaded = sourceFiles(path.join(root, 'src')).filter(file => !loaded.has(file))
const unexpected = unloaded.filter(file => !(file in NOT_YET_LOADED))
const nowLoaded = Object.keys(NOT_YET_LOADED).filter(file => !unloaded.includes(file))

if (unexpected.length) {
  console.error(`No spec loads ${unexpected.length} source file(s); add a spec that imports each:\n  ${unexpected.join('\n  ')}`)
  process.exit(1)
}
if (nowLoaded.length) {
  console.error(`Specs now load these, so take them off NOT_YET_LOADED in ${path.relative(root, __filename)}:\n  ${nowLoaded.join('\n  ')}`)
  process.exit(1)
}
console.log(`Every source file is loaded by a spec, apart from the ${unloaded.length} still listed in NOT_YET_LOADED`)
