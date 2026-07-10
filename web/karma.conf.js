const { execFileSync } = require('child_process')
process.env.CHROME_BIN = execFileSync(
  process.execPath,
  ['-e', "require('puppeteer').executablePath().then(p => process.stdout.write(p))"],
  { cwd: __dirname }
).toString().trim()

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
      'src/**/test/**/*.js'
    ],

    preprocessors: {
      'src/**/test/**/*.js': ['webpack']
    },

    webpack: require('./webpack.js'),

    reporters: coverage ? ['progress', 'junit', 'coverage'] : ['progress', 'junit'],

    coverageReporter: {
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
