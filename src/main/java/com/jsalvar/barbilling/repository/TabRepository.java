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

    @Query(value = """
        SELECT * FROM tabs t
        WHERE (:tableId IS NULL OR t.table_id = CAST(:tableId AS varchar))
        AND (:waiterId IS NULL OR t.user_id = CAST(:waiterId AS varchar))
        AND (CAST(:status AS varchar) IS NULL OR t.tab_status = CAST(:status AS varchar))
        AND (CAST(:from AS date) IS NULL OR t.opened_at >= CAST(:from AS date))
        AND (CAST(:to AS date) IS NULL OR t.opened_at <= CAST(:to AS date))
    """, nativeQuery = true)
    List<Tab> searchTab(
            @Param("tableId") String tableId,
            @Param("waiterId") String waiterId,
            @Param("status") TabStatus status,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );

}
