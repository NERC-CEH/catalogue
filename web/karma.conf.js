const { execFileSync } = require('child_process')
const chromePath = require('puppeteer').executablePath()
// puppeteer 22 returns a string; puppeteer 25+ returns a Promise
process.env.CHROME_BIN = typeof chromePath === 'string'
  ? chromePath
  : execFileSync(process.execPath, ['-e', "require('puppeteer').executablePath().then(p => process.stdout.write(p))"], { cwd: __dirname }).toString().trim()

module.exports = function (config) {
  config.set({

    frameworks: ['jasmine-ajax', 'jasmine', 'webpack'],

    files: [
      'src/*/test/*.js'
    ],

    preprocessors: {
      'src/*/test/*.js': ['webpack']
    },

    webpack: require('./webpack.js'),

    reporters: ['progress', 'junit'],

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
