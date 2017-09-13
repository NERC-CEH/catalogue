define [
  'cs!views/EditorView'
  'cs!views/editor/SingleObjectView'
  'cs!views/editor/InputView'
  'cs!views/editor/TextareaView'
  'cs!views/editor/ParentView'
  'cs!views/editor/KeywordView'
  'cs!views/editor/RelationshipView'
  'cs!views/editor/LinkView'
  'cs!views/editor/DatasetReferenceDateView'
  'cs!models/editor/MultipleDate'
  'cs!views/editor/TemporalExtentView'
  'cs!views/editor/ResourceIdentifierView'
], (
  EditorView,
  SingleObjectView,
  InputView,
  TextareaView,
  ParentView,
  KeywordView,
  RelationshipView,
  LinkView,
  DatasetReferenceDateView,
  MultipleDate,
  TemporalExtentView,
  ResourceIdentifierView
) -> EditorView.extend

  initialize: ->

    @model.set('type', 'publication') unless @model.has('type')

    @sections = [
      label: 'Basic Info'
      title: 'Basic Info'
      views: [

        new InputView
          model: @model
          modelAttribute: 'title'
          label: 'Title'
          helpText: """
                    <p>Name of publication</p>
                    """

        new TextareaView
          model: @model
          modelAttribute: 'description'
          rows: 13
          label: 'Description'
          helpText: """
                    <p>Description of publication</p>
                    """

        new SingleObjectView
          model: @model
          modelAttribute: 'referenceDate'
          ModelType: MultipleDate
          label: 'Reference Date'
          ObjectInputView: DatasetReferenceDateView,
          helpText: """
                    <p>Publication, creation & revision dates</p>
                    """

        new SingleObjectView
          model: @model
          modelAttribute: 'temporalExtent'
          ModelType: MultipleDate
          label: 'Temporal Extent'
          ObjectInputView: TemporalExtentView,
          helpText: """
                    <p>Temporal Extent of publication</p>
                    """

        new SingleObjectView
          model: @model
          modelAttribute: 'access'
          label: 'Access'
          ObjectInputView: LinkView
          helpText: """
                    <p>Access to publication</p>
                    """

        new ParentView
          model: @model
          modelAttribute: 'keywords'
          label: 'Keywords'
          ObjectInputView: KeywordView
          helpText: """
                    <p>Keywords for discovery</p>
                    """

        new ParentView
          model: @model
          modelAttribute: 'relationships'
          label: 'Relationships'
          ObjectInputView: RelationshipView
          multiline: true
          options: [
            {value: 'http://onto.nerc.ac.uk/CEHMD/rels/cites', label: 'Cites'},
            {value: 'http://onto.nerc.ac.uk/CEHMD/rels/related', label: 'Related'},
            {value: 'http://onto.nerc.ac.uk/CEHMD/rels/supercedes', label: 'Supercedes'},
            {value: 'http://onto.nerc.ac.uk/CEHMD/rels/produces', label: 'Produces'},
            {value: 'http://onto.nerc.ac.uk/CEHMD/rels/uses', label: 'Uses'}
          ]
          helpText: """
                    <p>Relationships to other OSDP document types</p>
                    """

        new ParentView
          model: @model
          modelAttribute: 'resourceIdentifiers'
          label: 'Resource Identifiers'
          ObjectInputView: ResourceIdentifierView
          helpText: """
                    <p>A unique string or number used to identify the resource.</p>
                    <p> The codespace identifies the context in which the code is unique.</p>
                    """
      ]
    ]

    EditorView.prototype.initialize.apply @