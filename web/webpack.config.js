const HtmlWebPackPlugin = require('html-webpack-plugin')
const { ModuleFederationPlugin } = require('webpack').container
module.exports = {

  entry: {
    catalogue: './scripts/catalogue/src/index.js',
    clipboard: './scripts/clipboard/src/index.js',
    editor: './scripts/editor/src/index.js',
    hubbub: './scripts/hubbub/src/index.js',
    permission: './scripts/permission/src/index.js',
    search: './scripts/search/src/index.js',
    simpleupload: './scripts/simple-upload/src/index.js',
    studyarea: './scripts/study-area/src/index.js'
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
  plugins: [
    new HtmlWebPackPlugin({
      filename: './index.html'
    }),
    new ModuleFederationPlugin({
      shared: {
        '@types/leaflet': { singleton: true, strictVersion: true, requiredVersion: '^0.7.7', eager: true },
        leaflet: { singleton: true, strictVersion: true, requiredVersion: '^0.7.7', eager: true },
        'leaflet-draw': { singleton: true, strictVersion: true, requiredVersion: '^1.0.4', eager: true }
      }
    })
  ]
}
