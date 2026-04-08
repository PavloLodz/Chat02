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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.ldz.chat.dto.RoomMemberRequestDto;
import pl.ldz.chat.dto.RoomMemberResponseDto;
import pl.ldz.chat.service.RoomMemberService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/room-members")
@Tag(name = "Room Members", description = "Endpoints for managing chat room members")
@RequiredArgsConstructor
public class RoomMemberController {

  private final RoomMemberService roomMemberService;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
  @Operation(summary = "Add a member to a room")
  @ApiResponse(responseCode = "201", description = "Member added")
  public RoomMemberResponseDto create(@Valid @RequestBody RoomMemberRequestDto request) {
    return roomMemberService.create(request);
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAnyRole('VIEWER', 'USER', 'ADMIN', 'AUDITOR')")
  @Operation(summary = "Get room member by ID")
  @ApiResponse(responseCode = "200", description = "Member found")
  @ApiResponse(responseCode = "404", description = "Member not found")
  public RoomMemberResponseDto getById(@PathVariable UUID id) {
    return roomMemberService.getById(id);
  }

  @GetMapping
  @PreAuthorize("hasAnyRole('VIEWER', 'USER', 'ADMIN', 'AUDITOR')")
  @Operation(summary = "Get all room members (paginated)")
  @ApiResponse(responseCode = "200", description = "Page of room members")
  public Page<RoomMemberResponseDto> getAll(@ParameterObject Pageable pageable) {
    return roomMemberService.getAll(pageable);
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
  @Operation(summary = "Update room member (e.g., role)")
  @ApiResponse(responseCode = "200", description = "Member updated")
  @ApiResponse(responseCode = "404", description = "Member not found")
  public RoomMemberResponseDto update(@PathVariable UUID id, @Valid @RequestBody RoomMemberRequestDto request) {
    return roomMemberService.update(id, request);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
  @Operation(summary = "Remove member from room")
  @ApiResponse(responseCode = "204", description = "Member removed")
  @ApiResponse(responseCode = "404", description = "Member not found")
  public void delete(@PathVariable UUID id) {
    roomMemberService.delete(id);
  }

  @GetMapping("/room/{roomId}")
  @PreAuthorize("hasAnyRole('VIEWER', 'USER', 'ADMIN', 'AUDITOR')")
  @Operation(summary = "Get all members of a specific room")
  @ApiResponse(responseCode = "200", description = "List of room members")
  public ResponseEntity<List<RoomMemberResponseDto>> findByRoomId(@PathVariable UUID roomId) {
    return ResponseEntity.ok(roomMemberService.findByRoomId(roomId));
  }
}
