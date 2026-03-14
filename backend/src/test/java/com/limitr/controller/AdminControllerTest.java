package com.limitr.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.limitr.domain.Incident;
import com.limitr.repository.IncidentRepository;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class AdminControllerTest {

    @Test
    void activeOnlyReturnsEmptyWhenThereAreNoPersistedActiveBans() {
        AtomicReference<Boolean> queriedActiveIncidents = new AtomicReference<>(false);
        IncidentRepository incidentRepository = incidentRepositoryProxy(queriedActiveIncidents, List.of());
        AdminController controller = new AdminController(null, incidentRepository, null, null);

        Map<String, Object> response = controller.incidents(true);

        assertEquals(List.of(), response.get("items"));
        assertTrue(queriedActiveIncidents.get(), "incident repository should be queried for persisted active bans");
    }

    @Test
    void activeOnlyReturnsPersistedActiveBanStates() {
        Incident incident = new Incident();
        incident.setPrincipalId("still-banned");

        AtomicReference<Boolean> queriedActiveIncidents = new AtomicReference<>(false);
        IncidentRepository incidentRepository = incidentRepositoryProxy(queriedActiveIncidents, List.of(incident));
        AdminController controller = new AdminController(null, incidentRepository, null, null);

        Map<String, Object> response = controller.incidents(true);

        assertEquals(List.of(incident), response.get("items"));
        assertTrue(queriedActiveIncidents.get(), "incident repository should be queried for active bans");
    }

    private static IncidentRepository incidentRepositoryProxy(
        AtomicReference<Boolean> queriedActiveIncidents,
        List<Incident> incidents
    ) {
        return (IncidentRepository) Proxy.newProxyInstance(
            IncidentRepository.class.getClassLoader(),
            new Class<?>[] { IncidentRepository.class },
            (proxy, method, args) -> {
                return switch (method.getName()) {
                    case "findActiveBanStates" -> {
                        queriedActiveIncidents.set(true);
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
