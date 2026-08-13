package com.bookstore.service;

import com.bookstore.dto.response.GenreResponse;
import com.bookstore.mapper.GenreMapper;
import com.bookstore.repository.GenreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for the genre catalogue.
 */
@Service
@RequiredArgsConstructor
public class GenreService {

    private final GenreRepository genreRepository;
    private final GenreMapper     genreMapper;

    /**
     * Return all genres ordered alphabetically.
     *
     * @return list of all genres
     */
    @Transactional(readOnly = true)
    public List<GenreResponse> findAll() {
        return genreRepository.findAllByOrderByNameAsc()
                .stream()
                .map(genreMapper::toResponse)
                .collect(Collectors.toList());
    }
}
