package pl.ldz.chat.repository;

import pl.ldz.chat.entity.Attachment;
import pl.ldz.chat.repository.base.AbstractRepository;
import java.util.UUID;

public interface AttachmentRepository extends AbstractRepository<Attachment, UUID> {
}
