package pl.ldz.chat.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.ldz.chat.dto.MessageRequestDto;
import pl.ldz.chat.dto.MessageResponseDto;
import pl.ldz.chat.service.MessageService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
@Tag(name = "Message", description = "Message Management API")
public class MessageController {

  private final MessageService messageService;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
  @Operation(summary = "Create a new message")
  @ApiResponse(responseCode = "201", description = "Message created")
  public MessageResponseDto create(@Valid @RequestBody MessageRequestDto request) {
    return messageService.create(request);
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
  @Operation(summary = "Get message by ID")
  public MessageResponseDto getById(@PathVariable UUID id) {
    return messageService.getById(id);
  }

  @GetMapping
  @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
  @Operation(summary = "Get all messages (paginated)")
  public Page<MessageResponseDto> getAll(@ParameterObject Pageable pageable) {
    return messageService.getAll(pageable);
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
  @Operation(summary = "Update message")
  public MessageResponseDto update(@PathVariable UUID id, @Valid @RequestBody MessageRequestDto request) {
    return messageService.update(id, request);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
  @Operation(summary = "Delete message")
  public void delete(@PathVariable UUID id) {
    messageService.delete(id);
  }
}
