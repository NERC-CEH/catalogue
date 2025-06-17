export function variableOnSelect (event, suggestionList) {
  const { target } = event
  const { value, title, units, description } = target.dataset

  if (target.checked) {
    suggestionList.push({ value, title, units, description, constraints: {} })
  } else {
    const index = suggestionList.findIndex(item => item.value === value)
    if (index !== -1) {
      suggestionList.splice(index, 1)
    }
  }
}

export function keywordOnSelect (event, suggestionList) {
  const { target } = event
  const { term: keywordName, uri: keywordUri = '' } = target.dataset

  if (target.checked) {
    suggestionList.push({ value: keywordName, uri: keywordUri })
  } else {
    const index = suggestionList.findIndex(kw => kw.value === keywordName)
    if (index !== -1) {
      suggestionList.splice(index, 1)
    }
  }
}
