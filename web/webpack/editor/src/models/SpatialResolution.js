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
      equivalentScale
    } = attrs
    const {
      distance
    } = attrs

    if (equivalentScale && distance) {
      errors.push({ message: 'You can <b>EITHER</b> enter an Equivalent scale <b>OR</b> a Distance but not both.' })
    }

    if (_.isEmpty(errors)) {
      // return nothing from Backbone.Model.validate
      // because returning something signals a validation error.

    } else {
      return errors
    }
  }
}))
