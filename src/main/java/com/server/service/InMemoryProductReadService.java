package com.server.service;

import com.server.model.Product;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class InMemoryProductReadService implements ProductReadService {

    private final ProductState state;

    public InMemoryProductReadService(final ProductState state) {
        Objects.requireNonNull(state);
        this.state = state;
    }

    @Override
    public Map<Integer, Product> findAllById(final Set<Integer> ids) {
        return ids.stream()
                .filter(id -> state.getData().containsKey(id))
                .collect(Collectors.toMap(id -> id, id -> state.getData().get(id)));
    }
}
