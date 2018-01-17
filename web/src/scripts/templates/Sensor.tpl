<div class="container">
    <span id="saved" style="display: none;">SAVED</span>
    <form id="form" class="new-form" data-document="sensor">
        <input name="type" type="hidden" value="dataset">
        <input name="resourveType" type="hidden" value="dataset">
        <div class='head'>
            <input name="title" type="text" class='title' placeholder="Title" required>
            <input name="shortName" type="text" class='subtitle' placeholder="Short Name">
            <textarea name="description" type="text" class='description' placeholder="Description"></textarea>
        </div>
        <div class='body'>
            <div class='value'>
                <label>Serial Number</label>
                <input name="serialNumber" type="text" placeholder="Serial Number">
            </div>
            <div class='value value-link' data-name="documentation">
                <label><a href="#">Documentation</a></label>
                <input name="documentation" type="text" placeholder="Documentation">
            </div>
            <div class='value'>
                <label>Process Type</label>
                <select name="processType">
                    <option value="Unknown">Unknown</option>
                    <option value="Simulation">Simulation</option>
                    <option value="Manual">Manual</option>
                    <option value="Sensor">Sensor</option>
                    <option value="Algorithm">Algorithm</option>
                </select>
            </div>
            <div class='value value-link' data-name="manufacturer" data-format="/documents/{manufacturer}">
                <label><a href="#">Manufacturer</a></label>
                <select id="manufacturer" name="manufacturer">
                    <option value=""></option>
                </select>
            </div>
            <div class='value'>
                <label>Default Parameters</label>
                <ul id="defaultParameters" class="list-unstyled">
                    <li>
                        <a href='#' class="delete"><i class="fa fa-times"></i></a>
                        <input name="defaultParameters[0]['value']" type="text" value="" placeholder="Default Parameter">
                    </li>
                </ul>
            </div>
        </div>
        <input type="submit" style="display: none;">
    </form>
</div>
