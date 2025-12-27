package marcomanfrin.atixbackend.ServiceInterfaces;

import marcomanfrin.atixbackend.DTO.workReports.WorkReportEntryRequest;
import marcomanfrin.atixbackend.DTO.workReports.WorkReportEntryResponse;
import marcomanfrin.atixbackend.DTO.workReports.WorkReportEntryUpdateRequest;
import marcomanfrin.atixbackend.DTO.workReports.WorkReportResponse;

import java.util.List;
import java.util.UUID;

public interface IWorkReportService {
    WorkReportResponse getWorkReportByWorkId(UUID workId);
    WorkReportEntryResponse createWorkReportEntry(WorkReportEntryRequest request);
    WorkReportEntryResponse updateWorkReportEntry(UUID entryId, WorkReportEntryUpdateRequest request);
    List<WorkReportEntryResponse> getWorkReportEntriesByWorkId(UUID workId);
    void deleteWorkReportEntry(UUID entryId);
}
