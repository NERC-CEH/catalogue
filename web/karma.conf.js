process.env.CHROME_BIN = require('puppeteer').executablePath()
const webpackConfig = require('./webpack.js')

module.exports = function (config) {
  config.set({

    frameworks: ['jasmine-ajax', 'jasmine', 'webpack'],

    files: [
      './scripts/*/test/*.js'
    ],

    loggers: [
      {
        type: 'console'
      }
    ],

    preprocessors: {
      './scripts/*/test/*.js': ['webpack']
    },

    webpack: webpackConfig,

    reporters: ['progress', 'junit'],

    colors: true,

    logLevel: config.LOG_INFO,

    autoWatch: true,

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

    browsers: ['ChromeHeadless_no_sandbox'],

    client: {
      clearContext: true
    },

    singleRun: true

  })
}
