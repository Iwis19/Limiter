package com.limitr.repository;

import com.limitr.domain.Incident;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface IncidentRepository extends JpaRepository<Incident, Long> {
    List<Incident> findTop200ByOrderByTimestampDesc();
    List<Incident> findByExpiresAtAfter(Instant now);
    List<Incident> findByPrincipalIdInAndExpiresAtAfterOrderByTimestampDesc(Collection<String> principalIds, Instant now);
    Optional<Incident> findTopByPrincipalIdOrderByTimestampDesc(String principalId);

    @Query(
        "select i from Incident i " +
        "where i.actionTaken = 'TEMP_BANNED' and i.expiresAt > :now " +
        "and i.timestamp = (" +
        "    select max(i2.timestamp) from Incident i2 where i2.principalId = i.principalId" +
        ")"
    )
    List<Incident> findActiveBanStates(@Param("now") Instant now);

    @Query(
        "select i.principalId as principalId, count(i) as incidents " +
        "from Incident i where i.timestamp >= :since group by i.principalId order by count(i) desc"
    )
    List<TopOffenderProjection> findTopOffenders(@Param("since") Instant since);

    interface TopOffenderProjection {
        String getPrincipalId();
        Long getIncidents();
    }
}
