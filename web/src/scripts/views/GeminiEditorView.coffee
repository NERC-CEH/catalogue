define [
  'cs!views/EditorView'
  'cs!views/editor/SingleObjectView'
  'cs!views/editor/ParentView'
  'cs!views/editor/PredefinedParentView'
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
  'cs!views/editor/BoundingBoxView'
  'cs!views/editor/OnlineResourceView'
], (EditorView, SingleObjectView, ParentView, PredefinedParentView, String, ResourceTypeView, ResourceType, InputView, TextareaView, TopicCategory, TopicCategoryView, ContactView, ResourceIdentifierView, DatasetReferenceDateView, Contact, BoundingBoxView, OnlineResourceView) -> EditorView.extend


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
                    <p>Type of resource.</p>
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
        new PredefinedParentView
          model: @model
          modelAttribute: 'boundingBoxes'
          label: 'Spatial Extents'
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
        new PredefinedParentView
          model: @model
          ModelType: Contact
          modelAttribute: 'responsibleParties'
          label: 'Contacts'
          ObjectInputView: ContactView
          multiline: true
          predefined:
            'CEH Author':
              organisationName: 'Centre for Ecology & Hydrology'
              role: 'author'
              email: 'enquiries@ceh.ac.uk'
              address:
                deliveryPoint: 'Maclean Building, Benson Lane, Crowmarsh Gifford'
                postalCode: 'OX10 8BB'
                city: 'Wallingford'
                administrativeArea: 'Oxfordshire'
                country: 'United Kingdom'
            'CEH Bangor':
              organisationName: 'Centre for Ecology & Hydrology'
              role: 'pointOfContact'
              email: 'enquiries@ceh.ac.uk'
              address:
                deliveryPoint: 'Environment Centre Wales, Deiniol Road'
                postalCode: 'LL57 2UW'
                city: 'Bangor'
                administrativeArea: 'Gwynedd'
                country: 'United Kingdom'
            'CEH Edinburgh':
              organisationName: 'Centre for Ecology & Hydrology'
              role: 'pointOfContact'
              email: 'enquiries@ceh.ac.uk'
              address:
                deliveryPoint: 'Bush Estate'
                postalCode: 'EH26 0QB'
                city: 'Penicuik'
                administrativeArea: 'Midlothian'
                country: 'United Kingdom'
            'CEH Lancaster':
              organisationName: 'Centre for Ecology & Hydrology'
              role: 'pointOfContact'
              email: 'enquiries@ceh.ac.uk'
              address:
                deliveryPoint: 'Lancaster Environment Centre, Library Avenue, Bailrigg'
                postalCode: 'LA1 4AP'
                city: 'Lancaster'
                administrativeArea: 'Lancashire'
                country: 'United Kingdom'
            'CEH Wallingford':
              organisationName: 'Centre for Ecology & Hydrology'
              role: 'pointOfContact'
              email: 'enquiries@ceh.ac.uk'
              address:
                deliveryPoint: 'Maclean Building, Benson Lane, Crowmarsh Gifford'
                postalCode: 'OX10 8BB'
                city: 'Wallingford'
                administrativeArea: 'Oxfordshire'
                country: 'United Kingdom'
            'EIDC Custodian':
              organisationName: 'EIDC'
              role: 'custodian'
              email: 'enquiries@ceh.ac.uk'
            'NERC Publisher':
              organisationName: 'NERC Environmental Information Data Centre'
              role: 'publisher'
              email: 'eidc@ceh.ac.uk'
            'Acreman Section':
              organisationName: 'Acreman Section'
              role: 'resourceProvider'
              email: 'enquiries@ceh.ac.uk'
              address:
                deliveryPoint: 'Maclean Building, Benson Lane, Crowmarsh Gifford'
                postalCode: 'OX10 8BB'
                city: 'Wallingford'
                administrativeArea: 'Oxfordshire'
                country: 'United Kingdom'
            'Dise Section':
              organisationName: 'Dise Section'
              role: 'resourceProvider'
              email: 'enquiries@ceh.ac.uk'
              address:
                deliveryPoint: 'Bush Estate'
                postalCode: 'EH26 0QB'
                city: 'Penicuik'
                administrativeArea: 'Midlothian'
                country: 'United Kingdom'
            'Emmett Section':
              organisationName: 'Emmett Section'
              role: 'resourceProvider'
              email: 'enquiries@ceh.ac.uk'
              address:
                deliveryPoint: 'Environment Centre Wales, Deiniol Road'
                postalCode: 'LL57 2UW'
                city: 'Bangor'
                administrativeArea: 'Gwynedd'
                country: 'United Kingdom'
            'Parr Section':
              organisationName: 'Parr Section'
              role: 'resourceProvider'
              email: 'enquiries@ceh.ac.uk'
              address:
                deliveryPoint: 'Lancaster Environment Centre, Library Avenue, Bailrigg'
                postalCode: 'LA1 4AP'
                city: 'Lancaster'
                administrativeArea: 'Lancashire'
                country: 'United Kingdom'
            'Pywell Section':
              organisationName: 'Pywell Section'
              role: 'resourceProvider'
              email: 'enquiries@ceh.ac.uk'
              address:
                deliveryPoint: 'Maclean Building, Benson Lane, Crowmarsh Gifford'
                postalCode: 'OX10 8BB'
                city: 'Wallingford'
                administrativeArea: 'Oxfordshire'
                country: 'United Kingdom'
            'Rees Section':
              organisationName: 'Rees Section'
              role: 'resourceProvider'
              email: 'enquiries@ceh.ac.uk'
              address:
                deliveryPoint: 'Maclean Building, Benson Lane, Crowmarsh Gifford'
                postalCode: 'OX10 8BB'
                city: 'Wallingford'
                administrativeArea: 'Oxfordshire'
                country: 'United Kingdom'
            'Reynard Section':
              organisationName: 'Reynard Section'
              role: 'resourceProvider'
              email: 'enquiries@ceh.ac.uk'
              address:
                deliveryPoint: 'Maclean Building, Benson Lane, Crowmarsh Gifford'
                postalCode: 'OX10 8BB'
                city: 'Wallingford'
                administrativeArea: 'Oxfordshire'
                country: 'United Kingdom'
            'Shore Section':
              organisationName: 'Shore Section'
              role: 'resourceProvider'
              email: 'enquiries@ceh.ac.uk'
              address:
                deliveryPoint: 'Lancaster Environment Centre, Library Avenue, Bailrigg'
                postalCode: 'LA1 4AP'
                city: 'Lancaster'
                administrativeArea: 'Lancashire'
                country: 'United Kingdom'
            'Watt Section':
              organisationName: 'Watt Section'
              role: 'resourceProvider'
              email: 'enquiries@ceh.ac.uk'
              address:
                deliveryPoint: 'Bush Estate'
                postalCode: 'EH26 0QB'
                city: 'Penicuik'
                administrativeArea: 'Midlothian'
                country: 'United Kingdom'
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
      views: [
        new ParentView
          model: @model
          modelAttribute: 'onlineResources'
          label: 'Online Resources'
          ObjectInputView: OnlineResourceView
          multiline: true
          helpText: """
                    <p>Links to websites and web services which provide additional information about the data resource.</p>
                    """
      ]
    ,
      label: 'Eight'
      views: []
    ,
      label: 'Nine'
      views: [
        new ParentView
          model: @model
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
