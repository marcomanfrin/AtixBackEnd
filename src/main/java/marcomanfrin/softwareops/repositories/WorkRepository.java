package marcomanfrin.softwareops.repositories;

import marcomanfrin.softwareops.entities.Work;
import marcomanfrin.softwareops.entities.users.SellerUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkRepository extends JpaRepository<Work, UUID> {
    Optional<Work> findByBidNumber(String bidNumber);
    Optional<Work> findByOrderNumber(String orderNumber);

    List<Work> findBySeller(SellerUser seller);
    List<Work> findByCompleted(boolean completed);
    List<Work> findByInvoiced(boolean invoiced);

    Page<Work> findByCompleted(boolean completed, Pageable pageable);

    @Query("SELECT w FROM Work w WHERE w.orderDate BETWEEN :startDate AND :endDate")
    List<Work> findByOrderDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT w FROM Work w WHERE w.completed = false AND w.expectedStartDate < :date")
    List<Work> findOverdueWorks(@Param("date") LocalDate date);
}
