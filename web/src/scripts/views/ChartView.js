/*
 * decaffeinate suggestions:
 * DS102: Remove unnecessary code created because of implicit returns
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/master/docs/suggestions.md
 */
define([
  'underscore',
  'jquery',
  'backbone',
  'chartjs'
], function(_, $, Backbone, Chart) { return Backbone.View.extend({
  initialize() {
    const data = this.$('li').map((i, el) => { 
      return {
        value: $('span.badge', el).html(),
        color: $('span.badge', el).css('background-color'),
        label: $('span.legend-label', el).html()
      };
    });

    const ctx = this.$('canvas')[0].getContext('2d');

    return this.chart = new Chart(ctx).Pie(data, {
      segmentStrokeWidth: 0,
      segmentStrokeColor: "#000000",
      responsive:         true
    }
    );
  }
});
 });
