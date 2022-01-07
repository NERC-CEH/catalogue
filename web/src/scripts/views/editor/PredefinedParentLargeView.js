// TODO: This file was created by bulk-decaffeinate.
// Sanity-check the conversion and remove this comment.
/*
 * decaffeinate suggestions:
 * DS102: Remove unnecessary code created because of implicit returns
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/master/docs/suggestions.md
 */
define([
  'underscore',
  'cs!views/editor/ParentLargeView',
  'tpl!templates/editor/PredefinedParent.tpl',
  'tpl!templates/editor/PredefinedParentDropdown.tpl'
], function(_, ParentLargeView, template, dropdownTemplate) { return ParentLargeView.extend({

  events: {
    'click .dropdown-menu': 'setPredefined'
  },

  render() {
    ParentLargeView.prototype.render.apply(this);
    this.$('button.add').replaceWith(template({data: this.data}));
    this.$('button').prop(this.data.disabled, this.data.disabled);
    const $dropdown = this.$('ul.dropdown-menu');
    _.chain(this.data.predefined)
    .keys()
    .each(item => $dropdown.append(dropdownTemplate({predefined: item})));
    return this;
  },

  setPredefined(event) {
    (event.preventDefault)();
    const value = $(event.target).text();
    let selected = {};

    if (value !== 'Custom') {
      selected = this.data.predefined[value];
    }

    return this.collection.add(selected);
  }
});
 });
