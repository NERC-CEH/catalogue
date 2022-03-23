// noinspection JSUnresolvedFunction
process.env.CHROME_BIN = require('puppeteer').executablePath()
const webpackConfig = require('./webpack.config.js')

module.exports = function (config) {
  config.set({
    browsers: ['ChromeHeadless_no_sandbox'],
    customLaunchers: {
      ChromeHeadless_no_sandbox: {
        base: 'Chrome',
        flags: [
          '--no-sandbox',
          '--disable-setuid-sandbox',
          '--headless',
          '--disable-gpu',
          '--remote-debugging-port=9222'
        ]
      }
    },
    files: [
      './test/**/*.js'
    ],
    frameworks: ['jasmine-ajax', 'jasmine', 'webpack'],
    preprocessors: {
      './test/**/*.js': ['webpack']
    },
    reporters: ['progress', 'junit'],
    webpack: webpackConfig
  })
}
