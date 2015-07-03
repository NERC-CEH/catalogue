define [
  'cs!views/EditorView'
  'cs!views/editor/SingleObjectView'
  'cs!views/editor/ParentView'
  'cs!models/editor/String'
  'cs!views/editor/ResourceTypeView'
  'cs!models/editor/ResourceType'
  'cs!views/editor/InputView'
  'cs!views/editor/TextareaView'
  'cs!models/editor/TopicCategory'
  'cs!views/editor/TopicCategoryView'
  'cs!views/editor/ContactView'
  'cs!views/editor/ResourceIdentifierView'
  'cs!views/editor/DatasetReferenceDateView'
  'cs!models/editor/Contact'
  'cs!models/editor/ResourceIdentifier'
  'cs!views/editor/BoundingBoxView'
], (EditorView, SingleObjectView, ParentView, String, ResourceTypeView, ResourceType, InputView, TextareaView, TopicCategory, TopicCategoryView, ContactView, ResourceIdentifierView, DatasetReferenceDateView, Contact, ResourceIdentifier, BoundingBoxView) -> EditorView.extend


  initialize: ->

    @sections = [
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
                    <p>A brief description of the data resource. This should include some explanation as to purpose and how the data resource has been used since creation.</p>
                    <p>It is best to write a concise abstract.</p>
                    """

        new SingleObjectView
          model: @model
          modelAttribute: 'datasetReferenceDate'
          label: 'Dataset Reference Date'
          ObjectInputView: DatasetReferenceDateView,
          helpText: """
                    <p>Creation date, the date the data resource is created.</p>
                    <p>The publication date is the date when the data resource is being made available or released for use - it is <strong>NOT</strong> the date of creation.</p>
                    <p>If you include a revision date, it implies that the resource has been changed as a consequence of edits or updates.
                    For EIDC Hub records it is usual practice for revised resources to have an entirely new record, therefore <em>revision date</em> is rarely necessary.</p>
                    """
      ]
    ,
      label: 'Two'
      views: [
        new ParentView
          model: @model
          modelAttribute: 'boundingBoxes'
          label: 'Spatial Extents'
          ObjectInputView: BoundingBoxView
          multiline: true
          helpText: """
                    <p>A bounding box representing the limits of the data resource's study area.</p>
                    <p>If you do not wish to reveal the exact location publicly, it is recommended that you generalise the location.  Such sensitive locations may include endangered species and their habitats.</p>
                    """
      ]
    ,
      label: 'Three'
      views: [
#        'dataFormat'
#        'spatialRepresentationType'
#        'spatialResolution'
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
        new ParentView
          model: @model
          ModelType: Contact
          modelAttribute: 'responsibleParties'
          label: 'Contacts'
          ObjectInputView: ContactView
          multiline: true
          helpText: """
                    <p>The organisation or person responsible for the authorship, maintenance and curation of the data resource.</p>
                    <p>A contact must include the contact's email address, role and an organisation name and/or individual's name.  Other elements are optional.</p>
                    <p>The names of individuals should be included in the format <em>Surname, First Initial. Second Initial.</em>  For example <b>Brown, A.B.</b></p>
                    """
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
                    <p>Information about the source data used in the construction of this data resource.</p>
                    <p>Quality assessments and enhancement processes applied to the data resource can also be noted and summarised here.</p>
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
        new ParentView
          model: @model
          ModelType: ResourceIdentifier
          modelAttribute: 'resourceIdentifiers'
          label: 'Dataset Identifiers'
          ObjectInputView: ResourceIdentifierView
          helpText: """
                    <p>A unique string or number used to identify the data resource.</p>
                    <p> The codespace identifies the context in which the code is unique.</p>
                    """
      ]
    ,
      label: 'Ten'
      views: []
    ]

    EditorView.prototype.initialize.apply @




#    @components = [
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
#    ]
