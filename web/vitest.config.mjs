// The front-end specs: Vitest, as DRI-UI uses, in a real browser (Chromium, driven by Playwright),
// replacing the retired Karma (NERC-CEH/dri-one#468). `npm test` runs them; `npm run test-coverage`
// adds the coverage report and the floor below, as CI does.
import { createRequire } from 'node:module'
import { existsSync } from 'node:fs'
import { transformAsync } from '@babel/core'
import { defineConfig } from 'vitest/config'
import { playwright } from '@vitest/browser-playwright'

const require = createRequire(import.meta.url)
const precompileTemplates = require('./scripts/babel-plugin-precompile-templates')

// The Chrome Puppeteer installs (`npx puppeteer browsers install chrome`), or CI's own chromium
const chrome = process.env.PUPPETEER_EXECUTABLE_PATH || await require('puppeteer').executablePath()
if (!existsSync(chrome)) {
  throw new Error(`No Chrome at ${chrome} for the specs: run \`npx puppeteer browsers install chrome\` in web/`)
}

// The catalogue's templates are compiled at build time (scripts/babel-plugin-precompile-templates.js,
// dri-one #464); the specs run the same compiled functions. Only that plugin: Chrome needs no preset.
const templates = {
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

export default defineConfig({
  plugins: [templates],
  resolve: {
    alias: [
      // jQuery UI's widgets are AMD modules naming what they need (../widget, ../version...) in their
      // define(). webpack loads those; Vite's dependency bundling doesn't, so each widget fell back on a
      // jQuery with no $.widget. Its complete build defines everything in order, for the specs.
      { find: /^jquery-ui\/ui\/.*$/, replacement: 'jquery-ui/dist/jquery-ui.js' }
    ]
  },
  test: {
    include: ['src/**/test/**/*.js'],
    // The specs' own support, not specs
    exclude: ['src/test/support.js', 'src/test/setup.js', '**/node_modules/**'],
    setupFiles: ['src/test/setup.js'],
    // Each spec's spies and fakes are undone after it, as Jasmine did
    restoreMocks: true,
    // In a random order, as Jasmine ran them, so a spec that leans on another's leftovers fails
    sequence: { shuffle: { files: false, tests: true } },
    browser: {
      enabled: true,
      headless: true,
      provider: playwright({ launchOptions: { executablePath: chrome, args: ['--no-sandbox'] } }),
      instances: [{ browser: 'chromium' }],
      // A failing spec's screenshot would be written beside it, into the source tree
      screenshotFailures: false
    },
    reporters: ['default', ['junit', { outputFile: 'TESTS-web.xml' }]],
    coverage: {
      provider: 'istanbul',
      // Every source file, loaded by a spec or not, so one nothing tests counts against the figures
      // rather than going missing from them. index.js only claims the jQuery globals and starts the
      // apps, on import (startup/apps.js, which the specs cover, holds the rest).
      include: ['src/**/*.js'],
      exclude: ['**/test/**', 'src/index.js'],
      reporter: ['text-summary', 'lcov', 'html'],
      reportsDirectory: 'coverage',
      // A floor, just under the coverage the specs reach, so it can't fall; raised as it rises
      // (NERC-CEH/dri-one#473). CI runs `npm run test-coverage`, so it fails below it.
      thresholds: { statements: 87, branches: 81, functions: 85, lines: 87 }
    }
  }
})
