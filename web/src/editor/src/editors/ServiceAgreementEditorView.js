import _ from 'underscore'
import Backbone from 'backbone'
import {
  AuthorView, CategoryView, EndUserLicenceView,
  FileView, FundingView, KeywordThemeView, KeywordVocabularyView,
  ParentView,
  PredefinedParentView, RightsHolderView,
  SingleObjectView, SupportingDocView,
  TextareaView,
  TextOnlyView, TopicCategoryView
} from '../views'
import { EditorView, InputView } from '../index'
import { Author, Funding, KeywordTheme, RightsHolder, SupportingDoc, TopicCategory } from '../models'
import { BoundingBox, BoundingBoxView } from '../geometryMap'

export default EditorView.extend({

  initialize () {
    this.sections = [{
      label: 'General',
      title: '',
      views: [
        new TextOnlyView({
          model: this.model,
          text: "<h1>EIDC service agreement</h1><p>For more information/guidance about this document see <a href='https://eidc.ac.uk/support/agreement' target='_blank' rel='noopener noreferrer'>https://eidc.ac.uk/deposit/agreement</a></p><p>* Fields indicated by <i class='fa fa-pencil'></i> are required for agreement.</p><p>If you have any questions or difficulties, please contact <a href=\"mailto:info@eidc.ac.uk\">info@eidc.ac.uk</a> quoting the Deposit Reference below  </p>"
        }),

        new InputView({
          model: this.model,
          modelAttribute: 'depositReference',
          label: 'Deposit Reference',
          required: true
        }),

        new InputView({
          model: this.model,
          modelAttribute: 'depositorName',
          label: 'Depositor Name',
          required: true
        }),

        new TextOnlyView({
          model: this.model,
          text: "<p>Please provide a current email address.</p>"
        }),

        new InputView({
          model: this.model,
          modelAttribute: 'depositorContactDetails',
          label: "Depositor's email",
          required: true
        }),

        new InputView({
          model: this.model,
          modelAttribute: 'eidcName',
          label: 'EIDC contact name',
          required: true
        }),

        new TextOnlyView({
          model: this.model,
          text: `\
<h3>Data identification and citation</h3>
<p><strong>PLEASE NOTE: once the dataset is published, the title and authors cannot be changed</strong><p>\
`
        }),

        new TextOnlyView({
          model: this.model,
          label: 'Title',
          text: `<p>Provide a brief title that best describes the data resource, <strong>not</strong> the project or activity from which the data were derived. Include references to the subject, spatial and temporal aspects of the data resource, if applicable. <a href='https://eidc.ac.uk/deposit/metadata/guidance' target='_blank' rel='noopener noreferrer' >Further guidance is available on our website</a>.</p>
`,
          required: true
        }),

        new InputView({
          model: this.model,
          modelAttribute: 'title'
        }),

        new TextOnlyView({
          model: this.model,
          label: 'Authors',
          text: `<p>List authors of the dataset below in the order in which they will appear in the citation.</p>
<p>Authors' names must be in the format <code>Surname, Initial(s)</code>. For example, <code>Smith, K.P.</code> <strong>not</strong> <code>Kim P. Smith</code></p>
<p>Authors' details will be published in a public data catalogue and held in EIDC systems.  UK law requires us to inform all individuals listed that they are being proposed as an author.  We therefore require a current, valid email address for all living authors.  Those without valid contact details are not eligible for authorship.  Please see our <a href='http://eidc.ceh.ac.uk/policies/privacy' target='_blank' rel='noopener noreferrer'>Privacy Notice</a> for further information</p>\
`,
          required: true
        }),

        new ParentView({
          model: this.model,
          ModelType: Author,
          modelAttribute: 'authors',
          ObjectInputView: AuthorView,
          multiline: true
        })
      ]
    },
      {
        label: 'The Data',
        title: 'The Data',
      views: [

        new TextOnlyView({
          model: this.model,
          label: 'Data retention',
          text: `<p>The EIDC's policy is to assign a DOI to all deposited data; such data will be kept in perpetuity.  If a DOI is not required or there are any other exceptions to this policy, please state them below.</p>
`
        }),

        new TextareaView({
          model: this.model,
          modelAttribute: 'policyExceptions',
          rows: 3
        }),

        new TextOnlyView({
          model: this.model,
          text: `<p>This is the total number of <strong>data</strong> files being provided</p>
`
        }),

        new InputView({
          model: this.model,
          modelAttribute: 'fileNumber',
          label: 'Number of files to be deposited',
          required: true
        }),

        new TextOnlyView({
          model: this.model,
          label: 'Files',
          text: `\
<p>List the files to be deposited below - filenames must not include any spaces or special characters other than hyphens or underscores.</p>
<p>If there are a too many files to list separately, you can specify a naming convention below instead.</p>\
`
        }),

        new PredefinedParentView({
          model: this.model,
          modelAttribute: 'files',
          ObjectInputView: FileView,
          predefined: {
            CSV: {
              format: 'csv'
            },
            NetCDF: {
              format: 'NetCDF'
            }
          },
          multiline: true
        }),

        new TextOnlyView({
          model: this.model,
          label: 'Naming convention',
          text: `\
<p>Please describe a convention, or multiple conventions, that cover any files to be deposited not described in the previous field. Indicate sizes (or a size range) for each file covered by the convention and also the total size of the files covered by the naming convention.</p>
<p>Example: 200 files CP_&lt;REGION&gt;_MInv_&lt;determinandLabel&gt;Stats_&lt;startDate-endDate&gt;.csv. Where &lt;REGION&gt; is the Region code: &lt;startDate-endDate&gt; correspond to date format YYYYMMDD-YYYYMMDD and &lt;determinandlLabel&gt; is the chemical determinand name. Size range: 2-2.5 MB Total size: 443.1 MB</p>\
`
        }),

        new TextareaView({
          model: this.model,
          modelAttribute: 'fileNamingConvention',
          rows: 5
        }),

        new TextOnlyView({
          model: this.model,
          text: `\
<p>Data Transfer</p>
<p>Deposits containing individual files larger than 2 GB or with a high total number of files (e.g. >2000) are not suitable for uploading via our catalogue tool, please indicate a cloud method of transfer.</p>
\
`
        }),

        new InputView({
          model: this.model,
          modelAttribute: 'transferMethod',
          label: 'Transfer Method',
          placeholderAttribute: 'Click to select an option or type in your preferred method',
          listAttribute: `
<option value='Upload via EIDC catalogue (preferred)' />
<option value='Cloud transfer e.g. via OneDrive' />
`,
          required: true
        }),

        new TextOnlyView({
          model: this.model,
          label: 'Data Category',
          text: `<p>If the data are wholly or partly funded by NERC, the data must be categorised as either <strong>Environmental Data</strong> or <strong>Information Product</strong>.</p><p>Environmental data are '<i>individual items or records ... obtained by measurement, observation or modelling of the natural world. This includes data generated through complex systems, such as ... models, including the model code used to produce the data.</i>' </p><p>Information Products are '<i>created by adding a level of intellectual input that refines or adds value to data through interpretation and/or combination with other data</i>'.</p>
`,
          required: true
        }),

        new SingleObjectView({
          model: this.model,
          modelAttribute: 'dataCategory',
          ObjectInputView: CategoryView
        })
      ]
    },
    {
      label: 'Supporting documentation',
      title: 'Supporting documentation',
      views: [

        new TextOnlyView({
          model: this.model,
          label: 'Supporting documents',
          text: `<p>Please provide the title and file extension of document(s), ensuring names contain no special characters with the exception of hyphens or underscores, you will provide to enable re-use of the data (see <a href="https://eidc.ac.uk/deposit/supportingDocumentation">https://eidc.ac.uk/deposit/supportingDocumentation</a>).</p>
<p>Describe the content of the documentation to be supplied. All mandatory elements must be provided, but not necessarily in a single document.</p>
`,
          required: true
        }),

        new ParentView({
          model: this.model,
          ModelType: SupportingDoc,
          modelAttribute: 'supportingDocs',
          ObjectInputView: SupportingDocView,
          multiline: true
        })

      ]
    },
    {
      label: 'Availability, access and licensing',
      title: 'Availability, access and licensing',
      views: [

        new TextOnlyView({
          model: this.model,
          label: 'End user licence',
          text: `
<p>Please state under which licence the data will be made available. the vast majority of NERC-funded data are provided under the Open Government Licence. We recommend that you seek guidance from your institution and/or funding agency as to the appropriate licence.</p>
`,
          required: true
        }),

        new SingleObjectView({
          model: this.model,
          modelAttribute: 'endUserLicence',
          ObjectInputView: EndUserLicenceView
        }),

        new TextOnlyView({
          model: this.model,
          text: `
<p>Intellectual property rights.</p>
<p>This is usually the organisation to which the grant is awarded and will be specified in the grant/funding conditions. If no other organisation is specified, IPR should default to the funder.</p>
`
        }),

        new ParentView({
          model: this.model,
          ModelType: RightsHolder,
          modelAttribute: 'ownersOfIpr',
          label: 'Owner of IPR',
          ObjectInputView: RightsHolderView,
          multiline: true,
          required: true
        }),

        new TextOnlyView({
          model: this.model,
          label: 'Availability',
          text: `\
<p>Depositors may request that access to the data be restricted for an agreed period (embargoed).  Embargoes and embargo periods may be subject to funder requirements. For NERC-funded research, a reasonable embargo period is considered to be a maximum of two years <strong>from the end of data collection.</strong></p>
<p>If an embargo is required, please specify below.</p>\
`
        }),

        new InputView({
          model: this.model,
          modelAttribute: 'availability',
          typeAttribute: 'date'
        }),

        new TextOnlyView({
          model: this.model,
          text: `\
<p>Please specify any additional constraints on use of the data resource required.</p>\
`
        }),

        new TextareaView({
          model: this.model,
          modelAttribute: 'useConstraints',
          label: 'Additional Use Constraints',
          rows: 3
        })
      ]
    },
    {
      label: 'Legislation & funding',
      title: 'Legislation & funding',
      views: [
        new TextOnlyView({
          model: this.model,
          label: 'Other policies/legislation',
          text: `<p>All environmental data deposited into the EIDC are subject to the requirements of the <a href='https://nerc.ukri.org/research/sites/environmental-data-service-eds/policy/' target='_blank' rel='noopener noreferrer'>NERC Data Policy.</a></p>
<p>By depositing data, you confirm that the data is compliant with the provisions of UK data protection laws.</p>
<p>Data and supporting documentation should not contain names, addresses or other personal information relating to 'identifiable natural persons'.  Discovery metadata (the catalogue record) may contain names and contact details of the authors of this data (<a href='https://eidc.ac.uk/policies/retentionPersonalData' target='_blank' rel='noopener noreferrer'>see our policy on retention and use of personal data</a>).</p>
<p>If other policies or legislation apply, please specify below.</p>\
`
        }),

        new TextareaView({
          model: this.model,
          modelAttribute: 'otherPoliciesOrLegislation',
          rows: 5
        }),

        new PredefinedParentView({
          model: this.model,
          modelAttribute: 'funding',
          ModelType: Funding,
          multiline: true,
          label: 'Grants/awards used to generate this resource',
          ObjectInputView: FundingView,
          predefined: {
            NERC: {
              funderName: 'Natural Environment Research Council',
              funderIdentifier: 'https://ror.org/02b5d8509'
            },
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
            STFC: {
              funderName: 'Science and Technology Facilities Council',
              funderIdentifier: 'https://ror.org/057g20z61'
            }
          }
        })
      ]
    },
    {
      label: 'Miscellaneous',
      title: 'Miscellaneous',
      views: [

        new TextOnlyView({
          model: this.model,
          label: 'Superseding existing data',
          text: `
<p>Please note, superseding is only available where errors have been discovered in a datasets or a dataset is being extended with new data and the replacement duplicates the previous version entirely.</p>
<p>e.g. Metadata ID of the dataset to be replaced: 2e3bec6e-1e62-42d5-a221-016d0ad447d9. Reason for superseding the dataset: incorrectly calibrated instrument Or Addition of two more years’ data.</p>
`
        }),

        new TextareaView({
          model: this.model,
          modelAttribute: 'supersededData',
          rows: 5
        }),

        new TextOnlyView({
          model: this.model,
          label: 'Related Data Holdings',
          text: `
<p>Please identify any related data resources already curated by, or being deposited with, the EIDC that you wish to link your data resource to, and the nature of the relationship.
Details of relationships we can accommodate are available at: <a href='eidc.ac.uk/metadata/relationships' target='_blank' rel='noopener noreferrer'>eidc.ac.uk/metadata/relationships</a></p>
<p>e.g. Related data resource: 2e3bec6e-1e62-42d5-a221-016d0ad447d9. Relationship: uses.</p>
`
        }),

        new TextareaView({
          model: this.model,
          modelAttribute: 'relatedDataHoldings',
          rows: 5
        }),

        new TextOnlyView({
          model: this.model,
          label: 'Other info',
          text: `
<p>If there is any other information you wish to provide, please include it below.</p>
`
        }),

        new TextareaView({
          model: this.model,
          modelAttribute: 'otherInfo',
          rows: 7
        })
      ]
    },
    {
      label: 'Keywords',
      title: 'Keywords',
      views: [
        new TextOnlyView({
          model: this.model,
          text: `<p>A list of words/phrases that will help users to find your data.</p>
<p>These may be discipline-specific but can also be geographical (e.g. 'hydrology', 'soil chemistry', 'Hampshire').</p>
<p>You can add multiple keywords under each category using the add button.</p></br>\
`
        }),
        new TextOnlyView({
          model: this.model,
          label: 'ISO 19115 topic categories',
          text: `<p>Please note these are very broad themes required by the ISO 19115 metadata standard and should not be confused with science topics. If its not clear which category your dataset fits into, please choose the closest or contact EIDC.</p>
<p>Multiple topic categories are allowed - please include all that are pertinent.  For example, "<i>Estimates of topsoil invertebrates</i>" = Biota <strong>and</strong> Environment <strong>and</strong> Geoscientific Information.</p>\
`,
          required: true
        }),
        new ParentView({
          model: this.model,
          ModelType: TopicCategory,
          modelAttribute: 'topicCategories',
          ObjectInputView: TopicCategoryView
        }),
        new TextOnlyView({
          model: this.model,
          label: 'Science topic',
          text: 'These are broad categories that are used to populate the catalogue\'s topic search filter. Try to include at least one ',
          required: true
        }),
        new ParentView({
          model: this.model,
          ModelType: KeywordTheme,
          modelAttribute: 'keywordsTheme',
          ObjectInputView: KeywordThemeView,
          multiline: false
        }),
        new TextOnlyView({
          model: this.model,
          label: 'Observed properties',
          text: 'Controlled keywords describing the observed properties/variables contained in this data resource.',
          required: true,
          className: 'hidden'
        }),
        new ParentView({
          model: this.model,
          modelAttribute: 'keywordsObservedProperty',
          ObjectInputView: KeywordVocabularyView,
          multiline: true,
          className: 'hidden'
        }),
        new TextOnlyView({
          model: this.model,
          label: 'Places',
          text: `<p>Keywords describing geographic places pertinent to this resource.</p>
<p>For example, named countries/regions in which the research was conducted.</p>\
<p>Please note our currently supported vocabularies do not include geographic locations, therefore please use the plain text keyword field.</p>\
`,
          required: true,
          className: 'hidden'
        }),
        new ParentView({
          model: this.model,
          modelAttribute: 'keywordsPlace',
          ObjectInputView: KeywordVocabularyView,
          multiline: true,
          className: 'hidden'
        }),
        new TextOnlyView({
          model: this.model,
          label: 'Projects',
          text: 'Controlled keywords describing projects that fund/support the creation of this resource.',
          required: true,
          className: 'hidden'
        }),
        new ParentView({
          model: this.model,
          modelAttribute: 'keywordsProject',
          ObjectInputView: KeywordVocabularyView,
          multiline: true,
          className: 'hidden'
        }),
        new TextOnlyView({
          model: this.model,
          label: 'Instruments',
          text: 'Controlled keywords describing instruments/sensors used to generate this data.',
          required: true,
          className: 'hidden'
        }),
        new ParentView({
          model: this.model,
          modelAttribute: 'keywordsInstrument',
          ObjectInputView: KeywordVocabularyView,
          multiline: true,
          className: 'hidden'
        }),
        new TextOnlyView({
          model: this.model,
          text: `<p>Custom keywords can be added either as plain text or as controlled terms from a vocabulary.</p>
<p><strong>Adding plain text:</strong>  Simply type the keyword in the box labelled "keyword".</p>\
<p><strong>Adding controlled terms using the vocabulary lookup:</strong> We support lookup of terms from the vocabularies listed here. Tick the names of the vocabularies you want to search and then start typing a keyword in the search box (where it says “Start typing to search controlled vocabularies”). If there is a match for your search, a list of candidate terms will be displayed. You can then click on the term you want to add.</p>\
<p><strong>Adding controlled terms without using the lookup:</strong> If you want to add a controlled term from a vocabulary that isn't included in the catalogue, add the keyword's uri and its label to the appropriate boxes.</p>\
`
        }),
        new TextOnlyView({
          model: this.model,
          label: 'Other keywords',
          text: 'All other keywords not described elsewhere.',
          required: true
        }),
        new ParentView({
          model: this.model,
          modelAttribute: 'keywordsOther',
          ObjectInputView: KeywordVocabularyView,
          multiline: true
        })
      ]
    },
    {
      label: 'Discovery metadata',
      title: 'Discovery metadata',
      views: [

        new TextOnlyView({
          model: this.model,
          label: 'Description (min 100 characters)',
          text: `\
<p>Data resources deposited with the EIDC have an entry in the EIDC data catalogue, enabling users to find and access them. Please provide the following information to help complete the catalogue record. <a href='https://eidc.ac.uk/deposit/metadata/guidance' target='_blank' rel='noopener noreferrer' >Further guidance is available on our website</a>.</p>
<p><em>Please note, this information is not fixed and may be subject to change and improvement over time</em></p>
<p>The description should describe the data resource in question, NOT the project/activity which produced it.</p>
<p>The description is an 'executive summary' that allows the reader to determine the relevance and usefulness of the resource.  The text should be concise but contain sufficient detail to allow the reader to ascertain rapidly the scope and limitations of the resource.</p>
<p>Write in plain English; in other words, write complete sentences rather than fragments.  It is recommended that the abstract is organised using the "What, Where, When, How, Why, Who" structure.</p>\
`,
          required: true
        }),

        new TextareaView({
          model: this.model,
          modelAttribute: 'description',
          rows: 12
        }),

        new TextOnlyView({
          model: this.model,
          label: 'Lineage',
          text: `\
<p>The lineage should describe how the data came into existence and the stages it has passed through before being provided to the data centre.</p>
<p>Briefly describe how the data were generated and any QA steps</p>\
<p><a href='https://eidc.ac.uk/deposit/metadata/guidance' target='_blank' rel='noopener noreferrer'>See further guidance</a></p>\
`,
          required: true
        }),
        new TextareaView({
          model: this.model,
          modelAttribute: 'lineage',
          rows: 10
        }),

        new TextOnlyView({
          model: this.model,
          label: 'Area of Study (if relevant)',
          text: `\
<p>Please click Add and select the appropriate option. Once selected, the area can be customized by providing latitude and longitude coordinates defining a bounding box that encompasses the location of study.</p>
<p>Clicking on the show/update map icon (little globe) will also allow you to manually draw a bounding box.</p>\
`
        }),

        new PredefinedParentView({
          model: this.model,
          modelAttribute: 'boundingBoxes',
          ModelType: BoundingBox,
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
          }
        })
      ]
    }
    ]

    return EditorView.prototype.initialize.apply(this)
  },

  exit () {
    _.invoke(this.sections, 'remove')
    this.remove()

    if (Backbone.history.location.pathname === `/documents/${this.model.get('id')}`) {
      return Backbone.history.location.replace(`/service-agreement/${this.model.get('id')}`)
    } else {
      return Backbone.history.location.reload()
    }
  }
})
