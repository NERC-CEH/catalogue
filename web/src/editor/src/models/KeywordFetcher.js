import { KeywordModel } from './index'

const KeywordFetcher = {
  fetchKeywordsFromLegilo: function (model) {
    const datasetId = model.get('id')
    if (!datasetId) {
      console.error('No dataset ID found')
      return Promise.reject('No dataset ID found')
    }

    const mockData = {
      summary: [
        { name: 'temperature', uri: 'custom-uri1', confidence: 0.63 },
        { name: 'trophic', uri: '', confidence: 0.35 },
        { name: 'transect', uri: 'custom-uri2', confidence: 0.25 },
        { name: 'pressure', uri: 'custom-uri3', confidence: 0.45 },
        { name: 'salinity', uri: '', confidence: 0.50 },
        { name: 'depth', uri: 'custom-uri4', confidence: 0.30 },
        { name: 'oxygen', uri: '', confidence: 0.55 },
        { name: 'chlorophyll', uri: 'custom-uri5', confidence: 0.60 },
        { name: 'phosphate', uri: '', confidence: 0.20 },
        { name: 'silicate', uri: 'custom-uri6', confidence: 0.15 },
        { name: 'nitrate', uri: 'custom-uri7', confidence: 0.40 }
      ]
    }

    return new Promise((resolve) => {
      setTimeout(() => {
        const keywords = mockData.summary.map(keywordData => new KeywordModel(keywordData))
        resolve(keywords)
      }, 1000)
    }).catch(error => {
      console.error('Error fetching keywords:', error)
      throw error
    })
  }
}

export default KeywordFetcher
