define [
  'cs!views/EditorView'
  'cs!views/editor/InputView'
], (EditorView, InputView) -> EditorView.extend

  initialize: ->
    @model.set('type', 'sampleArchive') unless @model.has('type')
    @sections = [
      label: 'One'
      title:  'General information'
      views: [
        new InputView
          model: @model
          modelAttribute: 'title'
          label: 'Title'
          helpText: """
                    <p>Title</p>
                    """
        ]
    ]
    
    EditorView.prototype.initialize.apply @