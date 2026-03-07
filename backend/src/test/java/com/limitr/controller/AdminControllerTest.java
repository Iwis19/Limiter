package com.limitr.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.limitr.domain.Incident;
import com.limitr.repository.IncidentRepository;
import com.limitr.service.EnforcementService;
import java.lang.reflect.Proxy;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class AdminControllerTest {

    @Test
    void activeOnlyReturnsEmptyWhenNoBansAreCurrentlyActive() {
        AtomicReference<Boolean> queriedActiveIncidents = new AtomicReference<>(false);
        IncidentRepository incidentRepository = incidentRepositoryProxy(queriedActiveIncidents, new AtomicReference<>(), List.of());
        EnforcementService enforcementService = enforcementServiceStub(Map.of());
        AdminController controller = new AdminController(null, incidentRepository, null, enforcementService);

        Map<String, Object> response = controller.incidents(true);

        assertEquals(List.of(), response.get("items"));
        assertTrue(!queriedActiveIncidents.get(), "incident repository should not be queried when there are no active bans");
    }

    @Test
    void activeOnlyFiltersIncidentsToCurrentActiveBanPrincipals() {
        Incident incident = new Incident();
        incident.setPrincipalId("still-banned");
        incident.setExpiresAt(Instant.now().plusSeconds(60));

        AtomicReference<Boolean> queriedActiveIncidents = new AtomicReference<>(false);
        AtomicReference<Collection<String>> queriedPrincipals = new AtomicReference<>();
        IncidentRepository incidentRepository = incidentRepositoryProxy(
            queriedActiveIncidents,
            queriedPrincipals,
            List.of(incident)
        );
        EnforcementService enforcementService = enforcementServiceStub(Map.of("still-banned", Instant.now().plusSeconds(60)));
        AdminController controller = new AdminController(null, incidentRepository, null, enforcementService);

        Map<String, Object> response = controller.incidents(true);

        assertEquals(List.of(incident), response.get("items"));
        assertTrue(queriedActiveIncidents.get(), "incident repository should be queried for active bans");
        assertEquals(Set.of("still-banned"), Set.copyOf(queriedPrincipals.get()));
    }

    private static EnforcementService enforcementServiceStub(Map<String, Instant> activeBans) {
        return new EnforcementService(null, null) {
            @Override
            public Map<String, Instant> getActiveBans() {
                return activeBans;
            }
        };
    }

    private static IncidentRepository incidentRepositoryProxy(
        AtomicReference<Boolean> queriedActiveIncidents,
        AtomicReference<Collection<String>> queriedPrincipals,
        List<Incident> incidents
    ) {
        return (IncidentRepository) Proxy.newProxyInstance(
            IncidentRepository.class.getClassLoader(),
            new Class<?>[] { IncidentRepository.class },
            (proxy, method, args) -> {
                return switch (method.getName()) {
                    case "findByPrincipalIdInAndExpiresAtAfterOrderByTimestampDesc" -> {
                        queriedActiveIncidents.set(true);
                        queriedPrincipals.set((Collection<String>) args[0]);
                        yield incidents;
                    }
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    case "toString" -> "IncidentRepositoryTestProxy";
                    default -> throw new UnsupportedOperationException("Unexpected repository method: " + method.getName());
                };
            }
        );
    }
}
