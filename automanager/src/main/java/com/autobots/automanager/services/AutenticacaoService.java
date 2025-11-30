package com.autobots.automanager.services;

import com.autobots.automanager.entitades.Credencial;
import com.autobots.automanager.entitades.CredencialUsuarioSenha;
import com.autobots.automanager.entitades.Usuario;
import com.autobots.automanager.repositorios.RepositorioUsuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class AutenticacaoService implements UserDetailsService {
    @Autowired
    private RepositorioUsuario userRepo;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario_banco = userRepo.findByEmailEspecifico(email).orElseThrow(() -> new UsernameNotFoundException("Email não encontrado: " + email));
        String senha = usuario_banco.getCredenciais().stream()
                .filter(credencial -> credencial instanceof CredencialUsuarioSenha)
                .map(c -> (CredencialUsuarioSenha) c)
                .filter(c -> !c.isInativo())
                .findFirst()
                .map(CredencialUsuarioSenha::getSenha)
                .orElseThrow(() -> new UsernameNotFoundException("Nenhuma credencial de senha encontrada!"));

        return new org.springframework.security.core.userdetails.User(
                email,
                senha,
                usuario_banco.getNivelAcesso().stream()
                        .map(role -> new SimpleGrantedAuthority(role.name()))
                        .collect(Collectors.toList())
        );
    }
}
