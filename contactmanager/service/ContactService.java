package com.example.contactmanager.service;

import com.example.contactmanager.model.Contact;
import com.example.contactmanager.repository.ContactRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ContactService {
    
    private final ContactRepository contactRepository;
    
    public List<Contact> getAllContacts() {
        return contactRepository.findAll();
    }
    
    public Contact getContactById(Long id) {
        return contactRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Contact not found with id: " + id));
    }
    
    public Contact createContact(Contact contact) {
        return contactRepository.save(contact);
    }
    
    public Contact updateContact(Long id, Contact contact) {
        Contact existingContact = getContactById(id);
        existingContact.setName(contact.getName());
        existingContact.setPhone(contact.getPhone());
        existingContact.setEmail(contact.getEmail());
        existingContact.setSocialMedia(contact.getSocialMedia());
        existingContact.setFavorite(contact.isFavorite());
        return contactRepository.save(existingContact);
    }
    
    public void deleteContact(Long id) {
        contactRepository.deleteById(id);
    }
    
    @Transactional
    public List<Contact> batchImport(List<Contact> contacts) {
        return contactRepository.saveAll(contacts);
    }
} 