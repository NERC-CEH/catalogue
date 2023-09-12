module.exports = {
  entry: {
    main: './scripts/index.js'
  },
  module: {
    rules: [
      {
        test: /\.js$/,
        exclude: /node_modules/,
        use: {
          loader: 'babel-loader'
        }
      },
      {
        test: /\.tpl$/,
        use: [
          {
            loader: 'html-loader'
          }
        ]
      }
    ]
  },
  output: {
    filename: '[name].bundle.js'
  }
}
