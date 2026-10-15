// The build of the catalogue's scripts and stylesheets: Vite, as the specs (vitest.config.mjs) and
// DRI-UI use, replacing webpack (NERC-CEH/dri-one#474). `npm run build` is the production build;
// `npm run watch` rebuilds on each change, for the local stack.
import fs from 'node:fs'
import path from 'node:path'
import { createRequire } from 'node:module'
import { defineConfig } from 'vite'
import { legacyModules, templates } from './scripts/vite-plugins.mjs'

const require = createRequire(import.meta.url)
const usedIcons = require('./scripts/font-awesome-icons')
const sassOptions = require('./scripts/sass-options')

const web = import.meta.dirname
const scss = path.join(web, 'scss')

// base.css, which every page links, and one theme per catalogue (style-<catalogue>.scss) that each
// page links after it; the other files are partials base.scss uses
const stylesheets = {
  base: path.join(scss, 'base.scss'),
  ...Object.fromEntries(fs.readdirSync(scss)
    .filter(file => file.startsWith('style-') && file.endsWith('.scss'))
    .map(file => [path.basename(file, '.scss'), path.join(scss, file)]))
}

const entries = {
  main: path.join(web, 'src', 'index.js'),
  // The SPARQL page (/sparql), which isn't built on the skeleton and loads only this
  sparql: path.join(web, 'src', 'sparql', 'index.js'),
  ...stylesheets
}

// The libraries every page loads (jQuery, Backbone, Bootstrap...) go in a chunk of their own, which a
// change to the catalogue's own code leaves as it is, and so cached. Libraries only a micro-app uses
// (Leaflet, select2, DataTables...) go with that micro-app's chunk. Rolldown asks only whether a module
// belongs in the chunk, so this works out which libraries main imports, other than by import().
const mainLibraries = new Set()
const vendor = {
  name: 'vendor-chunk',
  buildEnd () {
    mainLibraries.clear()
    const seen = new Set()
    const visit = id => {
      if (seen.has(id)) return
      seen.add(id)
      if (/[\\/]node_modules[\\/]/.test(id)) mainLibraries.add(id)
      this.getModuleInfo(id)?.importedIds.forEach(visit)
    }
    visit(entries.main)
  }
}

// Drops the rule Font Awesome emits for each icon unless the catalogue uses that icon: about 1,900
// rules, a sixth of base.css (NERC-CEH/dri-one#362). A PostCSS plugin, so it runs on the compiled Sass
// before the stylesheet is minified and named for its content, and in every build, so a missing icon
// shows up in development too. An icon's rule is `.fa-<name>::before { content: ... }` up to Font
// Awesome 6.6 and `.fa-<name> { --fa: ... }` from 6.7; the body is checked too, as utilities such as
// .fa-spin have the same selector shape.
const templatesDirectory = path.join(web, '..', 'templates')
const fontAwesomeSubset = {
  postcssPlugin: 'font-awesome-subset',
  Once (root, { result }) {
    if (result.opts.from !== stylesheets.base) return
    // So that the watcher rebuilds base.css when a template starts using another icon
    result.messages.push({ type: 'dir-dependency', plugin: 'font-awesome-subset', dir: templatesDirectory, glob: '**/*', parent: result.opts.from })
    const used = usedIcons()
    let iconRules = 0
    root.walkRules(rule => {
      const icons = rule.selectors.map(selector => selector.match(/^\.fa-([a-z0-9-]+)(?:::?before)?$/))
      if (!icons.every(Boolean)) return
      if (!rule.nodes.every(node => node.type === 'decl' && ['content', '--fa'].includes(node.prop))) return
      iconRules++
      const kept = icons.filter(([, name]) => used.has(name)).map(([selector]) => selector)
      if (kept.length) rule.selectors = kept
      else rule.remove()
    })
    // A Font Awesome upgrade that changes the rules' shape would otherwise keep every icon quietly
    if (iconRules < 1000) {
      throw root.error(`font-awesome-subset recognised only ${iconRules} icon rules: has Font Awesome changed how it writes them?`)
    }
  }
}

// Font Awesome's icons are characters outside ASCII (content: ""), written as they are, and the
// minifier drops the @charset Sass puts before them. Without it a browser decodes the stylesheet in
// the page's encoding, which is UTF-8 now but needn't stay so, so it is put back.
const charset = {
  name: 'charset',
  enforce: 'post',
  generateBundle (options, bundle) {
    for (const file of Object.values(bundle)) {
      if (file.type !== 'asset' || !file.fileName.endsWith('.css')) continue
      const css = file.source.toString()
      if (/[^\x00-\x7F]/.test(css) && !css.startsWith('@charset')) file.source = '@charset "UTF-8";' + css // eslint-disable-line no-control-regex
    }
  }
}

// The stylesheet a stylesheet entry (base, style-eidc...) compiles to
function isEntryStylesheet (file, entry) {
  return file.type === 'asset' && file.originalFileNames.includes(path.relative(web, stylesheets[entry]))
}

