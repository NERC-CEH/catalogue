define [
  'cs!views/EditorView'
  'cs!views/editor/SingleObjectView'
  'cs!views/editor/InputView'
  'cs!views/editor/TextareaView'
  'cs!views/editor/DataTypeSchemaView'
  'cs!views/editor/DataTypeProvenanceView'
], (
  EditorView,
  SingleObjectView,
  InputView,
  TextareaView,
  DataTypeSchemaView
  DataTypeProvenanceView
) -> EditorView.extend

  initialize: ->

    @sections = [
      label: 'Basic Info'
      title: 'Basic Info'
      views: [

        new InputView
          model: @model
          modelAttribute: 'title'
          label: 'Title'

        new TextareaView
          model: @model
          modelAttribute: 'description'
          rows: 13
          label: 'Description'

        new SingleObjectView
          model: @model
          modelAttribute: 'schema'
          label: 'Schema'
          ObjectInputView: DataTypeSchemaView,

        new SingleObjectView
          model: @model
          modelAttribute: 'provenance'
          label: 'Provenance'
          ObjectInputView: DataTypeProvenanceView,
      ]
    ]

    EditorView.prototype.initialize.apply @
