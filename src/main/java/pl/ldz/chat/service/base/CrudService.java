package pl.ldz.chat.service.base;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CrudService<T, ID, REQ, RES> {

  RES create(REQ request);

  RES getById(ID id);

  Page<RES> getAll(Pageable pageable);

  RES update(ID id, REQ request);

  void delete(ID id);
}
