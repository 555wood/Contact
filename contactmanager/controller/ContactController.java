package com.example.contactmanager.controller;

import com.example.contactmanager.model.Contact;
import com.example.contactmanager.service.ContactService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contacts")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173") // 允许前端开发服务器访问
public class ContactController {
    
    private final ContactService contactService;
    
    @GetMapping
    public List<Contact> getAllContacts() {
        return contactService.getAllContacts();
    }
    
    @GetMapping("/{id}")
    public Contact getContactById(@PathVariable Long id) {
        return contactService.getContactById(id);
    }
    
    @PostMapping
    public Contact createContact(@Valid @RequestBody Contact contact) {
        return contactService.createContact(contact);
    }
    
    @PutMapping("/{id}")
    public Contact updateContact(@PathVariable Long id, @Valid @RequestBody Contact contact) {
        return contactService.updateContact(id, contact);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContact(@PathVariable Long id) {
        contactService.deleteContact(id);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/batch")
    public List<Contact> batchImport(@RequestBody List<@Valid Contact> contacts) {
        return contactService.batchImport(contacts);
    }
} 