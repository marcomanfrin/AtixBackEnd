package marcomanfrin.atixbackend.services;

import marcomanfrin.atixbackend.entities.Rapportino;
import marcomanfrin.atixbackend.entities.RapportinoChecklistAnswer;
import marcomanfrin.atixbackend.entities.RapportinoMaterial;
import marcomanfrin.atixbackend.entities.users.TechnicianUser;
import marcomanfrin.atixbackend.enums.RapportinoWorkState;
import marcomanfrin.atixbackend.enums.SignatureSource;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.Deflater;

import static org.junit.jupiter.api.Assertions.*;

// Rendering del documento senza DB ne' storage: contenuti obbligatori, sezioni vuote esplicite, impaginazione
class RapportinoPdfServiceTest {

    private final RapportinoPdfService pdfService = new RapportinoPdfService(null);

    @Test
    void completeRapportinoContainsEveryRequiredFieldOnOnePage() throws Exception {
        Rapportino r = baseRapportino("it");
        r.setTypeMaintenance(true);
        r.setTypeWarranty(true);
        r.setDescription("Sostituito sensore <NTC> & verificato bus: perché è caduto?");
        r.replaceChecklistAnswers(List.of(
                new RapportinoChecklistAnswer(null, "Verifica funzionamento impianto", true, "Tutto ok", 0),
                new RapportinoChecklistAnswer(null, "Voce non svolta", false, null, 1)));
        r.replaceMaterials(List.of(new RapportinoMaterial("Cavo bus 2x0.8", new BigDecimal("12.5"), 0)));

        PdfText pdf = render(r, signaturePng());

        for (String expected : List.of(
                "RFL-2026-0007", "05/10/2026",
                "Provincia Autonoma di Bolzano", "Mario Rossi", "Palazzo di Giustizia", "ORD-2026-042",
                "Manutenzione da contratto", "In garanzia", "Luca Tecnico",
                "Verifica funzionamento impianto", "Tutto ok",
                "Sostituito sensore <NTC> & verificato bus: perché è caduto?",
                "Cavo bus 2x0.8", "12.5",
                "Ore lavoro", "4 h", "Ore viaggio", "2 h", "Totale ore", "6 h", "35 km",
                "Pasto", "Parcheggio", "Conclusione lavori",
                "Firma del cliente".toUpperCase(), "Giulia Bianchi", "Firmato il 05/10/2026 17:30",
                "INFORMATIVA PRIVACY")) {
            assertTrue(pdf.text.contains(expected), "missing in PDF: " + expected);
        }
        assertFalse(pdf.text.contains("Voce non svolta"), "unchecked activities are not printed");
        assertEquals(1, pdf.pages, "a typical rapportino fits on one page");
    }

    @Test
    void emptySectionsAreMarkedExplicitly() throws Exception {
        PdfText pdf = render(baseRapportino("it"), null);
        assertTrue(pdf.text.contains("Nessuna attività selezionata"));
        assertTrue(pdf.text.contains("Nessun materiale utilizzato"));
        assertTrue(pdf.text.contains("Nessuna descrizione"));
    }

    @Test
    void englishLocaleUsesEnglishLabels() throws Exception {
        PdfText pdf = render(baseRapportino("en"), null);
        assertTrue(pdf.text.contains("Service Report"));
        assertTrue(pdf.text.contains("No materials used"));
        assertTrue(pdf.text.contains("Travel hours"));
    }

    @Test
    void hashIsSha256Hex() {
        assertEquals("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
                RapportinoPdfService.sha256(new byte[0]));
    }

    // ---------- helpers ----------

    private record PdfText(String text, int pages) {}

    private PdfText render(Rapportino r, String signature) throws Exception {
        byte[] bytes = pdfService.render(r, signature);
        try (PDDocument doc = Loader.loadPDF(bytes)) {
            String text = new PDFTextStripper().getText(doc).replaceAll("\\s+", " ");
            return new PdfText(text, doc.getNumberOfPages());
        }
    }

    private static Rapportino baseRapportino(String locale) {
        TechnicianUser tech = new TechnicianUser();
        tech.setFirstName("Luca");
        tech.setLastName("Tecnico");

        Rapportino r = new Rapportino();
        r.setNumber("RFL-2026-0007");
        r.setLocale(locale);
        r.setInterventionDate(LocalDate.of(2026, 10, 5));
        r.setTechnician(tech);
        r.setClientName("Provincia Autonoma di Bolzano");
        r.setClientReference("Mario Rossi");
        r.setPlantLabel("Palazzo di Giustizia");
        r.setOrderNumber("ORD-2026-042");
        r.setWorkHours(new BigDecimal("4"));
        r.setTravelHours(new BigDecimal("2"));
        r.setTravelKm(new BigDecimal("35"));
        r.setMeal(true);
        r.setWorkState(RapportinoWorkState.COMPLETED);
        r.setSignerName("Giulia Bianchi");
        r.setSignedAt(LocalDateTime.of(2026, 10, 5, 17, 30));
        r.setPrivacyAcceptedAt(LocalDateTime.of(2026, 10, 5, 17, 30));
        r.setSignatureSource(SignatureSource.LOCAL);
        return r;
    }

    // PNG 1x1 valido costruito a mano (niente file di fixture)
    static String signaturePng() throws Exception {
        ByteArrayOutputStream png = new ByteArrayOutputStream();
        png.write(new byte[]{(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1a, '\n'});
        chunk(png, "IHDR", new byte[]{0, 0, 0, 1, 0, 0, 0, 1, 8, 6, 0, 0, 0});
        Deflater deflater = new Deflater();
        deflater.setInput(new byte[]{0, 0, 0, 0, (byte) 255});
        deflater.finish();
        byte[] buf = new byte[64];
        int len = deflater.deflate(buf);
        byte[] idat = new byte[len];
        System.arraycopy(buf, 0, idat, 0, len);
        chunk(png, "IDAT", idat);
        chunk(png, "IEND", new byte[0]);
        return "data:image/png;base64," + Base64.getEncoder().encodeToString(png.toByteArray());
    }

    private static void chunk(ByteArrayOutputStream out, String type, byte[] data) throws Exception {
        out.write(new byte[]{(byte) (data.length >>> 24), (byte) (data.length >>> 16), (byte) (data.length >>> 8), (byte) data.length});
        byte[] typeBytes = type.getBytes("US-ASCII");
        out.write(typeBytes);
        out.write(data);
        CRC32 crc = new CRC32();
        crc.update(typeBytes);
        crc.update(data);
        long c = crc.getValue();
        out.write(new byte[]{(byte) (c >>> 24), (byte) (c >>> 16), (byte) (c >>> 8), (byte) c});
    }
}
