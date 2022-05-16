/* eslint-disable
    no-undef,
*/
// TODO: This file was created by bulk-decaffeinate.
// Fix any style issues and re-enable lint.
/*
 * decaffeinate suggestions:
 * DS102: Remove unnecessary code created because of implicit returns
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/master/docs/suggestions.md
 */
define([
  'jquery',
  'backbone',
  'tpl!templates/FacetsPanel.tpl',
  'tpl!templates/FacetResults.tpl'
], function ($, Backbone, panelTpl, resultsTpl) {
  return Backbone.View.extend({

    initialize () {
      return this.listenTo(this.model, 'results-sync', this.render)
    },

    /*
  Render the facet results panel as long as we have some results currently set.

  The template panelTpl requires a sub template which renders the each facet
  results set.
  */
    render () {
      return this.$el.html(panelTpl({
        facets: this.model.getResults().attributes.facets,
        template: resultsTpl
      })
      )
    }
  })
})
