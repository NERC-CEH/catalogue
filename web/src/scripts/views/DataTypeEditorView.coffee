define [
  'cs!views/EditorView'
  'cs!views/editor/SingleObjectView'
  'cs!views/editor/InputView'
  'cs!views/editor/TextareaView'
  'cs!views/editor/ParentView'
  'cs!views/editor/PredefinedParentView'
  'cs!views/editor/DataTypeSchemaView'
  'cs!views/editor/DataTypeProvenanceView'
], (
  EditorView,
  SingleObjectView,
  InputView,
  TextareaView,
  ParentView,
  PredefinedParentView,
  DataTypeSchemaView,
  DataTypeProvenanceView
) -> EditorView.extend

  initialize: ->

    @sections = [
      label: 'General'
      title:  ''
      views: [
        new InputView
          model: @model
          modelAttribute: 'title'
          label: 'Title'

        new TextareaView
          model: @model
          modelAttribute: 'description'
          rows: 5
          label: 'Description'

        new SingleObjectView
          model: @model
          modelAttribute: 'provenance'
          label: 'Provenance'
          ObjectInputView: DataTypeProvenanceView,
        
        new PredefinedParentView
          model: @model
          modelAttribute: 'schema'
          multiline: true
          label: 'Schema'
          ObjectInputView: DataTypeSchemaView
          predefined:
            'Boolean (true/false)':
              type: 'boolean'
            'Date':
              type: 'date'
              format: 'YYYY-MM-DD'
            'Date & time':
              type: 'datetime'
              format: 'YYYY-MM-DDThh:mm:ss'
            'Decimal number':
              type: 'number'
            'Email':
              type: 'string'
              format: 'email'
            'Geographic point':
              type: 'geopoint'
              format: 'lon, lat'
            'Integer':
              type: 'integer'
            'Text':
              type: 'string'
            'Time':
              type: 'time'
              format: 'hh:mm:ss'
            'URI':
              type: 'string'
              format: 'uri'
            'UUID':
              type: 'string'
              format: 'uuid'
            'Year':
              type: 'year'
              format: 'YYYY'
            'Year & month':
              type: 'yearmonth'
              format: 'YYYY-MM'
      ]
    ]

    EditorView.prototype.initialize.apply @










 