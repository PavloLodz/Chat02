package pl.ldz.chat.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.ldz.chat.dto.AttachmentRequestDto;
import pl.ldz.chat.dto.AttachmentResponseDto;
import pl.ldz.chat.service.AttachmentService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/attachments")
@Tag(name = "Attachments", description = "Attachment management endpoints")
@RequiredArgsConstructor
public class AttachmentController {

  private final AttachmentService attachmentService;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
  @Operation(summary = "Create a new attachment")
  @ApiResponse(responseCode = "201", description = "Attachment created")
  @ApiResponse(responseCode = "400", description = "Validation error")
  @ApiResponse(responseCode = "404", description = "Message not found")
  public AttachmentResponseDto create(@Valid @RequestBody AttachmentRequestDto request) {
    return attachmentService.create(request);
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAnyRole('VIEWER', 'USER', 'ADMIN', 'AUDITOR')")
  @Operation(summary = "Get attachment by ID")
  @ApiResponse(responseCode = "200", description = "Attachment found")
  @ApiResponse(responseCode = "404", description = "Attachment not found")
  public AttachmentResponseDto getById(@PathVariable UUID id) {
    return attachmentService.getById(id);
  }

  @GetMapping
  @PreAuthorize("hasAnyRole('VIEWER', 'USER', 'ADMIN', 'AUDITOR')")
  @Operation(summary = "Get all attachments (paginated)")
  @ApiResponse(responseCode = "200", description = "Page of attachments")
  public Page<AttachmentResponseDto> getAll(Pageable pageable) {
    return attachmentService.getAll(pageable);
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
  @Operation(summary = "Update an attachment")
  @ApiResponse(responseCode = "200", description = "Attachment updated")
  @ApiResponse(responseCode = "404", description = "Attachment or Message not found")
  @ApiResponse(responseCode = "400", description = "Validation error")
  public AttachmentResponseDto update(@PathVariable UUID id, @Valid @RequestBody AttachmentRequestDto request) {
    return attachmentService.update(id, request);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
  @Operation(summary = "Delete an attachment")
  @ApiResponse(responseCode = "204", description = "Attachment deleted")
  @ApiResponse(responseCode = "404", description = "Attachment not found")
  public void delete(@PathVariable UUID id) {
    attachmentService.delete(id);
  }
}
