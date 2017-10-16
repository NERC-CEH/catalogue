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
  'cs!views/editor/BoundingBoxView'
  'cs!models/editor/BoundingBox'
  'cs!views/editor/ObservationCapabilityView'
  'cs!views/editor/GeometryView'
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
  BoundingBoxView,
  BoundingBox,
  ObservationCapabilityView,
  GeometryView
) -> EditorView.extend

  initialize: ->

    @model.set('type', 'monitoringFacility') unless @model.has('type')

    @sections = [
      label: 'Basic Info'
      title: 'Basic Info'
      views: [

        new InputView
          model: @model
          modelAttribute: 'title'
          label: 'Title'
          helpText: """
                    <p>Name of Monitoring Facility</p>
                    """

        new InputView
          model: @model
          modelAttribute: 'facilityType'
          label: 'Type'
          helpText: """
                    <p>Type of Monitoring Facility</p>
                    """

        new TextareaView
          model: @model
          modelAttribute: 'description'
          rows: 13
          label: 'Description'
          helpText: """
                    <p>Description of Monitoring Facility</p>
                    """

        new SingleObjectView
          model: @model
          modelAttribute: 'temporalExtent'
          ModelType: MultipleDate
          label: 'Temporal Extent'
          ObjectInputView: TemporalExtentView,
          helpText: """
                    <p>Temporal Extent of Monitoring Facility</p>
                    """

        new ParentView
          model: @model
          modelAttribute: 'keywords'
          label: 'Keywords'
          ObjectInputView: KeywordView
          helpText: """
                    <p>Keywords for discovery</p>
                    """

        new SingleObjectView
          model: @model
          modelAttribute: 'boundingBox'
          ModelType: BoundingBox
          label: 'Bounding Box'
          ObjectInputView: BoundingBoxView,
          helpText: """
                    <p>Bounding Box of Monitoring Facility</p>
                    """

        new GeometryView
          model: @model
          modelAttribute: 'geometry'
          label: 'Geometry'
          helpText: """
                    <p>Geometry of Monitoring Facility</p>
                    """

        new ParentView
          model: @model
          modelAttribute: 'relationships'
          label: 'Relationships'
          ObjectInputView: RelationshipView
          multiline: true
          options: [
            {value: 'http://onto.nerc.ac.uk/CEHMD/rels/partOf', label: 'Part of'}
          ]
          helpText: """
                    <p>Relationships to other OSDP document types</p>
                    """

        new ParentView
          model: @model
          modelAttribute: 'observationCapabilities'
          label: 'Observation Capabilities'
          ObjectInputView: ObservationCapabilityView
          multiline: true
          helpText: """
                    <p>Observation capabilities of the Monitoring Facility</p>
                    """
      ]
    ]

    EditorView.prototype.initialize.apply @