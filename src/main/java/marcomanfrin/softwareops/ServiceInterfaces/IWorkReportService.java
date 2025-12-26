package marcomanfrin.softwareops.ServiceInterfaces;

import marcomanfrin.softwareops.DTO.workReports.WorkReportEntryRequest;
import marcomanfrin.softwareops.DTO.workReports.WorkReportEntryResponse;
import marcomanfrin.softwareops.DTO.workReports.WorkReportResponse;

import java.util.List;
import java.util.UUID;

public interface IWorkReportService {
    WorkReportResponse getWorkReportByWorkId(UUID workId);
    WorkReportEntryResponse createWorkReportEntry(WorkReportEntryRequest request);
    WorkReportEntryResponse updateWorkReportEntry(UUID entryId, WorkReportEntryRequest request);
    List<WorkReportEntryResponse> getWorkReportEntriesByWorkId(UUID workId);
    void deleteWorkReportEntry(UUID entryId);
}
