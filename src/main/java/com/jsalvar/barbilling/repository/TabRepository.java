package com.jsalvar.barbilling.repository;
import com.jsalvar.barbilling.entity.Tab;
import com.jsalvar.barbilling.entity.enums.TabStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TabRepository extends JpaRepository<Tab, String> {
    List<Tab> findByStatus(TabStatus status);

    @Query("""
        SELECT t FROM Tab t
        WHERE (:tableId IS NULL OR t.barTable.id = :tableId)
        AND (:waiterId IS NULL OR t.waiter.id = :waiterId)
        AND (:status IS NULL OR t.status = :status)
        AND (CAST(:from AS date) IS NULL OR t.openedAt >= :from)
        AND (CAST(:to AS date) IS NULL OR t.openedAt <= :to)
    """)
    List<Tab> searchTab(
            @Param("tableId") String tableId,
            @Param("waiterId") String waiterId,
            @Param("status") TabStatus status,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );

}
