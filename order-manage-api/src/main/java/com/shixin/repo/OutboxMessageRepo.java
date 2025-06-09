package com.shixin.repo;

import com.shixin.po.OutboxMessage;
import com.shixin.po.OutboxMessageStatus;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OutboxMessageRepo extends CrudRepository<OutboxMessage, String> {

    @Query("select o from OutboxMessage o where o.status = :status")
    List<OutboxMessage> findByStatus(@Param("status") OutboxMessageStatus status);
}
