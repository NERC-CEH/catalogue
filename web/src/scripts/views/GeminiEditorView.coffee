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
  'cs!models/editor/MultipleDate'
  'cs!models/editor/Contact'
  'cs!models/editor/BoundingBox'
  'cs!views/editor/BoundingBoxView'
  'cs!views/editor/OnlineResourceView'
  'cs!models/editor/OnlineResource'
  'cs!views/editor/ResourceConstraintView'
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
  'cs!models/editor/SpatialResolution'
  'cs!views/editor/ServiceView'
  'cs!models/editor/Service'
  'cs!views/editor/ConformanceResultView'
], (EditorView, SingleObjectView, InputView, TextareaView, ParentView, PredefinedParentView, ParentStringView, ResourceTypeView, ResourceType, TopicCategory, TopicCategoryView, ContactView, ResourceIdentifierView, DatasetReferenceDateView, MultipleDate, Contact, BoundingBox, BoundingBoxView, OnlineResourceView, OnlineResource, ResourceConstraintView, OtherConstraintView, TemporalExtentView, ResourceStatusView, ResourceMaintenanceView, SpatialReferenceSystemView, SpatialRepresentationTypeView, DescriptiveKeywordView, DescriptiveKeyword, DistributionFormatView, SpatialResolutionView, SpatialResolution, ServiceView, Service, ConformanceResultView) -> EditorView.extend

  initialize: ->

    @sections = [
      label: 'One'
      title:  'General information'
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
                    <p>Only the leading letter and proper nouns of the title should be capitalised.  If it's necessary to include acronyms in the title, then include both the acronym (in parentheses) and the phrase/word from which it was formed. Acronyms should not include full-stops between each letter.</p>
                    <p>If there are multiple titles or translations of titles (e.g. in Welsh), these should be added as alternative titles.</p>
                    <p>Please refer to <a href="https://eip.ceh.ac.uk/catalogue/help/editing/metadataauthorguide" target="_blank">guidance for metadata authors</a></p>
                    """

        new ParentStringView
          model: @model
          modelAttribute: 'alternateTitles'
          label: 'Alternative Titles'
          helpText: """
                    <p>Alternative titles allow you to add multiple titles and non-English translations of titles (e.g. Welsh).</p>
                    <p>Only the leading letter and proper nouns of titles should be capitalised. If the title includes acronyms, include both the acronym (in parentheses) and its definition. Acronyms should not include full-stops between each letter.</p>
                    """

        new TextareaView
          model: @model
          modelAttribute: 'description'
          label: 'Description'
          rows: 17
          helpText: """
                    <p>The description should describe the data resource in question, NOT the project/activity which produced it.</p>
                    <p>The description is an 'executive summary' that allows the reader to determine the relevance and usefulness of the resource.  The text should be concise but should contain sufficient detail to allow the reader to ascertain rapidly the scope and limitations of the resource.</p>
                    <p>Write in plain English; in other words, write complete sentences rather than fragments.  It is recommended that the abstract is organised using the "What, Where, When, How, Why, Who" structure - see <a href="https://eip.ceh.ac.uk/catalogue/help/editing/metadataauthorguide" target="_blank">guidance for metadata authors</a></p>
                    """

        new PredefinedParentView
          model: @model
          modelAttribute: 'resourceIdentifiers'
          label: 'Dataset Identifiers'
          ObjectInputView: ResourceIdentifierView
          predefined:
            DOI:
              codeSpace: 'doi:'
              code: '10.'
          helpText: """
                    <p>A unique string or number used to identify the data resource.</p>
                    <p> The codespace identifies the context in which the code is unique.</p>
                    """

        new ResourceStatusView
          model: @model
          modelAttribute: 'resourceStatus'
          label: 'Resource Status'
          helpText: """
                    <p>The current status of the data resource.  For data curated by the EIDC, <strong>Completed</strong> is the usual expected value.  <strong>Historical archive</strong> is acceptable if the data have been withdrawn or replaced (e.g. due to corrections/updated data published elsewhere).  <strong>Obsolete</strong> may be used in exceptional circumstances.</p>
                    """

        new SingleObjectView
          model: @model
          modelAttribute: 'datasetReferenceDate'
          ModelType: MultipleDate
          label: 'Dataset Reference Date'
          ObjectInputView: DatasetReferenceDateView,
          helpText: """
                    <p>The publication date is the date when the data resource is being made available or released for use. <u>This is different from the creation date</u> which is the date on which the data resource was created.</p>
                    <p>If you include a revision date, it implies that the resource has been changed as a consequence of edits or updates.
                    For EIDC records it is usual practice for revised resources to have an entirely new record, therefore revision date is rarely necessary.</p>
                    """

        new TextareaView
          model: @model
          modelAttribute: 'supplementalInfo'
          label: 'Additional Information Source'
          rows: 7
          helpText: """
                    <p>Source(s) of further information about the data resource (e.g. journal articles).</p>
                    <p>If the sources of information are available online, it is recommended they are added as <strong>Online resources</strong>, otherwise they can be added as plain-text here.</p>
                    """
      ]
    ,
      label: 'Two'
      title: 'Spatial and temporal extent'
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
                    <p>If you do not wish to reveal the exact location publicly (for example, if locations are sensitive) it is recommended that you generalise the location.</p>
                    """

        new ParentView
          model: @model
          modelAttribute: 'temporalExtents'
          ModelType: MultipleDate
          label: 'Temporal Extents'
          ObjectInputView: TemporalExtentView
          helpText: """
                    <p>The time period(s) the data resource covers.  This is often the same as the data capture period but it need not be so.</p>
                    """
      ]
    ,
      label: 'Three'
      title:  'Spatial characteristics of the data resource'
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
                    <p>The spatial referencing system used within the data resource.  <strong>This is mandatory for datasets</strong>; if the dataset has no spatial component (e.g. if it is a laboratory study) the resource type ‘non-geographic data’ should be used instead.</p>
                    """

        new SpatialRepresentationTypeView
          model: @model
          modelAttribute: 'spatialRepresentationTypes'
          label: 'Spatial Representation Types'

        new ParentView
          model: @model
          modelAttribute: 'spatialResolutions'
          ModelType: SpatialResolution
          label: 'Spatial Resolutions'
          ObjectInputView: SpatialResolutionView
          helpText: """
                    <p>For gridded data, this is the area of the ground (in metres) represented in each pixel.</p>
                    <p>For point data, the ground sample distance is the degree of confidence in the point's location (e.g. for a point expressed as a six-figure grid reference, SN666781, the resolution would be 100m)</p>
                    """
      ]
    ,
      label: 'Four'
      title:  'Categorisation and keywords'
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
      title: 'Contacts'
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
              organisationName: 'Environmental Information Data Centre'
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
                    <p>The names of individuals should be included in the format Surname, First Initial. Second Initial. For example <strong>Brown, A.B.</strong></p>
                    """
      ]
    ,
      label: 'Six'
      title: 'Distribution details and lineage'
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
            'EIDC':
              organisationName: 'Environmental Information Data Centre'
              role: 'distributor'
              email: 'eidc@ceh.ac.uk'
          helpText: """
                    <p>The organisation responsible for distributing the data resource</p>
                    <p>A contact must include the contact's email address, role and an organisation name and/or individual's name.  Other elements are optional.</p>
                    <p>The names of individuals should be included in the format Surname, First Initial. Second Initial.  For example <strong>Brown, A.B.</strong></p>
                    """

        new ParentView
          model: @model
          modelAttribute: 'distributionFormats'
          label: 'Distribution Formats'
          ObjectInputView: DistributionFormatView
          helpText: """
                    <p>The format(s) in which the data is available.  Version is mandatory but if it’s not applicable, enter "unknown"</p>
                    """

        new TextareaView
          model: @model
          modelAttribute: 'lineage'
          label: 'Lineage'
          rows: 15
          helpText: """
                    <p>Information about the source data used in the construction of this data resource.</p>
                    <p>Quality assessments and enhancement processes applied to the data resource can also be noted and summarised here.</p>
                    <p>See <a href="https://eip.ceh.ac.uk/catalogue/help/editing/metadataauthorguide" target="_blank">guidance for metadata authors</a>.</p>
                    """

        new ParentView
          model: @model
          modelAttribute: 'resourceMaintenance'
          label: 'Resource Maintenance'
          ObjectInputView: ResourceMaintenanceView
          helpText: """
                    <p>This states how often the updated data resource is made available to the user.  For the vast majority of EIDC data, this value will be "not planned".</p>
                    """

        new PredefinedParentView
          model: @model
          modelAttribute: 'conformanceResults'
          label: 'Conformance Result'
          multiline: true
          ObjectInputView: ConformanceResultView
          predefined:
            INSPIRE:
              title: 'COMMISSION REGULATION (EU) No 1089/2010 of 23 November 2010 implementing Directive 2007/2/EC of the European Parliament and of the Council as regards interoperability of spatial data sets and services'
              date: '2010-12-08'
              dateType: 'publication'
      ]
    ,
      label: 'Seven'
      title: 'Online Resources'
      views: [
        new PredefinedParentView
          model: @model
          modelAttribute: 'onlineResources'
          ModelType: OnlineResource
          label: 'Online Resources'
          ObjectInputView: OnlineResourceView
          multiline: true
          predefined:
            'Supporting Information':
              url: 'http://eidc.ceh.ac.uk/metadata/{fileIdentifier}/zip_export/'
              name: 'Supporting information'
              description: 'Supporting information available to assist in re-use of this dataset'
              function: 'information'
            'Online Ordering':
              url: 'https://catalogue.ceh.ac.uk/download?fileIdentifier={fileIdentifier}'
              name: 'Download the data'
              description: 'Download a copy of this data'
              function: 'order'
            'Embargoed data':
              url: 'http://eidc.ceh.ac.uk/administration-folder/tools/embargo'
              name: 'Online ordering'
              description: 'This resource is under embargo and will be made available by {dd/mm/yyyy} at the latest'
              function: 'order'
            'Embargoed docs':
              url: 'http://eidc.ceh.ac.uk/administration-folder/tools/embargo'
              name: 'Supporting information'
              description: 'This resource is under embargo and will be made available by {dd/mm/yyyy} at the latest'
              function: 'information'
          helpText: """
                    <p>Links to web services to access the data and websites which provide additional information about the resource.</p>
                    """
      ]
    ,
      label: 'Eight'
      title: 'Licensing and constraints'
      views: [
        new PredefinedParentView
          model: @model
          modelAttribute: 'useConstraints'
          label: 'Use Constraints'
          ObjectInputView: ResourceConstraintView
          multiline: true
          predefined:
            'Open Government Licence':
              value: 'This resource is made available under the terms of the Open Government Licence'
              uri: 'http://eidc.ceh.ac.uk/administration-folder/tools/ceh-standard-licence-texts/ceh-open-government-licence/plain'
              code: 'license'
            'Open Government Licence Non-CEH':
              value: 'This resource is made available under the terms of the Open Government Licence'
              uri: 'http://eidc.ceh.ac.uk/administration-folder/tools/ceh-standard-licence-texts/open-government-licence-non-ceh-data/plain'
              code: 'license'
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
          modelAttribute: 'accessConstraints'
          label: 'Limitations on public access'
          ObjectInputView: ResourceConstraintView
          multiline: true
          predefined:
            'Registration required':
              value: 'Registration is required to access this data'
              uri: 'https://eip.ceh.ac.uk/catalogue/help/faq/registration'
              code: 'otherRestrictions'
            'no limitations':
              value: 'no limitations'
              code:  'otherRestrictions'
          helpText: """
                    <p>Any conditions that are in place to restrict a user's <strong>access</strong> to the data. These may include, for example, restrictions imposed for reasons of security or for licensing purposes.</p>
                    <p>You MUST enter something even if there are no access limitations. In the rare case that there are none, enter "no limitations".</p>
                    """
      ]
    ,
      label: 'Nine'
      title: 'Web service details'
      views: [
        new ServiceView
          model: @model
          modelAttribute: 'service'
          ModelType: Service
          label: 'Service'

        new ParentStringView
          model: @model
          modelAttribute: 'partOfRepository'
          label: 'Repository Membership'
          helpText: """
                    <p>File Identifier of repository.</p>
                    """

        new InputView
          model: @model
          modelAttribute: 'revisionOfIdentifier'
          label: 'Revision Of'
          helpText: """
                    <p>File Identifier of data resource being revised.</p>
                    """
      ]
    ,
      label: 'Ten'
      title: 'Metadata about metadata'
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
            'EIDC Point of Contact':
              organisationName: 'Environmental Information Data Centre'
              role: 'pointOfContact'
              email: 'eidc@ceh.ac.uk'
              address:
                country: 'United Kingdom'
          helpText: """
                    <p>The organisation or person responsible for the authorship, maintenance and curation of the metadata resource.</p>
                    <p>A contact must include the contact's email address, role and an organisation name and/or individual's name.  Other elements are optional.</p>
                    <p>The names of individuals should be included in the format Surname, First Initial. Second Initial.  For example <strong>Brown, A.B.</strong></p>
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
