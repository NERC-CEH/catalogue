define [
  'cs!views/EditorView'
  'cs!views/editor/InputView'
  'cs!views/editor/TextareaView'
  'cs!views/editor/ParentView'
  'cs!views/editor/PredefinedParentView'
  'cs!views/editor/ParentStringView'
  'cs!views/editor/KeywordView'
  'cs!views/editor/SupplementalView'
  'cs!models/editor/Supplemental'
  'cs!views/editor/SingleObjectView'
  'cs!views/editor/DataInfoView'
  'cs!views/editor/ModelInfoView'
  'cs!views/editor/ContactView'
  'cs!models/editor/Contact'
  'cs!models/editor/BoundingBox'
  'cs!views/editor/BoundingBoxView'


], (
  EditorView,
  InputView,
  TextareaView,
  ParentView,
  PredefinedParentView,
  ParentStringView,
  KeywordView,
  SupplementalView,
  Supplemental,
  SingleObjectView,
  DataInfoView,
  ModelInfoView,
  ContactView,
  Contact,
  BoundingBox,
  BoundingBoxView
) -> EditorView.extend

  initialize: ->

    @model.set('type', 'modelApplication') unless @model.has('type')

    @sections = [
      label: 'Project Info'
      title: 'Project Info'
      views: [

        new InputView
          model: @model
          modelAttribute: 'title'
          label: 'Title'
          helpText: """
                    <p>Title of model application/project</p>
                    """

        new InputView
          model: @model
          modelAttribute: 'projectCode'
          label: 'Project code'

        new TextareaView
          model: @model
          modelAttribute: 'projectObjectives'
          label: 'Objectives'
          rows: 5

        new TextareaView
          model: @model
          modelAttribute: 'description'
          label: 'Description'
          rows: 10
          helpText: """
                    <p>Longer description of project including why models were used to answer the science question, assumptions made, key outputs, etc.</p>
                    """

        new ParentView
          model: @model
          modelAttribute: 'keywords'
          label: 'Keywords'
          ObjectInputView: KeywordView
          helpText: """
                    <p>5-10 keywords to enable searching for the project</p>
                    """

        new InputView
          model: @model
          modelAttribute: 'projectCompletionDate'
          typeAttribute: 'date'
          label: 'Project completion date'
          placeholderAttribute: 'yyyy-mm-dd'


        new InputView
          model: @model
          modelAttribute: 'projectWebsite'
          label: 'Project website'
          helpText: """
                    <p>Public-facing website if available e.g. http://www.ceh.ac.uk/our-science/projects/upscape</p>
                    """

        new TextareaView
          model: @model
          modelAttribute: 'funderDetails'
          label: 'Funder details'
          rows: 3
          helpText: """
                    <p>Funder details, including grant number if appropriate</p>
                    """

        new ParentView
          model: @model
          ModelType: Contact
          modelAttribute: 'responsibleParties'
          label: 'Contacts'
          ObjectInputView: ContactView
          multiline: true
          helpText: """
                    <p>Contact details for the PI/project representative</p>
                    """

        new TextareaView
          model: @model
          modelAttribute: 'multipleModelsUsed'
          label: 'Multiple models used?'
          rows: 4
          helpText: """
                    <p>Were multiple models used in the project? If so, specify which ones?</p>
                    """

        new TextareaView
          model: @model
          modelAttribute: 'multipleModelLinkages'
          label: 'Multiple model linkages'
          rows: 4
          helpText: """
                    <p>If multiple models were used how was this done e.g. chained, independent runs, comparisons, ensemble</p>
                    """

      ]
    ,
      label: 'Model Info'
      title: 'Model Info'
      views: [
        new ParentView
          model: @model
          modelAttribute: 'modelInfos'
          label: 'Model info'
          ObjectInputView: ModelInfoView
          multiline: true
          helpText: """
                    <p>Models used in the project</p>
                    <dl><dt>Name</dt><dd>Name of model (searches catalogue for matching models)</dd>
                    <dt>Version</dt><dd>Version of the model used for the implementation (not necessarily the current release version) e.g. v1.5.2 (if applicable)</dd>
                    <dt>Rationale</dt><dd>Why was this model chosen for use in this project?</dd>
                    <dt>Spatial extent of implementation</dt><dd>What spatial extent best describes the implementation?</dd>
                    <dt>Available spatial data</dt><dd>Can the application be described by either a shapefile/polygon or bounding box coordinates?</dd>
                    <dt>Spatial resolution of the implementation</dt><dd>Spatial resolution at which model outputs were generated e.g. 1km²; 5m² (if applicable)</dd>
                    <dt>Temporal extent of the implementation (start date)</dt><dd>Start date of implementation (if applicable)</dd>
                    <dt>Temporal extent of the implementation (end date)</dt><dd>End date of implementation (if applicable)</dd>
                    <dt>Temporal resolution of the implementation</dt><dd>Time step used in the model implementation e.g. 1s; annual (if applicable)</dd>
                    <dt>Calibration conditions</dt><dd>How was the model calibrated (if applicable)?</dd></dl>
                    """
                    
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

      ]
    ,
      label: 'Input data'
      title: 'Input data'
      views: [
        new ParentView
          model: @model
          modelAttribute: 'inputData'
          label: 'Input Data'
          ObjectInputView: DataInfoView
          multiline: true
          helpText: """
                    <p>Detailed description of input data including: variable name, units, file format, URL to data catalogue record for each input</p>
                    """
      ]
    ,
      label: 'Output data'
      title: 'Output data'
      views: [
        new ParentView
          model: @model
          modelAttribute: 'outputData'
          label: 'Output Data'
          ObjectInputView: DataInfoView
          multiline: true
          helpText: """
                    <p>Detailed description of model outputs including: variable name, units, file format, URL to data catalogue record for each output (or alternative location of model outputs from this application)</p>
                    """
      ]
    ,
      label: 'Quality'
      title: 'Quality assurance'
      views: [
        new TextareaView
          model: @model
          modelAttribute: 'sensitivityAnalysis'
          label: 'Sensitivity analysis'
          rows: 5

        new TextareaView
          model: @model
          modelAttribute: 'uncertaintyAnalysis'
          label: 'Uncertainty analysis'
          rows: 5

        new TextareaView
          model: @model
          modelAttribute: 'validation'
          label: 'Validation'
          rows: 5
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
          helpText: """
                    <p>Citation - Add publication citations here</p>
                    <p>DOI - DOI link for the citation e.g. https://doi.org/10.1111/journal-id.1882</p>
                    <p>NORA - NORA links of the citation e.g. http://nora.nerc.ac.uk/513147/</p>
                    """
      ]

    ]

    EditorView.prototype.initialize.apply @