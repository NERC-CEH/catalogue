/* eslint-disable
    no-undef,
*/
// TODO: This file was created by bulk-decaffeinate.
// Fix any style issues and re-enable lint.
/*
 * decaffeinate suggestions:
 * DS102: Remove unnecessary code created because of implicit returns
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/master/docs/suggestions.md
 */
define([
  'underscore',
  'backbone'
], (_, Backbone) => Backbone.Model.extend({

  validate (attrs) {
    const errors = []

    const {
      name
    } = attrs
    const {
      type
    } = attrs
    const {
      version
    } = attrs

    if (!version && (name || type)) {
      errors.push({ message: "The version is mandatory - if it's not applicable, enter 'unknown'" })
    }

    if (_.isEmpty(errors)) {
      // return nothing from Backbone.Model.validate
      // because returning something signals a validation error.

    } else {
      return errors
    }
  }
}))
