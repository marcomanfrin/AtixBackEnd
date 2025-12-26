package marcomanfrin.atixbackend.repositories;

import marcomanfrin.atixbackend.entities.Attachment;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AttachmentRepository extends JpaRepository<@NonNull Attachment, @NonNull UUID> {
}
