package marcomanfrin.atixbackend.services;

import marcomanfrin.atixbackend.entities.Rapportino;
import marcomanfrin.atixbackend.entities.RapportinoChecklistAnswer;
import marcomanfrin.atixbackend.entities.RapportinoMaterial;
import marcomanfrin.atixbackend.enums.RapportinoWorkState;
import org.springframework.web.util.HtmlUtils;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// XHTML del rapportino (layout ricalcato dal prototipo), renderizzato in PDF da RapportinoPdfService.
// Deve restare XML ben formato: tutto il testo utente passa da esc().
final class RapportinoDocumentTemplate {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final Map<String, Map<String, String>> LABELS = Map.of(
            "it", Map.ofEntries(
                    Map.entry("title", "Rapporto di Lavoro"),
                    Map.entry("client", "Cliente e impianto"),
                    Map.entry("clientName", "Ragione sociale"),
                    Map.entry("reference", "Referente"),
                    Map.entry("plant", "Impianto / Sede"),
                    Map.entry("order", "Ordine N."),
                    Map.entry("type", "Tipo intervento"),
                    Map.entry("technician", "Tecnico"),
                    Map.entry("maintenance", "Manutenzione da contratto"),
                    Map.entry("callOut", "Su chiamata"),
                    Map.entry("quote", "Su offerta / ordine"),
                    Map.entry("warranty", "In garanzia"),
                    Map.entry("activities", "Attività svolte"),
                    Map.entry("noActivities", "Nessuna attività selezionata"),
                    Map.entry("description", "Descrizione intervento"),
                    Map.entry("noDescription", "Nessuna descrizione"),
                    Map.entry("materials", "Materiali utilizzati"),
                    Map.entry("noMaterials", "Nessun materiale utilizzato"),
                    Map.entry("qty", "Q."),
                    Map.entry("hours", "Ore di lavoro e viaggio"),
                    Map.entry("workHours", "Ore lavoro"),
                    Map.entry("travelHours", "Ore viaggio"),
                    Map.entry("totalHours", "Totale ore"),
                    Map.entry("km", "Km trasferta"),
                    Map.entry("meal", "Pasto"),
                    Map.entry("parking", "Parcheggio"),
                    Map.entry("workState", "Stato lavoro"),
                    Map.entry("IN_PROGRESS", "Lavori in corso"),
                    Map.entry("COMPLETED", "Conclusione lavori"),
                    Map.entry("yes", "Sì"),
                    Map.entry("no", "No"),
                    Map.entry("signature", "Firma del cliente"),
                    Map.entry("signedOn", "Firmato il"),
                    Map.entry("remote", "firma remota"),
                    Map.entry("privacyTitle", "INFORMATIVA PRIVACY (art. 13 GDPR)"),
                    Map.entry("privacy", "Firmando il presente rapporto il sottoscritto approva le attività eseguite. I dati personali (nome, firma) sono trattati da ATIX S.r.l. ai sensi dell'art. 6.1(b) GDPR esclusivamente per la gestione documentale degli interventi. Non saranno ceduti a terzi. Diritti: info@atix.it"),
                    Map.entry("privacyAccepted", "Informativa accettata il"),
                    Map.entry("footer", "ATIX S.r.l. - Smart Building Solutions | Cazzago di Pianiga (VE)")
            ),
            "en", Map.ofEntries(
                    Map.entry("title", "Service Report"),
                    Map.entry("client", "Client and site"),
                    Map.entry("clientName", "Company name"),
                    Map.entry("reference", "Contact"),
                    Map.entry("plant", "Plant / Site"),
                    Map.entry("order", "Order No."),
                    Map.entry("type", "Intervention type"),
                    Map.entry("technician", "Technician"),
                    Map.entry("maintenance", "Contract maintenance"),
                    Map.entry("callOut", "Call-out"),
                    Map.entry("quote", "Quote / order"),
                    Map.entry("warranty", "Warranty"),
                    Map.entry("activities", "Activities performed"),
                    Map.entry("noActivities", "No activity selected"),
                    Map.entry("description", "Intervention description"),
                    Map.entry("noDescription", "No description"),
                    Map.entry("materials", "Materials used"),
                    Map.entry("noMaterials", "No materials used"),
                    Map.entry("qty", "Qty"),
                    Map.entry("hours", "Work and travel hours"),
                    Map.entry("workHours", "Work hours"),
                    Map.entry("travelHours", "Travel hours"),
                    Map.entry("totalHours", "Total hours"),
                    Map.entry("km", "Travel km"),
                    Map.entry("meal", "Meal"),
                    Map.entry("parking", "Parking"),
                    Map.entry("workState", "Work status"),
                    Map.entry("IN_PROGRESS", "Work in progress"),
                    Map.entry("COMPLETED", "Work completed"),
                    Map.entry("yes", "Yes"),
                    Map.entry("no", "No"),
                    Map.entry("signature", "Customer signature"),
                    Map.entry("signedOn", "Signed on"),
                    Map.entry("remote", "remote signature"),
                    Map.entry("privacyTitle", "PRIVACY NOTICE (art. 13 GDPR)"),
                    Map.entry("privacy", "By signing this report the undersigned approves the work performed. Personal data (name, signature) is processed by ATIX S.r.l. under art. 6.1(b) GDPR solely for the document management of interventions. It will not be disclosed to third parties. Rights: info@atix.it"),
                    Map.entry("privacyAccepted", "Notice accepted on"),
                    Map.entry("footer", "ATIX S.r.l. - Smart Building Solutions | Cazzago di Pianiga (VE)")
            )
    );

