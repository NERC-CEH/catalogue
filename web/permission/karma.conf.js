// Karma configuration
// Generated on Tue Feb 08 2022 14:50:21 GMT+0000 (Greenwich Mean Time)
const webpackConfig = require('./webpack.config.js')
webpackConfig.entry = {}

module.exports = function (config) {
  config.set({

    // base path that will be used to resolve all patterns (eg. files, exclude)
    basePath: '',

    // frameworks to use
    // available frameworks: https://www.npmjs.com/search?q=keywords:karma-adapter
    frameworks: ['jasmine', 'webpack'],

    // list of files / patterns to load in the browser
    files: [
      './dist/permission-app.js',
      './spec/IdentityPermission.spec.js'
    ],
    // list of files / patterns to exclude
    exclude: [
    ],
    loggers: [
      {
        type: 'console'
      }
    ],

    // preprocess matching files before serving them to the browser
    // available preprocessors: https://www.npmjs.com/search?q=keywords:karma-preprocessor
    preprocessors: {
      './src/IdentityPermissionView.js': ['webpack'],
      './spec/IdentityPermission.spec.js': ['webpack']
    },
    webpack: webpackConfig,

    // test results reporter to use
    // possible values: 'dots', 'progress'
    // available reporters: https://www.npmjs.com/search?q=keywords:karma-reporter
    reporters: ['progress'],

    // web server port
    port: 9876,

    // enable / disable colors in the output (reporters and logs)
    colors: true,

    // level of logging
    // possible values: config.LOG_DISABLE || config.LOG_ERROR || config.LOG_WARN || config.LOG_INFO || config.LOG_DEBUG
    logLevel: config.LOG_INFO,

    // enable / disable watching file and executing tests whenever any file changes
    autoWatch: true,

    // start these browsers
    // available browser launchers: https://www.npmjs.com/search?q=keywords:karma-launcher
    browsers: [],

    client: {
      clearContext: true
    },

    singleRun: false,

    // Concurrency level
    // how many browser instances should be started simultaneously
    concurrency: Infinity
  })
}
