define [
  'cs!views/EditorView'
  'cs!views/editor/InputView'
], (EditorView, InputView) -> EditorView.extend

  initialize: ->
    @sections = [
      label: 'One'
      title:  'General information'
      views: [
        new InputView
          model: @model
          modelAttribute: 'archiveType'
          label: 'Temporal extent'
          helpText: """
                    <p>Sample Archive</p>
                    """
        ]
    ]
    
    EditorView.prototype.initialize.apply @