define [
  'cs!views/EditorView'
  'cs!views/editor/TitleView'
  'cs!views/editor/SingleObjectView'
  'cs!views/editor/ParentView'
  'cs!models/editor/String'
  'cs!views/editor/ResourceTypeView'
  'cs!models/editor/ResourceType'
  'cs!views/editor/InputView'
  'cs!views/editor/TextareaView'
  'cs!models/editor/TopicCategory'
  'cs!views/editor/TopicCategoryView'
  'cs!views/editor/ContactsView'
  'cs!views/editor/ResourceIdentifiersView'
  'cs!views/editor/DatasetReferenceDateView'
], (EditorView, TitleView, SingleObjectView, ParentView, String, ResourceTypeView, ResourceType, InputView, TextareaView, TopicCategory, TopicCategoryView, ContactsView, ResourceIdentifiersView, DatasetReferenceDateView) -> EditorView.extend


  initialize: ->
    EditorView.prototype.initialize.apply @

    @steps = [
      label: 'Zero'
      views: []
    ,
      label: 'One'
      views: [
        new SingleObjectView
          model: @model
          modelAttribute: 'resourceType'
          ModelType: ResourceType
          label: 'Resource Type'
          ObjectInputView: ResourceTypeView,
          helpText: """
                    Type of resource.
                    """
        new SingleObjectView
          model: @model
          modelAttribute: 'title'
          ModelType: String
          label: 'Title'
          ObjectInputView: InputView,
          helpText: """
                    <p>Provide a title that best describes that data resource. Include references to the subject, spatial and temporal aspects of the data resource.</p>
                    <p>Jargon should be avoided so as to provide clarity to a broad audience from various specialisation across the public sector</p>
                    <p>The leading letter and proper nouns of the title should be capitalised.</p>
                    <p>If it's necessary to include acronyms in the formal title of a data resource, then include both the acronym (in parentheses) and its phrase or word from which it was formed.</p>
                    <p>In the event that there are multiple titles, translations of titles (e.g. Welsh), and those with acronyms, these titles should be added as alternative titles</p>
                    """

        new ParentView
          model: @model
          ModelType: String
          modelAttribute: 'alternateTitles'
          label: 'Alternative Titles'
          ObjectInputView: InputView
          helpText: """
                    <p>Alternative titles allow for entries of multiple titles, translations of titles (e.g. Welsh), and those with acronyms.</p>
                    <p>The leading letter and proper nouns of the title only should be capitalised.</p>
                    <p>In the event that the alternative title includes acronyms in the formal title of a data resource, then include
                    both the acronym (in parentheses) and its definition. Acronyms should not include full-stops between each letter.</p>
                    """

        new SingleObjectView
          model: @model
          modelAttribute: 'description'
          ModelType: String
          label: 'Description'
          ObjectInputView: TextareaView,
          rows: 17
          helpText: """
                    A brief description of the data resource. This should include some explanation as to purpose and how the data resource has been used since creation. It is best to write a concise abstract.
                    """
#
#        new SingleObjectView
#          model: @model
#          modelAttribute: 'datasetReferenceDate'
#          label: 'Dataset Reference Date'
#          ObjectInputView: DatasetReferenceDateView,
#          helpText: """
#                    <p>Creation date, the date the data resource is created.</p>
#                    <p>The publication date is the date when the data resource is being made available or released for use - it is <strong>NOT</strong> the date of creation.</p>
#                    <p>If you include a revision date, it implies that the resource has been changed as a consequence of edits or updates.
#                    For EIDC Hub records it is usual practice for revised resources to have an entirely new record, therefore <em>revision date</em> is rarely necessary.</p>
#                    """
      ]
    ,
      label: 'Two'
      views: [
        'spatialExtent'
      ]
    ,
      label: 'Three'
      views: [
        'dataFormat'
        'spatialRepresentationType'
        'spatialResolution'
      ]
    ,
      label: 'Four'
      views: [
        new ParentView
          model: @model
          ModelType: TopicCategory
          modelAttribute: 'topicCategories'
          label: 'Topic Categories'
          ObjectInputView: TopicCategoryView
          helpText: """
                    <p>The main theme(s) of the data resource as defined by the INSPIRE Directive.</p>
                    <p>Please note these are very broad themes and should not be confused with EIDC science topics.</p>
                    <p>Multiple topic categories are allowed - please include all that are pertinent.  For example, "Estimates of topsoil invertebrates" = Biota AND Environment AND Geoscientific Information.</p>
                    """
      ]
    ,
      label: 'Five'
      views: [
#        new ContactsView
#          model: @model
      ]
    ,
      label: 'Six'
      views: [
        new SingleObjectView
          model: @model
          modelAttribute: 'lineage'
          ModelType: String
          label: 'Lineage'
          ObjectInputView: TextareaView,
          rows: 15
          helpText: """
                    Information about the source data used in the construction of this data resource. Quality assessments and enhancement processes applied to the data resource can also be noted and summarised here.
                    """
      ]
    ,
      label: 'Seven'
      views: []
    ,
      label: 'Eight'
      views: []
    ,
      label: 'Nine'
      views: [
#        new ResourceIdentifiersView
#          model: @model
      ]
    ,
      label: 'Ten'
      views: []
    ]
    do @render




    @components = [
#      new TitleView
#        el: @$('#editorTitle')
#        model: @model
#
#      new SingleObjectView
#        el: @$('#editorResourceType')
#        model: @model
#        modelAttribute: 'resourceType'
#        label: 'Resource Type'
#        ObjectInputView: ResourceTypeView,
#        helpText: """
#                  Type of resource.
#                  """
#
#      new SingleObjectView
#        el: @$('#editorDatasetReferenceDate')
#        model: @model
#        modelAttribute: 'datasetReferenceDate'
#        label: 'Dataset Reference Date'
#        ObjectInputView: DatasetReferenceDateView,
#        helpText: """
#                  <p>Creation date, the date the data resource is created.</p>
#                  <p>The publication date is the date when the data resource is being made available or released for use - it is <strong>NOT</strong> the date of creation.</p>
#                  <p>If you include a revision date, it implies that the resource has been changed as a consequence of edits or updates.  For EIDC Hub records it is usual practice for revised resources to have an entirely new record, therefore <em>revision date</em> is rarely necessary.</p>
#                  """
#
#      new AlternateTitlesView
#        el: @$('#editorAlternateTitles')
#        model: @model
#
#      new DescriptionView
#        el: @$('#editorDescription')
#        model: @model
#
#      new LineageView
#        el: @$('#editorLineage')
#        model: @model
#
#      new TopicCategoriesView
#        el: @$('#editorTopicCategories')
#        model: @model
#
#      new ContactsView
#        el: @$('#editorContacts')
#        model: @model
#
#      new ResourceIdentifiersView
#        el: @$('#editorResourceIdentifiers')
#        model: @model
    ]
