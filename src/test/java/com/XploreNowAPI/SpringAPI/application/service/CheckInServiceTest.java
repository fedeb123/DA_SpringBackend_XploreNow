package com.XploreNowAPI.SpringAPI.application.service;

import com.XploreNowAPI.SpringAPI.application.dto.checkin.CheckInCodeResponseDto;
import com.XploreNowAPI.SpringAPI.application.dto.checkin.CheckInScanRequest;
import com.XploreNowAPI.SpringAPI.application.dto.checkin.CheckInScanResponseDto;
import com.XploreNowAPI.SpringAPI.application.dto.reservation.VoucherDto;
import com.XploreNowAPI.SpringAPI.domain.model.entity.Activity;
import com.XploreNowAPI.SpringAPI.domain.model.entity.ActivitySchedule;
import com.XploreNowAPI.SpringAPI.domain.model.entity.AppUser;
import com.XploreNowAPI.SpringAPI.domain.model.entity.CheckIn;
import com.XploreNowAPI.SpringAPI.domain.model.entity.Destination;
import com.XploreNowAPI.SpringAPI.domain.model.entity.GuideProfile;
import com.XploreNowAPI.SpringAPI.domain.model.entity.Reservation;
import com.XploreNowAPI.SpringAPI.domain.model.enumtype.ActivityCategory;
import com.XploreNowAPI.SpringAPI.domain.model.enumtype.ActivityLanguage;
import com.XploreNowAPI.SpringAPI.domain.model.enumtype.CheckInStatus;
import com.XploreNowAPI.SpringAPI.domain.model.enumtype.ReservationStatus;
import com.XploreNowAPI.SpringAPI.domain.repository.ActivityScheduleRepository;
import com.XploreNowAPI.SpringAPI.domain.repository.CheckInRepository;
import com.XploreNowAPI.SpringAPI.domain.repository.ReservationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CheckInServiceTest {

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ActivityScheduleRepository activityScheduleRepository;

    @Mock
    private QrCodeSigner qrCodeSigner;

    private final Map<Long, Boolean> checkInExistsByReservationId = new HashMap<>();

    @Test
    void getVoucher_ReturnsCompleteVoucher() {
        CheckInService checkInService = newService();
        AppUser currentUser = user(10L, "Ana", "Gomez");
        Reservation reservation = reservation(currentUser, ReservationStatus.CONFIRMED);

        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(reservationRepository.findById(50L)).thenReturn(Optional.of(reservation));
        checkInExistsByReservationId.put(50L, true);

        VoucherDto voucher = checkInService.getVoucher(50L);

        assertEquals(50L, voucher.reservationId());
        assertEquals("Free Tour Centro Historico", voucher.activityName());
        assertEquals("Maria Perez", voucher.guideName());
        assertEquals(true, voucher.checkedIn());
    }

    @Test
    void generateCheckInCode_ReturnsQrContentAndExpiration() {
        CheckInService checkInService = newService();
        ActivitySchedule schedule = reservationSchedule();
        when(activityScheduleRepository.findById(5L)).thenReturn(Optional.of(schedule));
        when(qrCodeSigner.sign(eq(5L), any(Instant.class))).thenReturn("qr-content");

        CheckInCodeResponseDto response = checkInService.generateCheckInCode(5L);

        assertEquals(5L, response.scheduleId());
        assertEquals("qr-content", response.qrContent());
        assertNotNull(response.expiresAt());
    }

    @Test
    void scan_WhenSuccessful_CreatesCheckInAndReturnsConfirmation() {
        CheckInService checkInService = newService();
        AppUser currentUser = user(10L, "Ana", "Gomez");
        Reservation reservation = reservation(currentUser, ReservationStatus.CONFIRMED);
        QrCodeSigner.ParsedQrPayload payload = new QrCodeSigner.ParsedQrPayload(
                5L,
                LocalDateTime.now().plusMinutes(10).atZone(ZoneId.systemDefault()).toEpochSecond()
        );

        when(qrCodeSigner.verify("qr-content")).thenReturn(payload);
        when(qrCodeSigner.isExpired(payload)).thenReturn(false);
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(reservationRepository.findById(50L)).thenReturn(Optional.of(reservation));

        CheckInScanResponseDto response = checkInService.scan(new CheckInScanRequest(50L, "qr-content"));

        assertEquals(CheckInStatus.CONFIRMED, response.status());
        assertEquals(50L, response.reservationId());
        assertEquals("Asistencia confirmada", response.message());
        assertEquals(true, checkInExistsByReservationId.get(50L));
    }

    @Test
    void scan_WhenQrIsInvalid_ThrowsUnprocessableEntity() {
        CheckInService checkInService = newService();
        when(qrCodeSigner.verify("bad-qr")).thenThrow(new IllegalArgumentException("invalid"));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> checkInService.scan(new CheckInScanRequest(50L, "bad-qr")));

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, ex.getStatusCode());
        assertEquals("QR inválido", ex.getReason());
    }

    @Test
    void scan_WhenAlreadyCheckedIn_ThrowsConflict() {
        CheckInService checkInService = newService();
        AppUser currentUser = user(10L, "Ana", "Gomez");
        Reservation reservation = reservation(currentUser, ReservationStatus.CONFIRMED);
        QrCodeSigner.ParsedQrPayload payload = new QrCodeSigner.ParsedQrPayload(
                5L,
                LocalDateTime.now().plusMinutes(10).atZone(ZoneId.systemDefault()).toEpochSecond()
        );

        when(qrCodeSigner.verify("qr-content")).thenReturn(payload);
        when(qrCodeSigner.isExpired(payload)).thenReturn(false);
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(reservationRepository.findById(50L)).thenReturn(Optional.of(reservation));
        checkInExistsByReservationId.put(50L, true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> checkInService.scan(new CheckInScanRequest(50L, "qr-content")));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        assertEquals("Ya registraste tu asistencia", ex.getReason());
    }

    private CheckInService newService() {
        CheckInRepository stub = checkInRepositoryStub();
        CheckInService service = new CheckInService(
                currentUserService,
                reservationRepository,
                activityScheduleRepository,
                stub,
                qrCodeSigner
        );
        ReflectionTestUtils.setField(service, "expirationMinutes", 180L);
        return service;
    }

    private CheckInRepository checkInRepositoryStub() {
        InvocationHandler handler = (proxy, method, args) -> switch (method.getName()) {
            case "existsByReservationIdAndStatus" -> checkInExistsByReservationId.getOrDefault((Long) args[0], false);
            case "save" -> {
                CheckIn checkIn = (CheckIn) args[0];
                checkInExistsByReservationId.put(checkIn.getReservation().getId(), true);
                yield checkIn;
            }
            default -> throw new UnsupportedOperationException("Method not supported in test stub: " + method.getName());
        };

        return (CheckInRepository) Proxy.newProxyInstance(
                CheckInRepository.class.getClassLoader(),
                new Class<?>[]{CheckInRepository.class},
                handler
        );
    }

    private Reservation reservation(AppUser user, ReservationStatus status) {
        Activity activity = Activity.builder()
                .id(1L)
                .name("Free Tour Centro Historico")
                .destination(Destination.builder().id(2L).name("Buenos Aires").build())
                .guide(GuideProfile.builder()
                        .id(3L)
                        .user(user(20L, "Maria", "Perez"))
                        .build())
                .category(ActivityCategory.CULTURA)
                .durationMinutes(120)
                .basePrice(BigDecimal.ZERO)
                .currency("ARS")
                .language(ActivityLanguage.SPANISH)
                .meetingPoint("Plaza de Mayo")
                .cancellationPolicy("Cancelacion hasta 24h")
                .build();

        return Reservation.builder()
                .id(50L)
                .user(user)
                .schedule(reservationSchedule(activity))
                .seats(2)
                .totalAmount(BigDecimal.ZERO)
                .status(status)
                .build();
    }

    private ActivitySchedule reservationSchedule() {
        Activity activity = Activity.builder()
                .id(1L)
                .name("Free Tour Centro Historico")
                .destination(Destination.builder().id(2L).name("Buenos Aires").build())
                .guide(GuideProfile.builder()
                        .id(3L)
                        .user(user(20L, "Maria", "Perez"))
                        .build())
                .category(ActivityCategory.CULTURA)
                .durationMinutes(120)
                .basePrice(BigDecimal.ZERO)
                .currency("ARS")
                .language(ActivityLanguage.SPANISH)
                .meetingPoint("Plaza de Mayo")
                .cancellationPolicy("Cancelacion hasta 24h")
                .build();

        return reservationSchedule(activity);
    }

    private ActivitySchedule reservationSchedule(Activity activity) {
        return ActivitySchedule.builder()
                .id(5L)
                .activity(activity)
                .startDateTime(LocalDateTime.now().plusDays(1).truncatedTo(ChronoUnit.MINUTES))
                .endDateTime(LocalDateTime.now().plusDays(1).plusHours(2).truncatedTo(ChronoUnit.MINUTES))
                .price(BigDecimal.ZERO)
                .totalSpots(10)
                .reservedSpots(2)
                .build();
    }

    private AppUser user(Long id, String firstName, String lastName) {
        return AppUser.builder()
                .id(id)
                .firstName(firstName)
                .lastName(lastName)
                .email(firstName.toLowerCase() + "@test.com")
                .enabled(true)
                .build();
    }
}
