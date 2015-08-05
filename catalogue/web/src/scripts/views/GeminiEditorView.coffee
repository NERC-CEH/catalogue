define [
  'cs!views/EditorView'
  'cs!views/editor/SingleObjectView'
  'cs!views/editor/InputView'
  'cs!views/editor/TextareaView'
  'cs!views/editor/ParentView'
  'cs!views/editor/PredefinedParentView'
  'cs!views/editor/ParentStringView'
  'cs!views/editor/ResourceTypeView'
  'cs!models/editor/ResourceType'
  'cs!models/editor/TopicCategory'
  'cs!views/editor/TopicCategoryView'
  'cs!views/editor/ContactView'
  'cs!views/editor/ResourceIdentifierView'
  'cs!views/editor/DatasetReferenceDateView'
  'cs!models/editor/Contact'
  'cs!models/editor/BoundingBox'
  'cs!views/editor/BoundingBoxView'
  'cs!views/editor/OnlineResourceView'
  'cs!views/editor/UseLimitationView'
  'cs!views/editor/OtherConstraintView'
  'cs!views/editor/TemporalExtentView'
  'cs!views/editor/ResourceStatusView'
  'cs!views/editor/ResourceMaintenanceView'
  'cs!views/editor/SpatialReferenceSystemView'
  'cs!views/editor/SpatialRepresentationTypeView'
  'cs!views/editor/DescriptiveKeywordView'
  'cs!models/editor/DescriptiveKeyword'
  'cs!views/editor/DistributionFormatView'
  'cs!views/editor/SpatialResolutionView'
  'cs!views/editor/ServiceView'
  'cs!models/editor/Service'
], (EditorView, SingleObjectView, InputView, TextareaView, ParentView, PredefinedParentView, ParentStringView, ResourceTypeView, ResourceType, TopicCategory, TopicCategoryView, ContactView, ResourceIdentifierView, DatasetReferenceDateView, Contact, BoundingBox, BoundingBoxView, OnlineResourceView, UseLimitationView, OtherConstraintView, TemporalExtentView, ResourceStatusView, ResourceMaintenanceView, SpatialReferenceSystemView, SpatialRepresentationTypeView, DescriptiveKeywordView, DescriptiveKeyword, DistributionFormatView, SpatialResolutionView, ServiceView, Service) -> EditorView.extend


  initialize: ->

    @sections = [
      label: 'One'
      views: [
        new SingleObjectView
          model: @model
          modelAttribute: 'resourceType'
          ModelType: ResourceType
          label: 'Resource Type'
          ObjectInputView: ResourceTypeView

        new InputView
          model: @model
          modelAttribute: 'title'
          label: 'Title'
          helpText: """
                    <p>Provide a title that best describes that data resource. Include references to the subject, spatial and temporal aspects of the data resource.</p>
                    <p>Jargon should be avoided so as to provide clarity to a broad audience from various specialisation across the public sector</p>
                    <p>The leading letter and proper nouns of the title should be capitalised.</p>
                    <p>If it's necessary to include acronyms in the formal title of a data resource, then include both the acronym (in parentheses) and its phrase or word from which it was formed.</p>
                    <p>In the event that there are multiple titles, translations of titles (e.g. Welsh), and those with acronyms, these titles should be added as alternative titles</p>
                    """

        new ParentStringView
          model: @model
          modelAttribute: 'alternateTitles'
          label: 'Alternative Titles'
          helpText: """
                    <p>Alternative titles allow for entries of multiple titles, translations of titles (e.g. Welsh), and those with acronyms.</p>
                    <p>The leading letter and proper nouns of the title only should be capitalised.</p>
                    <p>In the event that the alternative title includes acronyms in the formal title of a data resource, then include
                    both the acronym (in parentheses) and its definition. Acronyms should not include full-stops between each letter.</p>
                    """

        new TextareaView
          model: @model
          modelAttribute: 'description'
          label: 'Description'
          rows: 17
          helpText: """
                    <p>A brief description of the data resource. This should include some explanation as to purpose and how the data resource has been used since creation.</p>
                    <p>It is best to write a concise abstract.</p>
                    """

        new ResourceStatusView
          model: @model
          modelAttribute: 'resourceStatus'
          label: 'Resource Status'

        new SingleObjectView
          model: @model
          modelAttribute: 'datasetReferenceDate'
          label: 'Dataset Reference Date'
          ObjectInputView: DatasetReferenceDateView,
          helpText: """
                    <p>Creation date, the date the data resource is created.</p>
                    <p>The publication date is the date when the data resource is being made available or released for use - it is <strong>NOT</strong> the date of creation.</p>
                    <p>If you include a revision date, it implies that the resource has been changed as a consequence of edits or updates.
                    For EIDC records it is usual practice for revised resources to have an entirely new record, therefore <em>revision date</em> is rarely necessary.</p>
                    """

        new TextareaView
          model: @model
          modelAttribute: 'supplementalInfo'
          label: 'Additional Information Source'
          rows: 7
      ]
    ,
      label: 'Two'
      views: [
        new PredefinedParentView
          model: @model
          modelAttribute: 'boundingBoxes'
          ModelType: BoundingBox
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

        new ParentView
          model: @model
          modelAttribute: 'temporalExtents'
          label: 'Temporal Extents'
          ObjectInputView: TemporalExtentView
      ]
    ,
      label: 'Three'
      views: [
        new PredefinedParentView
          model: @model
          modelAttribute: 'spatialReferenceSystems'
          label: 'Spatial Reference Systems'
          ObjectInputView: SpatialReferenceSystemView
          predefined:
            'OSGB 1936 / British National Grid':
              code: 27700
              codeSpace: 'urn:ogc:def:crs:EPSG'
            'TM65 / Irish National Grid':
              code: 29900
              codeSpace: 'urn:ogc:def:crs:EPSG'
            'OSNI 1952 / Irish National Grid':
              code: 29901
              codeSpace: 'urn:ogc:def:crs:EPSG'
            'TM65 / Irish Grid':
              code: 29902
              codeSpace: 'urn:ogc:def:crs:EPSG'
            'WGS 84':
              code: 4326
              codeSpace: 'urn:ogc:def:crs:EPSG'
            'WGS 84 longitude-latitude (CRS:84)':
              code: 'CRS:84'
              codeSpace: 'urn:ogc:def:crs:EPSG'
          helpText: """
                    <p>The spatial referencing systems used within the data resource.</p>
                    <p>There are three commonly used co-ordinate systems for British Isle data resources:</p>
                    <ul  class="list-unstyled">
                    <li>British National Grid</li>
                    <li>Irish National Grid</li>
                    <li>WGS84 (which is a global reference system)</li>
                    </ul>
                    """

        new SpatialRepresentationTypeView
          model: @model
          modelAttribute: 'spatialRepresentationTypes'
          label: 'Spatial Representation Types'

        new ParentView
          model: @model
          modelAttribute: 'spatialResolutions'
          label: 'Spatial Resolutions'
          ObjectInputView: SpatialResolutionView
          helpText: """
                    <p>For gridded data, this is the area of the ground (in metres) represented in each pixel.</p>
                    <p>For point data, the ground sample distance is the degree of confidence in the point's location (e.g. for a point expressed as a six-figure grid reference, SN666781, the resolution would be 100m)</p>
                    """
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

        new PredefinedParentView
          model: @model
          ModelType: DescriptiveKeyword
          modelAttribute: 'descriptiveKeywords'
          label: 'Keywords'
          ObjectInputView: DescriptiveKeywordView
          multiline: true
          predefined:
            'INSPIRE Theme':
              type: 'theme'
              thesaurusName:
                title: 'GEMET - INSPIRE themes, version 1.0'
                date: '2008-06-01'
                dateType: 'revision'
            'CEH Topic':
              type: 'theme'
              thesaurusName:
                title: 'CEH Metadata Vocabulary'
                date: '2014-09-19'
                dateType: 'creation'
          helpText: """
                    <p>Keywords (preferably taken from a controlled vocabulary) categorising and describing the data resource.</p>
                    <p>Good quality keywords help to improve the efficiency of search, making it easier to find relevant records.</p>
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
              email: 'eidc@ceh.ac.uk'
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
        new PredefinedParentView
          model: @model
          ModelType: Contact
          modelAttribute: 'distributorContacts'
          label: 'Distributor Contacts'
          ObjectInputView: ContactView
          multiline: true
          predefined:
            'CEH Distributor':
              organisationName: 'Centre for Ecology & Hydrology'
              role: 'distributor'
              email: 'enquiries@ceh.ac.uk'
          helpText: """
                    <p>The organisation responsible for distributing the data resource</p>
                    <p>A contact must include the contact's email address, role and an organisation name and/or individual's name.  Other elements are optional.</p>
                    <p>The names of individuals should be included in the format <em>Surname, First Initial. Second Initial.</em>  For example <b>Brown, A.B.</b></p>
                    """

        new ParentView
          model: @model
          modelAttribute: 'distributionFormats'
          label: 'Distribution Formats'
          ObjectInputView: DistributionFormatView
          helpText: """
                    <p>File format of dataset</p>
                    """

        new TextareaView
          model: @model
          modelAttribute: 'lineage'
          label: 'Lineage'
          rows: 15
          helpText: """
                    <p>Information about the source data used in the construction of this data resource.</p>
                    <p>Quality assessments and enhancement processes applied to the data resource can also be noted and summarised here.</p>
                    """

        new ParentView
          model: @model
          modelAttribute: 'resourceMaintenance'
          label: 'Resource Maintenance'
          ObjectInputView: ResourceMaintenanceView
      ]
    ,
      label: 'Seven'
      views: [
        new PredefinedParentView
          model: @model
          modelAttribute: 'onlineResources'
          label: 'Online Resources'
          ObjectInputView: OnlineResourceView
          multiline: true
          predefined:
            'Supporting Information':
              url: 'http://eidc.ceh.ac.uk/metadata/{fileIdentifier}/zip_export/'
              name: 'Supporting information'
              description: 'Supporting information available to assist in re-use of this datase. Link to data citation details.'
              'function': 'information'
            'Online Ordering':
              url: 'https://catalogue.ceh.ac.uk/download?fileIdentifier={fileIdentifier}'
              name: 'Online ordering'
              description: 'Order a copy of this dataset'
              'function': 'order'
          helpText: """
                    <p>Links to websites and web services which provide additional information about the data resource.</p>
                    <p>In the templates replace <samp>{fileIdentifier}</samp> with the actual metadata file identifier</p>
                    """
      ]
    ,
      label: 'Eight'
      views: [
        new PredefinedParentView
          model: @model
          modelAttribute: 'useLimitations'
          label: 'Use Constraints'
          ObjectInputView: UseLimitationView
          multiline: true
          predefined:
            'Open Government Licence':
              value: 'Open Government Licence'
              uri: 'http://eidc.ceh.ac.uk/administration-folder/tools/ceh-standard-licence-texts/ceh-open-government-licence/plain'
            'Open Government Licence - Non CEH data':
              value: 'Open Government Licence'
              uri: 'http://eidc.ceh.ac.uk/administration-folder/tools/ceh-standard-licence-texts/open-government-licence-non-ceh-data/plain'
            'CEH Licence':
              value: 'Licence terms and conditions apply'
              uri: 'http://eidc.ceh.ac.uk/administration-folder/tools/ceh-standard-licence-texts/standard-click-through/plain'
            'Licence terms ...':
              value: 'Licence terms and conditions apply'
          helpText: """
                    <p>Describe any restrictions and legal prerequisites placed on the <strong>use</strong> of a data resource once it has been accessed. For example:</p>
                    <ul class="list-unstyled">
                      <li>"Licence conditions apply"</li>
                      <li>"If you reuse this data you must cite …"</li>
                      <li>"Do not use for navigation purposes"</li>
                    </ul>
                    <p>Where possible include a link to a document describing the terms and conditions.</p>
                    <p>You MUST enter something even if there are no constraints. In the rare case that there are none, enter "no conditions apply".</p>
                    """

        new PredefinedParentView
          model: @model
          modelAttribute: 'otherConstraints'
          label: 'Limitations on public access'
          ObjectInputView: OtherConstraintView
          predefined:
            'no limitations':
              value: 'no limitations'
          helpText: """
                    <p>Any conditions that are in place to restrict a user's <strong>access</strong> to the data. These may include, for example, restrictions imposed for reasons of security or for licensing purposes.</p>
                    <p>You MUST enter something even if there are no access limitations. In the rare case that there are none, enter "no limitations".</p>
                    """
      ]
    ,
      label: 'Nine'
      views: [
        new PredefinedParentView
          model: @model
          modelAttribute: 'resourceIdentifiers'
          label: 'Dataset Identifiers'
          ObjectInputView: ResourceIdentifierView
          predefined:
            DOI:
              codeSpace: 'doi:'
          helpText: """
                    <p>A unique string or number used to identify the data resource.</p>
                    <p> The codespace identifies the context in which the code is unique.</p>
                    """

        new ServiceView
          model: @model
          modelAttribute: 'service'
          ModelType: Service
          label: 'Service'
      ]
    ,
      label: 'Ten'
      views: [
        new PredefinedParentView
          model: @model
          ModelType: Contact
          modelAttribute: 'metadataPointsOfContact'
          label: 'Metadata Points of Contact'
          ObjectInputView: ContactView
          multiline: true
          predefined:
            'CEH Point of Contact':
              organisationName: 'Centre for Ecology & Hydrology'
              role: 'pointOfContact'
              email: 'enquiries@ceh.ac.uk'
              address:
                deliveryPoint: 'Maclean Building, Benson Lane, Crowmarsh Gifford'
                postalCode: 'OX10 8BB'
                city: 'Wallingford'
                administrativeArea: 'Oxfordshire'
                country: 'United Kingdom'
          helpText: """
                    <p>The organisation or person responsible for the authorship, maintenance and curation of the metadata resource.</p>
                    <p>A contact must include the contact's email address, role and an organisation name and/or individual's name.  Other elements are optional.</p>
                    <p>The names of individuals should be included in the format <em>Surname, First Initial. Second Initial.</em>  For example <b>Brown, A.B.</b></p>
                    """

        new InputView
          model: @model
          modelAttribute: 'parentIdentifier'
          label: 'Parent Identifier'
          helpText: """
                    <p>File identifier of parent series.</p>
                    """

        new InputView
          model: @model
          modelAttribute: 'id'
          label: 'File Identifier'
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
          label: 'Metadata Date'
          readonly: true
          helpText: """
                    <p>Date and time metadata last updated.</p>
                    <p>For information only, not editable.</p>
                    """
      ]
    ]

    EditorView.prototype.initialize.apply @
