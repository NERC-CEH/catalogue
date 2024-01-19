import {
    ContactView,
    DataTypeSchemaSimpleView, FundingView, KeywordVocabularyView,
    ModelQAView,
    AdditionalInfoView,
    ModelResolutionView, OnlineLinkView,
    ParentView,
    PredefinedParentView,
    SupplementalView, TextareaView
} from '../views'
import { Contact, DataTypeSchema, Funding } from '../models'
import EditorView from '../EditorView'
import InputView from '../InputView'
import BoundingBox from '../geometryMap/BoundingBox'
import BoundingBoxView from '../geometryMap/BoundingBoxView'

export default EditorView.extend({

    initialize () {
        if (!this.model.has('type')) { this.model.set('type', 'nercModel') }

        this.sections = [{
            label: 'Basic info',
            title: 'Basic info',
            views: [

                new InputView({
                    model: this.model,
                    modelAttribute: 'title',
                    label: 'Title'
                }),

                new TextareaView({
                    model: this.model,
                    modelAttribute: 'description',
                    label: 'Description',
                    rows: 6,
                    helpText: `
<p>Longer description of model e.g. development history, use to answer science questions, overview of structure</p>
`
                }),

                new TextareaView({
                    model: this.model,
                    modelAttribute: 'purpose',
                    label: 'Purpose',
                    rows: 6
                }),

                new InputView({
                    model: this.model,
                    modelAttribute: 'modelType',
                    label: 'Model type',
                    listAttribute: `\
<option value='Deterministic' />
<option value='Stochastic' />\
`
                }),

                new InputView({
                    model: this.model,
                    modelAttribute: 'version',
                    label: 'Version',
                    placeholderAttribute: 'e.g. 2.5.10'
                }),

                new InputView({
                    model: this.model,
                    modelAttribute: 'releaseDate',
                    typeAttribute: 'text',
                    label: 'Release date'
                }),

                new PredefinedParentView({
                    model: this.model,
                    ModelType: Contact,
                    modelAttribute: 'responsibleParties',
                    label: 'Contacts',
                    ObjectInputView: ContactView,
                    multiline: true,
                    predefined: {
                        BAS: {
                            role: 'pointOfContact',
                            organisationName: 'British Antarctic Survey',
                            email: 'information@bas.ac.uk',
                            organisationIdentifier: 'https://ror.org/01rhff309'
                        },
                        BGS: {
                            role: 'pointOfContact',
                            organisationName: 'British Geological Survey',
                            email: 'enquiries@bgs.ac.uk',
                            organisationIdentifier: 'https://ror.org/04a7gbp98'
                        },
                        CEDA: {
                            role: 'pointOfContact',
                            organisationName: 'Centre for Environmental Data Analysis',
                            organisationIdentifier: 'https://ror.org/04j4kad11'
                        },
                        NOC: {
                            role: 'pointOfContact',
                            organisationName: 'National Oceanography Centre',
                            organisationIdentifier: 'https://ror.org/00874hx02'
                        },
                        UKCEH: {
                            role: 'pointOfContact',
                            organisationName: 'UK Centre for Ecology & Hydrology',
                            email: 'enquiries@ceh.ac.uk',
                            organisationIdentifier: 'https://ror.org/00pggkr55'
                        }
                    }
                }),

                new ParentView({
                    model: this.model,
                    modelAttribute: 'keywords',
                    label: 'Keywords',
                    ObjectInputView: KeywordVocabularyView,
                    helpText: `
<p>Keywords help with model discovery</p>
`
                }),

                new PredefinedParentView({
                    model: this.model,
                    modelAttribute: 'funding',
                    ModelType: Funding,
                    multiline: true,
                    label: 'Funding',
                    ObjectInputView: FundingView,
                    predefined: {
                        BBSRC: {
                            funderName: 'Biotechnology and Biological Sciences Research Council',
                            funderIdentifier: 'https://ror.org/00cwqg982'
                        },
                        Defra: {
                            funderName: 'Department for Environment Food and Rural Affairs',
                            funderIdentifier: 'https://ror.org/00tnppw48'
                        },
                        EPSRC: {
                            funderName: 'Engineering and Physical Sciences Research Council',
                            funderIdentifier: 'https://ror.org/0439y7842'
                        },
                        ESRC: {
                            funderName: 'Economic and Social Research Council',
                            funderIdentifier: 'https://ror.org/03n0ht308'
                        },
                        'Innovate UK': {
                            funderName: 'Innovate UK',
                            funderIdentifier: 'https://ror.org/05ar5fy68'
                        },
                        MRC: {
                            funderName: 'Medical Research Council',
                            funderIdentifier: 'https://ror.org/03x94j517'
                        },
                        NERC: {
                            funderName: 'Natural Environment Research Council',
                            funderIdentifier: 'https://ror.org/02b5d8509'
                        },
                        STFC: {
                            funderName: 'Science and Technology Facilities Council',
                            funderIdentifier: 'https://ror.org/057g20z61'
                        }
                    },
                    helpText: `\
<p>Include here details of any grants or awards that were used to generate this resource.</p>
<p>If you include funding information, the Funding body is MANDATORY, other fields are useful but optional.</p>
<p>Award URL is either the unique identifier for the award or sa link to the funder's  grant page (if it exists). It is <b>NOT</b> a link to a project website.</p>\
`
                })
            ]
        },
        {
            label: 'Access',
            title: 'Access',
            views: [

                new ParentView({
                    model: this.model,
                    modelAttribute: 'onlineResources',
                    label: 'Online resources',
                    ObjectInputView: OnlineLinkView,
                    multiline: true,
                    listAttribute: `\
<option value='code'>Link to location of the model code (e.g. GitHub repository)</option>
<option value='documentation'>Link to documentation describing how to use the model</option>
<option value='website'/>
<option value='browseGraphic'>Image to display on metadata record</option>\
`,
                    helpText: `\
<p>Websites/online resources to access and further descibe the model</p>
<p>You should include the location of the model code repository e.g. https://github.com/...</p>\
`
                }),

                new InputView({
                    model: this.model,
                    modelAttribute: 'licenseType',
                    label: 'License',
                    listAttribute: `\
<option value='open' />
<option value='non-open' />\
`,
                    helpText: `
<p>License type (open or non-open) under which the model is distributed</p>
`
                })
            ]
        },
        {
            label: 'Technical',
            title: 'Technical information',
            views: [
                new TextareaView({
                    model: this.model,
                    modelAttribute: 'calibration',
                    label: 'Model calibration',
                    rows: 7,
                    helpText: `
<p>Does the model need calibration before running? If so, what needs to be supplied to do this? (if applicable)</p>
`
                }),

                new TextareaView({
                    model: this.model,
                    modelAttribute: 'configuration',
                    label: 'Model configuration',
                    rows: 7
                }),

                new InputView({
                    model: this.model,
                    modelAttribute: 'language',
                    label: 'Language',
                    placeholderAttribute: 'e.g. Python 2.7, C++, R 3.6',
                    helpText: `
<p>Language in which the model is written.  You should include the release number if relevant</p>
`
                }),

                new InputView({
                    model: this.model,
                    modelAttribute: 'compiler',
                    label: 'Compiler',
                    placeholderAttribute: 'e.g. C++ compiler',
                    helpText: `
<p>Compiler required (if applicable)</p>
`
                }),

                new InputView({
                    model: this.model,
                    modelAttribute: 'operatingSystem',
                    label: 'Operating system',
                    helpText: `
<p>Operating system typically used to run the model</p>
`
                }),

                new InputView({
                    model: this.model,
                    modelAttribute: 'systemMemory',
                    label: 'System memory',
                    helpText: `
<p>Memory required to run code (if known)</p>
`
                }),

                new ParentView({
                    model: this.model,
                    modelAttribute: 'additionalTechnicalInfo',
                    multiline: true,
                    label: 'Technical details not recorded elsewhere',
                    ObjectInputView: AdditionalInfoView
                })

            ]
        },
        {
            label: 'Scale',
            title: 'Spatial and temporal scale',
            views: [
                new PredefinedParentView({
                    model: this.model,
                    modelAttribute: 'boundingBoxes',
                    ModelType: BoundingBox,
                    label: 'Spatial extent',
                    ObjectInputView: BoundingBoxView,
                    multiline: true,
                    predefined: {
                        England: {
                            northBoundLatitude: 55.812,
                            eastBoundLongitude: 1.768,
                            southBoundLatitude: 49.864,
                            westBoundLongitude: -6.452,
                            extentName: 'England',
                            extentUri: 'http://sws.geonames.org/6269131'
                        },
                        'Great Britain': {
                            northBoundLatitude: 60.861,
                            eastBoundLongitude: 1.768,
                            southBoundLatitude: 49.864,
                            westBoundLongitude: -8.648,
                            extentName: 'Great Britain'
                        },
                        'Northern Ireland': {
                            northBoundLatitude: 55.313,
                            eastBoundLongitude: -5.432,
                            southBoundLatitude: 54.022,
                            westBoundLongitude: -8.178,
                            extentName: 'Northern Ireland',
                            extentUri: 'http://sws.geonames.org/2641364'
                        },
                        Scotland: {
                            northBoundLatitude: 60.861,
                            eastBoundLongitude: -0.728,
                            southBoundLatitude: 54.634,
                            westBoundLongitude: -8.648,
                            extentName: 'Scotland',
                            extentUri: 'http://sws.geonames.org/2638360'
                        },
                        'United Kingdom': {
                            northBoundLatitude: 60.861,
                            eastBoundLongitude: 1.768,
                            southBoundLatitude: 49.864,
                            westBoundLongitude: -8.648,
                            extentName: 'United Kingdom',
                            extentUri: 'http://sws.geonames.org/2635167'
                        },
                        Wales: {
                            northBoundLatitude: 53.434,
                            eastBoundLongitude: -2.654,
                            southBoundLatitude: 51.375,
                            westBoundLongitude: -5.473,
                            extentName: 'Wales',
                            extentUri: 'http://sws.geonames.org/2634895'
                        },
                        World: {
                            northBoundLatitude: 90.00,
                            eastBoundLongitude: 180.00,
                            southBoundLatitude: -90.00,
                            westBoundLongitude: -180.00
                        }
                    },
                    helpText: `\
        <p>A bounding box representing the limits of the data resource's study area.</p>
        <p>If you do not wish to reveal the exact location publicly (for example, if locations are sensitive) it is recommended that you generalise the location.</p>\
        `
                }),

                new InputView({
                    model: this.model,
                    modelAttribute: 'spatialDomain',
                    label: 'Spatial domain',
                    placeholderAttribute: 'e.g. Parameterised for UK only or global',
                    listAttribute: `\
        <option value='UK' />
        <option value='Global' />\
        `,
                    helpText: `
        <p>Is the model only applicable to certain areas?</p>
        `
                }),

                new ParentView({
                    model: this.model,
                    modelAttribute: 'resolution',
                    multiline: true,
                    label: 'Resolution',
                    ObjectInputView: ModelResolutionView
                })

            ]
        },
        {
            label: 'Quality',
            title: 'Quality',
            views: [
                new ParentView({
                    model: this.model,
                    modelAttribute: 'qa',
                    multiline: true,
                    label: 'Quality assurance',
                    ObjectInputView: ModelQAView
                })
            ]
        },
        {
            label: 'References',
            title: 'References',
            views: [
                new ParentView({
                    model: this.model,
                    modelAttribute: 'references',
                    multiline: true,
                    label: 'References',
                    ObjectInputView: SupplementalView,
                    helpText: `\
<p>You can add information not documented elsewhere here. This includes links to related papers, grey literature or websites.  For example:</p>
<ul><li>papers that cite this resource</li><li>papers/reports that provide relevant supporting information but which do not cite this resource</li><li>project websites</li></ul>
<p>When linking to published articles, please use DOIs whenever possible.</p>
<p><small class='text-danger'><i class='fa-solid fa-exclamation-triangle'> </i> NOTE: Some websites may be maintained for a limited period and may therefore soon become unavailable.</small></p>\
`
                })
            ]
        },
        {
            label: 'Input parameters',
            title: 'Input parameters',
            views: [
                new PredefinedParentView({
                    model: this.model,
                    ModelType: DataTypeSchema,
                    modelAttribute: 'inputParameters',
                    multiline: true,
                    label: 'Input parameters',
                    ObjectInputView: DataTypeSchemaSimpleView,
                    predefined: {
                        'Boolean (true/false)': {
                            type: 'boolean'
                        },
                        Date: {
                            type: 'date'
                        },
                        'Date & time': {
                            type: 'datetime'
                        },
                        'Decimal number': {
                            type: 'number'
                        },
                        'Geographic point': {
                            type: 'geopoint'
                        },
                        Integer: {
                            type: 'integer'
                        },
                        Text: {
                            type: 'string'
                        },
                        Time: {
                            type: 'time',
                            format: 'hh:mm:ss'
                        },
                        URI: {
                            type: 'string',
                            format: 'uri'
                        },
                        UUID: {
                            type: 'string',
                            format: 'uuid'
                        }
                    }
                })
            ]
        },
        {
            label: 'Output parameters',
            title: 'Output parameters',
            views: [
                new PredefinedParentView({
                    model: this.model,
                    ModelType: DataTypeSchema,
                    modelAttribute: 'outputParameters',
                    multiline: true,
                    label: 'Output parameters',
                    ObjectInputView: DataTypeSchemaSimpleView,
                    predefined: {
                        'Boolean (true/false)': {
                            type: 'boolean'
                        },
                        Date: {
                            type: 'date'
                        },
                        'Date & time': {
                            type: 'datetime'
                        },
                        'Decimal number': {
                            type: 'number'
                        },
                        'Geographic point': {
                            type: 'geopoint'
                        },
                        Integer: {
                            type: 'integer'
                        },
                        Text: {
                            type: 'string'
                        },
                        Time: {
                            type: 'time',
                            format: 'hh:mm:ss'
                        },
                        URI: {
                            type: 'string',
                            format: 'uri'
                        },
                        UUID: {
                            type: 'string',
                            format: 'uuid'
                        }
                    }
                })
            ]
        },
        {
            label: 'Additional info',
            title: 'Additional info',
            views: [
                new ParentView({
                    model: this.model,
                    modelAttribute: 'additionalInfo',
                    multiline: true,
                    label: 'Any other information not recorded elsewhere',
                    ObjectInputView: AdditionalInfoView
                })
            ]
        }

        ]

        return EditorView.prototype.initialize.apply(this)
    }
})
