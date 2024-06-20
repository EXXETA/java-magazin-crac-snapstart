package com.exxeta.serverless.repository;

import com.exxeta.serverless.repository.model.Person;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonRepository extends ListCrudRepository<Person, String> {
}

