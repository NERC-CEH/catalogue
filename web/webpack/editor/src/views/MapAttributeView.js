/*
 * decaffeinate suggestions:
 * DS102: Remove unnecessary code created because of implicit returns
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/master/docs/suggestions.md
 */
define([
  'underscore',
  'cs!views/editor/ObjectInputView',
  'cs!views/editor/ChildView',
  'cs!views/editor/MapValueView',
  'cs!views/editor/MapBucketView',
  'tpl!templates/editor/MapAttribute.tpl'
], function(_, ObjectInputView, ChildView, MapValueView, MapBucketView, template) { return ObjectInputView.extend({

  template,
  
  defaultLegend: {
    style: {
      colour: '#000000'
    }
  },

  dataTypes:[
    {name: 'Text',   value: 'TEXT'},
    {name: 'Number', value: 'NUMBER'}
  ],

  events() {
    return _.extend({}, ObjectInputView.prototype.events, {
      'click .addValue': 'addValue',
      'click .addBucket': 'addBucket'
    }
    );
  },

  initialize(options) {
    ObjectInputView.prototype.initialize.call(this, _.extend({}, options,
      {types: this.dataTypes})
    );

    this.buckets = this.model.getRelatedCollection('buckets');
    this.values  = this.model.getRelatedCollection('values');

    this.createList(this.buckets, '.buckets', this.newBucket);
    return this.createList(this.values, '.values', this.newValue);
  },

  addValue() { return this.values.add(this.defaultLegend); },
  addBucket() { return this.buckets.add(this.defaultLegend); },

  newValue(m) { 
    return new ChildView({ 
      model: m,
      ObjectInputView: MapValueView,
      disabled: this.data.disabled
    });
  },

  newBucket(m) { 
    return new ChildView({ 
      model: m,
      ObjectInputView: MapBucketView,
      disabled: this.data.disabled
    });
  }
});
 });