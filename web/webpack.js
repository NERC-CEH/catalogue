const fs = require('fs')
const path = require('path')
const MiniCssExtractPlugin = require('mini-css-extract-plugin')
const sassOptions = require('./scripts/sass-options')

const scss = path.join(__dirname, 'scss')

// One stylesheet per catalogue (style-<catalogue>.scss); the other files are partials they use
const stylesheets = Object.fromEntries(fs.readdirSync(scss)
  .filter(file => file.startsWith('style-') && file.endsWith('.scss'))
  .map(file => [path.basename(file, '.scss'), path.join(scss, file)]))

// The images the inlined library stylesheets load from css/images (url("images/..."))
const IMAGES = [
  'node_modules/leaflet-draw/dist/images',
  'node_modules/jquery-ui/dist/themes/base/images'
]

class CopyLibraryImages {
  apply (compiler) {
    compiler.hooks.afterEmit.tapPromise('CopyLibraryImages', async () => {
      const images = path.join(__dirname, 'css', 'images')
      await fs.promises.mkdir(images, { recursive: true })
      for (const dir of IMAGES) {
        await fs.promises.cp(path.join(__dirname, dir), images, { recursive: true })
      }
    })
  }
}

// Tidies what webpack makes of a stylesheet entry. It still gets a JavaScript file, holding nothing
// but webpack's runtime, which is dropped. And the library stylesheets are concatenated ahead of the
// Sass output, so its @charset lands mid-file, where browsers ignore it; it is moved to the top.
class StylesheetEntries {
  apply (compiler) {
    const { Compilation, sources } = compiler.webpack
    compiler.hooks.thisCompilation.tap('StylesheetEntries', compilation => {
      compilation.hooks.processAssets.tap(
        { name: 'StylesheetEntries', stage: Compilation.PROCESS_ASSETS_STAGE_SUMMARIZE },
        () => {
          for (const name of Object.keys(stylesheets)) {
            for (const file of [...compilation.namedChunks.get(name).files]) {
              if (file.endsWith('.js')) {
                compilation.deleteAsset(file)
              } else if (file.endsWith('.css')) {
                compilation.updateAsset(file, source => {
                  const css = source.source().toString()
                  const charset = '@charset "UTF-8";\n'
                  return css.includes(charset)
                    ? new sources.RawSource(charset + css.replaceAll(charset, ''))
                    : source
                })
              }
            }
          }
        }
      )
    })
  }
}

module.exports = {
  entry: {
    main: './src/index.js',
    ...stylesheets
  },
  output: {
    // Served as /static/scripts; the stylesheets go to ../css, served as /static/css
    path: path.join(__dirname, 'dist'),
    clean: true
  },
  module: {
    rules: [
      {
        test: /\.js$/,
        exclude: /node_modules/,
        use: {
          loader: 'babel-loader',
          options: { cacheDirectory: true }
        }
      },
      {
        test: /\.scss$/,
        use: [
          MiniCssExtractPlugin.loader,
          {
            loader: 'css-loader',
            // Inline the library stylesheets the Sass @imports as plain CSS (leaflet, select2,
            // DataTables...), as they are, and leave url()s alone: images and fonts are served as
            // they are
            options: { url: false }
          },
          {
            loader: 'sass-loader',
            options: { sassOptions: { ...sassOptions, style: 'expanded' } }
          }
        ]
      }
    ]
  },
  plugins: [
    new MiniCssExtractPlugin({ filename: '../css/[name].css' }),
    new CopyLibraryImages(),
    new StylesheetEntries()
  ]
}
