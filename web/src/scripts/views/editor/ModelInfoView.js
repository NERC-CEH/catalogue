// TODO: This file was created by bulk-decaffeinate.
// Sanity-check the conversion and remove this comment.
/*
 * decaffeinate suggestions:
 * DS102: Remove unnecessary code created because of implicit returns
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/master/docs/suggestions.md
 */
define([
  'underscore',
  'jquery',
  'cs!views/editor/ObjectInputView',
  'tpl!templates/editor/ModelInfo.tpl',
  'jquery-ui/autocomplete'
], function(_, $, ObjectInputView, template) { return ObjectInputView.extend({

  template,

  initialize() {
    ObjectInputView.prototype.initialize.apply(this);
    const catalogue = $('html').data('catalogue');
    
    this.$('.autocomplete').autocomplete({
      minLength: 0,
      source(request, response) {
        let query;
        const term = request.term.trim();
        if (_.isEmpty(term)) {
          query = `/${catalogue}/documents?term=documentType:CEH_MODEL`;
        } else {
          query = `/${catalogue}/documents?term=documentType:CEH_MODEL AND ${request.term}`;
        }
        
        return $.getJSON(query, data => response(_.map(data.results, d => ({
          value: d.title,
          label: d.title,
          identifier: d.identifier
        }))));
      }});

    return this.$('.autocomplete').on('autocompleteselect', (event, ui) => {
        this.model.set('id', ui.item.identifier);
        return this.$('.identifier').val(ui.item.identifier);
    });
  },

  render() {
    ObjectInputView.prototype.render.apply(this);
    this.$('select.spatial-data').val(this.model.get('availableSpatialData'));
    return this;
  }
});
 });