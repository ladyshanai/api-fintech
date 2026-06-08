package com.fintech.api.repository;

import com.fintech.api.dto.ClientRequest;
import com.fintech.api.entity.ClientEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<ClientEntity, Long> {

}
