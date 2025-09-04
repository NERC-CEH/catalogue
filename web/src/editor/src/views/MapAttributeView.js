import _ from 'underscore'
import ObjectInputView from './ObjectInputView'
import ChildView from './ChildView'
import MapValueView from './MapValueView'
import MapBucketView from './MapBucketView'
import template from '../templates/MapAttribute'
import { MapDataSource } from '../models'

export default ObjectInputView.extend({

  events: function () {
    return _.extend({}, ObjectInputView.prototype.events, {
      'click .addValue': 'addValue',
      'click .addBucket': 'addBucket'
    })
  },

  defaultLegend: {
    style: {
      colour: '#000000'
    }
  },

  dataTypes: [
    { name: 'Text', value: 'TEXT' },
    { name: 'Number', value: 'NUMBER' }
  ],

  initialize (options) {
    this.template = template
    ObjectInputView.prototype.initialize.call(this, _.extend({}, options,
      { types: this.dataTypes })
    )

    this.buckets = this.model.getRelatedCollection('buckets')
    this.values = this.model.getRelatedCollection('values')

    this.buckets.reset(this.buckets.map(m =>
      m instanceof MapDataSource ? m : new MapDataSource(m.attributes)
    ))

    this.values.reset(this.values.map(m =>
      m instanceof MapDataSource ? m : new MapDataSource(m.attributes)
    ))

    this.createList(this.buckets, '.buckets', this.newBucket, false, {
      onCollectionChange: () => this.updateBucketsInModel()
    })

    this.createList(this.values, '.values', this.newValue, false, {
      onCollectionChange: () => this.updateValuesInModel()
    })
  },

  addValue () {
    this.values.add(new MapDataSource(this.defaultLegend))
  },

  addBucket () {
    this.buckets.add(new MapDataSource(this.defaultLegend))
  },

  newValue (m) {
    // eslint-disable-next-line no-unused-vars
    return new ChildView({
      model: m,
      ObjectInputView: MapValueView,
      disabled: this.data.disabled
    })
  },

  newBucket (m) {
    // eslint-disable-next-line no-unused-vars
    return new ChildView({
      model: m,
      ObjectInputView: MapBucketView,
      disabled: this.data.disabled
    })
  },

  updateBucketsInModel () {
    this.model.set('buckets', this.buckets.toJSON())
  },

  updateValuesInModel () {
    this.model.set('values', this.values.toJSON())
  }
})
