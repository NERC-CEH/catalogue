const gulp = require('gulp')
const dartSass = require('sass')
const gulpSass = require('gulp-sass')(dartSass)
const cleanCSS = require('gulp-clean-css')
const sourcemaps = require('gulp-sourcemaps')
const postcss = require('gulp-postcss')
const postcssImport = require('postcss-import')

function buildDevStyles () {
  return gulp.src('./scss/style-*.scss')
    .pipe(sourcemaps.init())
    .pipe(gulpSass({
      quietDeps: true,
      includePaths: ['./scss', './node_modules']
    }).on('error', gulpSass.logError))
    .pipe(postcss([postcssImport()]))
    .pipe(sourcemaps.write('.'))
    .pipe(gulp.dest('./css'))
}

function buildProdStyles () {
  return gulp.src('./scss/style-*.scss')
    .pipe(gulpSass({ quietDeps: true }).on('error', gulpSass.logError))
    .pipe(postcss([postcssImport()]))
    .pipe(cleanCSS()) // Minify CSS
    .pipe(gulp.dest('./css'))
}

function watchStyles () {
  gulp.watch('./scss/**/*.scss', buildDevStyles)
}

exports.dev = gulp.series(buildDevStyles)
exports.prod = gulp.series(buildProdStyles)
exports.watch = watchStyles
