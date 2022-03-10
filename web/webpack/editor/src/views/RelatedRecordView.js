/*
 * decaffeinate suggestions:
 * DS102: Remove unnecessary code created because of implicit returns
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/master/docs/suggestions.md
 */
define([
  'underscore',
  'jquery',
  'cs!views/editor/ObjectInputView',
  'tpl!templates/editor/RelatedRecord.tpl',
  'jquery-ui/autocomplete'
], function(_, $, ObjectInputView, template) { return ObjectInputView.extend({  

  template,

  initialize() {
    ObjectInputView.prototype.initialize.apply(this);
    const catalogue = $('html').data('catalogue');
    
    this.$('.autocomplete').autocomplete({
      minLength: 2,
      source(request, response) {
        let query;
        const term = request.term.trim();
        if (_.isEmpty(term)) {
          query = `/${catalogue}/documents`;
        } else {
          query = `/${catalogue}/documents?term=${request.term}`;
        }
        
        return $.getJSON(query, data => response(_.map(data.results, d => ({
          value: d.title,
          label: d.title,
          identifier: d.identifier,
          type: d.resourceType
        }))));
      }});

    return this.$('.autocomplete').on('autocompleteselect', (event, ui) => {
        this.model.set('identifier', ui.item.identifier);
        this.$('.identifier').val(ui.item.identifier);
        this.model.set('href', 'https://catalogue.ceh.ac.uk/id/' + ui.item.identifier);
        this.$('.href').val('https://catalogue.ceh.ac.uk/id/' + ui.item.identifier);
        this.model.set('associationType',  ui.item.type);
        this.$('.associationType').val(ui.item.type);
        this.model.set('title', ui.item.label);
        return this.$('.title').val(ui.item.label);
    });
  },


  render() { 
    ObjectInputView.prototype.render.apply(this);
    this.$('select.rel').val(this.model.get('rel'));
    return this;
  }
});
 });