    private final Map<String, String> l;

    private RapportinoDocumentTemplate(String locale) {
        this.l = LABELS.getOrDefault(locale, LABELS.get("it"));
    }

    static String render(Rapportino r, String signatureImage) {
        return new RapportinoDocumentTemplate(r.getLocale()).build(r, signatureImage);
    }

    private String build(Rapportino r, String signatureImage) {
        StringBuilder h = new StringBuilder(8_000);
        h.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
         .append("<html xmlns=\"http://www.w3.org/1999/xhtml\"><head><meta charset=\"UTF-8\"/><style>")
         .append(CSS)
         .append("</style></head><body>");

        h.append("<div class=\"footer\">").append(esc(l.get("footer"))).append("</div>");

        h.append("<table class=\"header\"><tr>")
         .append("<td class=\"brand\">ATIX <span>").append(esc(l.get("title"))).append("</span></td>")
         .append("<td class=\"num\">N. ").append(esc(r.getNumber())).append(" - ").append(r.getInterventionDate().format(DATE)).append("</td>")
         .append("</tr></table>");

        section(h, l.get("client"));
        h.append("<table class=\"rows\">");
        row(h, l.get("clientName"), r.getClientName());
        row(h, l.get("reference"), r.getClientReference());
        row(h, l.get("plant"), r.getPlantLabel());
        row(h, l.get("order"), r.getOrderNumber());
        row(h, l.get("type"), interventionTypes(r));
        row(h, l.get("technician"), r.getTechnician() != null
                ? r.getTechnician().getFirstName() + " " + r.getTechnician().getLastName() : null);
        h.append("</table>");

        section(h, l.get("activities"));
        List<RapportinoChecklistAnswer> checked = r.getChecklistAnswers().stream().filter(RapportinoChecklistAnswer::isChecked).toList();
        if (checked.isEmpty()) {
            empty(h, l.get("noActivities"));
        } else {
            h.append("<table class=\"list\">");
            for (RapportinoChecklistAnswer a : checked) {
                h.append("<tr><td class=\"mark\">[x]</td><td>").append(esc(a.getLabelSnapshot()));
                if (a.getNote() != null && !a.getNote().isBlank()) {
                    h.append("<div class=\"note\">").append(esc(a.getNote())).append("</div>");
                }
                h.append("</td></tr>");
            }
            h.append("</table>");
        }

        section(h, l.get("description"));
        if (r.getDescription() == null || r.getDescription().isBlank()) {
            empty(h, l.get("noDescription"));
        } else {
            h.append("<div class=\"text\">").append(esc(r.getDescription()).replace("\n", "<br/>")).append("</div>");
        }

        section(h, l.get("materials"));
        if (r.getMaterials().isEmpty()) {
            empty(h, l.get("noMaterials"));
        } else {
            h.append("<table class=\"list\">");
            for (RapportinoMaterial m : r.getMaterials()) {
                h.append("<tr><td class=\"mark\">-</td><td>").append(esc(m.getDescription()))
                 .append("</td><td class=\"qty\">").append(esc(l.get("qty"))).append(" ").append(num(m.getQuantity())).append("</td></tr>");
            }
            h.append("</table>");
        }

        section(h, l.get("hours"));
        h.append("<table class=\"rows\">");
        row(h, l.get("workHours"), num(r.getWorkHours()) + " h");
        row(h, l.get("travelHours"), num(r.getTravelHours()) + " h");
        row(h, l.get("totalHours"), num(r.getTotalHours()) + " h");
        row(h, l.get("km"), num(r.getTravelKm()) + " km");
        row(h, l.get("meal"), r.isMeal() ? l.get("yes") : l.get("no"));
        row(h, l.get("parking"), r.isParking() ? l.get("yes") : l.get("no"));
        RapportinoWorkState state = r.getWorkState() != null ? r.getWorkState() : RapportinoWorkState.IN_PROGRESS;
        row(h, l.get("workState"), l.get(state.name()));
        h.append("</table>");

        h.append("<div class=\"signature\">");
        section(h, l.get("signature"));
        if (signatureImage != null) {
            h.append("<img class=\"sig\" src=\"").append(esc(signatureImage)).append("\"/>");
        }
        h.append("<div class=\"signer\">").append(esc(r.getSignerName())).append("</div>");
        if (r.getSignedAt() != null) {
            h.append("<div class=\"muted\">").append(esc(l.get("signedOn"))).append(" ").append(r.getSignedAt().format(DATE_TIME));
            if (r.getSignatureSource() != null && r.getSignatureSource().name().equals("REMOTE")) {
                h.append(" (").append(esc(l.get("remote"))).append(")");
            }
            h.append("</div>");
        }
        if (r.getPrivacyAcceptedAt() != null) {
            h.append("<div class=\"muted\">").append(esc(l.get("privacyAccepted"))).append(" ")
             .append(r.getPrivacyAcceptedAt().format(DATE_TIME)).append("</div>");
        }
        h.append("</div>");

        h.append("<div class=\"privacy\"><b>").append(esc(l.get("privacyTitle"))).append("</b><br/>")
         .append(esc(l.get("privacy"))).append("</div>");

        h.append("</body></html>");
        return h.toString();
    }

