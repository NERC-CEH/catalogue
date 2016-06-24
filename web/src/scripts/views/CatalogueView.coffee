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
          options: [
              value: ''
              label: '- Select Catalogue -'
            ,
              value: 'Catchment Management Platform'
              label: 'Catchment Management Platform'
            ,
              value: 'Environmental Information Data Centre'
              label: 'Environmental Information Data Centre'
          ]
      ]
    ]
    EditorView.prototype.initialize.apply @
