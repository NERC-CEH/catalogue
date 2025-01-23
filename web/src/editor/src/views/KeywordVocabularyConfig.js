export const vocabsList = new Map([
  ['assist-topics', { id: 'assist-topics', name: 'Topics' }],
  ['assist-research-themes', { id: 'assist-research-themes', name: 'Research Themes' }],
  ['cast', { id: 'cast', name: 'CAST' }],
  ['envThes', { id: 'envThes', name: 'EnvThes' }],
  ['dukems-pollutant', { id: 'dukems-pollutant', name: 'Pollutants' }],
  ['dukems-sector', { id: 'dukems-sector', name: 'Sectors' }],
  ['elterCL', { id: 'elterCL', name: 'elterCL' }],
  ['gemet', { id: 'gemet', name: 'GEMET' }],
  ['inms', { id: 'inms', name: 'INMS' }],
  ['research-theme', { id: 'research-theme', name: 'Research themes' }],
  ['research-project', { id: 'research-project', name: 'Research projects' }],
  ['science-challenge', { id: 'science-challenge', name: 'Science challenges' }],
  ['service', { id: 'service', name: 'Services' }]
])

export const vocabsPredefined = {
  assist: { // eidc profile
    'assist-topics': vocabsList.get('assist-topics'),
    'assist-research-themes': vocabsList.get('assist-research-themes'),
    cast: vocabsList.get('cast')
  },
  eidc: {
    cast: vocabsList.get('cast'),
    envThes: vocabsList.get('envThes'),
    gemet: vocabsList.get('gemet')
  },
  ukceh: {
    cast: vocabsList.get('cast'),
    envThes: vocabsList.get('envThes'),
    'research-project': vocabsList.get('research-project'),
    'research-theme': vocabsList.get('research-theme'),
    'science-challenge': vocabsList.get('science-challenge'),
    service: vocabsList.get('service')
  },
  nm: {
    cast: vocabsList.get('cast')
  },
  ukeof: {
    gemet: vocabsList.get('gemet')
  },
  ukscape: {
    'research-project': vocabsList.get('research-project'),
    'research-theme': vocabsList.get('research-theme'),
    'science-challenge': vocabsList.get('science-challenge'),
    service: vocabsList.get('service')
  },
  datalabs: { // datalabs profile
    'dukems-pollutant': vocabsList.get('dukems-pollutant'),
    'dukems-sector': vocabsList.get('dukems-sector')
  },
  elter: { // elter profile
    elterCL: vocabsList.get('elterCL')
  },
  inms: { // inms profile
    inms: vocabsList.get('inms')
  },
  pimfe: { // pimfe profile
    cast: vocabsList.get('cast'),
    'research-theme': vocabsList.get('research-theme')
  }
}
