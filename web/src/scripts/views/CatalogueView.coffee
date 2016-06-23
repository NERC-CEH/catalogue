define [
  'cs!views/EditorView'
  'cs!views/editor/ParentStringDropdownView'
  ], (EditorView, ParentStringDropdownView) -> EditorView.extend

  initialize: ->

    @sections = [
      label: 'One'
      title:  'Catalogues'
      views: [
        new ParentStringDropdownView
          model: @model
          modelAttribute: 'catalogues'
          label: 'Catalogues'
          helpText: """
                    <p>Catalogues</p>
                    """
      ]
    ]
    EditorView.prototype.initialize.apply @
