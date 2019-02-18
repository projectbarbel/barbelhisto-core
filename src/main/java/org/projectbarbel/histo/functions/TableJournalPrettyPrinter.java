package org.projectbarbel.histo.functions;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.projectbarbel.histo.model.Bitemporal;

/**
 * Default pretty printer for journals.
 * 
 * @author Niklas Schlimm
 *
 */
public class TableJournalPrettyPrinter implements Function<List<Bitemporal>, String> {

    // @formatter:off
    public String prettyPrint(List<Bitemporal> objects, Object id, Function<Bitemporal, String> customField) {
        return "\n" + "Document-ID: " + (objects.size() > 0 ? id : "<empty jounral>")
                + "\n\n"
                + String.format("|%-40s|%-15s|%-16s|%-8s|%-21s|%-45s|%-21s|%-45s|%-31s|", "Version-ID", "Effective-From",
                        "Effective-Until", "State", "Created-By", "Created-At", "Inactivated-By", "Inactivated-At", "Data")
                + "\n|" + StringUtils.leftPad("|", 41, "-") + StringUtils.leftPad("|", 16, "-")
                + StringUtils.leftPad("|", 17, "-") + StringUtils.leftPad("|", 9, "-")
                + StringUtils.leftPad("|", 22, "-") + StringUtils.leftPad("|", 46, "-")
                + StringUtils.leftPad("|", 22, "-") + StringUtils.leftPad("|", 46, "-") + StringUtils.leftPad("|", 32, "-") + 
                "\n"
                + objects.stream()
                     .map(d -> prettyPrint((Bitemporal)d, customField)).collect(Collectors.joining("\n"));
    }

    private String prettyPrint(Bitemporal bitemporal, Function<Bitemporal, String> customField) {
        return String.format("|%1$-40s|%2$-15tF|%3$-16tF|%4$-8s|%5$-21s|%6$-45s|%7$-21s|%8$-45s|%9$-31s|",
                bitemporal.getBitemporalStamp().getVersionId(), bitemporal.getBitemporalStamp().getEffectiveTime().from(),
                bitemporal.getBitemporalStamp().getEffectiveTime().until(),
                bitemporal.getBitemporalStamp().getRecordTime().getState().name(),
                StringUtils.truncate(bitemporal.getBitemporalStamp().getRecordTime().getCreatedBy(), 20),
                DateTimeFormatter.ISO_ZONED_DATE_TIME
                        .format(bitemporal.getBitemporalStamp().getRecordTime().getCreatedAt()),
                        StringUtils.truncate(bitemporal.getBitemporalStamp().getRecordTime().getInactivatedBy(), 20),
                        DateTimeFormatter.ISO_ZONED_DATE_TIME
                        .format(bitemporal.getBitemporalStamp().getRecordTime().getInactivatedAt()), StringUtils.truncate(customField.apply(bitemporal),30));
    }
    // @formatter:on

    @Override
    public String apply(List<Bitemporal> objectsToPrint) {
        return prettyPrint(objectsToPrint, ((Bitemporal) objectsToPrint.get(0)).getBitemporalStamp().getDocumentId(),
                b -> b.toString());
    }

}
