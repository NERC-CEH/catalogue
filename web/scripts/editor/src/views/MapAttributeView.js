import _ from 'underscore'
import ObjectInputView from './ObjectInputView'
import ChildView from './ChildView'
import MapValueView from './MapValueView'
import MapBucketView from './MapBucketView'
import template from '../templates/MapAttribute.tpl'

export default ObjectInputView.extend({

  defaultLegend: {
    style: {
      colour: '#000000'
    }
  },

  dataTypes: [
    { name: 'Text', value: 'TEXT' },
    { name: 'Number', value: 'NUMBER' }
  ],

  events () {
    _.extend({}, ObjectInputView.prototype.events, {
      'click .addValue': 'addValue',
      'click .addBucket': 'addBucket'
    }
    )
  },

  initialize (options) {
    this.template = _.template(template)
    ObjectInputView.prototype.initialize.call(this, _.extend({}, options,
      { types: this.dataTypes })
    )

    this.buckets = this.model.getRelatedCollection('buckets')
    this.values = this.model.getRelatedCollection('values')

    this.createList(this.buckets, '.buckets', this.newBucket)
    this.createList(this.values, '.values', this.newValue)
  },

  addValue () { this.values.add(this.defaultLegend) },
  addBucket () { this.buckets.add(this.defaultLegend) },

  newValue (m) {
    return new ChildView({
      model: m,
      ObjectInputView: MapValueView,
      disabled: this.data.disabled
    })
  },

  newBucket (m) {
    return new ChildView({
      model: m,
      ObjectInputView: MapBucketView,
      disabled: this.data.disabled
    })
  }
})
