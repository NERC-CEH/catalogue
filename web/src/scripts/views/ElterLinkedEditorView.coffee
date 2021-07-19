define [
  'cs!views/EditorView'
  'cs!views/editor/InputView'
  'cs!views/editor/ReadOnlyView'
], (EditorView, InputView,  ReadOnlyView) -> EditorView.extend

  initialize: ->

    @model.set('title','This title will be replaced on a successful document retrieval')

    @sections = [
      label: 'General'
      title:  ''
      views: [
        new ReadOnlyView
          model: @model
          modelAttribute: 'title'
          label: 'Title'

        new InputView
          model: @model
          modelAttribute: 'linkedDocumentUri'
          label: 'Linked document URL'
          helpText: """
                    <p>For creating linked documents, add the URL here.</p>
                    <p>This should be a link to a metadata document in JSON format.</p>
                    """

        new InputView
          model: @model
          modelAttribute: 'linkedDocumentType'
          label: 'Linked document type'
          helpText: """
                    <p>Enter the type of the linked document, if applicable.</p>
                    """
      ]
    ]

    EditorView.prototype.initialize.apply @
