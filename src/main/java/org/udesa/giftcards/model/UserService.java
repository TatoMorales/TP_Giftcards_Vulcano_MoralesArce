package org.udesa.giftcards.model;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService extends ModelService<UserVault, UserRepository> {

    @Override protected void updateData(UserVault existingObject, UserVault updatedObject) {
        existingObject.setName(updatedObject.getName());
        existingObject.setPassword(updatedObject.getPassword());
    }

    @Transactional(readOnly = true)
    public UserVault findByName(String name) {
        // Tira una exception si no existe
        return repository.findByName(name)
                .orElseThrow(() -> new RuntimeException("InvalidUser"));
    }
}