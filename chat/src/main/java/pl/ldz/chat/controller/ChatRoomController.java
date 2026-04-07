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
import pl.ldz.chat.dto.ChatRoomRequestDto;
import pl.ldz.chat.dto.ChatRoomResponseDto;
import pl.ldz.chat.entity.ChatRoom;
import pl.ldz.chat.service.base.CrudService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/chat-rooms")
@Tag(name = "ChatRooms", description = "Chat room management endpoints")
@RequiredArgsConstructor
public class ChatRoomController {

  private final CrudService<ChatRoom, UUID, ChatRoomRequestDto, ChatRoomResponseDto> chatRoomService;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
  @Operation(summary = "Create a new chat room")
  @ApiResponse(responseCode = "201", description = "Chat room created")
  @ApiResponse(responseCode = "422", description = "Validation error")
  public ChatRoomResponseDto create(@Valid @RequestBody ChatRoomRequestDto request) {
    return chatRoomService.create(request);
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAnyRole('VIEWER', 'USER', 'ADMIN', 'AUDITOR')")
  @Operation(summary = "Get chat room by ID")
  @ApiResponse(responseCode = "200", description = "Chat room found")
  @ApiResponse(responseCode = "404", description = "Chat room not found")
  public ChatRoomResponseDto getById(@PathVariable UUID id) {
    return chatRoomService.getById(id);
  }

  @GetMapping
  @PreAuthorize("hasAnyRole('VIEWER', 'USER', 'ADMIN', 'AUDITOR')")
  @Operation(summary = "Get all chat rooms (paginated)")
  @ApiResponse(responseCode = "200", description = "Page of chat rooms")
  public Page<ChatRoomResponseDto> getAll(@ParameterObject Pageable pageable) {
    return chatRoomService.getAll(pageable);
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
  @Operation(summary = "Update a chat room")
  @ApiResponse(responseCode = "200", description = "Chat room updated")
  @ApiResponse(responseCode = "404", description = "Chat room not found")
  @ApiResponse(responseCode = "422", description = "Validation error")
  public ChatRoomResponseDto update(@PathVariable UUID id, @Valid @RequestBody ChatRoomRequestDto request) {
    return chatRoomService.update(id, request);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Delete a chat room")
  @ApiResponse(responseCode = "204", description = "Chat room deleted")
  @ApiResponse(responseCode = "404", description = "Chat room not found")
  public void delete(@PathVariable UUID id) {
    chatRoomService.delete(id);
  }
}
