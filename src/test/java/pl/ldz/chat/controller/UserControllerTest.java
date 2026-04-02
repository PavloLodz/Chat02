package pl.ldz.chat.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import pl.ldz.chat.dto.UserRequestDto;
import pl.ldz.chat.dto.UserResponseDto;
import pl.ldz.chat.exception.EntityNotFoundException;
import pl.ldz.chat.exception.GlobalExceptionHandler;
import pl.ldz.chat.service.base.CrudService;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private static final String BASE_URL = "/api/v1/users";

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private CrudService<pl.ldz.chat.entity.User, UUID, UserRequestDto, UserResponseDto> userService;

    @BeforeEach
    void setUp() {
        UserController userController = new UserController(userService);
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private UserRequestDto buildRequest(String suffix) {
        return new UserRequestDto(
                "user_" + suffix,
                "user_" + suffix + "@example.com",
                "hash_" + suffix,
                "Display " + suffix,
                null,
                false,
                "USER"
        );
    }

    private UserResponseDto buildResponse(UUID id, String suffix) {
        return new UserResponseDto(
                id,
                0L,
                Instant.now(),
                Instant.now(),
                "user_" + suffix,
                "user_" + suffix + "@example.com",
                "Display " + suffix,
                null,
                false,
                "USER"
        );
    }

    // -------------------------------------------------------------------------
    // POST /api/v1/users
    // -------------------------------------------------------------------------

    @Test
    void create_shouldReturn201AndResponseBody() throws Exception {
        UUID id = UUID.randomUUID();
        UserRequestDto request = buildRequest("1");
        UserResponseDto response = buildResponse(id, "1");

        when(userService.create(any(UserRequestDto.class))).thenReturn(response);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.username").value("user_1"))
                .andExpect(jsonPath("$.email").value("user_1@example.com"));

        verify(userService).create(any(UserRequestDto.class));
    }

    @Test
    void create_shouldReturn422WhenRequestBodyIsInvalid() throws Exception {
        // Blank username violates @NotBlank
        UserRequestDto invalid = new UserRequestDto("", "bad-email", "", null, null, false, null);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.title").value("Validation Error"))
                .andExpect(jsonPath("$.errors.username").exists())
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.passwordHash").exists());

        verifyNoInteractions(userService);
    }


    // -------------------------------------------------------------------------
    // GET /api/v1/users/{id}
    // -------------------------------------------------------------------------

    @Test
    void getById_shouldReturn200AndResponseBody() throws Exception {
        UUID id = UUID.randomUUID();
        UserResponseDto response = buildResponse(id, "3");

        when(userService.getById(id)).thenReturn(response);

        mockMvc.perform(get(BASE_URL + "/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.username").value("user_3"));

        verify(userService).getById(id);
    }

    @Test
    void getById_shouldReturn404WhenUserNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(userService.getById(id)).thenThrow(new EntityNotFoundException("User", id));

        mockMvc.perform(get(BASE_URL + "/{id}", id))
                .andExpect(status().isNotFound());
    }


    // -------------------------------------------------------------------------
    // GET /api/v1/users
    // -------------------------------------------------------------------------

    @Test
    void getAll_shouldReturn200AndPagedContent() throws Exception {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        List<UserResponseDto> users = List.of(buildResponse(id1, "4"), buildResponse(id2, "5"));
        Page<UserResponseDto> page = new PageImpl<>(users, PageRequest.of(0, 10), 2);

        when(userService.getAll(any())).thenReturn(page);

        mockMvc.perform(get(BASE_URL).param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].username").value("user_4"))
                .andExpect(jsonPath("$.content[1].username").value("user_5"));

        verify(userService).getAll(any());
    }

    @Test
    void getAll_shouldReturn200ForAuditorRole() throws Exception {
        Page<UserResponseDto> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
        when(userService.getAll(any())).thenReturn(emptyPage);

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk());
    }

    // -------------------------------------------------------------------------
    // PUT /api/v1/users/{id}
    // -------------------------------------------------------------------------

    @Test
    void update_shouldReturn200AndUpdatedResponseBody() throws Exception {
        UUID id = UUID.randomUUID();
        UserRequestDto request = buildRequest("6");
        UserResponseDto response = buildResponse(id, "6");

        when(userService.update(eq(id), any(UserRequestDto.class))).thenReturn(response);

        mockMvc.perform(put(BASE_URL + "/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.username").value("user_6"));

        verify(userService).update(eq(id), any(UserRequestDto.class));
    }

    @Test
    void update_shouldReturn404WhenUserNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        UserRequestDto request = buildRequest("7");

        when(userService.update(eq(id), any())).thenThrow(new EntityNotFoundException("User", id));

        mockMvc.perform(put(BASE_URL + "/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }


    // -------------------------------------------------------------------------
    // DELETE /api/v1/users/{id}
    // -------------------------------------------------------------------------

    @Test
    void delete_shouldReturn204WhenUserDeleted() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(userService).delete(id);

        mockMvc.perform(delete(BASE_URL + "/{id}", id))
                .andExpect(status().isNoContent());

        verify(userService).delete(id);
    }

    @Test
    void delete_shouldReturn404WhenUserNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new EntityNotFoundException("User", id)).when(userService).delete(id);

        mockMvc.perform(delete(BASE_URL + "/{id}", id))
                .andExpect(status().isNotFound());
    }

}
