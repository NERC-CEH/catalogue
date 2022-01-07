/*
 * decaffeinate suggestions:
 * DS102: Remove unnecessary code created because of implicit returns
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/master/docs/suggestions.md
 */
define([
  'backbone',
  'underscore'
], function(Backbone, _) { return Backbone.Model.extend({

  defaults: {
    address: {}
  },

  validate(attrs) {

    const rorRegEx = new RegExp(`\
^https?:\\/\\/ror.org\\/\\w{8,10}$\
`);

    const errors = [];

    const {
      organisationName
    } = attrs;
    const {
      organisationIdentifier
    } = attrs;

    const isValidROR = id => rorRegEx.test(id);

    if (organisationIdentifier && !isValidROR(organisationIdentifier)) {
      errors.push({
        message:
          "That RoR is invalid "
      });
    }

    if (!organisationName) {
      errors.push({
        message: "Affiliation (organisation name) is  mandatory."});
    }

    if (_.isEmpty(errors)) {
      // return nothing from Backbone.Model.validate
      // because returning something signals a validation error.
      return;
    } else {
      return errors;
    }

    if (_.isEmpty(warnings)) {
      return;
    } else {
      return warnings;
    }
  },

  toJSON() {
    if (_.isEmpty(this.get('address'))) {
      return _.omit(this.attributes, 'address');
    } else {
      return _.clone(this.attributes);
    }
  }
});
 });