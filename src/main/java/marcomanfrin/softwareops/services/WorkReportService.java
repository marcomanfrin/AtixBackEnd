package marcomanfrin.softwareops.services;

import marcomanfrin.softwareops.DTO.workReports.WorkReportEntryRequest;
import marcomanfrin.softwareops.DTO.workReports.WorkReportEntryResponse;
import marcomanfrin.softwareops.DTO.workReports.WorkReportResponse;
import marcomanfrin.softwareops.ServiceInterfaces.IWorkReportService;
import marcomanfrin.softwareops.entities.Work;
import marcomanfrin.softwareops.entities.WorkReport;
import marcomanfrin.softwareops.entities.WorkReportEntry;
import marcomanfrin.softwareops.exceptions.NotFoundException;
import marcomanfrin.softwareops.repositories.WorkReportEntryRepository;
import marcomanfrin.softwareops.repositories.WorkReportRepository;
import marcomanfrin.softwareops.repositories.WorkRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class WorkReportService implements IWorkReportService {
    private final WorkReportRepository workReportRepository;
    private final WorkReportEntryRepository workReportEntryRepository;
    private final WorkRepository workRepository;

    public WorkReportService(WorkReportRepository workReportRepository,
                           WorkReportEntryRepository workReportEntryRepository,
                           WorkRepository workRepository) {
        this.workReportRepository = workReportRepository;
        this.workReportEntryRepository = workReportEntryRepository;
        this.workRepository = workRepository;
    }

    @Override
    public WorkReportResponse getWorkReportByWorkId(UUID workId) {
        WorkReport workReport = workReportRepository.findByWorkId(workId)
                .orElseGet(() -> createWorkReportForWork(workId));
        return toWorkReportResponse(workReport);
    }

    @Override
    @Transactional
    public WorkReportEntryResponse createWorkReportEntry(WorkReportEntryRequest request) {
        Work work = workRepository.findById(request.workId())
                .orElseThrow(() -> new NotFoundException("Work not found with id: " + request.workId()));

        // Get or create work report
        WorkReport workReport = workReportRepository.findByWork(work)
                .orElseGet(() -> {
                    WorkReport newReport = new WorkReport();
                    newReport.setWork(work);
                    return workReportRepository.save(newReport);
                });

        // Create entry
        WorkReportEntry entry = new WorkReportEntry(
                workReport,
                request.description(),
                request.hours()
        );

        WorkReportEntry savedEntry = workReportEntryRepository.save(entry);

        // Update total hours
        updateTotalHours(workReport);

        return toWorkReportEntryResponse(savedEntry);
    }

    @Override
    @Transactional
    public WorkReportEntryResponse updateWorkReportEntry(UUID entryId, WorkReportEntryRequest request) {
        WorkReportEntry entry = workReportEntryRepository.findById(entryId)
                .orElseThrow(() -> new NotFoundException("Work report entry not found with id: " + entryId));

        // PATCH logic: update only non-null fields
        if (request.description() != null) {
            entry.setDescription(request.description());
        }
        if (request.hours() != null) {
            entry.setHours(request.hours());
        }

        WorkReportEntry updatedEntry = workReportEntryRepository.save(entry);

        // Update total hours
        updateTotalHours(entry.getReport());

        return toWorkReportEntryResponse(updatedEntry);
    }

    @Override
    public List<WorkReportEntryResponse> getWorkReportEntriesByWorkId(UUID workId) {
        WorkReport workReport = workReportRepository.findByWorkId(workId)
                .orElseGet(() -> createWorkReportForWork(workId));

        return workReport.getEntries().stream()
                .map(this::toWorkReportEntryResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteWorkReportEntry(UUID entryId) {
        WorkReportEntry entry = workReportEntryRepository.findById(entryId)
                .orElseThrow(() -> new NotFoundException("Work report entry not found with id: " + entryId));

        WorkReport workReport = entry.getReport();
        workReportEntryRepository.deleteById(entryId);

        // Update total hours
        updateTotalHours(workReport);
    }

    @Transactional
    private WorkReport createWorkReportForWork(UUID workId) {
        Work work = workRepository.findById(workId)
                .orElseThrow(() -> new NotFoundException("Work not found with id: " + workId));

        WorkReport workReport = new WorkReport();
        workReport.setWork(work);
        return workReportRepository.save(workReport);
    }

    private void updateTotalHours(WorkReport workReport) {
        BigDecimal totalHours = workReport.getEntries().stream()
                .map(WorkReportEntry::getHours)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        workReport.setTotalHours(totalHours);
        workReportRepository.save(workReport);
    }

    private WorkReportResponse toWorkReportResponse(WorkReport workReport) {
        BigDecimal totalHours = workReport.getEntries().stream()
                .map(WorkReportEntry::getHours)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new WorkReportResponse(
                workReport.getId(),
                workReport.getWork().getId(),
                totalHours,
                workReport.getCreatedAt(),
                workReport.getEntries().stream()
                        .map(this::toWorkReportEntryResponse)
                        .collect(Collectors.toList())
        );
    }

    private WorkReportEntryResponse toWorkReportEntryResponse(WorkReportEntry entry) {
        return new WorkReportEntryResponse(
                entry.getId(),
                entry.getReport().getId(),
                entry.getDescription(),
                entry.getHours()
        );
    }
}
