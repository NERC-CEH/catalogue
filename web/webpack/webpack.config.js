const HtmlWebPackPlugin = require('html-webpack-plugin')

module.exports = {

  entry: {
    catalogue: {
      import: './catalogue/src/index.js',
      dependOn: 'editor'
    },
    clipboard: './clipboard/src/index.js',
    editor: './editor/src/index.js',
    hubbub: './hubbub/src/index.js',
    permission: './permission/src/index.js',
    simpleupload: './simple-upload/src/index.js'
  },
  mode: 'development',
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
        test: /\.(png|svg|jpg|gif)$/,
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
  plugins: [
    new HtmlWebPackPlugin({
      template: './catalogue/src/index.html',
      filename: './index.html'
    }),
    new HtmlWebPackPlugin({
      template: './clipboard/src/index.html',
      filename: './index.html'
    }),
    new HtmlWebPackPlugin({
      template: './editor/src/index.html',
      filename: './index.html'
    }),
    new HtmlWebPackPlugin({
      template: './hubbub/src/index.html',
      filename: './index.html'
    }),
    new HtmlWebPackPlugin({
      template: './permission/src/index.html',
      filename: './index.html'
    }),
    new HtmlWebPackPlugin({
      template: './simple-upload/src/index.html',
      filename: './index.html'
    })
  ]
}
