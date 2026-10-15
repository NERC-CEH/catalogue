/*
 * Run before each spec file, for the differences between the specs' build (Vite) and the page's
 * (webpack), which wires up AMD modules where Vite's dependency bundling takes the CommonJS route:
 * - The page's jQuery is global (src/index.js), and jQuery UI falls back on window.jQuery.
 * - select2's CommonJS form exports a function to call with jQuery, where its AMD form adds the
 *   plugin to jQuery as it loads. So it is called here, on the jQuery the code under test imports.
 */
import $ from 'jquery'
import select2 from 'select2'

window.jQuery = window.$ = $ // eslint-disable-line no-restricted-properties
select2(window, $)
