define [
  'cs!views/EditorView'
  'cs!views/editor/TitleView'
  'cs!views/editor/SingleObjectView'
  'cs!views/editor/ResourceTypeView'
  'cs!views/editor/AlternateTitlesView'
  'cs!views/editor/DescriptionView'
  'cs!views/editor/LineageView'
  'cs!views/editor/TopicCategoriesView'
  'cs!views/editor/ContactsView'
  'cs!views/editor/ResourceIdentifiersView'
  'cs!views/editor/DatasetReferenceDateView'
], (EditorView, TitleView, SingleObjectView, ResourceTypeView, AlternateTitlesView, DescriptionView, LineageView, TopicCategoriesView, ContactsView, ResourceIdentifiersView, DatasetReferenceDateView) -> EditorView.extend

  steps:
    One: [
      'resourceType'
      'title'
      'alternativeTitles'
      'description'
      'datasetReferenceDate'
    ]
    'Spatial Areas': [
      'spatialExtent'
    ]
    Three: [
      'dataFormat'
      'spatialRepresentationType'
      'spatialResolution'
    ]

  initialize: ->
    EditorView.prototype.initialize.apply @



    @components = [
      new TitleView
        el: @$('#editorTitle')
        model: @model

      new SingleObjectView
        el: @$('#editorResourceType')
        model: @model
        modelAttribute: 'resourceType'
        label: 'Resource Type'
        ObjectInputView: ResourceTypeView,
        helpText: """
                  Type of resource.
                  """

      new SingleObjectView
        el: @$('#editorDatasetReferenceDate')
        model: @model
        modelAttribute: 'datasetReferenceDate'
        label: 'Dataset Reference Date'
        ObjectInputView: DatasetReferenceDateView,
        helpText: """
                  <p>Creation date, the date the data resource is created.</p>
                  <p>The publication date is the date when the data resource is being made available or released for use - it is <strong>NOT</strong> the date of creation.</p>
                  <p>If you include a revision date, it implies that the resource has been changed as a consequence of edits or updates.  For EIDC Hub records it is usual practice for revised resources to have an entirely new record, therefore <em>revision date</em> is rarely necessary.</p>
                  """

      new AlternateTitlesView
        el: @$('#editorAlternateTitles')
        model: @model

      new DescriptionView
        el: @$('#editorDescription')
        model: @model

      new LineageView
        el: @$('#editorLineage')
        model: @model

      new TopicCategoriesView
        el: @$('#editorTopicCategories')
        model: @model

      new ContactsView
        el: @$('#editorContacts')
        model: @model

      new ResourceIdentifiersView
        el: @$('#editorResourceIdentifiers')
        model: @model
    ]
