package org.udesa.giftcards.model;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MerchantService extends ModelService<MerchantVault, MerchantRepository> {
    private final String invalidMerchantException = "Merchant no registrado";
    @Override
    protected void updateData(MerchantVault existingObject, MerchantVault updatedObject) {
        existingObject.setName(updatedObject.getName());
    }

    @Transactional(readOnly = true)
    public MerchantVault findByName(String name) {
        return repository.findByName(name)
                .orElseThrow(() -> new RuntimeException(invalidMerchantException));
    }

    @Transactional(readOnly = true)
    public boolean exists(String name) {
        return repository.findByName(name).isPresent();
    }
}
