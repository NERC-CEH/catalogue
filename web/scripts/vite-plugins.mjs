/*
 * The Vite plugins the build (vite.config.mjs) and the specs (vitest.config.mjs) share, so that a spec
 * runs the code as the page does (NERC-CEH/dri-one#474).
 */
import { createRequire } from 'node:module'
import { transformAsync } from '@babel/core'

const require = createRequire(import.meta.url)
const precompileTemplates = require('./babel-plugin-precompile-templates')

// The catalogue's templates are compiled at build time (babel-plugin-precompile-templates.js), so the
// Content-Security-Policy needs no unsafe-eval (dri-one #464). Only that plugin: the build's target
// browsers need no other transform.
export const templates = {
  name: 'precompile-templates',
  enforce: 'pre',
  async transform (code, id) {
    if (!/\/src\/.*\.js$/.test(id) || id.includes('/node_modules/') || !code.includes('template')) return null
    const result = await transformAsync(code, {
      filename: id,
      babelrc: false,
      configFile: false,
      sourceMaps: true,
      plugins: [precompileTemplates]
    })
    return { code: result.code, map: result.map }
  }
}

// Libraries written for AMD or for globals, which webpack wired up by itself and Rolldown doesn't:
// - jQuery UI's widgets and select2 are UMD, taking the AMD route if there is a define(). Each is
//   given one, which imports what it names and calls the factory with it, as webpack did. Otherwise a
//   jQuery UI widget falls back on a jQuery with no $.widget, and select2 on its CommonJS form, which
//   exports a function to call with jQuery rather than adding itself to it.
// - leaflet-draw adds itself to a global L, which nothing promises is there when it runs, so it is
//   given Leaflet's as an import.
// The code is added ahead of the library's first line and after its last, so the lines in between
// keep their numbers and need no source map of their own.
const AMD = /[\\/]node_modules[\\/](jquery-ui[\\/]ui|select2[\\/]dist[\\/]js)[\\/][^\\/]+(?:[\\/][^\\/]+)?\.js$/
const LEAFLET_PLUGIN = /[\\/]node_modules[\\/]leaflet-draw[\\/]dist[\\/][^\\/]+\.js$/

export const legacyModules = {
  name: 'legacy-modules',
  transform (code, id) {
    // In development, a dependency's id ends in a version (?v=...)
    const file = id.split('?')[0]
    if (LEAFLET_PLUGIN.test(file)) {
      return { code: "import L from 'leaflet';" + code, map: null }
    }
    if (!AMD.test(file)) return null
    const define = code.match(/\bdefine\(\s*\[([^\]]*)\]\s*,\s*factory\s*\)/)
    if (!define) this.error(`No define([...], factory) in ${id} for the legacy-modules plugin to answer`)
    const dependencies = [...define[1].matchAll(/["']([^"']+)["']/g)].map(([, dependency]) => dependency)
    const names = dependencies.map((_, i) => `__amd${i}`)
    const imports = dependencies.map((dependency, i) => `import ${names[i]} from ${JSON.stringify(dependency)};`)
    return {
      code: imports.join('') +
        `let __amdExport;const define=(_,factory)=>{__amdExport=factory(${names.join(',')})};define.amd=true;` +
        code + '\nexport default __amdExport;\n',
      map: null
    }
  }
}

// What vitest needs to apply legacyModules: its dependency pre-bundling would otherwise take these
// libraries past the plugin, as Vite's development server does
export const legacyModulePackages = ['jquery-ui', 'select2', 'leaflet-draw']
