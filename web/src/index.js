import 'bootstrap'
import { startApps } from './startup/apps'

// EIDC pages load the Jira issue collector, which brings its own jQuery (3.x, with jQuery Migrate) and,
// finding no window.jQuery, leaves it there for the page. Claiming the globals first makes it keep
// its jQuery to itself: its noConflict(true) puts these back, and it exposes its own only where the
// global is undefined. So no page has a usable global jQuery, and anything reaching for one fails on
// every catalogue alike rather than quietly using Jira's. This runs before the collector's scripts,
// which are deferred after this one.
window.jQuery = window.$ = null // eslint-disable-line no-restricted-properties

// Every page loads this file, so it holds only what every page needs; each micro-app is fetched only
// on a page that has it (startup/apps.js, startup/load.js, NERC-CEH/dri-one#364)
startApps(document)
