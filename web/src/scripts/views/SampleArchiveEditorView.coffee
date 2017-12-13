define [
  'cs!views/EditorView'
  'cs!views/editor/InputView'
  'cs!views/editor/TextareaView'
  'cs!views/editor/KeywordView'
  'cs!views/editor/ParentView'
  'cs!views/editor/ParentStringView'
  'cs!views/editor/ParentStringTextboxView'
  'cs!views/editor/PredefinedParentView'
  'cs!models/editor/BoundingBox'
  'cs!models/editor/Contact'
  'cs!models/editor/MultipleDate'
  'cs!models/editor/PointOfContact'
  'cs!models/editor/TopicCategory'
  'cs!views/editor/BoundingBoxView'
  'cs!views/editor/TemporalExtentView'
  'cs!views/editor/SingleObjectView'
  'cs!views/editor/SingleView'
  'cs!views/editor/TopicCategoryView'
  'cs!views/editor/PointOfContactView'
], (EditorView, InputView, TextareaView, KeywordView, ParentView, ParentStringView, ParentStringTextboxView, PredefinedParentView, BoundingBox, Contact, MultipleDate, PointOfContact, TopicCategory, BoundingBoxView, TemporalExtentView, SingleObjectView, SingleView, TopicCategoryView, PointOfContactView) -> EditorView.extend

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
          label: 'Temporal extent'
          ObjectInputView: TemporalExtentView,
          helpText: """
                    <p>The time span of the sampling regime or project. The start date is mandatory, it is for the earliest sample in the archive and may be approximate if not precisely known. The end date is optional, if it is provided it represents the last sample in the archive.</p>
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

        new TextareaView
          model: @model
          modelAttribute: 'accessRestrictions'
          label: 'Access restrictions'
          rows: 5
          helpText: """
                    <p>An overview of any restrictions that will apply, usually things like IPR and T&C.</p>
                    """

        new TextareaView
          model: @model
          modelAttribute: 'storage'
          label: 'Storage'
          rows: 5
          helpText: """
                    <p>An overview of how the samples are stored to help a potential sample user understand what will be required if they request a sample.</p>
                    """

        new TextareaView
          model: @model
          modelAttribute: 'healthSafety'
          label: 'Health and safety'
          rows: 5
          helpText: """
                    <p>Any information users need to know regarding Health and Safety when they are considering taking on samples.</p>
                    """
      ]
    ,
      label: 'Contacts'
      title:  'Contacts'
      views: [
        new ParentStringTextboxView
          model: @model
          modelAttribute: 'archiveLocations'
          label: 'Archive location'
          rows: 5
          helpText: """
                    <p>Archive location</p>
                    """

        new ParentView
          model: @model
          ModelType: PointOfContact
          modelAttribute: 'archiveContacts'
          label: 'Archive contact(s)'
          ObjectInputView: PointOfContactView
          multiline: true
          helpText: """
                    <p>One or more points of contact for the sample archive.</p>
                    """

      ]
    ,
      label: 'Metadata'
      title:  'Metadata'
      views: [
        new ParentView
          model: @model
          ModelType: PointOfContact
          modelAttribute: 'metadataContacts'
          label: 'Metadata contact(s)'
          ObjectInputView: PointOfContactView
          multiline: true
          helpText: """
                    <p>The organisation or person responsible for the authorship, maintenance and curation of the metadata resource.</p>
                    <p>A contact must include the contact's email address, role and an organisation name and/or individual's name. Other elements are optional.</p>
                    """

        new InputView
          model: @model
          modelAttribute: 'id'
          label: 'File identifier'
          readonly: true
          helpText: """
                    <p>File identifier of metadata record.</p>
                    <p>For information only, not editable.</p>
                    """

        new InputView
          model: @model
          modelAttribute: 'uri'
          label: 'URL'
          readonly: true
          helpText: """
                    <p>URL of metadata record.</p>
                    <p>For information only, not editable.</p>
                    """

        new InputView
          model: @model
          modelAttribute: 'metadataDate'
          label: 'Metadata date'
          readonly: true
          helpText: """
                    <p>Date and time metadata last updated.</p>
                    <p>For information only, not editable.</p>
                    """
      ]
    ]
    
    EditorView.prototype.initialize.apply @