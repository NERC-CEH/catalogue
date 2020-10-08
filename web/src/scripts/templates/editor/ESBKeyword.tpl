<div class="keywordGroup keywordGroup--<%= data.index %>">
  <div class="row ">
    <div class="col-sm-1">
      <label class="control-label" for="descriptiveKeyword<%= data.index %>type">Type</label>
    </div>
    <div class="col-sm-11">
      <select data-name="type" class="editor-input type" id="descriptiveKeyword<%= data.index %>type">
        <option value="" selected >- Select Type -</option>
        <option value="physicalState">Physical state</option>
        <option value="sampleType">Sample type</option>
        <option value="taxon">Taxon</option>
      </select>
    </div>
  </div>
  <div class="keywords row col-sm-11 col-sm-offset-1"></div>

  <div class="row">
    <div class="col-sm-11 col-sm-offset-1">
      <div class="keyword-add hidden" id="taxon">
        <button class="editor-button dropdown-toggle" type="button" data-toggle="dropdown" aria-haspopup="true" aria-expanded="true">
          Add taxon <span class="caret"></span>
        </button>
        <ul class="dropdown-menu predefined" aria-labelledby="dropdownTaxonMenu">
          <li><a href="http://onto.nerc.ac.uk/CEHMD/topic/1">Taxon 1</a></li>
          <li><a href="http://onto.nerc.ac.uk/CEHMD/topic/20">Taxon 2</a></li>
          <li><a href="http://onto.nerc.ac.uk/CEHMD/topic/2">Taxon 3</a></li>
        </ul>
      </div>
      
      <div class="keyword-add hidden" id="physicalState">
        <button class="editor-button dropdown-toggle" type="button" data-toggle="dropdown" aria-haspopup="true" aria-expanded="true">
          Add physical state <span class="caret"></span>
        </button>
        <ul class="dropdown-menu predefined" aria-labelledby="dropdownPhysicalStateMenu">
          <li><a href="http://onto.nerc.ac.uk/CEHMD/topic/1">physicalState 1</a></li>
          <li><a href="http://onto.nerc.ac.uk/CEHMD/topic/20">physicalState 2</a></li>
          <li><a href="http://onto.nerc.ac.uk/CEHMD/topic/2">physicalState 3</a></li>
        </ul>
      </div>
      
      <div class="keyword-add hidden" id="sampleType">
        <button class="editor-button dropdown-toggle" type="button" data-toggle="dropdown" aria-haspopup="true" aria-expanded="true">
          Add sample type <span class="caret"></span>
        </button>
        <ul class="dropdown-menu predefined" aria-labelledby="dropdownSampleTypeMenu">
          <li><a href="http://onto.nerc.ac.uk/CEHMD/topic/1">sampleType 1</a></li>
          <li><a href="http://onto.nerc.ac.uk/CEHMD/topic/20">sampleType 2</a></li>
          <li><a href="http://onto.nerc.ac.uk/CEHMD/topic/2">sampleType 3</a></li>
        </ul>
      </div>
      
      <div class="keyword-add">
        <button class="editor-button add">Add <span class="fas fa-plus" aria-hidden="true"></span></button>
      </div>

    </div>
  </div>
</div>