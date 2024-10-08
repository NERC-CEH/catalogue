const path = require('path')
const MiniCssExtractPlugin = require('mini-css-extract-plugin')
const RemoveEmptyScriptsPlugin = require('webpack-remove-empty-scripts')
const CssMinimizerPlugin = require('css-minimizer-webpack-plugin')

module.exports = {
  entry: {
    'style-assist': './scss/style-assist.scss',
    'style-ukceh': './scss/style-ukceh.scss',
    'style-cmp': './scss/style-cmp.scss',
    'style-datalabs': './scss/style-datalabs.scss',
    'style-edge': './scss/style-edge.scss',
    'style-elter': './scss/style-elter.scss',
    'style-erammp': './scss/style-erammp.scss',
    'style-inlicensed': './scss/style-inlicensed.scss',
    'style-inms': './scss/style-inms.scss',
    'style-nc': './scss/style-nc.scss',
    'style-nm': './scss/style-nm.scss',
    'style-m': './scss/style-m.scss',
    'style-osdp': './scss/style-osdp.scss',
    'style-pimfe': './scss/style-pimfe.scss',
    'style-infrastructure': './scss/style-infrastructure.scss',
    'style-sa': './scss/style-sa.scss',
    'style-ukeof': './scss/style-ukeof.scss',
    'style-ukscape': './scss/style-ukscape.scss',
    'style-eidc': './scss/style-eidc.scss'
  },
  output: {
    path: path.resolve(__dirname, 'css'),
    filename: '[name].js'
  },
  module: {
    rules: [
      {
        test: /\.scss$/,
        use: [
          MiniCssExtractPlugin.loader,
          {
            loader: 'css-loader',
            options: {
              sourceMap: true,
              url: false
            }
          },
          {
            loader: 'postcss-loader',
            options: {
              sourceMap: true
            }
          },
          {
            loader: 'sass-loader',
            options: {
              sourceMap: true,
              sassOptions: {
                quietDeps: true,
                includePaths: [
                  path.resolve(__dirname, 'node_modules'),
                  path.resolve(__dirname, 'scss')
                ]
              }
            }
          }
        ]
      }
    ]
  },
  optimization: {
    minimizer: [
      new CssMinimizerPlugin()
    ]
  },
  plugins: [
    new MiniCssExtractPlugin({
      filename: '[name].css'
    }),
    new RemoveEmptyScriptsPlugin()
  ],
  mode: 'production'
}
