/*
 * decaffeinate suggestions:
 * DS102: Remove unnecessary code created because of implicit returns
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/master/docs/suggestions.md
 */
define([
  'underscore',
  'cs!views/editor/ObjectInputView',
  'tpl!templates/editor/DataTypeSchema.tpl'
], function(_, ObjectInputView, template) { return ObjectInputView.extend({

  template,


  modify(event) {
    const $target = $(event.target);
    const name = $target.data('name');  //CHECK THIS
    const value = $target.val();

    if (_.contains(['maximum', 'minimum', 'maxLength', 'minLength', 'unique'], name)) {
      let constraints = _.clone(this.model.get('constraints'));
      if (value) {
        constraints[name] = value;
        return this.model.set('constraints', constraints);
      } else {
        constraints = _.omit(constraints, name);
        return this.model.set('constraints', constraints);
      }
    } else {
      if (value) {
        return this.model.set(name, value);
      } else {
        return this.model.unset(name);
      }
    }
  }
});
 });
