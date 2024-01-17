import EditorView from '../EditorView'
import InputView from '../InputView'
import { Reference } from '../models'
import { TextareaView, ParentView, KeywordView, ReferenceView, DataInfoView, ModelInfoView } from '../views'

export default EditorView.extend({

    initialize () {
        if (!this.model.has('type')) { this.model.set('type', 'modelApplication') }

        this.sections = [{
            label: 'Project Info',
            title: 'Project Info',
            views: [

                new InputView({
                    model: this.model,
                    modelAttribute: 'title',
                    label: 'Project title',
                    helpText: `
<p>Title of project</p>
`
                }),

                new InputView({
                    model: this.model,
                    modelAttribute: 'projectCode',
                    label: 'Project code',
                    helpText: `
<p>RMS project code</p>
`
                }),

                new TextareaView({
                    model: this.model,
                    modelAttribute: 'projectObjectives',
                    label: 'Project objectives',
                    rows: 17,
                    helpText: `
<p>Brief description of the main objectives</p>
`
                }),

                new TextareaView({
                    model: this.model,
                    modelAttribute: 'description',
                    label: 'Project description',
                    rows: 17,
                    helpText: `
<p>Longer description of project incl. why models were used to answer the science question, assumptions made, key outputs</p>
`
                }),

                new ParentView({
                    model: this.model,
                    modelAttribute: 'keywords',
                    label: 'Keywords',
                    ObjectInputView: KeywordView,
                    helpText: `
<p>5-10 keywords to enable searching for the project</p>
`
                }),

                new InputView({
                    model: this.model,
                    modelAttribute: 'projectCompletionDate',
                    label: 'Project completion date',
                    helpText: `
<p>Project end date</p>
`
                }),

                new InputView({
                    model: this.model,
                    modelAttribute: 'projectWebsite',
                    label: 'Project website',
                    helpText: `
<p>Public-facing website if available e.g. http://www.ceh.ac.uk/our-science/projects/upscape</p>
`
                }),

                new TextareaView({
                    model: this.model,
                    modelAttribute: 'funderDetails',
                    label: 'Funder details',
                    rows: 3,
                    helpText: `
<p>Funder details, including grant number if appropriate</p>
`
                }),

                new InputView({
                    model: this.model,
                    modelAttribute: 'contactName',
                    label: 'Contact name',
                    helpText: `
<p>Name of UKCEH PI/project representative</p>
`
                }),

                new InputView({
                    model: this.model,
                    modelAttribute: 'contactEmail',
                    label: 'Contact email',
                    helpText: `
<p>Email of UKCEH PI/project representative e.g. someone@ceh.ac.uk</p>
`
                }),

                new TextareaView({
                    model: this.model,
                    modelAttribute: 'multipleModelsUsed',
                    label: 'Multiple models used?',
                    rows: 7,
                    helpText: `
<p>Were multiple models used in the project? If so, which ones?</p>
`
                }),

                new TextareaView({
                    model: this.model,
                    modelAttribute: 'multipleModelLinkages',
                    label: 'Multiple model linkages',
                    rows: 7,
                    helpText: `
<p>If multiple models were used how was this done e.g. chained, independent runs, comparisons, ensemble</p>
`
                })

            ]
        },
        {
            label: 'References',
            title: 'References',
            views: [
                new ParentView({
                    model: this.model,
                    ModelType: Reference,
                    modelAttribute: 'references',
                    label: 'References',
                    ObjectInputView: ReferenceView,
                    multiline: true,
                    helpText: `\
<p>Citation - Add publication citations here</p>
<p>DOI - DOI link for the citation e.g. https://doi.org/10.1111/journal-id.1882</p>
<p>NORA - NORA links of the citation e.g. http://nora.nerc.ac.uk/513147/</p>\
`
                })
            ]
        },
        {
            label: 'Model Info',
            title: 'Model Info',
            views: [
                new ParentView({
                    model: this.model,
                    modelAttribute: 'modelInfos',
                    label: 'Model info',
                    ObjectInputView: ModelInfoView,
                    multiline: true,
                    helpText: `\
<p>Models used in the project</p>
<p>Name - Name of model (searches catalogue for matching models)
<p>Version - Version of the model used for the application (not necessarily the current release version) e.g. v1.5.2 (if applicable)</p>
<p>Rationale - Why was this model chosen for use in this project?</p>
<p>Spatial extent of application - What spatial extent best describes the application?</p>
<p>Available spatial data - Can the application be described by either a shapefile/polygon or bounding box coordinates?</p>
<p>Spatial resolution of application - Spatial resolution at which model outputs were generated e.g. 1km²; 5m² (if applicable)</p>
<p>Temporal extent of application (start date) - Start date of application (if applicable)</p>
<p>Temporal extent of application (end date) - End date of application (if applicable)</p>
<p>Temporal resolution of application - Time step used in the model application e.g. 1s; annual (if applicable)</p>
<p>Calibration conditions - How was the model calibrated (if applicable)?</p>\
`
                })
            ]
        },
        {
            label: 'Data Info',
            title: 'Data Info',
            views: [
                new ParentView({
                    model: this.model,
                    modelAttribute: 'inputData',
                    label: 'Input Data',
                    ObjectInputView: DataInfoView,
                    multiline: true,
                    helpText: `
<p>Detailed description of input data including: variable name, units, file format, URL to data catalogue record for each input</p>
`
                }),

                new ParentView({
                    model: this.model,
                    modelAttribute: 'outputData',
                    label: 'Output Data',
                    ObjectInputView: DataInfoView,
                    multiline: true,
                    helpText: `
<p>Detailed description of model outputs including: variable name, units, file format, URL to data catalogue record for each output (or alternative location of model outputs from this application)</p>
`
                })
            ]
        }
        ]

        return EditorView.prototype.initialize.apply(this)
    }
})
