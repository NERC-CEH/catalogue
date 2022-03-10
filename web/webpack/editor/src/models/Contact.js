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

    const emailRegEx = new RegExp(`\
^[a-zA-Z0-9.!#$%&'*+\\/=?^_\`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$\
`);
    const urlRegEx = new RegExp(`\
^(?:(?:(?:https?|ftp):)?\\/\\/)(?:\\S+(?::\\S*)?@)?(?:(?!(?:10|127)(?:\\.\\d{1,3}){3})(?!(?:169\\.254|192\\.168)(?:\\.\\d{1,3}){2})(?!172\\.(?:1[6-9]|2\\d|3[0-1])(?:\\.\\d{1,3}){2})(?:[1-9]\\d?|1\\d\\d|2[01]\\d|22[0-3])(?:\\.(?:1?\\d{1,2}|2[0-4]\\d|25[0-5])){2}(?:\\.(?:[1-9]\\d?|1\\d\\d|2[0-4]\\d|25[0-4]))|(?:(?:[a-z\\u00a1-\\uffff0-9]-*)*[a-z\\u00a1-\\uffff0-9]+)(?:\\.(?:[a-z\\u00a1-\\uffff0-9]-*)*[a-z\\u00a1-\\uffff0-9]+)*(?:\\.(?:[a-z\\u00a1-\\uffff]{2,})).?)(?::\\d{2,5})?(?:[/?#]\\S*)?$\
`);
    const orcidRegEx = new RegExp(`\
^https?:\\/\\/orcid.org\\/(\\d{4}-){3}\\d{3}[\\dX]$\
`);
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
    const {
      role
    } = attrs;
    const {
      email
    } = attrs;
    const {
      nameIdentifier
    } = attrs;

    const isValidEmail = address => emailRegEx.test(address);

    const isValidnameIdentifier = id => urlRegEx.test(id);

    const isValidORCID = id => orcidRegEx.test(id);
    
    const isValidROR = id => rorRegEx.test(id);

    if (email && !isValidEmail(email)) {
      errors.push({
        message:
          "That email address is wrong"
      });
    }
    
    if (nameIdentifier && !isValidORCID(nameIdentifier)) {
      errors.push({
        message:
          "If that's supposed to be an ORCiD, it's not quite right!"
      });
    }

    if (nameIdentifier && !isValidnameIdentifier(nameIdentifier)) {
      errors.push({
        message:
          "Are you using the <i>fully-qualified</i> name identifier. For example, ORCiDs should be entered as https://orcid.org/0000-1234-5678-999X <b>not</b> 0000-1234-5678-999X"
      });
    }
  
    if (organisationIdentifier && !isValidROR(organisationIdentifier)) {
      errors.push({
        message:
          "If that's supposed to be an ROR, it's not quite right!"
      });
    }

    if (!organisationName || !role) {
      errors.push({
        message: "The organisation name and role are mandatory."});
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