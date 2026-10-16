/*
 * Run before each spec file. The page's jQuery is global (src/index.js, which the specs don't load),
 * as Bootstrap's jQuery events need, so it is made global here too.
 */
import $ from 'jquery'

window.jQuery = window.$ = $ // eslint-disable-line no-restricted-properties
