define [
  'cs!views/EditorView'
  'cs!views/editor/InputView'
  'cs!views/editor/TextareaView'
  'cs!views/editor/ParentView'
  'cs!views/editor/PredefinedParentView'
  'cs!views/editor/ParentStringView'
  'cs!views/editor/KeywordView'
  'cs!views/editor/ContactView'
  'cs!views/editor/SingleObjectView'
  'cs!views/editor/QaView'
  'cs!views/editor/VersionHistoryView'
  'cs!views/editor/ProjectUsageView'
  'cs!views/editor/OnlineLinkView'
  'cs!models/editor/Contact'
  'cs!models/editor/DataTypeSchema'
  'cs!views/editor/DataTypeSchemaSimpleView'
  'cs!models/editor/BoundingBox'
  'cs!views/editor/BoundingBoxView'
  'cs!models/editor/Supplemental'
  'cs!views/editor/SupplementalView'

], (
  EditorView,
  InputView,
  TextareaView,
  ParentView,
  PredefinedParentView,
  ParentStringView,
  KeywordView,
  ContactView,
  SingleObjectView,
  QaView,
  VersionHistoryView,
  ProjectUsageView,
  OnlineLinkView,
  Contact,
  DataTypeSchema,
  DataTypeSchemaSimpleView,
  BoundingBox,
  BoundingBoxView,
  Supplemental,
  SupplementalView
) -> EditorView.extend

  initialize: ->

    @model.set('type', 'model') unless @model.has('type')

    @sections = [
      label: 'Basic info'
      title: 'Basic info'
      views: [

        new InputView
          model: @model
          modelAttribute: 'title'
          label: 'Title'

        new TextareaView
          model: @model
          modelAttribute: 'description'
          label: 'Model description'
          rows: 7
          helpText: """
                    Longer description of model e.g. development history, use to answer science questions, overview of structure
                    """

        new TextareaView
          model: @model
          modelAttribute: 'primaryPurpose'
          label: 'Primary purpose'
          rows: 3
          helpText: """
                    <p>Short phrase to describe primary aim of model</p>
                    """

        new InputView
          model: @model
          modelAttribute: 'modelType'
          label: 'Model type'
          listAttribute: """
                    <option value='Unknown' />
                    <option value='Deterministic' />
                    <option value='Stochastic' />
                    """
          helpText: """
                    Type which best fits the model<
                    """

        new InputView
          model: @model
          modelAttribute: 'currentModelVersion'
          label: 'Current model version'
          placeholderAttribute: 'e.g. 2.5.10'
          helpText: """
                    Most recent release version (if applicable)
                    """

        new InputView
          model: @model
          modelAttribute: 'releaseDate'
          typeAttribute: 'date'
          label: 'Release date'
          placeholderAttribute: 'yyyy-mm-dd'
          helpText: """
                    Date of release of current model version (if applicable)
                    """

        new PredefinedParentView
          model: @model
          ModelType: Contact
          modelAttribute: 'responsibleParties'
          label: 'Contacts'
          ObjectInputView: ContactView
          multiline: true
          predefined:
            'SRO - CEH':
              organisationName: 'UK Centre for Ecology & Hydrology'
              role: 'owner'
              organisationIdentifier: 'https://ror.org/00pggkr55'
          helpText: """
                    You <b>must</b> include one Senior Responsible Officer (SRO) - the person who is the "owner" and primary contact for the model
                    """

        new ParentView
          model: @model
          modelAttribute: 'keywords'
          label: 'Keywords'
          ObjectInputView: KeywordView
          helpText: """
                    Keywords or phrases to help model discovery
                    """
        
        new ParentView
          model: @model
          modelAttribute: 'onlineResources'
          label: 'Online resources'
          ObjectInputView: OnlineLinkView
          multiline: true
          listAttribute: """
                    <option value='code'>Location of the model code such as GitHub repository</option>
                    <option value='documentation'>Online documentation describing how to use the model</option>
                    <option value='website'/>
                    <option value='browseGraphic'>Image to display on metadata record</option>
                    """
          helpText: """
                    <p>Websites and online resources to access and further descibe the model</p>
                    <p>You should include the location of the model code repository e.g. https://github.com/NERC-CEH/...</p>
                    <p><b>If your model is not currently under version control and you are unsure about how to achieve this please talk to your Informatics Liaison representative.</b></p>
                    """

        new InputView
          model: @model
          modelAttribute: 'licenseType'
          label: 'License type'
          listAttribute: """
                    <option value='unknown' />
                    <option value='open' />
                    <option value='non-open' />
                    """
      ]
    ,
      label: 'Input variables'
      title: 'Input variables'
      views: [
        new PredefinedParentView
          model: @model
          ModelType: DataTypeSchema
          modelAttribute: 'inputVariables'
          multiline: true
          label: 'Input variables'
          ObjectInputView: DataTypeSchemaSimpleView
          predefined:
            'Boolean (true/false)':
              type: 'boolean'
            'Date':
              type: 'date'
            'Date & time':
              type: 'datetime'
            'Decimal number':
              type: 'number'
            'Geographic point':
              type: 'geopoint'
            'Integer':
              type: 'integer'
            'Text':
              type: 'string'
            'Time':
              type: 'time'
              format: 'hh:mm:ss'
            'URI':
              type: 'string'
              format: 'uri'
            'UUID':
              type: 'string'
              format: 'uuid'
      ]
    ,
      label: 'Output variables'
      title: 'Output variables'
      views: [
        new PredefinedParentView
          model: @model
          ModelType: DataTypeSchema
          modelAttribute: 'outputVariables'
          multiline: true
          label: 'Output variables'
          ObjectInputView: DataTypeSchemaSimpleView
          predefined:
            'Boolean (true/false)':
              type: 'boolean'
            'Date':
              type: 'date'
            'Date & time':
              type: 'datetime'
            'Decimal number':
              type: 'number'
            'Geographic point':
              type: 'geopoint'
            'Integer':
              type: 'integer'
            'Text':
              type: 'string'
            'Time':
              type: 'time'
              format: 'hh:mm:ss'
            'URI':
              type: 'string'
              format: 'uri'
            'UUID':
              type: 'string'
              format: 'uuid'
      ]
    ,
      label: 'Spatio-temporal'
      title: 'Spatio-temporal details'
      views: [
        new PredefinedParentView
          model: @model
          modelAttribute: 'boundingBoxes'
          ModelType: BoundingBox
          label: 'Spatial extent'
          ObjectInputView: BoundingBoxView
          multiline: true
          predefined:
            'England':
              northBoundLatitude: 55.812
              eastBoundLongitude: 1.768
              southBoundLatitude: 49.864
              westBoundLongitude: -6.452
              extentName: 'England'
              extentUri: 'http://sws.geonames.org/6269131'
            'Great Britain':
              northBoundLatitude: 60.861
              eastBoundLongitude: 1.768
              southBoundLatitude: 49.864
              westBoundLongitude: -8.648
              extentName: 'Great Britain'
            'Northern Ireland':
              northBoundLatitude: 55.313
              eastBoundLongitude: -5.432
              southBoundLatitude: 54.022
              westBoundLongitude: -8.178
              extentName: 'Northern Ireland'
              extentUri: 'http://sws.geonames.org/2641364'
            Scotland:
              northBoundLatitude: 60.861
              eastBoundLongitude: -0.728
              southBoundLatitude: 54.634
              westBoundLongitude: -8.648
              extentName: 'Scotland'
              extentUri: 'http://sws.geonames.org/2638360'
            'United Kingdom':
              northBoundLatitude: 60.861
              eastBoundLongitude: 1.768
              southBoundLatitude: 49.864
              westBoundLongitude: -8.648
              extentName: 'United Kingdom'
              extentUri: 'http://sws.geonames.org/2635167'
            Wales:
              northBoundLatitude: 53.434
              eastBoundLongitude: -2.654
              southBoundLatitude: 51.375
              westBoundLongitude: -5.473
              extentName: 'Wales'
              extentUri: 'http://sws.geonames.org/2634895'
            World:
              northBoundLatitude: 90.00
              eastBoundLongitude: 180.00
              southBoundLatitude: -90.00
              westBoundLongitude: -180.00
          helpText: """
                    <p>A bounding box representing the limits of the data resource's study area.</p>
                    <p>If you do not wish to reveal the exact location publicly (for example, if locations are sensitive) it is recommended that you generalise the location.</p>
                    """

        new InputView
          model: @model
          modelAttribute: 'spatialDomain'
          label: 'Spatial domain'
          placeholderAttribute: 'e.g. Parameterised for UK only or global'
          listAttribute: """
                    <option value='UK' />
                    <option value='Global' />
                    """
          helpText: """
                    Is the model only applicable to certain areas?
                    """

        new InputView
          model: @model
          modelAttribute: 'spatialResolution'
          label: 'Spatial resolution'
          placeholderAttribute: 'e.g. 1km2 or 5m2;'
          helpText: """
                    Spatial resolution at which model works or at which model outputs are generated (if applicable)
                    """

        new InputView
          model: @model
          modelAttribute: 'temporalResolutionMin'
          label: 'Minimimum temporal resolution'
          placeholderAttribute: 'e.g. 1 second or 10 days'
          helpText: """
                    Minimum time step supported by the model (if applicable)
                    """

        new InputView
          model: @model
          modelAttribute: 'temporalResolutionMax'
          label: 'Maximum temporal resolution'
          placeholderAttribute: 'e.g. annual or decadal '
          helpText: """
                    Maximum time step supported by the model (if applicable)
                    """

      ]
    ,
      label: 'Technical info'
      title: 'Technical info'
      views: [
        new TextareaView
          model: @model
          modelAttribute: 'modelCalibration'
          label: 'Model calibration'
          rows: 7
          helpText: """
                    Does the model need calibration before running? If so, what needs to be supplied to do this? (if applicable)
                    """

        new InputView
          model: @model
          modelAttribute: 'language'
          label: 'Language'
          placeholderAttribute: 'e.g. Python 2.7, C++, R 3.6'
          helpText: """
                    Language in which the model is written.  You should include the release number if relevant
                    """

        new InputView
          model: @model
          modelAttribute: 'compiler'
          label: 'Compiler'
          placeholderAttribute: 'e.g. C++ compiler'
          helpText: """
                    Compiler required (if applicable)
                    """

        new InputView
          model: @model
          modelAttribute: 'operatingSystem'
          label: 'Operating system'
          helpText: """
                    Operating system typically used to run the model
                    """

         new InputView
          model: @model
          modelAttribute: 'systemMemory'
          label: 'System memory'
          helpText: """
                    Memory required to run code (if known)
                    """
      ]
    ,
      label: 'Quality'
      title: 'Quality assurance'
      views: [
        new SingleObjectView
          model: @model
          modelAttribute: 'developerTesting'
          label: 'Developer testing'
          ObjectInputView: QaView
          helpText: """
                    Use of a range of developer tools including parallel build and analytical review or sense check
                    """

        new SingleObjectView
          model: @model
          modelAttribute: 'internalPeerReview'
          label: 'Internal peer review'
          ObjectInputView: QaView
          helpText: """
                    Obtaining a critical evaluation from a third party independent of the development of the model but from within the same organisation
                    """

        new SingleObjectView
          model: @model
          modelAttribute: 'externalPeerReview'
          label: 'External peer review'
          ObjectInputView: QaView
          helpText: """
                    Formal or informal engagement of a third party to conduct critical evaluation from outside the organisation in which the model is being developed
                    """

        new SingleObjectView
          model: @model
          modelAttribute: 'internalModelAudit'
          label: 'Internal model audit'
          ObjectInputView: QaView
          helpText: """
                    Formal audit of a model within the organisation, perhaps involving use of internal audit functions
                    """

        new SingleObjectView
          model: @model
          modelAttribute: 'externalModelAudit'
          label: 'External model audit'
          ObjectInputView: QaView
          helpText: """
                    Formal engagement of external professional to conduct a critical evaluation of the model, perhaps involving audit professionals
                    """

        new SingleObjectView
          model: @model
          modelAttribute: 'qaGuidelinesAndChecklists'
          label: 'Quality assurance guidelines & checklists'
          ObjectInputView: QaView
          helpText: """
                    Model development refers to department’s guidance or other documented QA processes (e.g. third party publications)
                    """

        new SingleObjectView
          model: @model
          modelAttribute: 'governance'
          label: 'Governance'
          ObjectInputView: QaView
          helpText: """
                    At least one of planning, design and/or sign-off of model for use is referred to a more senior person.  There is a clear line of accountability for the model
                    """

        new SingleObjectView
          model: @model
          modelAttribute: 'transparency'
          label: 'Transparency'
          ObjectInputView: QaView
          helpText: """
                    Model is placed in the wider domain for scrutiny, and/or results are published
                    """

        new SingleObjectView
          model: @model
          modelAttribute: 'periodicReview'
          label: 'Periodic review'
          ObjectInputView: QaView
          helpText: """
                    Model is reviewed at intervals to ensure it remains fit for the intended purpose, if used on an ongoing basis
                    """
      ]
    ,
      label: 'References'
      title: 'References'
      views: [
        new ParentView
          model: @model
          ModelType: Supplemental
          modelAttribute: 'supplemental'
          label: 'References'
          ObjectInputView: SupplementalView
          multiline: true
      ]
    ,
      label: 'Version control'
      title: 'Version control history'
      views: [
        new ParentView
          model: @model
          modelAttribute: 'versionHistories'
          label: 'Version control change notes'
          ObjectInputView: VersionHistoryView
          multiline: true
      ]
    ,
      label: 'Project use'
      title: 'Project use'
      views: [
        new ParentView
          model: @model
          modelAttribute: 'projectUsages'
          label: 'Project use'
          ObjectInputView: ProjectUsageView
      ]
    ]

    EditorView.prototype.initialize.apply @