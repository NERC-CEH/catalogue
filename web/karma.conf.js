const { execFileSync } = require('child_process')
const fs = require('fs')
process.env.CHROME_BIN = execFileSync(
  process.execPath,
  ['-e', "require('puppeteer').executablePath().then(p => process.stdout.write(p))"],
  { cwd: __dirname }
).toString().trim()
// Puppeteer downloads its Chrome when it is installed, unless npm skips its install script (npm 11
// can) or a Puppeteer upgrade wants a newer Chrome. CI sets PUPPETEER_EXECUTABLE_PATH to its own.
if (!fs.existsSync(process.env.CHROME_BIN)) {
  throw new Error(`No Chrome at ${process.env.CHROME_BIN} for the specs: run \`npx puppeteer browsers install chrome\` in web/`)
}

// Opt-in coverage: `npm run test-coverage` sets COVERAGE, which selects the
// Babel `coverage` env (babel-plugin-istanbul) and adds the coverage reporter.
// Must be set before webpack.js is required so babel-loader picks up the env.
const coverage = !!process.env.COVERAGE
if (coverage) {
  process.env.BABEL_ENV = 'coverage'
}

module.exports = function (config) {
  config.set({

    frameworks: ['jasmine-ajax', 'jasmine', 'webpack'],

    files: [
      'src/**/test/**/*.js',
      // Catalogue themes compiled by scripts/build-test-css.js, for the contrast specs
      { pattern: 'build/test-css/*.css', included: false, served: true, watched: false }
    ],

    preprocessors: {
      'src/**/test/**/*.js': ['webpack']
    },

    // Only the loaders: the build's entries (main.js and every stylesheet) and plugins aren't wanted
    // for the specs, which karma-webpack bundles itself
    webpack: { module: require('./webpack.js')({}, { mode: 'development' }).module },

    reporters: coverage ? ['progress', 'junit', 'coverage'] : ['progress', 'junit'],

    coverageReporter: {
      // A floor, just under the coverage the specs reach, so it can't fall; raised as it rises, towards
      // 80% of lines and 70% of branches before Karma is replaced, when the new runner has to reach
      // the same (NERC-CEH/dri-one#473, #468). CI runs `npm run test-coverage`, so it fails below it.
      check: {
        global: { statements: 75, branches: 71, functions: 72, lines: 76 }
      },
      dir: 'coverage',
      reporters: [
        { type: 'html', subdir: 'html' },
        { type: 'text-summary' },
        { type: 'lcovonly', subdir: '.', file: 'lcov.info' }
      ]
    },

    colors: true,

    autoWatch: true,

    browsers: ['ChromeHeadless_CI'],

    customLaunchers: {
      ChromeHeadless_CI: {
        base: 'ChromeHeadless',
        flags: ['--no-sandbox']
      }
    },

    singleRun: true

  })
}
