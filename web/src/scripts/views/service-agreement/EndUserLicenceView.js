/*
 * decaffeinate suggestions:
 * DS102: Remove unnecessary code created because of implicit returns
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/master/docs/suggestions.md
 */
define([
  'cs!views/editor/ObjectInputView',
  'tpl!templates/service-agreement/EndUserLicence.tpl'
], function(ObjectInputView, template) { return ObjectInputView.extend({

  template,

  events: {
    'change .ogl': 'setOgl',
    'change .other': 'setOther',
    'change .value': 'setValue',
    'change .uri': 'setUri'
  },

  initialize(options) {
    ObjectInputView.prototype.initialize.call(this, options);
    this.$resourceConstraint = this.$('.resourceConstraint');
    const hasUri = this.model.has('uri');
    const hasValue = this.model.has('value');

    if (hasUri || hasValue) {
      if (hasUri && (this.model.get('uri') === 'https://eidc.ceh.ac.uk/licences/OGL/plain')) {
        return this.$('input.ogl').prop('checked', true);
      } else {
        this.$('input.other').prop('checked', true);
        this.$resourceConstraint.removeClass('hidden');
        if (hasValue) {
          return this.$('.value').val(this.model.get('value'));
        }
      }
    } else {
      return this.$('input.ogl').prop('checked', true).change();
    }
  },

  setOgl() {
    this.$resourceConstraint.addClass('hidden');
    return this.model.set({
      value: 'This resource is available under the terms of the Open Government Licence',
      code: 'license',
      uri: 'https://eidc.ceh.ac.uk/licences/OGL/plain'
    });
  },

  setOther() {
    this.$resourceConstraint.removeClass('hidden');
    this.model.unset('uri');
    return this.model.unset('value');
  },

  setValue(event) {
    return this.model.set({
      code: 'license',
      value: event.target.value
    });
  }
});
 });
