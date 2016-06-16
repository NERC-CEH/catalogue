define [
  'cs!views/EditorView'
  'cs!views/editor/ParentStringView'
  ], (EditorView, ParentStringView) -> EditorView.extend

  initialize: ->

    @sections = [
      label: 'One'
      title:  'Catalogues'
      views: [
        new ParentStringView
          model: @model
          modelAttribute: 'catalogues'
          label: 'Catalogues'
          helpText: """
                    <p>Catalogues</p>
                    """
      ]
    ]
    EditorView.prototype.initialize.apply @
