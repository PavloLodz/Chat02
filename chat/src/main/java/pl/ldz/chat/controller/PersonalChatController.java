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
import pl.ldz.chat.dto.PersonalChatRequestDto;
import pl.ldz.chat.dto.PersonalChatResponseDto;
import pl.ldz.chat.service.PersonalChatService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/personal-chats")
@RequiredArgsConstructor
@Tag(name = "Personal Chat", description = "Personal Chat Management API")
public class PersonalChatController {

  private final PersonalChatService personalChatService;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
  @Operation(summary = "Create a new personal chat")
  @ApiResponse(responseCode = "201", description = "Personal chat created")
  @ApiResponse(responseCode = "422", description = "Validation failed")
  public PersonalChatResponseDto create(@Valid @RequestBody PersonalChatRequestDto request) {
    return personalChatService.create(request);
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')") // Simplifying for now as per task list, though requirements mentioned participant check
  @Operation(summary = "Get personal chat by ID")
  @ApiResponse(responseCode = "200", description = "Personal chat found")
  @ApiResponse(responseCode = "404", description = "Personal chat not found")
  public PersonalChatResponseDto getById(@PathVariable UUID id) {
    return personalChatService.getById(id);
  }

  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Get all personal chats (paginated)")
  @ApiResponse(responseCode = "200", description = "List of personal chats")
  public Page<PersonalChatResponseDto> getAll(@ParameterObject Pageable pageable) {
    return personalChatService.getAll(pageable);
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Update personal chat")
  @ApiResponse(responseCode = "200", description = "Personal chat updated")
  @ApiResponse(responseCode = "404", description = "Personal chat not found")
  public PersonalChatResponseDto update(@PathVariable UUID id, @Valid @RequestBody PersonalChatRequestDto request) {
    return personalChatService.update(id, request);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Delete personal chat")
  @ApiResponse(responseCode = "204", description = "Personal chat deleted")
  @ApiResponse(responseCode = "404", description = "Personal chat not found")
  public void delete(@PathVariable UUID id) {
    personalChatService.delete(id);
  }
}
