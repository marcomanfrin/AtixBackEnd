package marcomanfrin.atixbackend.services;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import marcomanfrin.atixbackend.ServiceInterfaces.IAttachmentService;
import marcomanfrin.atixbackend.entities.AttachmentLink;
import marcomanfrin.atixbackend.entities.Rapportino;
import marcomanfrin.atixbackend.enums.AttachmentTargetType;
import marcomanfrin.atixbackend.exceptions.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.io.ByteArrayOutputStream;
import java.security.MessageDigest;
import java.util.HexFormat;

// Genera, archivia e restituisce il PDF di un rapportino firmato.
// I PDF stanno sotto il prefisso privato "rapportini/" del bucket: mai serviti via URL diretto.
@Service
public class RapportinoPdfService {

    static final String STORAGE_PREFIX = "rapportini";

    private final IAttachmentService attachmentService;

    public RapportinoPdfService(IAttachmentService attachmentService) {
        this.attachmentService = attachmentService;
    }

    /**
     * Da chiamare dentro la transazione di firma. Se il rapportino ha gia' un documento non ne produce
     * un altro (il documento firmato non si rigenera). In caso di rollback l'oggetto caricato viene rimosso.
     */
    public void generateAndArchive(Rapportino rapportino, String signatureImage) {
        if (rapportino.getPdfAttachmentId() != null) {
            return;
        }

        byte[] pdf = render(rapportino, signatureImage);

        AttachmentLink link = attachmentService.storeGenerated(
                pdf, rapportino.getNumber() + ".pdf", "application/pdf",
                STORAGE_PREFIX, AttachmentTargetType.REPORT, rapportino.getId());

        String objectKey = link.getAttachment().getPublicId();
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    if (status != STATUS_COMMITTED) {
                        attachmentService.removeObjectQuietly(objectKey);
                    }
                }
            });
        }

        rapportino.setPdfAttachmentId(link.getAttachment().getId());
        rapportino.setPdfHash(sha256(pdf));
    }

    public byte[] loadDocument(Rapportino rapportino) {
        if (rapportino.getPdfAttachmentId() == null) {
            throw new NotFoundException("No document exists yet for rapportino " + rapportino.getNumber());
        }
        return attachmentService.readContent(rapportino.getPdfAttachmentId());
    }

    byte[] render(Rapportino rapportino, String signatureImage) {
        String html = RapportinoDocumentTemplate.render(rapportino, signatureImage);
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null);
            builder.toStream(out);
            builder.run();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("PDF rendering failed for rapportino " + rapportino.getNumber(), e);
        }
    }

    static String sha256(byte[] content) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(content));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
