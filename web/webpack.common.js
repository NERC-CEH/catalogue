const HtmlWebPackPlugin = require('html-webpack-plugin')
const webpack = require('webpack')
const ESLintPlugin = require('eslint-webpack-plugin')

module.exports = {

  entry: {
    main: './scripts/main.js'
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
        test: /\.(html|tpl)$/,
        use: [
          {
            loader: 'html-loader',
            options: { minimize: true }
          }
        ]
      },
      {
        test: /\.(sa|sc|c)ss$/,
        use: [
          { loader: 'style-loader' },
          {
            loader: 'css-loader',
            options: {
              sourceMap: true
            }
          },
          {
            loader: 'resolve-url-loader',
            options: {
              sourceMap: true,
              engine: 'rework'
            }
          }
        ]
      },
      {
        test: /\.(png|jpg|jpeg|gif|svg|woff|woff2|ttf|eot)$/,
        use: [
          'file-loader'
        ]
      }
    ]
  },
  devServer: {
    host: '0.0.0.0',
    port: 8081
  },
  output: {
    filename: '[name].bundle.js'
  },
  optimization: {
    usedExports: false
  },
  plugins: [
    new HtmlWebPackPlugin({
      filename: './index.html'
    }),
    new webpack.ProvidePlugin({
      $: 'jquery',
      jQuery: 'jquery'
    }),
    new ESLintPlugin()
  ]
}
