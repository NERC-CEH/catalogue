import $ from 'jquery'
import 'bootstrap'
import { startApps } from './startup/apps'

// The page's jQuery globals are the catalogue's own jQuery. What reads them:
// - Bootstrap, which finding window.jQuery raises each of its events as a jQuery event too. Only that
//   copy reaches a Backbone events hash: the native event's dotted name (shown.bs.tab) is read by
//   jQuery as an event with namespaces. Listened for in SearchAppView ('shown.bs.tab .map-tab a').
// - The Jira issue collector on EIDC pages, which brings its own jQuery (3.x, with jQuery Migrate):
//   finding the globals taken it keeps that to itself, its noConflict(true) putting ours back. So they
//   are set here, before the collector's scripts, which are deferred after this one.
// The catalogue's own code imports jQuery rather than use the global (eslint.config.js).
window.jQuery = window.$ = $ // eslint-disable-line no-restricted-properties

// Every page loads this file, so it holds only what every page needs; each micro-app is fetched only
// on a page that has it (startup/apps.js, startup/load.js, NERC-CEH/dri-one#364)
startApps(document)
