/* eslint-disable
    no-undef,
*/
// TODO: This file was created by bulk-decaffeinate.
// Fix any style issues and re-enable lint.
/*
 * decaffeinate suggestions:
 * DS101: Remove unnecessary use of Array.from
 * DS102: Remove unnecessary code created because of implicit returns
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/master/docs/suggestions.md
 */
define([
  'cs!views/editor/ObjectInputViewForObjects',
  'tpl!templates/editor/ObservationCapability.tpl'
], function (ObjectInputViewForObjects, template) {
  return ObjectInputViewForObjects.extend({

    template,

    render () {
      if (!Array.from(this.data).includes('observedPropertyName')) {
        this.data.observedPropertyName = {}
      }

      if (!Array.from(this.data).includes('observedPropertyUnitOfMeasure')) {
        this.data.observedPropertyUnitOfMeasure = {}
      }

      if (!Array.from(this.data).includes('procedureName')) {
        this.data.procedureName = {}
      }

      return ObjectInputViewForObjects.prototype.render.apply(this)
    }
  })
})
