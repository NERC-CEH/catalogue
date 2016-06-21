define [
  'cs!views/EditorView'
  'cs!views/editor/ParentStringView'
  'tpl!templates/Catalogues.tpl'
  ], (EditorView, ParentStringView, childTemplate) -> EditorView.extend

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
          childTemplate: childTemplate
      ]
    ]
    EditorView.prototype.initialize.apply @

  # render: ->
  #   ObjectInputView.prototype.render.apply @
  #   @$('select').val @model.get 'value'
  #   @
