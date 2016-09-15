define [
  'cs!views/EditorView'
  'cs!views/editor/LinkDocumentSelectorView'
  ], (EditorView, LinkDocumentSelectorView) -> EditorView.extend

  initialize: ->
    @sections = [
      label: 'One'
      title:  'General information'
      views: [
        new LinkDocumentSelectorView
          model: @model
          modelAttribute: 'linkedDocumentId'
          label: 'Identifier of linked Document'
        ]
    ]

    EditorView.prototype.initialize.apply @