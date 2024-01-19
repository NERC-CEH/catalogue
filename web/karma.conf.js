process.env.CHROME_BIN = require('puppeteer').executablePath()

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
