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

// Drops the rule Font Awesome emits for each icon unless the catalogue uses that icon: about 1,900
// rules, a sixth of base.css (NERC-CEH/dri-one#362). Done before the minifier runs, and in every
// build, so a missing icon shows up in development too. An icon's rule is `.fa-<name>::before
// { content: ... }` up to Font Awesome 6.6 and `.fa-<name> { --fa: ... }` from 6.7; the body is checked
// too, as utilities such as .fa-spin have the same selector shape.
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
            let iconRules = 0
            root.walkRules(rule => {
              const icons = rule.selectors.map(selector => selector.match(/^\.fa-([a-z0-9-]+)(?:::?before)?$/))
              if (!icons.every(Boolean)) return
              if (!rule.nodes.every(node => node.type === 'decl' && ['content', '--fa'].includes(node.prop))) return
              iconRules++
              const kept = icons.filter(([, name]) => used.has(name)).map(([selector]) => selector)
              if (kept.length) rule.selectors = kept
              else rule.remove()
            })
            // A Font Awesome upgrade that changes the rules' shape would otherwise keep every icon quietly
            if (iconRules < 1000) {
              compilation.errors.push(new compiler.webpack.WebpackError(
                `FontAwesomeSubset recognised only ${iconRules} icon rules in ${file}: has Font Awesome changed how it writes them?`))
            }
            compilation.updateAsset(file, new sources.RawSource(root.toString()))
          }
        }
      )
    })
  }
}

// Every file the build writes has a content hash in its name, so it can be cached for a year. This
// writes manifest.json, which lists the files each entry needs, in order, and is how the templates
// find them (StaticAssets on the Java side, NERC-CEH/dri-one#364):
//   { "main": ["/static/scripts/vendor.1a2b3c4d.js", "/static/scripts/main.5e6f7a8b.js"],
//     "base": ["/static/css/base.9c0d1e2f.css"], ... }
class Manifest {
  apply (compiler) {
    const { Compilation, sources } = compiler.webpack
    compiler.hooks.thisCompilation.tap('Manifest', compilation => {
      compilation.hooks.processAssets.tap(
        { name: 'Manifest', stage: Compilation.PROCESS_ASSETS_STAGE_REPORT },
        () => {
          const publicPath = compilation.outputOptions.publicPath
          const manifest = {}
          for (const [name, entrypoint] of compilation.entrypoints) {
            manifest[name] = entrypoint.getFiles()
              .filter(file => /\.(js|css)$/.test(file) && compilation.getAsset(file))
              .map(file => publicPath + file)
          }
          compilation.emitAsset('manifest.json', new sources.RawSource(JSON.stringify(manifest, null, 2)))
        }
      )
    })
  }
}

module.exports = (env, { mode } = {}) => ({
  // Development's default source maps are eval()ed, which the Content-Security-Policy refuses
  // (dri-one #464); these are files beside the scripts instead
  devtool: mode === 'development' ? 'cheap-module-source-map' : false,
  entry: {
    main: './src/index.js',
    // The SPARQL page (/sparql), which isn't built on the skeleton and loads only this
    sparql: './src/sparql/index.js',
    ...stylesheets
  },
  output: {
    // Laid out as it is served, under /static: scripts/, css/ and assets/ (the images and fonts the
    // stylesheets use), plus manifest.json
    path: path.join(__dirname, 'dist'),
    publicPath: '/static/',
    filename: 'scripts/[name].[contenthash:8].js',
    chunkFilename: 'scripts/[name].[contenthash:8].js',
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
            options: {
              // Relative url()s, the images and fonts of Font Awesome and the libraries, are built
              // as hashed assets. /static/img/... and other absolute ones are served as they are.
              url: { filter: url => !/^(\/|#|data:|[a-z]+:\/\/)/.test(url) }
            }
          },
          {
            loader: 'sass-loader',
            options: { sassOptions: { ...sassOptions, style: 'expanded' } }
          }
        ]
      },
      {
        // Library stylesheets imported from JavaScript (YASGUI's)
        test: /\.css$/,
        use: [MiniCssExtractPlugin.loader, 'css-loader']
      },
      {
        test: /\.(png|svg|gif|jpe?g|woff2?|ttf|eot)$/,
        type: 'asset/resource',
        generator: { filename: 'assets/[name].[contenthash:8][ext]' }
      }
    ]
  },
  optimization: {
    // '...' keeps webpack's own JavaScript minifier; cssnano minifies the stylesheets, library CSS
    // included, in production builds (NERC-CEH/dri-one#362)
    // (cssnano's SVG step is off: it can't parse the URL-encoded SVGs Bootstrap puts in data: URIs,
    // and warns on each)
    minimizer: ['...', new CssMinimizerPlugin({ minimizerOptions: { preset: ['default', { svgo: false }] } })],
    splitChunks: {
      cacheGroups: {
        // The libraries every page loads (jQuery, Backbone, Bootstrap...) in a chunk of their own,
        // which a change to the catalogue's own code leaves as it is, and so cached. Libraries only
        // a micro-app uses (Leaflet, select2, DataTables...) go with that micro-app's chunk.
        vendor: {
          test: /[\\/]node_modules[\\/]/,
          name: 'vendor',
          chunks: chunk => chunk.name === 'main'
        }
      }
    }
  },
  performance: {
    // The budget webpack warns past. Three files exceed its default 244 KiB, each for a reason:
    // base.css is shared by every page and cached for a year; the editor's chunk loads only in the
    // editor; and the .ttf fonts are a fallback that browsers with woff2 never fetch. The SPARQL page's
    // YASGUI (1 MB, as unpkg served it) is left out: only that page loads it.
    assetFilter: file => !/\.(map|ttf)$/.test(file) && !/(^|\/)sparql\./.test(file),
    maxAssetSize: 450 * 1024,
    maxEntrypointSize: 450 * 1024
  },
  plugins: [
    new MiniCssExtractPlugin({ filename: 'css/[name].[contenthash:8].css' }),
    new StylesheetEntries(),
    new FontAwesomeSubset(),
    new Manifest()
  ]
})
