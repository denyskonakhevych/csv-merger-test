package com.server.service;

import com.server.model.Product;

import java.util.Map;
import java.util.Set;

public interface ProductReadService {

    Map<Integer, Product> findAllById(Set<Integer> ids);
}
