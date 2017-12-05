define [
  'cs!views/EditorView'
  'cs!views/editor/InputView'
  'cs!views/editor/TextareaView'
  'cs!views/editor/KeywordView'
  'cs!views/editor/ParentView'
  'cs!views/editor/PredefinedParentView'
  'cs!models/editor/BoundingBox'
  'cs!models/editor/MultipleDate'
  'cs!models/editor/TopicCategory'
  'cs!views/editor/BoundingBoxView'
  'cs!views/editor/TemporalExtentView'
  'cs!views/editor/SingleObjectView'
  'cs!views/editor/SingleView'
  'cs!views/editor/TopicCategoryView'
], (EditorView, InputView, TextareaView, KeywordView, ParentView, PredefinedParentView, BoundingBox, MultipleDate, TopicCategory, BoundingBoxView, TemporalExtentView, SingleObjectView, SingleView, TopicCategoryView) -> EditorView.extend

  initialize: ->
    @model.set('type', 'sampleArchive') unless @model.has('type')
    @sections = [
      label: 'General'
      title:  'General information'
      views: [
        new TextareaView
          model: @model
          modelAttribute: 'title'
          label: 'Title'
          rows: 1
          helpText: """
                    <p>An informative title</p>
                    """

        new TextareaView
          model: @model
          modelAttribute: 'description'
          label: 'Description'
          rows: 5
          helpText: """
                    <p>An abstract that gives details about the sample collection.</p>
                    """

        new ParentView
          model: @model
          modelAttribute: 'specimenTypes'
          label: 'Specimen Types'
          ObjectInputView: KeywordView
          helpText: """
                    <p>A list of keywords and terms that describe the type(s) of specimens in the archive.</p>
                    """
        
        new SingleObjectView
          model: @model
          modelAttribute: 'temporalExtent'
          ModelType: MultipleDate
          label: 'Reference dates'
          ObjectInputView: TemporalExtentView,
          helpText: """
                    <p><u>Created</u> date is the date on which the data resource was originally created.</p>
                    <p><u>Published</u> date is the date when this metadata record is made available publicly.</p>
                    <p>For embargoed resources, the <u>Released</u> date is the date on which the embargo was lifted.</p>
                    <p><u>Superseded</u> date is the date on which the resource was superseded by another resource (where relevant).</p>
                    """
      ]
    ,
      label: 'Spatial'
      title:  'Geographic area that the archive covers'
      views: [
        new PredefinedParentView
          model: @model
          modelAttribute: 'boundingBoxes'
          ModelType: BoundingBox
          label: 'Spatial extent'
          ObjectInputView: BoundingBoxView
          multiline: true
          # These bounding box values were copied from terraCatalog
          predefined:
            England:
              northBoundLatitude: 55.812
              eastBoundLongitude: 1.7675
              southBoundLatitude: 49.864
              westBoundLongitude: -6.4526
            'Great Britain':
              northBoundLatitude: 60.861
              eastBoundLongitude: 1.768
              southBoundLatitude: 49.864
              westBoundLongitude: -8.648
            'Northern Ireland':
              northBoundLatitude: 55.313
              eastBoundLongitude: -5.432
              southBoundLatitude: 54.022
              westBoundLongitude: -8.178
            Scotland:
              northBoundLatitude: 60.861
              eastBoundLongitude: -0.728
              southBoundLatitude: 54.634
              westBoundLongitude: -8.648
            'United Kingdom':
              northBoundLatitude: 60.86099
              eastBoundLongitude: 1.767549
              southBoundLatitude: 49.86382
              westBoundLongitude: -8.648393
            Wales:
              northBoundLatitude: 53.434
              eastBoundLongitude: -2.654
              southBoundLatitude: 51.375
              westBoundLongitude: -5.473
            World:
              northBoundLatitude: 90.00
              eastBoundLongitude: 180.00
              southBoundLatitude: -90.00
              westBoundLongitude: -180.00
          helpText: """
                    <p>A bounding box representing the limits of the data resource's study area.</p>
                    <p>If you do not wish to reveal the exact location publicly (for example, if locations are sensitive) it is recommended that you generalise the location.</p>
                    """
      ]
    ,
      label: 'Three'
      title:  'Three'
      views: [
        new TextareaView
          model: @model
          modelAttribute: 'lineage'
          label: 'Lineage'
          rows: 5
          helpText: """
                    <p>An overview of how samples are collected and any QC or QA.</p>
                    """

        new InputView
          model: @model
          modelAttribute: 'language'
          label: 'Language'
          helpText: """
                    <p>The principle language of the archive.</p>
                    """

        new ParentView
          model: @model
          ModelType: TopicCategory
          modelAttribute: 'topicCategories'
          label: 'Topic categories'
          ObjectInputView: TopicCategoryView
          helpText: """
                    <p>The main theme(s) of the data resource as defined by the INSPIRE Directive.</p>
                    <p>Please note these are very broad themes and should not be confused with EIDC science topics.</p>
                    <p>Multiple topic categories are allowed - please include all that are pertinent.  For example, "Estimates of topsoil invertebrates" = Biota AND Environment AND Geoscientific Information.</p>
                    """

        new ParentView
          model: @model
          modelAttribute: 'keywords'
          label: 'Keywords'
          ObjectInputView: KeywordView
          helpText: """
                    <p>A list of keywords that help identify and describe the archive.</p>
                    """

        new TextareaView
          model: @model
          modelAttribute: 'availability'
          label: 'Availability'
          rows: 5
          helpText: """
                    <p>Information about how readily available the sampes are, who is allowed to have access and how to gain access.</p>
                    """
      ]
    ]
    
    EditorView.prototype.initialize.apply @