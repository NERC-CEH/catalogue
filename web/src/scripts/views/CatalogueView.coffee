define [
  'cs!views/EditorView'
  'cs!views/editor/SelectView'
  ], (EditorView, SelectView) -> EditorView.extend

  initialize: ->

    @sections = [
      label: 'One'
      title:  'Catalogue'
      views: [
        new SelectView
          model: @model
          modelAttribute: 'value'
          label: 'Catalogue'
          helpText: """
                    <p>Catalogue</p>
                    """
      ]
    ]
    EditorView.prototype.initialize.apply @
