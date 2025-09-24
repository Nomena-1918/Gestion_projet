package com.example.films.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class CreateComedienDTO {
    private Long projetId;
    private String nom;
    private Integer age;
    private String email;
    private String photoPath;
    private LocalDate dateDisponibilite;
    private String statutDisponibilite;
}