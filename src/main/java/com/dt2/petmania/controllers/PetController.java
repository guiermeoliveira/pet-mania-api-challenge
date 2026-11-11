package com.dt2.petmania.controllers;

import com.dt2.petmania.entities.Pet;
import com.dt2.petmania.repositories.PetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Contract;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pets")
@CrossOrigin(origins = "*")
public class PetController {

    private final PetRepository repository;

    @Autowired
    public PetController(PetRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Pet> mostrar() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Pet> buscarPorId(@PathVariable Integer id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Pet> criar(@RequestBody Pet p) {
        Pet novoPet = repository.save(p);
        return ResponseEntity.status(HttpStatus.CREATED).body(novoPet);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Integer id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}