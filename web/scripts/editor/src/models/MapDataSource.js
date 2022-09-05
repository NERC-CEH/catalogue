import _ from 'underscore'
import { NestedModel } from './NestedModel'
import MapAttribute from './MapAttribute'

export default NestedModel.extend({

  defaults: {
    type: 'POLYGON',
    epsgCode: 4326,
    bytetype: 'false',
    features: {
      style: {
        colour: '#000000'
      }
    }
  },

  initialize () {
    NestedModel.prototype.initialize.apply(this, arguments)

    // Determine the current styling mode
    this.stylingMode = _.isEmpty(this.attributes.attributes) ? 'features' : 'attributes'
  },

  /*
  Return a releated collection of the attributes element of this model
  */
  getAttributes () { return this.getRelatedCollection('attributes', MapAttribute) },

  /*
  Update the styling mode and trigger a change event as either the features
  or attributes will now be hidden.
  */
  setStylingMode (mode) {
    this.stylingMode = mode
    this.trigger('change', this, {})
  },

  validate (attrs) {
    const errors = []

    // Validate all of the min and max values of any defined buckets
    // eslint-disable-next-line prefer-regex-literals
    const numRegex = new RegExp('^-?(?:\\d+(?:\\.\\d+)?|\\.\\d+)$')

    if (!_.isEmpty(attrs.attributes)) {
      if (_.chain(attrs.attributes)
        .pluck('buckets')
        .flatten()
        .select(n => n != null)
        .map(b => [b.min, b.max])
        .flatten()
        .select(n => n != null)
        .any(n => !numRegex.test(n))
        .value()) {
        errors.push({ message: 'Bucket values must be numbers' })
      }

      if (_.chain(attrs.attributes)
        .pluck('name')
        .uniq()
        .value()
        .length !== attrs.attributes.length) {
        errors.push({ message: 'Layer names must be unique' })
      }
    }

    if (_.isEmpty(errors)) { return undefined } else { return errors }
  },

  /*
  Depending on the stylingMode, either hide the `features` or `attributes`
  attributes.
  */
  toJSON () {
    const json = NestedModel.prototype.toJSON.apply(this, arguments)
    switch (this.stylingMode) {
      case 'features': return _.omit(json, 'attributes')
      case 'attributes': return _.omit(json, 'features')
    }
  }
})
