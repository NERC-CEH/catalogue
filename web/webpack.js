module.exports = {
  entry: {
    main: './src/index.js'
  },
  module: {
    rules: [
      {
        test: /\.js$/,
        exclude: /node_modules/,
        use: {
          loader: 'babel-loader',
          options: [
            ['@babel/preset-env', { targets: 'default' }]
          ]
        }
      },
      {
        test: /\.tpl$/,
        exclude: /node_modules/,
        use: {
          loader: 'html-loader'
        }
      }
    ]
  },
  output: {
    filename: '[name].bundle.js'
  }
}
