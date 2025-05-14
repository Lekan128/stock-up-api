package com.business.business.store;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class StoreService {
    private final StoreRepository storeRepository;

    public Store createStore(@NotBlank StoreDto storeDto) {
        Store store = new Store();
        store.name = storeDto.name();
        store.address = storeDto.address();
        return storeRepository.save(store);
    }

    public List<Store> getAllStores() {
        return storeRepository.findAll();
    }

    public Store getStoreById(UUID id) {
        return storeRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Store not found"));
    }

    public Store getStoreByName(String name) {
        return storeRepository.findByName(name).orElseThrow(() -> new IllegalArgumentException("Store not found"));
    }

    public boolean existsByName(String name) {
        return storeRepository.existsByName(name);
    }

    public Store updateStore(UUID id, StoreDto dto) {
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Store not found"));
        store.name = dto.name();
        store.address = dto.address();
        return storeRepository.save(store);
    }

    public void deleteStore(UUID id) {
        if (!storeRepository.existsById(id)) {
            throw new IllegalArgumentException("Store not found");
        }
        storeRepository.deleteById(id);
    }
}
