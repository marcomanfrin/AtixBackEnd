package marcomanfrin.softwareops.services;

import jakarta.transaction.Transactional;
import marcomanfrin.softwareops.entities.*;
import marcomanfrin.softwareops.exceptions.ForbiddenException;
import marcomanfrin.softwareops.exceptions.NotFoundException;
import marcomanfrin.softwareops.repositories.*;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class WorkReportService implements IWorkReportService {

    private final WorkReportRepository workReportRepository;
    private final WorkRepository workRepository;
    private final TicketRepository ticketRepository;   // opzionale: solo se ti serve altrove
    private final TaskRepository taskRepository;
    private final WorkReportEntryRepository workReportEntryRepository;

    public WorkReportService(
            WorkReportRepository workReportRepository,
            WorkRepository workRepository,
            TicketRepository ticketRepository,
            TaskRepository taskRepository,
            WorkReportEntryRepository workReportEntryRepository
    ) {
        this.workReportRepository = workReportRepository;
        this.workRepository = workRepository;
        this.ticketRepository = ticketRepository;
        this.taskRepository = taskRepository;
        this.workReportEntryRepository = workReportEntryRepository;
    }

    @Override
    @Transactional
    public WorkReport createForWork(UUID workId) {
        Work work = workRepository.findById(workId)
                .orElseThrow(() -> new NotFoundException("Work not found: " + workId));

        // 1-per-work: se già esiste, non crearne un altro
        workReportRepository.findByWork_Id(workId).ifPresent(r -> {
            throw new DuplicateKeyException("Work report already exists for work: " + workId);
        });

        WorkReport report = new WorkReport();
        report.setWork(work);

        // se WorkFromTicket -> collega ticket automaticamente
        if (work instanceof WorkFromTicket wft) {
            report.setTicket(wft.getTicket());
        }

        report.setTotalHours(BigDecimal.ZERO);
        report.setFinalized(false);
        report.setInvoiced(false);

        return workReportRepository.save(report);
    }

    @Override
    public WorkReport getByWorkIdOrThrow(UUID workId) {
        return workReportRepository.findByWork_Id(workId)
                .orElseThrow(() -> new NotFoundException("No report for work: " + workId));
    }

    @Override
    @Transactional
    public WorkReportEntry addEntryByWorkId(UUID workId, UUID taskId, BigDecimal hours) {
        WorkReport report = getByWorkIdOrThrow(workId);

        if (report.isFinalized()) {
            throw new ForbiddenException("Report is finalized, cannot add entries");
        }

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException("Task not found: " + taskId));

        WorkReportEntry entry = new WorkReportEntry();
        entry.setReport(report);
        entry.setTask(task);
        entry.setHours(hours);

        WorkReportEntry saved = workReportEntryRepository.save(entry);
        recalc(report);
        return saved;
    }

    @Override
    @Transactional
    public WorkReportEntry updateEntry(UUID workId, UUID entryId, BigDecimal hours) {
        WorkReport report = getByWorkIdOrThrow(workId);

        if (report.isFinalized()) {
            throw new ForbiddenException("Report is finalized, cannot update entries");
        }

        WorkReportEntry entry = workReportEntryRepository.findById(entryId)
                .orElseThrow(() -> new NotFoundException("Entry not found: " + entryId));

        // safety: entry deve appartenere al report di quel work
        if (!entry.getReport().getId().equals(report.getId())) {
            throw new RuntimeException("Entry does not belong to work report for work: " + workId);
        }

        entry.setHours(hours);
        WorkReportEntry saved = workReportEntryRepository.save(entry);
        recalc(report);
        return saved;
    }

    @Override
    @Transactional
    public void removeEntry(UUID workId, UUID entryId) {
        WorkReport report = getByWorkIdOrThrow(workId);

        if (report.isFinalized()) {
            throw new RuntimeException("Report is finalized, cannot remove entries");
        }

        WorkReportEntry entry = workReportEntryRepository.findById(entryId)
                .orElseThrow(() -> new NotFoundException("Entry not found: " + entryId));

        if (!entry.getReport().getId().equals(report.getId())) {
            throw new RuntimeException("Entry does not belong to work report for work: " + workId);
        }

        workReportEntryRepository.delete(entry);
        recalc(report);
    }

    @Override
    @Transactional
    public WorkReport finalizeByWorkId(UUID workId) {
        WorkReport report = getByWorkIdOrThrow(workId);

        if (report.isFinalized()) return report;

        recalc(report);
        report.setFinalized(true);
        report.setFinalizedAt(LocalDateTime.now());
        return workReportRepository.save(report);
    }

    @Override
    @Transactional
    public WorkReport invoiceByReportId(UUID reportId) {
        WorkReport report = workReportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Work report not found: " + reportId));

        if (report.isInvoiced()) return report;

        if (!report.isFinalized()) {
            throw new RuntimeException("Cannot invoice a non-finalized report");
        }

        report.setInvoiced(true);
        report.setInvoicedAt(LocalDateTime.now());
        return workReportRepository.save(report);
    }

    private void recalc(WorkReport report) {
        BigDecimal total = workReportEntryRepository.sumHoursByReportId(report.getId());
        report.setTotalHours(total);
        // report è managed in @Transactional, ma lascio save esplicito per chiarezza/coerenza
        workReportRepository.save(report);
    }
}