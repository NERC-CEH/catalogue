<#import "../new-form.html.tpl" as form>

<div class="value composite">
    <@form.value name="observationPlacholders" label="Observation Placeholders">
        <ul class="list-unstyled">
            <li>
                <div class='composite-observation-placholder'>
                    <@form.delete name="observationPlacholder"></@form.delete>
                    <div class='composite-observation-placholder-value'>
                        <@form.select name="foiType" class="observation-placeholder">
                            <option selected value="id2">First</option>
                            <option value="id2">Second</option>
                            <option value="other">Other</option>
                        </@form.select>
                        <div>
                            <a class="static-value" href="/documents/9699d939-a9e0-4ca5-816a-c2581b1c0298">First</a>
                        </div>
                    </div>
                </div>
            </li>
            <li>
                <div class='composite-observation-placholder'>
                    <@form.delete name="observationPlacholder"></@form.delete>
                    <div class='composite-observation-placholder-value'>
                        <@form.select name="foiType" class="observation-placeholder">
                            <option value="id2">First</option>
                            <option selected value="id2">Second</option>
                            <option value="other">Other</option>
                        </@form.select>
                        <div>
                            <a class="static-value" href="/documents/9699d939-a9e0-4ca5-816a-c2581b1c0298">Second With a really really really really really long name</a>
                        </div>
                    </div>
                </div>
            </li>
            <li>
                <div class='composite-observation-placholder'>
                    <@form.delete name="observationPlacholder"></@form.delete>
                    <div class='composite-observation-placholder-value'>
                        <@form.select name="foiType" class="observation-placeholder">
                            <option value="id2"></option>
                            <option value="id2">First</option>
                            <option value="id2">Second</option>
                            <option value="other">Other</option>
                        </@form.select>
                    </div>
                </div>
            </li>
        </ul>
    </@form.value>
    <@form.value name="observationPlacholderName" label="Observation Placeholder Name" hidden=true errorMessage="Name is required">
        <input disabled name="observationPlacholderName" placeholder="Observation Placeholder Name" required>
    </@form.value>
</div>