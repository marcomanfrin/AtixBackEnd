package marcomanfrin.softwareops.services;

import marcomanfrin.softwareops.entities.WorkReport;
import marcomanfrin.softwareops.entities.WorkReportEntry;

import java.math.BigDecimal;
import java.util.UUID;

public interface IWorkReportService {

    WorkReport createForWork(UUID workId);

    WorkReport getByWorkIdOrThrow(UUID workId);

    WorkReportEntry addEntryByWorkId(UUID workId, UUID taskId, BigDecimal hours);

    WorkReportEntry updateEntry(UUID workId, UUID entryId, BigDecimal hours);

    void removeEntry(UUID workId, UUID entryId);

    WorkReport finalizeByWorkId(UUID workId);

    WorkReport invoiceByReportId(UUID reportId);
}