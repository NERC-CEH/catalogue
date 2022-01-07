// TODO: This file was created by bulk-decaffeinate.
// Sanity-check the conversion and remove this comment.
/*
 * decaffeinate suggestions:
 * DS102: Remove unnecessary code created because of implicit returns
 * DS207: Consider shorter variations of null checks
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/master/docs/suggestions.md
 */
define([
  'underscore',
  'backbone',
  'cs!views/editor/ObjectInputView',
  'cs!views/editor/ChildView',
  'cs!views/editor/KeywordVocabularyView',
  'tpl!templates/editor/DescriptiveKeyword.tpl'
], function(_, Backbone, ObjectInputView, ChildView, KeywordVocabularyView, template) { return ObjectInputView.extend({

  template,

  events() {
    return _.extend({}, ObjectInputView.prototype.events, {
      'click .add'() { return (this.add)(); },

      'click .predefined'(event) { return this.addPredefined(event); }
    }
    );
  },

  initialize(options) {
    ObjectInputView.prototype.initialize.call(this, options);
    (this.render)();
    const keywordType = this.model.get('type');
    if (keywordType != null) {
      // IE only supports .startsWith() in MS Edge (> version 11)
      if (keywordType.lastIndexOf('Catalogue topic', 0) === 0) {
        this.$('#catalogueTopic').removeClass('hidden');
        this.$('.add').addClass('hidden');
        this.$('select.type').attr('disabled', 'disabled');
      }
    }

    this.keywords = this.model.getRelatedCollection('keywords');
    this.createList(this.keywords, '.keywords', this.addOne);

    return this.$('input.date').datepicker({dateFormat: "yy-mm-dd"});
  },

  render() {
    ObjectInputView.prototype.render.apply(this);
    this.$('.type').val(this.model.get('type'));
    return this;
  },

  addOne(model, keywordIndex) {
    this.data = _.omit(this.data, 'el');
    return new ChildView(_.extend({}, this.data, {
      model,
      keywordIndex,
      ObjectInputView: KeywordVocabularyView
    }
    )
    );
  },

  add() { return this.keywords.add({}); },

  addPredefined(event) {
    event.preventDefault();
    const $target = this.$(event.target);
    return this.keywords.add({
      value: $target.text(),
      uri: $target.attr('href')
    });
  },

  modify(event) {
    const $target = $(event.target);
    const name = $target.data('name');
    const value = $target.val();

    if (value) {
      return this.model.set(name, value);
    } else {
      return this.model.unset(name);
    }
  }
});
 });
