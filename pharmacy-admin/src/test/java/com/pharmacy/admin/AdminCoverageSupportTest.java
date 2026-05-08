package com.pharmacy.admin;

import com.pharmacy.admin.entity.Category;
import com.pharmacy.admin.entity.InventoryBatch;
import com.pharmacy.admin.entity.Medicine;
import com.pharmacy.admin.entity.Order;
import com.pharmacy.admin.entity.Prescription;
import com.pharmacy.admin.enums.OrderStatus;
import com.pharmacy.admin.enums.PrescriptionStatus;
import com.pharmacy.admin.enums.Role;
import com.pharmacy.admin.exception.GlobalExceptionHandler;
import com.pharmacy.admin.security.JwtAuthFilter;
import com.pharmacy.admin.security.JwtUtil;
import com.pharmacy.admin.service.UserDetailsServiceImpl;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminCoverageSupportTest {

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
            PharmacyAdminApplication.main(args);

            springApplication.verify(() ->
                SpringApplication.run(PharmacyAdminApplication.class, args));
        }
    }

    @Test
    void jwtUtil_ShouldExtractAndValidateToken() {
        JwtUtil jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", SECRET);
        ReflectionTestUtils.setField(jwtUtil, "expiration", 60_000L);

        UserDetails userDetails = org.springframework.security.core.userdetails.User
            .withUsername("admin@pharmacy.com")
            .password("password")
            .authorities("ROLE_ADMIN")
            .build();

        String token = createToken("admin@pharmacy.com",
            new Date(System.currentTimeMillis() + 60_000));

        assertThat(jwtUtil.extractUsername(token)).isEqualTo("admin@pharmacy.com");
        assertThat(jwtUtil.extractExpiration(token)).isAfter(new Date());
        assertThat(jwtUtil.validateToken(token, userDetails)).isTrue();
    }

    @Test
    void jwtAuthFilter_ShouldHandleMissingInvalidAndValidTokens() throws Exception {
        JwtUtil jwtUtil = mock(JwtUtil.class);
        UserDetailsServiceImpl userDetailsService = mock(UserDetailsServiceImpl.class);
        JwtAuthFilter filter = new JwtAuthFilter();
        ReflectionTestUtils.setField(filter, "jwtUtil", jwtUtil);
        ReflectionTestUtils.setField(filter, "userDetailsService", userDetailsService);

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
            .withUsername("admin@pharmacy.com")
            .password("password")
            .authorities("ROLE_ADMIN")
            .build();
        when(jwtUtil.extractUsername("valid")).thenReturn("admin@pharmacy.com");
        when(userDetailsService.loadUserByUsername("admin@pharmacy.com")).thenReturn(userDetails);
        when(jwtUtil.validateToken("valid", userDetails)).thenReturn(true);

        filter.doFilter(validRequest, new MockHttpServletResponse(), new MockFilterChain());
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();

        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken("existing", null, List.of()));
        filter.doFilter(validRequest, new MockHttpServletResponse(), new MockFilterChain());
        verify(jwtUtil, atLeastOnce()).extractUsername("valid");
    }

    @Test
    void globalExceptionHandler_ShouldBuildExpectedResponses() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        Map<String, Object> runtimeBody = handler.handleRuntime(
            new RuntimeException("bad request")).getBody();
        Map<String, Object> generalBody = handler.handleGeneral(
            new Exception("general")).getBody();

        assertThat(runtimeBody.get("status")).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(runtimeBody.get("message")).isEqualTo("bad request");
        assertThat(generalBody.get("status")).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(generalBody.get("message")).isEqualTo("Something went wrong");
    }

    @Test
    void entityAndEnumTypes_ShouldStoreValues() {
        LocalDateTime now = LocalDateTime.now();
        LocalDate manufactureDate = LocalDate.now().minusDays(10);
        LocalDate expiryDate = LocalDate.now().plusDays(10);

        assertThat(new Category().isActive()).isTrue();
        assertThat(new Medicine().isActive()).isTrue();
        assertThat(new Prescription().getStatus()).isEqualTo(PrescriptionStatus.PENDING);

        Category category = new Category();
        category.setId(1L);
        category.setName("Pain Relief");
        category.setDescription("Desc");
        category.setImageUrl("/image.png");
        category.setActive(true);
        category.setCreatedAt(now);

        Medicine medicine = new Medicine();
        medicine.setId(2L);
        medicine.setName("Paracetamol");
        medicine.setDescription("Desc");
        medicine.setCategory(category);
        medicine.setManufacturer("Maker");
        medicine.setPrice(BigDecimal.TEN);
        medicine.setStock(12);
        medicine.setRequiresPrescription(false);
        medicine.setImageUrl("/medicine.png");
        medicine.setDosageInfo("daily");
        medicine.setSideEffects("none");
        medicine.setActive(true);
        ReflectionTestUtils.setField(medicine, "createdAt", now.plusHours(1));
        ReflectionTestUtils.setField(medicine, "updatedAt", now.plusHours(2));

        InventoryBatch batch = new InventoryBatch();
        batch.setId(3L);
        batch.setMedicine(medicine);
        batch.setBatchNumber("B-1");
        batch.setQuantity(10);
        batch.setExpiryDate(expiryDate);
        batch.setManufactureDate(manufactureDate);
        batch.setAddedAt(now.plusHours(3));

        Prescription prescription = new Prescription();
        prescription.setId(4L);
        prescription.setUserId(20L);
        prescription.setOrderId(30L);
        prescription.setFileUrl("/rx.pdf");
        prescription.setFileName("rx.pdf");
        prescription.setFileType("application/pdf");
        prescription.setStatus(PrescriptionStatus.APPROVED);
        prescription.setUploadedAt(now.plusHours(4));
        prescription.setReviewedAt(now.plusHours(5));
        prescription.setReviewNote("ok");

        Order order = new Order();
        order.setId(5L);
        order.setUserEmail("user@pharmacy.com");
        order.setStatus(OrderStatus.PAID);
        order.setTotalAmount(BigDecimal.ONE);
        order.setDeliverySlot("10AM-12PM");
        order.setPrescriptionId(40L);
        order.setCreatedAt(now.plusHours(6));
        order.setUpdatedAt(now.plusHours(7));

        assertThat(Role.values()).containsExactly(Role.CUSTOMER, Role.ADMIN);
        assertThat(category.getId()).isEqualTo(1L);
        assertThat(category.getName()).isEqualTo("Pain Relief");
        assertThat(category.getDescription()).isEqualTo("Desc");
        assertThat(category.getImageUrl()).isEqualTo("/image.png");
        assertThat(category.isActive()).isTrue();
        assertThat(category.getCreatedAt()).isEqualTo(now);

        assertThat(medicine.getId()).isEqualTo(2L);
        assertThat(medicine.getName()).isEqualTo("Paracetamol");
        assertThat(medicine.getDescription()).isEqualTo("Desc");
        assertThat(medicine.getCategory()).isSameAs(category);
        assertThat(medicine.getManufacturer()).isEqualTo("Maker");
        assertThat(medicine.getPrice()).isEqualTo(BigDecimal.TEN);
        assertThat(medicine.getStock()).isEqualTo(12);
        assertThat(medicine.isRequiresPrescription()).isFalse();
        assertThat(medicine.getImageUrl()).isEqualTo("/medicine.png");
        assertThat(medicine.getDosageInfo()).isEqualTo("daily");
        assertThat(medicine.getSideEffects()).isEqualTo("none");
        assertThat(medicine.isActive()).isTrue();
        assertThat(medicine.getCreatedAt()).isEqualTo(now.plusHours(1));
        assertThat(medicine.getUpdatedAt()).isEqualTo(now.plusHours(2));

        assertThat(batch.getId()).isEqualTo(3L);
        assertThat(batch.getMedicine()).isSameAs(medicine);
        assertThat(batch.getBatchNumber()).isEqualTo("B-1");
        assertThat(batch.getQuantity()).isEqualTo(10);
        assertThat(batch.getExpiryDate()).isEqualTo(expiryDate);
        assertThat(batch.getManufactureDate()).isEqualTo(manufactureDate);
        assertThat(batch.getAddedAt()).isEqualTo(now.plusHours(3));

        assertThat(prescription.getId()).isEqualTo(4L);
        assertThat(prescription.getUserId()).isEqualTo(20L);
        assertThat(prescription.getOrderId()).isEqualTo(30L);
        assertThat(prescription.getFileUrl()).isEqualTo("/rx.pdf");
        assertThat(prescription.getFileName()).isEqualTo("rx.pdf");
        assertThat(prescription.getFileType()).isEqualTo("application/pdf");
        assertThat(prescription.getStatus()).isEqualTo(PrescriptionStatus.APPROVED);
        assertThat(prescription.getUploadedAt()).isEqualTo(now.plusHours(4));
        assertThat(prescription.getReviewedAt()).isEqualTo(now.plusHours(5));
        assertThat(prescription.getReviewNote()).isEqualTo("ok");

        assertThat(order.getId()).isEqualTo(5L);
        assertThat(order.getUserEmail()).isEqualTo("user@pharmacy.com");
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
        assertThat(order.getTotalAmount()).isEqualTo(BigDecimal.ONE);
        assertThat(order.getDeliverySlot()).isEqualTo("10AM-12PM");
        assertThat(order.getPrescriptionId()).isEqualTo(40L);
        assertThat(order.getCreatedAt()).isEqualTo(now.plusHours(6));
        assertThat(order.getUpdatedAt()).isEqualTo(now.plusHours(7));
    }

    private String createToken(String subject, Date expiration) {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET);
        return Jwts.builder()
            .subject(subject)
            .issuedAt(new Date())
            .expiration(expiration)
            .signWith(Keys.hmacShaKeyFor(keyBytes))
            .compact();
    }
}
