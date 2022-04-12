// TODO: This file was created by bulk-decaffeinate.
// Sanity-check the conversion and remove this comment.
/*
 * decaffeinate suggestions:
 * DS102: Remove unnecessary code created because of implicit returns
 * DS207: Consider shorter variations of null checks
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/master/docs/suggestions.md
 */
define([
  'cs!models/EditorMetadata'
], function(EditorMetadata) { return EditorMetadata.extend({

  initialize(data, options) {
    EditorMetadata.prototype.initialize.apply(this);
    if ('id' in data) {
      this.id = data.id;
      return this.parameters = '';
    } else {
      this.id = options.id;
      return this.parameters = '?catalogue=eidc';
    }
  },

  urlRoot() { return `/service-agreement/${this.id}${this.parameters}`; },

  validate(attrs) {
    const errors = EditorMetadata.prototype.validate.call(this, attrs) || [];
    if ((attrs != null ? attrs.depositorContactDetails : undefined) == null) {
      errors.push('Depositor contact details are mandatory');
    }

    if (_.isEmpty(errors)) {
    // return nothing from Backbone.Model.validate because returning something signals a validation error.
      return;
    } else {
      return errors;
    }
  }
});
 });
