process.env.CHROME_BIN = require('puppeteer').executablePath()
const webpackConfig = require('./webpack.config.js')

module.exports = function (config) {
  config.set({
    browsers: ['ChromeHeadless'],
    files: [
      './test/**/*.js'
    ],
    frameworks: ['jasmine', 'webpack'],
    preprocessors: {
      './test/**/*.js': ['webpack']
    },
    reporters: ['progress', 'junit'],
    webpack: webpackConfig
  })
}
