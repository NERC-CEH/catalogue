define [
  'cs!views/EditorView'
  'cs!views/editor/TitleView'
  'cs!views/editor/SingleObjectView'
  'cs!views/editor/ParentView'
  'cs!models/editor/String'
  'cs!views/editor/ResourceTypeView'
  'cs!views/editor/InputView'
  'cs!views/editor/DescriptionView'
  'cs!views/editor/LineageView'
  'cs!models/editor/TopicCategory'
  'cs!views/editor/TopicCategoryView'
  'cs!views/editor/ContactsView'
  'cs!views/editor/ResourceIdentifiersView'
  'cs!views/editor/DatasetReferenceDateView'
], (EditorView, TitleView, SingleObjectView, ParentView, String, ResourceTypeView, InputView, DescriptionView, LineageView, TopicCategory, TopicCategoryView, ContactsView, ResourceIdentifiersView, DatasetReferenceDateView) -> EditorView.extend


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
          label: 'Resource Type'
          ObjectInputView: ResourceTypeView,
          helpText: """
                    Type of resource.
                    """
        new TitleView
          model: @model

#        new ParentView
#          model: @model
#          ModelType: String
#          modelAttribute: 'alternateTitles'
#          label: 'Alternative Titles'
#          ObjectInputView: InputView
#          helpText: """
#                    <p>Alternative titles allow for entries of multiple titles, translations of titles (e.g. Welsh), and those with acronyms.</p>
#                    <p>The leading letter and proper nouns of the title only should be capitalised.</p>
#                    <p>In the event that the alternative title includes acronyms in the formal title of a data resource, then include
#                    both the acronym (in parentheses) and its definition. Acronyms should not include full-stops between each letter.</p>
#                    """

#        new DescriptionView
#          model: @model
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
#        new LineageView
#          model: @model
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
