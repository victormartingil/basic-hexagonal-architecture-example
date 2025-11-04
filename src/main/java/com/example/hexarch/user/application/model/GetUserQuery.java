package com.example.hexarch.user.application.model;

import java.util.UUID;

/**
 * APPLICATION LAYER - Query (CQRS Read)
 *
 * Representa la intención de obtener un usuario por su ID.
 *
 * DIFERENCIA CON COMMAND:
 * - Query: operación de LECTURA (no modifica estado)
 * - Command: operación de ESCRITURA (modifica estado)
 *
 * NOMENCLATURA:
 * - Queries: Get{Entity}Query
 * - Commands: {Action}{Entity}Command
 *
 * CQRS (Command Query Responsibility Segregation):
 * - Separa operaciones de lectura (Query) de escritura (Command)
 * - Permite optimizar cada una independientemente
 * - En arquitecturas avanzadas, pueden usar diferentes BDs (write DB vs read DB)
 *
 * @param userId ID del usuario a buscar
 */
public record GetUserQuery(UUID userId) {
}
