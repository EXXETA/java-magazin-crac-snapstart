package com.exxeta.serverless.controller;


import com.exxeta.serverless.repository.PersonRepository;
import com.exxeta.serverless.repository.model.Person;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
public class PersonController {
    private final PersonRepository personRepository;

    public PersonController(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    @GetMapping("/api/person")
    public List<Person> findAll() {
        return personRepository.findAll();
    }

    @GetMapping("/api/person/{id}")
    public Optional<Person> findById(@PathVariable String id) {
        return personRepository.findById(id);
    }

    @PostMapping("/api/person")
    public Person save(@RequestBody Person person) {
        return personRepository.save(person);
    }

    @PutMapping("/api/person/{id}")
    public Person update(@PathVariable String id, @RequestBody Person person) {
        person.setId(id);
        return personRepository.save(person);
    }

    @DeleteMapping("/api/person/{id}")
    public void deleteById(@PathVariable String id) {
        personRepository.deleteById(id);
    }
}

