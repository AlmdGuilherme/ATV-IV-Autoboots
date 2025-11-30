package com.autobots.automanager.controle;

import com.autobots.automanager.dto.LoginRequest;
import com.autobots.automanager.entitades.Usuario;
import com.autobots.automanager.repositorios.RepositorioUsuario;
import com.autobots.automanager.services.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoginController {
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private TokenService tokenService;
    @Autowired
    private RepositorioUsuario repositorioUsuario;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest dados) {
        UsernamePasswordAuthenticationToken tokenLogin =
                new UsernamePasswordAuthenticationToken(dados.getEmail(), dados.getSenha());

        Authentication authentication = authenticationManager.authenticate(tokenLogin);
        String email = ((org.springframework.security.core.userdetails.User) authentication.getPrincipal()).getUsername();
        Usuario usuarioReal = repositorioUsuario.findByEmailEspecifico(email).orElseThrow();
        String tokenJwt = tokenService.gerarToken(usuarioReal);
        return ResponseEntity.ok(tokenJwt);
    }
}