    private String interventionTypes(Rapportino r) {
        List<String> types = new ArrayList<>();
        if (r.isTypeMaintenance()) types.add(l.get("maintenance"));
        if (r.isTypeCallOut()) types.add(l.get("callOut"));
        if (r.isTypeQuote()) types.add(l.get("quote"));
        if (r.isTypeWarranty()) types.add(l.get("warranty"));
        return types.isEmpty() ? null : String.join(", ", types);
    }

    private static void section(StringBuilder h, String title) {
        h.append("<div class=\"section\">").append(esc(title.toUpperCase())).append("</div>");
    }

    private static void row(StringBuilder h, String label, String value) {
        h.append("<tr><td class=\"label\">").append(esc(label)).append(":</td><td>")
         .append(value == null || value.isBlank() ? "-" : esc(value)).append("</td></tr>");
    }

    private static void empty(StringBuilder h, String text) {
        h.append("<div class=\"empty\">").append(esc(text)).append("</div>");
    }

    private static String num(BigDecimal value) {
        return value == null ? "0" : value.stripTrailingZeros().toPlainString();
    }

    private static String esc(String value) {
        return value == null ? "" : HtmlUtils.htmlEscapeDecimal(value);
    }

    private static final String CSS = """
            @page { size: A4; margin: 15mm 15mm 22mm 15mm;
                    @bottom-center { content: element(footer); } }
            body { font-family: Helvetica, Arial, sans-serif; font-size: 9.5pt; color: #1a2e4a; }
            .footer { position: running(footer); font-size: 7.5pt; color: #6b7785; text-align: center; }
            table { width: 100%; border-collapse: collapse; }
            .header { background: #1a2e4a; color: #ffffff; margin-bottom: 3mm; }
            .header td { padding: 4mm 4mm; }
            .brand { font-size: 22pt; font-weight: bold; }
            .brand span { font-size: 11pt; font-weight: normal; margin-left: 4mm; }
            .num { text-align: right; font-size: 14pt; }
            .section { background: #e8600a; color: #ffffff; font-size: 9pt; font-weight: bold;
                       padding: 1.2mm 3mm; margin: 3.5mm 0 1.5mm 0; }
            .rows td { padding: 0.6mm 0; vertical-align: top; }
            .rows .label { width: 45mm; color: #6b7785; font-weight: bold; font-size: 8.5pt; }
            .list td { padding: 0.8mm 0; vertical-align: top; }
            .list .mark { width: 8mm; }
            .list .qty { width: 25mm; text-align: right; }
            .note { color: #6b7785; font-size: 8.5pt; }
            .text { line-height: 1.4; }
            .empty { color: #6b7785; font-style: italic; }
            .signature { page-break-inside: avoid; }
            .sig { width: 70mm; height: 26mm; border: 0.3mm solid #dde2ea; margin-top: 2mm; }
            .signer { font-weight: bold; margin-top: 2mm; }
            .muted { color: #6b7785; font-size: 8pt; }
            .privacy { margin-top: 4mm; padding: 2mm 3mm; background: #f5f7fa; border: 0.3mm solid #dde2ea;
                       font-size: 6.5pt; color: #6b7785; page-break-inside: avoid; }
            """;
}
