package com.example.demo;

import com.example.demo.catalog.CatalogServiceApplication;
import com.example.demo.config.OpenApiSecurityConfig;
import com.example.demo.dto.CategoryDTO;
import com.example.demo.dto.MedicineDTO;
import com.example.demo.dto.MedicineRequest;
import com.example.demo.dto.PrescriptionResponseDTO;
import com.example.demo.entitty.Category;
import com.example.demo.entitty.InventoryBatch;
import com.example.demo.entitty.Medicine;
import com.example.demo.entitty.User;
import com.example.demo.enums.PrescriptionStatus;
import com.example.demo.enums.Role;
import com.example.demo.exception.GlobalExceptionHandler;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtAuthFilter;
import com.example.demo.security.JwtUtil;
import com.example.demo.service.UserDetailsServiceImpl;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CatalogCoverageSupportTest {

    private static final String SECRET =
        "cGhhcm1hY3ktc3VwZXItc2VjcmV0LWtleS0yMDI0LXNwcmluZy1ib290LWp3dA==";

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void main_ShouldDelegateToSpringApplication() {
        String[] args = {"--test"};

        try (MockedStatic<SpringApplication> springApplication =
                     mockStatic(SpringApplication.class)) {
            CatalogServiceApplication.main(args);

            springApplication.verify(() ->
                SpringApplication.run(CatalogServiceApplication.class, args));
        }
    }

    @Test
    void openApiConfig_ShouldExposeGatewayFriendlyServerAndBearerSecurity() {
        OpenApiSecurityConfig config = new OpenApiSecurityConfig();

        OpenAPI openAPI = config.serviceOpenAPI();

        assertThat(openAPI.getServers()).singleElement()
            .extracting("url").isEqualTo("/");
        assertThat(openAPI.getSecurity()).hasSize(1);
    }

    @Test
    void jwtUtil_ShouldGenerateExtractAndValidateToken() {
        JwtUtil jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", SECRET);
        ReflectionTestUtils.setField(jwtUtil, "expiration", 60_000L);

        UserDetails userDetails = org.springframework.security.core.userdetails.User
            .withUsername("user@pharmacy.com")
            .password("password")
            .authorities("ROLE_CUSTOMER")
            .build();

        String token = jwtUtil.generateToken(userDetails);

        assertThat(jwtUtil.extractUsername(token)).isEqualTo("user@pharmacy.com");
        assertThat(jwtUtil.extractExpiration(token)).isAfter(new Date());
        assertThat(jwtUtil.validateToken(token, "user@pharmacy.com")).isTrue();
        assertThat(jwtUtil.extractAuthorities(token)).extracting("authority")
            .containsExactly("ROLE_CUSTOMER");
    }

    @Test
    void jwtAuthFilter_ShouldHandleMissingInvalidAndValidTokens() throws Exception {
        JwtUtil jwtUtil = mock(JwtUtil.class);
        JwtAuthFilter filter = new JwtAuthFilter();
        ReflectionTestUtils.setField(filter, "jwtUtil", jwtUtil);

        filter.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(),
            new MockFilterChain());
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();

        MockHttpServletRequest invalidRequest = new MockHttpServletRequest();
        invalidRequest.addHeader("Authorization", "Bearer broken");
        when(jwtUtil.extractUsername("broken")).thenThrow(new IllegalArgumentException("bad"));
        filter.doFilter(invalidRequest, new MockHttpServletResponse(), new MockFilterChain());
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();

        MockHttpServletRequest validRequest = new MockHttpServletRequest();
        validRequest.addHeader("Authorization", "Bearer valid");
        UserDetails userDetails = org.springframework.security.core.userdetails.User
            .withUsername("user@pharmacy.com")
            .password("password")
            .authorities("ROLE_CUSTOMER")
            .build();
        when(jwtUtil.extractUsername("valid")).thenReturn("user@pharmacy.com");
        when(jwtUtil.validateToken("valid", "user@pharmacy.com")).thenReturn(true);
        when(jwtUtil.extractAuthorities("valid"))
            .thenReturn(java.util.List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER")));

        filter.doFilter(validRequest, new MockHttpServletResponse(), new MockFilterChain());
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();

        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken("existing", null, java.util.List.of()));
        filter.doFilter(validRequest, new MockHttpServletResponse(), new MockFilterChain());
        verify(jwtUtil, atLeastOnce()).extractUsername("valid");
    }

    @Test
    void userDetailsService_ShouldLoadExistingUser() {
        UserRepository userRepository = mock(UserRepository.class);
        UserDetailsServiceImpl service = new UserDetailsServiceImpl();
        ReflectionTestUtils.setField(service, "userRepository", userRepository);

        User user = new User();
        user.setId(1L);
        user.setName("Catalog User");
        user.setEmail("user@pharmacy.com");
        user.setMobile("9999999999");
        user.setPassword("secret");
        user.setRole(Role.ADMIN);
        user.setActive(true);

        when(userRepository.findByEmail("user@pharmacy.com")).thenReturn(Optional.of(user));

        UserDetails loaded = service.loadUserByUsername("user@pharmacy.com");
        assertThat(loaded.getUsername()).isEqualTo("user@pharmacy.com");
        assertThat(loaded.getAuthorities()).extracting("authority")
            .containsExactly("ROLE_ADMIN");
    }

    @Test
    void globalExceptionHandler_ShouldBuildValidationAndErrorResponses() throws Exception {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        BeanPropertyBindingResult bindingResult =
            new BeanPropertyBindingResult(new Object(), "request");
        bindingResult.addError(new FieldError("request", "file", "required"));
        Method method = HandlerStub.class.getDeclaredMethod("upload", String.class);
        MethodArgumentNotValidException validationException =
            new MethodArgumentNotValidException(new org.springframework.core.MethodParameter(method, 0),
                bindingResult);

        Map<String, Object> validationBody = handler.handleValidation(validationException).getBody();
        Map<String, Object> fileSizeBody = handler.handleFileSize(
            new MaxUploadSizeExceededException(5_000_000)).getBody();
        Map<String, Object> runtimeBody = handler.handleRuntime(
            new RuntimeException("bad request")).getBody();
        Map<String, Object> generalBody = handler.handleGeneral(new Exception("general")).getBody();

        assertThat(validationBody.get("status")).isEqualTo(400);
        assertThat(((Map<?, ?>) validationBody.get("fields")).get("file")).isEqualTo("required");
        assertThat(fileSizeBody.get("message")).isEqualTo("File size exceeds 5MB limit");
        assertThat(runtimeBody.get("status")).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(generalBody.get("status")).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    @Test
    void entityAndDtoTypes_ShouldStoreValues() {
        LocalDateTime now = LocalDateTime.now();

        Category category = new Category();
        category.setId(9L);
        category.setName("Pain Relief");
        category.setDescription("Desc");
        category.setImageUrl("/category.png");
        category.setActive(true);
        category.setCreatedAt(now.minusDays(1));

        Medicine medicine = new Medicine();
        medicine.setId(10L);
        medicine.setName("Paracetamol");
        medicine.setDescription("Pain reliever");
        medicine.setCategory(category);
        medicine.setManufacturer("Maker");

        InventoryBatch batch = new InventoryBatch();
        batch.setId(11L);
        batch.setMedicine(medicine);
        batch.setBatchNumber("B-1");
        batch.setQuantity(10);
        batch.setExpiryDate(LocalDate.now().plusDays(30));
        batch.setManufactureDate(LocalDate.now().minusDays(30));
        batch.setAddedAt(now);

        User user = new User();
        user.setId(12L);
        user.setName("Catalog User");
        user.setEmail("user@pharmacy.com");
        user.setMobile("9999999999");
        user.setPassword("secret");
        user.setRole(Role.CUSTOMER);
        user.setActive(true);
        user.setCreatedAt(now.plusDays(1));
        user.setUpdatedAt(now.plusDays(2));

        PrescriptionResponseDTO dto = new PrescriptionResponseDTO();
        dto.setId(13L);
        dto.setUserId(12L);
        dto.setOrderId(14L);
        dto.setFileName("rx.pdf");
        dto.setFileUrl("/rx.pdf");
        dto.setFileType("application/pdf");
        dto.setStatus(PrescriptionStatus.APPROVED);
        dto.setUploadedAt(now.plusDays(3));
        dto.setReviewedAt(now.plusDays(4));
        dto.setReviewNote("ok");

        CategoryDTO categoryDTO = new CategoryDTO();
        categoryDTO.setId(21L);
        categoryDTO.setName("Pain Relief");
        categoryDTO.setDescription("Desc");
        categoryDTO.setImageUrl("/category.png");

        MedicineDTO medicineDTO = new MedicineDTO();
        medicineDTO.setId(22L);
        medicineDTO.setName("Paracetamol");
        medicineDTO.setDescription("Pain reliever");
        medicineDTO.setCategory(categoryDTO);
        medicineDTO.setManufacturer("Maker");
        medicineDTO.setPrice(java.math.BigDecimal.TEN);
        medicineDTO.setStock(20);
        medicineDTO.setRequiresPrescription(false);
        medicineDTO.setImageUrl("/medicine.png");
        medicineDTO.setDosageInfo("daily");
        medicineDTO.setSideEffects("none");
        medicineDTO.setActive(true);

        MedicineRequest medicineRequest = new MedicineRequest();
        medicineRequest.setName("Ibuprofen");
        medicineRequest.setDescription("Anti inflammatory");
        medicineRequest.setCategoryId(21L);
        medicineRequest.setManufacturer("Maker");
        medicineRequest.setPrice(java.math.BigDecimal.ONE);
        medicineRequest.setStock(15);
        medicineRequest.setRequiresPrescription(true);
        medicineRequest.setImageUrl("/ibuprofen.png");
        medicineRequest.setDosageInfo("after food");
        medicineRequest.setSideEffects("drowsy");

        assertThat(category.getId()).isEqualTo(9L);
        assertThat(category.getName()).isEqualTo("Pain Relief");
        assertThat(category.getDescription()).isEqualTo("Desc");
        assertThat(category.getImageUrl()).isEqualTo("/category.png");
        assertThat(category.isActive()).isTrue();
        assertThat(category.getCreatedAt()).isEqualTo(now.minusDays(1));
        assertThat(batch.getMedicine()).isSameAs(medicine);
        assertThat(batch.getId()).isEqualTo(11L);
        assertThat(batch.getBatchNumber()).isEqualTo("B-1");
        assertThat(batch.getQuantity()).isEqualTo(10);
        assertThat(batch.getExpiryDate()).isAfter(LocalDate.now());
        assertThat(batch.getManufactureDate()).isBefore(LocalDate.now());
        assertThat(batch.getAddedAt()).isEqualTo(now);
        assertThat(user.getId()).isEqualTo(12L);
        assertThat(user.getName()).isEqualTo("Catalog User");
        assertThat(user.getEmail()).isEqualTo("user@pharmacy.com");
        assertThat(user.getMobile()).isEqualTo("9999999999");
        assertThat(user.getPassword()).isEqualTo("secret");
        assertThat(user.getRole()).isEqualTo(Role.CUSTOMER);
        assertThat(user.isActive()).isTrue();
        assertThat(user.getCreatedAt()).isEqualTo(now.plusDays(1));
        assertThat(user.getUpdatedAt()).isEqualTo(now.plusDays(2));
        assertThat(dto.getId()).isEqualTo(13L);
        assertThat(dto.getUserId()).isEqualTo(12L);
        assertThat(dto.getOrderId()).isEqualTo(14L);
        assertThat(dto.getFileName()).isEqualTo("rx.pdf");
        assertThat(dto.getFileUrl()).isEqualTo("/rx.pdf");
        assertThat(dto.getFileType()).isEqualTo("application/pdf");
        assertThat(dto.getStatus()).isEqualTo(PrescriptionStatus.APPROVED);
        assertThat(dto.getUploadedAt()).isEqualTo(now.plusDays(3));
        assertThat(dto.getReviewedAt()).isEqualTo(now.plusDays(4));
        assertThat(dto.getReviewNote()).isEqualTo("ok");
        assertThat(medicineDTO.getId()).isEqualTo(22L);
        assertThat(medicineDTO.getName()).isEqualTo("Paracetamol");
        assertThat(medicineDTO.getDescription()).isEqualTo("Pain reliever");
        assertThat(medicineDTO.getCategory()).isSameAs(categoryDTO);
        assertThat(medicineDTO.getManufacturer()).isEqualTo("Maker");
        assertThat(medicineDTO.getPrice()).isEqualTo(java.math.BigDecimal.TEN);
        assertThat(medicineDTO.getStock()).isEqualTo(20);
        assertThat(medicineDTO.isRequiresPrescription()).isFalse();
        assertThat(medicineDTO.getImageUrl()).isEqualTo("/medicine.png");
        assertThat(medicineDTO.getDosageInfo()).isEqualTo("daily");
        assertThat(medicineDTO.getSideEffects()).isEqualTo("none");
        assertThat(medicineDTO.isActive()).isTrue();
        assertThat(medicineRequest.getName()).isEqualTo("Ibuprofen");
        assertThat(medicineRequest.getDescription()).isEqualTo("Anti inflammatory");
        assertThat(medicineRequest.getCategoryId()).isEqualTo(21L);
        assertThat(medicineRequest.getManufacturer()).isEqualTo("Maker");
        assertThat(medicineRequest.getPrice()).isEqualTo(java.math.BigDecimal.ONE);
        assertThat(medicineRequest.getStock()).isEqualTo(15);
        assertThat(medicineRequest.isRequiresPrescription()).isTrue();
        assertThat(medicineRequest.getImageUrl()).isEqualTo("/ibuprofen.png");
        assertThat(medicineRequest.getDosageInfo()).isEqualTo("after food");
        assertThat(medicineRequest.getSideEffects()).isEqualTo("drowsy");
    }

    private static class HandlerStub {
        @SuppressWarnings("unused")
        public void upload(String file) {
        }
    }
}
