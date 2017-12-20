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
  'cs!views/editor/LinkView'
], (EditorView, InputView, TextareaView, KeywordView, ParentView, ParentStringView, ParentStringTextboxView, PredefinedParentView, BoundingBox, Contact, MultipleDate, PointOfContact, TopicCategory, BoundingBoxView, TemporalExtentView, SingleObjectView, SingleView, TopicCategoryView, PointOfContactView, LinkView) -> EditorView.extend

  initialize: ->
    @model.set('type', 'sampleArchive') unless @model.has('type')

    @sections = [
      label: 'Sample Archive Metadata Entry'
      title:  'Sample Archive Metadata Entry'
      views: [
        new TextareaView
          model: @model
          modelAttribute: 'title'
          label: 'Title'
          rows: 1
          helpText: """
                    <p>A brief and informative title.</p>
                    """

        new TextareaView
          model: @model
          modelAttribute: 'description'
          label: 'Description'
          rows: 5
          helpText: """
                    <p>An abstract that gives details about the sample collection. A well filled in description will provide many of the search terms that ensure the record appears appropriately in catalogue searches.</p>
                    """

        new ParentView
          model: @model
          modelAttribute: 'specimenTypes'
          label: 'Specimen Types'
          ObjectInputView: KeywordView
          helpText: """
                    <p>A list of keywords and terms that describe the types of specimens in the archive.</p>
                    """

        new InputView
          model: @model
          modelAttribute: 'website'
          label: 'Website url'
          helpText: """
                    <p>A link to a website for this archive.</p>
                    """

        new ParentView
          model: @model
          ModelType: PointOfContact
          modelAttribute: 'archiveContacts'
          label: 'Archive contacts'
          ObjectInputView: PointOfContactView
          multiline: true
          helpText: """
                    <p>One or more points of contact for the sample archive.</p>
                    """

        new ParentStringTextboxView
          model: @model
          modelAttribute: 'archiveLocations'
          label: 'Archive locations'
          rows: 5
          helpText: """
                    <p>Address of the archiving facility. If it is spread over more than one location then provide a list of addresses.</p>
                    """

        new TextareaView
          model: @model
          modelAttribute: 'availability'
          label: 'Availability'
          rows: 5
          helpText: """
                    <p>Information about how readily available the samples are, who is allowed to have access and how to gain access.</p>
                    """

        new TextareaView
          model: @model
          modelAttribute: 'accessRestrictions'
          label: 'Access restrictions'
          rows: 5
          helpText: """
                    <p>An overview of any restrictions that will apply, usually things like Intellectual Property Rights and Terms and Conditions.</p>
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
        
        new SingleObjectView
          model: @model
          modelAttribute: 'temporalExtent'
          ModelType: MultipleDate
          label: 'Temporal extent'
          ObjectInputView: TemporalExtentView,
          helpText: """
                    <p>The time span of the sampling regime or project. The start date is mandatory, it is for the earliest sample in the archive and may be approximate if not precisely known. The end date is optional, if it is provided it represents the last sample in the archive.</p>
                    """

        new PredefinedParentView
          model: @model
          modelAttribute: 'boundingBoxes'
          ModelType: BoundingBox
          label: 'Spatial extents'
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
                    <p>A bounding box showing the area that the archive covers. It will encompass the remit of the archive, which may be larger than that represented by the samples actually in the archive. It is represented by north, south, east and west in decimal degrees (WGS84).</p>
                    """

        new TextareaView
          model: @model
          modelAttribute: 'lineage'
          label: 'Lineage'
          rows: 5
          helpText: """
                    <p>An overview of how samples are collected and any Quality Control or Quality Assurance.</p>
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
                    <p>An ISO 19115 topic category. This is a high level keyword used to categorise datasets that have a spatial component.</p>
                    """

        new ParentView
          model: @model
          modelAttribute: 'keywords'
          label: 'Keywords'
          ObjectInputView: KeywordView
          helpText: """
                    <p>A list of words that help to identify and describe the archive. It will be used to improve search results and filtering. A keyword may be associated with an entry in a vocabulary via a uri, or just be a simple custom keyword. The list is unique on keyword-uri combinations.</p>
                    """

        new ParentView
          model: @model
          modelAttribute: 'resourceLocators'
          label: 'Additional Resources'
          ObjectInputView: LinkView
          helpText: """
                    <p>A list of links to additional resources that may be of use to the user. These are in the form of name: url pairs.</p>
                    """

        new ParentView
          model: @model
          ModelType: PointOfContact
          modelAttribute: 'metadataContacts'
          label: 'Metadata contacts'
          ObjectInputView: PointOfContactView
          multiline: true
          helpText: """
                    <p>One or more points of contact for the owners/authors of this metadata record if different to the Archive Contacts.</p>
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
                    <p>Date and time the record was last updated. This is autogenerated and not editable.</p>
                    """
      ]
    ]
    
    EditorView.prototype.initialize.apply @