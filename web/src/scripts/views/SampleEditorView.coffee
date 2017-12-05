define [
  'cs!views/EditorView'
  'cs!views/editor/SingleObjectView'
  'cs!views/editor/InputView'
  'cs!views/editor/TextareaView'
  'cs!views/editor/ParentStringView'
  'cs!views/editor/ParentView'
  'cs!views/editor/LinkView'
  'cs!views/editor/SpatialExtent'
  'cs!views/editor/SpatialExtentView'
  'cs!views/editor/TemporalExtent'
  'cs!views/editor/TemporalExtentView'
  'cs!models/editor/TopicCategory'
  'cs!views/editor/TopicCategoryView'
  'cs!views/editor/KeywordsView'
  ], (EditorView, SingleObjectView, InputView, TextareaView, ParentStringView, ParentView, LinkView, SpatialExtent, SpatialExtentView, TemporalExtent, TemporalExtentView, TopicCategory, TopicCategoryView, KeywordsView) -> EditorView.extend

  initialize: ->
    @sections = [
      label: 'One'
      title:  'General information'
      views: [
        new InputView
          model: @model
          modelAttribute: 'uri'
          label: 'URL'
          readonly: true

        new InputView
          model: @model
          modelAttribute: 'title'
          label: 'Title'
              
        new TextareaView
          model: @model
          modelAttribute: 'description'
          label: 'Description'
          rows: 17

        new SingleObjectView
          model: @model
          modelAttribute: 'type'
          label: 'Type'
          ObjectInputView: InputView
           
        new ParentView
          model: @model
          modelAttribute: 'spatialExtent'
          ModelType: spatialExtent
          label: 'Spatial Extent'
          ObjectInputView: SpatialExtentView
          multiline: true

        new ParentView
          model: @model
          modelAttribute: 'temporalExtent'
          ModelType: MultipleDate
          label: 'Temporal Extent'
          ObjectInputView: TemporalExtentView

        new TextareaView
          model: @model
          modelAttribute: 'lineage'
          label: 'Lineage'
          rows: 15
 
        new InputView
          model: @model
          modelAttribute: 'language'
          label: 'Language'

        new ParentView
          model: @model
          ModelType: TopicCategory
          modelAttribute: 'topicCategory'
          label: 'Topic Category'
          ObjectInputView: TopicCategoryView

        new ParentView
          model: @model
          ModelType: Keywords
          modelAttribute: 'keywords'
          label: 'Keywords'
          ObjectInputView: KeywordsView
          multiline: true

        new TextareaView
          model: @model
          modelAttribute: 'availability'
          label: 'Availability'
          rows: 5
        
        new TextareaView
          model: @model
          modelAttribute: 'accessRestrictions'
          label: 'Access Restrictions'
          rows: 5

        new TextareaView
          model: @model
          modelAttribute: 'storage'
          label: 'Storage'
          rows: 15

        new TextareaView
          model: @model
          modelAttribute: 'healthAndSafety'
          label: 'Health And Safety'
          rows: 15

        new ParentStringView
          model: @model
          modelAttribute: 'archiveLocation'
          label: 'Archive Location'

        new ParentView
          model: @model
          modelAttribute: 'website'
          label: 'Wbsite'
          ObjectInputView: LinkView

        new ParentView
          model: @model
          modelAttribute: 'resourceLocator'
          label: 'Resource Locator'
          ObjectInputView: LinkView

        new ParentView
          model: @model
          modelAttribute: 'archiveContacts'
          label: 'Archive Contacts'
          ObjectInputView: LinkView

        new InputView
          model: @model
          modelAttribute: 'metadataDate'
          label: 'Metadata Date'
          readonly: true

        new ParentView
          model: @model
          modelAttribute: 'metadataContacts'
          label: 'Metadata Contacts'
          ObjectInputView: LinkView

        ]
    ]
    
    console.log @sections
    
    EditorView.prototype.initialize.apply @