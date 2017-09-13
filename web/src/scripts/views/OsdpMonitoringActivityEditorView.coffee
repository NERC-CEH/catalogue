define [
  'cs!views/EditorView'
  'cs!views/editor/SingleObjectView'
  'cs!views/editor/InputView'
  'cs!views/editor/TextareaView'
  'cs!views/editor/ParentView'
  'cs!views/editor/KeywordView'
  'cs!views/editor/RelationshipView'
  'cs!views/editor/LinkView'
  'cs!models/editor/MultipleDate'
  'cs!views/editor/TemporalExtentView'
  'cs!views/editor/ResourceIdentifierView'
  'cs!views/editor/ParametersMeasuredView'
], (
  EditorView,
  SingleObjectView,
  InputView,
  TextareaView,
  ParentView,
  KeywordView,
  RelationshipView,
  LinkView,
  MultipleDate,
  TemporalExtentView,
  ResourceIdentifierView,
  ParametersMeasuredView
) -> EditorView.extend

  initialize: ->

    @model.set('type', 'monitoringActivity') unless @model.has('type')

    @sections = [
      label: 'Basic Info'
      title: 'Basic Info'
      views: [

        new InputView
          model: @model
          modelAttribute: 'title'
          label: 'Name'
          helpText: """
                    <p>Name of Monitoring Activity</p>
                    """

        new TextareaView
          model: @model
          modelAttribute: 'description'
          rows: 13
          label: 'Description'
          helpText: """
                    <p>Description of Monitoring Activity</p>
                    """

        new SingleObjectView
          model: @model
          modelAttribute: 'temporalExtent'
          ModelType: MultipleDate
          label: 'Temporal Extent'
          ObjectInputView: TemporalExtentView,
          helpText: """
                    <p>Temporal Extent of Monitoring Activity</p>
                    """

        new ParentView
          model: @model
          modelAttribute: 'parametersMeasured'
          label: 'Parameters Measured'
          ObjectInputView: ParametersMeasuredView
          multiline: true
          helpText: """
                    <p>Parameters measured as part of Monitoring Activity</p>
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
            {value: 'http://onto.nerc.ac.uk/CEHMD/rels/produces', label: 'Produces'},
            {value: 'http://onto.nerc.ac.uk/CEHMD/rels/uses', label: 'Uses'}
          ]
          helpText: """
                    <p>Relationships to other OSDP document types</p>
                    """
      ]
    ]

    EditorView.prototype.initialize.apply @