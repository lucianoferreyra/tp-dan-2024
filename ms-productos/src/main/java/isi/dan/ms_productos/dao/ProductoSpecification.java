package isi.dan.ms_productos.dao;

import org.springframework.data.jpa.domain.Specification;

import isi.dan.ms_productos.modelo.Producto;
import jakarta.persistence.criteria.Predicate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ProductoSpecification {

    public static Specification<Producto> withFilters(
            String nombre,
            BigDecimal precioMin,
            BigDecimal precioMax,
            Integer stockMin,
            Integer stockMax) {

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filtro por nombre (búsqueda parcial, case-insensitive)
            if (nombre != null && !nombre.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("nombre")),
                        "%" + nombre.toLowerCase() + "%"));
            }

            // Filtro por precio mínimo
            if (precioMin != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("precio"), precioMin));
            }

            // Filtro por precio máximo
            if (precioMax != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("precio"), precioMax));
            }

            // Filtro por stock mínimo
            if (stockMin != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("stockActual"), stockMin));
            }

            // Filtro por stock máximo
            if (stockMax != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("stockActual"), stockMax));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
