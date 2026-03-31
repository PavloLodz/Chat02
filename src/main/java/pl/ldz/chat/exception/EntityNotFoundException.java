package pl.ldz.chat.exception;

public class EntityNotFoundException extends RuntimeException {

  public EntityNotFoundException(String entityName, Object id) {
    super(entityName + " with id '" + id + "' not found");
  }
}
