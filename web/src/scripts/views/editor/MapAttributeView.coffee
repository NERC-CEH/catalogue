define [
  'cs!views/editor/ObjectInputView'
  'cs!views/editor/ChildView'
  'cs!views/editor/MapValueView'
  'cs!views/editor/MapBucketView'
  'tpl!templates/editor/MapAttribute.tpl'
], (ObjectInputView, ChildView, MapValueView, MapBucketView, template) -> ObjectInputView.extend

  template: template
  
  defaultLegend:
    style:
      colour: '#000000'

  dataTypes:[
    {name: 'Text',   value: 'TEXT'}
    {name: 'Number', value: 'NUMBER'}
  ]

  events: ->
    _.extend {}, ObjectInputView.prototype.events,
      'click .addValue': 'addValue'
      'click .addBucket': 'addBucket'

  initialize: (options) ->
    ObjectInputView.prototype.initialize.call @, _.extend {}, options,
      types: @dataTypes

    @buckets = @model.getRelatedCollection 'buckets'
    @values  = @model.getRelatedCollection 'values'

    @createList @buckets, '.buckets', @newBucket
    @createList @values, '.values', @newValue

  addValue:  -> @values.add @defaultLegend
  addBucket: -> @buckets.add @defaultLegend

  newValue: (m) -> new ChildView model: m, ObjectInputView: MapValueView
  newBucket: (m) -> new ChildView model: m, ObjectInputView: MapBucketView