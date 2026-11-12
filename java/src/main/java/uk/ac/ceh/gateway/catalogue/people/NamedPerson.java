package uk.ac.ceh.gateway.catalogue.people;

import lombok.extern.slf4j.Slf4j;
import uk.ac.ceh.gateway.catalogue.model.ResponsibleParty;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * A person one record names with an ORCID, as the record has them: the name and organisation the
 * depositor gave. Indexed with the record, in the {@code person} field, so the editor's people look-up
 * can offer the iDs the catalogue already holds (dri-one #534). The look-up searches records rather
 * than a collection of its own so that search's visibility filter decides whose drafts it may read.
 *
 * @param orcid the bare iD, e.g. 0000-0002-1825-0097
 */
@Slf4j
public record NamedPerson(String orcid, String givenName, String familyName, String organisationName, String organisationIdentifier) {

    /** Not a character any name or identifier contains, so a packed person always unpacks */
    private static final String SEPARATOR = "\u001F";

    /** How the person is kept in the index: its five parts in order */
    public String pack() {
        return String.join(SEPARATOR, orcid, givenName, familyName, organisationName, organisationIdentifier);
    }

    public static Optional<NamedPerson> unpack(String packed) {
        var parts = packed.split(SEPARATOR, -1);
        if (parts.length != 5) return Optional.empty();
        return Optional.of(new NamedPerson(parts[0], parts[1], parts[2], parts[3], parts[4]));
    }

    /** "Clare Rowland": what the look-up searches */
    public String name() {
        return Stream.of(givenName, familyName).filter(s -> !s.isBlank()).reduce((a, b) -> a + " " + b).orElse("");
    }

    static NamedPerson of(ResponsibleParty party, String orcid) {
        return new NamedPerson(
            orcid,
            party.getGivenName().strip(),
            party.getFamilyName().strip(),
            party.getOrganisationName().strip(),
            party.isRor() ? party.getOrganisationIdentifier() : ""
        );
    }

    /**
     * Everyone a record names with an ORCID and a name, once each, wherever in the record they are:
     * a Gemini record's authors and contacts, a monitoring record's responsible parties and partners,
     * a model's contacts. Found by their type rather than listed by field, so a document type that
     * names people in a new field is indexed without anyone remembering to add it here.
     */
    public static List<NamedPerson> in(Object document) {
        Set<NamedPerson> people = new LinkedHashSet<>();
        for (var party : parties(document)) {
            Orcid.id(party.getNameIdentifier()).ifPresent(orcid -> {
                var person = NamedPerson.of(party, orcid);
                if (!person.name().isBlank()) people.add(person);
            });
        }
        return new ArrayList<>(people);
    }

    private static List<ResponsibleParty> parties(Object document) {
        List<ResponsibleParty> parties = new ArrayList<>();
        for (Class<?> type = document.getClass(); type != null && type != Object.class; type = type.getSuperclass()) {
            for (Field field : type.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers())) continue;
                var value = read(field, document);
                if (value instanceof ResponsibleParty party) {
                    parties.add(party);
                } else if (value instanceof Collection<?> values) {
                    values.stream()
                        .filter(ResponsibleParty.class::isInstance)
                        .map(ResponsibleParty.class::cast)
                        .forEach(parties::add);
                }
            }
        }
        return parties;
    }

    private static Object read(Field field, Object document) {
        // Only fields that can hold a contact are read, so a field of some other type the
        // module system won't open (a JDK type, say) is never touched
        if (!ResponsibleParty.class.isAssignableFrom(field.getType())
            && !Collection.class.isAssignableFrom(field.getType())) {
            return null;
        }
        try {
            field.setAccessible(true);
            return field.get(document);
        } catch (ReflectiveOperationException | RuntimeException ex) {
            log.warn("Can't read {}.{} for the people it names", field.getDeclaringClass().getSimpleName(), field.getName(), ex);
            return null;
        }
    }
}
