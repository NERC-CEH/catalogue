<#import "../new-form.html.tpl" as form>

<@form.value label="Observation Placeholders" class="composite">
    <ul class="list-unstyled">
        <li>
            <@form.link name="linky" label="9699d939-a9e0-4ca5-816a-c2581b1c0298" href="http://google.com"></@form.link>
            <@form.select name="foiType" class="observation-placeholder">
                <option value="id2">Second</option>
            </@form.select>
        </li>
        <li>
            <@form.delete name="observationPlacholder"></@form.delete>
            <@form.link name="linky" label="9699d939-a9e0-4ca5-816a-c2581b1c0298" href="http://google.com"></@form.link>
            <@form.select name="foiType" class="observation-placeholder">
                <option value="id1">First</option>
            </@form.select>
        </li>
        <li>
            <@form.delete name="observationPlacholder"></@form.delete>
            <@form.link name="linky" label="" href="#"></@form.link>
            <@form.select name="foiType" class="observation-placeholder">
                <option value="idx"></option>
                <option value="id1">First</option>
                <option value="id2">Second</option>
                <option value="other-observation-placeholder">Other</option>
            </@form.select>
        </li>
    </ul>
</@form.value>