package com.autobots.automanager.repositorios;

import com.autobots.automanager.entitades.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RepositorioUsuario extends JpaRepository<Usuario, Long> {
    @Query("SELECT u FROM Usuario u JOIN u.emails e WHERE e.endereco = :email")
    Optional<Usuario> findByEmailEspecifico(@Param("email") String email);
}
