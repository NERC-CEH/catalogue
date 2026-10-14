const fs = require('fs')
const path = require('path')
const CssMinimizerPlugin = require('css-minimizer-webpack-plugin')
const MiniCssExtractPlugin = require('mini-css-extract-plugin')
const postcss = require('postcss')
const usedIcons = require('./scripts/font-awesome-icons')
const sassOptions = require('./scripts/sass-options')

const scss = path.join(__dirname, 'scss')

// base.css, which every page links, and one theme per catalogue (style-<catalogue>.scss) that each
// page links after it; the other files are partials base.scss uses
const stylesheets = {
  base: path.join(scss, 'base.scss'),
  ...Object.fromEntries(fs.readdirSync(scss)
    .filter(file => file.startsWith('style-') && file.endsWith('.scss'))
    .map(file => [path.basename(file, '.scss'), path.join(scss, file)]))
}

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

// Drops the rule Font Awesome emits for each icon (.fa-<name>::before) unless the catalogue uses that
// icon: about 1,900 rules, a sixth of base.css (NERC-CEH/dri-one#362). Done before the minifier runs,
// and in every build, so a missing icon shows up in development too.
class FontAwesomeSubset {
  apply (compiler) {
    const { Compilation, sources } = compiler.webpack
    compiler.hooks.thisCompilation.tap('FontAwesomeSubset', compilation => {
      // So that the watcher rebuilds when a template starts using another icon
      compilation.contextDependencies.add(path.join(__dirname, '..', 'templates'))
      compilation.hooks.processAssets.tap(
        { name: 'FontAwesomeSubset', stage: Compilation.PROCESS_ASSETS_STAGE_OPTIMIZE },
        () => {
          const used = usedIcons()
          for (const file of compilation.namedChunks.get('base').files) {
            if (!file.endsWith('.css')) continue
            const root = postcss.parse(compilation.getAsset(file).source.source().toString())
            root.walkRules(rule => {
              const icons = rule.selectors.map(selector => selector.match(/^\.fa-([a-z0-9-]+)::?before$/))
              if (!icons.every(Boolean)) return
              const kept = icons.filter(([, name]) => used.has(name)).map(([selector]) => selector)
              if (kept.length) rule.selectors = kept
              else rule.remove()
            })
            compilation.updateAsset(file, new sources.RawSource(root.toString()))
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
  optimization: {
    // '...' keeps webpack's own JavaScript minifier; cssnano minifies the stylesheets, library CSS
    // included, in production builds (NERC-CEH/dri-one#362)
    minimizer: ['...', new CssMinimizerPlugin()]
  },
  plugins: [
    new MiniCssExtractPlugin({ filename: '../css/[name].css' }),
    new CopyLibraryImages(),
    new StylesheetEntries(),
    new FontAwesomeSubset()
  ]
}