// YASGUI's stylesheet bundles DataTables', with old Internet Explorer hacks (*cursor:hand, *zoom:1)
// that every other browser ignores. LightningCSS, which minifies the stylesheets, rejects them, so
// they are dropped. Its errorRecovery would drop the whole rule (cursor:pointer with *cursor:hand),
// and anything else it couldn't read, without a word: an unquoted data: URL went that way
// (dri-one #474).
const ieHacks = {
  name: 'drop-ie-hacks',
  transform (code, id) {
    if (!/[\\/]node_modules[\\/]@triply[\\/]yasgui[\\/].*\.css$/.test(id)) return null
    return { code: code.replace(/([{;])\*[a-z-]+:[^;}]*;?/g, '$1'), map: null }
  }
}

// Every file the build writes has a content hash in its name, so it can be cached for a year. This
// writes manifest.json, which lists the files each entry needs, in order, and is how the templates
// find them (StaticAssets on the Java side, NERC-CEH/dri-one#364):
//   { "main": ["/static/scripts/vendor.1a2b3c4d.js", "/static/scripts/main.5e6f7a8b.js"],
//     "base": ["/static/css/base.9c0d1e2f.css"], ... }
// A script's stylesheets come first, then the chunks it imports, then the script. The page loads the
// chunks itself, so that they download alongside the entry rather than once it has been read.
function manifest (base) {
  return {
    name: 'manifest',
    enforce: 'post',
    generateBundle (options, bundle) {
      const files = Object.values(bundle)
      const result = {}
      for (const name of Object.keys(entries)) {
        if (stylesheets[name]) {
          result[name] = files.filter(file => isEntryStylesheet(file, name)).map(file => base + file.fileName)
          continue
        }
        const entry = files.find(file => file.type === 'chunk' && file.isEntry && file.name === name)
        const chunks = []
        const visit = chunk => {
          if (chunks.includes(chunk)) return
          chunk.imports.forEach(imported => visit(bundle[imported]))
          chunks.push(chunk)
        }
        visit(entry)
        result[name] = [
          ...new Set(chunks.flatMap(chunk => [...chunk.viteMetadata.importedCss])),
          ...chunks.map(chunk => chunk.fileName)
        ].map(file => base + file)
      }
      this.emitFile({ type: 'asset', fileName: 'manifest.json', source: JSON.stringify(result, null, 2) })
    }
  }
}

export default defineConfig(({ mode }) => {
  const development = mode === 'development'
  const base = '/static/'
  return {
    base,
    // Nothing is copied into the output as it is: /static/img is served from web/img
    publicDir: false,
    plugins: [templates, legacyModules, ieHacks, vendor, charset, manifest(base)],
    css: {
      preprocessorOptions: { scss: sassOptions },
      postcss: { plugins: [fontAwesomeSubset] }
    },
    build: {
      // Laid out as it is served, under /static: scripts/, css/ and assets/ (the images and fonts the
      // stylesheets use, and files a script asks for by URL), plus manifest.json
      outDir: 'dist',
      assetsDir: 'assets',
      emptyOutDir: true,
      // Pages are built by the Java side, not from an index.html
      manifest: false,
      minify: !development,
      // Files beside the scripts in development: inline maps would be no smaller, and eval()ed ones
      // are refused by the Content-Security-Policy (dri-one #464)
      sourcemap: development,
      // The budget Vite warns past. The SPARQL page's YASGUI (1 MB) is past it, and only that page
      // loads it.
      chunkSizeWarningLimit: 1100,
      // Every asset is a file of its own, as webpack wrote them. Vite would otherwise put small ones
      // into the stylesheets and scripts as data: URLs, which the Content-Security-Policy's
      // connect-src refuses for prefixes.json (YASGUI fetches it).
      assetsInlineLimit: 0,
      rolldownOptions: {
        input: entries,
        checks: {
          // A report on the plugins' share of each build's time, unasked for
          pluginTimings: false
        },
        onLog (level, log, handler) {
          // YASGUI's own build calls eval(), as it did under webpack; and select2 tests for CommonJS's
          // module, a global in an ES module, after its AMD route (legacyModules) has already been taken
          if (log.code === 'EVAL' && /[\\/]@triply[\\/]yasgui[\\/]/.test(log.id)) return
          if (log.code === 'COMMONJS_VARIABLE_IN_ESM' && /[\\/]select2[\\/]/.test(log.id)) return
          handler(level, log)
        },
        output: {
          entryFileNames: 'scripts/[name].[hash].js',
          chunkFileNames: 'scripts/[name].[hash].js',
          assetFileNames: ({ names }) => names.some(name => name.endsWith('.css'))
            ? 'css/[name].[hash][extname]'
            : 'assets/[name].[hash][extname]',
          codeSplitting: {
            groups: [{ name: 'vendor', test: id => mainLibraries.has(id) }]
          }
        }
      }
    }
  }
})
