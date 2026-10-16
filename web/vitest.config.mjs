// The front-end specs: Vitest, as DRI-UI uses, in a real browser (Chromium, driven by Playwright),
// replacing the retired Karma (NERC-CEH/dri-one#468). `npm test` runs them; `npm run test-coverage`
// adds the coverage report and the floor below, as CI does.
import { createRequire } from 'node:module'
import { existsSync } from 'node:fs'
import { defineConfig } from 'vitest/config'
import { playwright } from '@vitest/browser-playwright'
import { legacyModulePackages, legacyModules, templates } from './scripts/vite-plugins.mjs'

const require = createRequire(import.meta.url)

// The Chrome Puppeteer installs (`npx puppeteer browsers install chrome`), or CI's own chromium
const chrome = process.env.PUPPETEER_EXECUTABLE_PATH || await require('puppeteer').executablePath()
if (!existsSync(chrome)) {
  throw new Error(`No Chrome at ${chrome} for the specs: run \`npx puppeteer browsers install chrome\` in web/`)
}

export default defineConfig({
  // The build's own plugins (vite.config.mjs), so the specs run the code as the page does: the
  // templates compiled, and jQuery UI, select2 and leaflet-draw wired up as the build wires them
  plugins: [templates, legacyModules],
  optimizeDeps: { exclude: legacyModulePackages },
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
