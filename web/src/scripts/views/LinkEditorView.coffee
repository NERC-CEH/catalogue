define [
  'cs!views/EditorView'
  'cs!views/editor/LinkDocumentSelectorView'
  'cs!views/editor/ParentView'
  'cs!views/editor/KeywordView'
  ], (EditorView, LinkDocumentSelectorView, ParentView, KeywordView) -> EditorView.extend

  initialize: ->
    @sections = [
      label: 'One'
      title:  'General information'
      views: [
        new ParentView
          model: @model
          modelAttribute: 'additionalKeywords'
          label: 'Additional Keywords'
          ObjectInputView: KeywordView
          helpText: """
                    <p>Keywords (preferably taken from a controlled vocabulary) categorising and describing the data resource.</p>
                    """

        new LinkDocumentSelectorView
          model: @model
          modelAttribute: 'linkedDocumentId'
          label: 'Identifier of linked Document'
          helpText: """
                    <p>Metadata record linked to by this document.</p>
                    """
        ]
    ]

    EditorView.prototype.initialize.apply @