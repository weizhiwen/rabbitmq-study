package com.shixin.repo;

import com.shixin.po.InboxMessage;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InboxMessageRepo extends CrudRepository<InboxMessage, String> {
}
