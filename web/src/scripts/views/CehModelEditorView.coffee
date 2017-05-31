define [
  'cs!views/EditorView'
  'cs!views/editor/InputView'
  'cs!views/editor/TextareaView'
  'cs!views/editor/ParentView'
  'cs!views/editor/ParentStringView'
  'cs!views/editor/KeywordView'
  'cs!views/editor/SelectView'
  'cs!views/editor/ReferenceView'
], (
  EditorView,
  InputView,
  TextareaView,
  ParentView,
  ParentStringView,
  KeywordView,
  SelectView,
  ReferenceView
) -> EditorView.extend

  initialize: ->
    @sections = [
      label: 'Basic Info'
      title: 'Basic Info'
      views: [

        new InputView
          model: @model
          modelAttribute: 'title'
          label: 'Title'
          helpText: """
                    <p>Name of the model</p>
                    """

        new TextareaView
          model: @model
          modelAttribute: 'primaryPurpose'
          label: 'Primary purpose'
          rows: 17
          helpText: """
                    <p>Short phrase to describe primary aim of model</p>
                    """

        new InputView
          model: @model
          modelAttribute: 'website'
          label: 'Website url'
          helpText: """
                    <p>Link to outward facing model website if one exists e.g. https://jules.jchmr.org/</p>
                    """

        new InputView
          model: @model
          modelAttribute: 'seniorResponsibleOfficer'
          label: 'Senior responsible officer'
          helpText: """
                    <p>Senior responsible officer for the model (this should be the person who is the primary contact for the model)</p>
                    """

        new InputView
          model: @model
          modelAttribute: 'seniorResponsibleOfficerEmail'
          label: 'Senior responsible officer email'
          helpText: """
                    <p>Email address of the Senior Responsible Officer e.g. someone@ceh.ac.uk</p>
                    """

        

        new ParentView
          model: @model
          modelAttribute: 'keywords'
          label: 'Keywords'
          ObjectInputView: KeywordView
          helpText: """
                    <p>5-10 keywords for model discovery e.g. rainfall; species distribution; nitrogen deposition; global circulation model</p>
                    """

        new SelectView
          model: @model
          modelAttribute: 'licenseType'
          label: 'License'
          options: [
            {value: 'unknown', label: 'Unknown'},
            {value: 'open', label: 'Open'},
            {value: 'non-open', label: 'Non-open'}
          ]
          helpText: """
                    <p>License type (open or non-open) under which the model is distributed</p>
                    """

        new InputView
          model: @model
          modelAttribute: 'codeRepositoryUrl'
          label: 'Code repository url'
          helpText: """
                    <p>Location of code repository e.g. https://github.com/NERC-CEH/Spatial-Upscaling-model</p>
                    """

      ]
    ,
      label: 'References'
      title: 'References'
      views: [
        new ParentView
          model: @model
          modelAttribute: 'references'
          label: 'Refrences'
          ObjectInputView: ReferenceView
          multiline: true
          helpText: """
                    <p>Citation - Add publication citation here</p>
                    <p>DOI - DOI link for the citation e.g. http://dx.doi.org/10.1179/2042349715Y.0000000010</p>
                    <p>NORA - NORA links of the citation e.g. http://nora.nerc.ac.uk/513147/</p>
                    """
      ]
    ,
      label: 'Model Description'
      title: 'Model Description'
      views: [
        new ParentStringView
          model: @model
          modelAttribute: 'keyInputVariables'
          label: 'Key input variables'
          helpText: """
                    <p>Short phrases to describe basic types of model inputs e.g. rainfall; temperature; land use; atmospheric deposition</p>
                    """

        new ParentStringView
          model: @model
          modelAttribute: 'keyOutputVariables'
          label: 'Key output variables'
          helpText: """
                    <p>Short phrases to describe basic types of model outputs e.g. nutrient runoff; deposition time series; species occurrence</p>
                    """

        new TextareaView
          model: @model
          modelAttribute: 'description'
          label: 'Model description'
          rows: 17
          helpText: """
                    <p>Longer description of model e.g. development history, use to answer science questions, overview of structure</p>
                    """

        new SelectView
          model: @model
          modelAttribute: 'modelType'
          label: 'Model type'
          options: [
            {value: 'unknown', label: 'Unknown'},
            {value: 'deterministic', label: 'Deterministic'},
            {value: 'stochastic', label: 'Stochastic'},
            {value: 'other', label: 'Other'}
          ]
          helpText: """
                    <p>Type which best fits model</p>
                    """

        new InputView
          model: @model
          modelAttribute: 'currentModelVersion'
          label: 'Current model version'
          helpText: """
                    <p>Most recent release version (if applicable) e.g. v2.5.10</p>
                    """

        new InputView
          model: @model
          modelAttribute: 'releaseDate'
          label: 'Release date'
          helpText: """
                    <p>Date of release of current model version (if applicable) e.g. 2012-02-17</p>
                    """

        new TextareaView
          model: @model
          modelAttribute: 'modelCalibration'
          label: 'Model calibration'
          rows: 17
          helpText: """
                    <p>Does the model need calibration before running? If so, what needs to be supplied to do this? (if applicable)</p>
                    """
      ]
    ,
      label: 'Spatio-Temporal Info'
      title: 'Spatio-Temporal Info'
      views: [
        new InputView
          model: @model
          modelAttribute: 'spatialDomain'
          label: 'Spatial domain'
          helpText: """
                    <p>Is the model only applicable to certain areas? E.g. Parameterised for UK only or global (if applicable)</p>
                    """

        new InputView
          model: @model
          modelAttribute: 'spatialResolution'
          label: 'Spatial resolution'
          helpText: """
                    <p>Spatial resolution at which model works or at which model outputs are generated e.g. 1km&sup2;  or 5m&sup2; (if applicable)</p>
                    """

        new InputView
          model: @model
          modelAttribute: 'temporalResolutionMin'
          label: 'Temporal resolution (min)'
          helpText: """
                    <p>Minimum time step supported by the model e.g. 1 second or 10 days (if applicable)</p>
                    """

        new InputView
          model: @model
          modelAttribute: 'temporalResolutionMax'
          label: 'Temporal resolution (max)'
          helpText: """
                    <p>Maximum time step supported by the model e.g. annual or decadal (if applicable)</p>
                    """
      ]
    ,
      label: 'Technical Info'
      title: 'Technical Info'
      views: [
        new InputView
          model: @model
          modelAttribute: 'language'
          label: 'Language'
          helpText: """
                    <p>Computing language in which the model is written e.g. C++; R</p>
                    """

        new InputView
          model: @model
          modelAttribute: 'compiler'
          label: 'Compiler'
          helpText: """
                    <p>Compiled required e.g. C++ compiler (if applicable)</p>
                    """

        new InputView
          model: @model
          modelAttribute: 'operatingSystem'
          label: 'Operating system'
          helpText: """
                    <p>Operating system typically used to run the model</p>
                    """

         new InputView
          model: @model
          modelAttribute: 'systemMemory'
          label: 'System memory'
          helpText: """
                    <p>Memory required to run code (if known)</p>
                    """

        new InputView
          model: @model
          modelAttribute: 'documentation'
          label: 'Documentation url'
          helpText: """
                    <p>Memory required to run code (if known)</p>
                    """
      ]
    ,
      label: 'QA Info'
      title: 'QA Info'
      views: [
      ]
    ,
      label: 'Version Control History'
      title: 'Version Control History'
      views: [
      ]
    ,
      label: 'Project Usage'
      title: 'Project Usage'
      views: [
      ]
    ]

    EditorView.prototype.initialize.apply @