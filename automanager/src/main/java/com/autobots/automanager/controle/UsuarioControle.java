package com.autobots.automanager.controle;

import com.autobots.automanager.entitades.Usuario;
import com.autobots.automanager.enumeracoes.PerfilUsuario;
import com.autobots.automanager.enumeracoes.UserRoles;
import com.autobots.automanager.modelos.AdicionadorLinkUsuario;
import com.autobots.automanager.modelos.selecionadores.UsuarioSelecionador;
import com.autobots.automanager.repositorios.RepositorioUsuario;
import com.autobots.automanager.services.GlobalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/usuarios")
public class UsuarioControle {
    @Autowired
    private RepositorioUsuario repositorioUsuario;
    @Autowired
    private UsuarioSelecionador selecionadorUsuario;
    @Autowired
    private AdicionadorLinkUsuario adicionadorLinkUsuario;
    @Autowired
    private GlobalService service;

    private Usuario getUsuarioLogado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return repositorioUsuario.findByEmailEspecifico(email).orElseThrow();
    }

    private boolean isAdminOuGerente(Usuario usuario) {
        return usuario.getNivelAcesso().contains(UserRoles.ROLE_ADMIN) ||
                usuario.getNivelAcesso().contains(UserRoles.ROLE_GERENTE);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<List<Usuario>> listarUsuarios() {
        List<Usuario> usuarios = repositorioUsuario.findAll();
        if (usuarios.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            adicionadorLinkUsuario.adicionadorLinkGeral(usuarios);
            return new ResponseEntity<>(usuarios, HttpStatus.OK);
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Usuario> buscarUsuarioPorId(@PathVariable long id) {
        Usuario logado = getUsuarioLogado();
        Optional<Usuario> usuarioAlvo = repositorioUsuario.findById(id);

        if (usuarioAlvo.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        if (!isAdminOuGerente(logado) && !logado.getId().equals(id)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        Usuario usuario = usuarioAlvo.get();
        adicionadorLinkUsuario.adicionadorLink(usuario);
        return new ResponseEntity<>(usuario, HttpStatus.OK);
    }

    @PostMapping("/cadastrar-cliente")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENDEDOR')")
    public ResponseEntity<Usuario> cadastrarCliente(@RequestBody Usuario usuario) {
        if (usuario == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } else {
            usuario.getPerfis().add(PerfilUsuario.CLIENTE);
            usuario.getNivelAcesso().add(UserRoles.ROLE_CLIENTE);
            repositorioUsuario.save(usuario);
            return new ResponseEntity<>(usuario, HttpStatus.OK);
        }
    }

    @PostMapping("/cadastrar-fornecedor")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<Usuario> cadastrarFornecedor(@RequestBody Usuario usuario) {
        if (usuario == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } else {
            usuario.getPerfis().add(PerfilUsuario.FORNECEDOR);
            repositorioUsuario.save(usuario);
            return new ResponseEntity<>(usuario, HttpStatus.OK);
        }
    }

    @PostMapping("/cadastrar-funcionario")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<Usuario> cadastrarFuncionario(@RequestBody Usuario usuario) {
        if (usuario == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } else {
            usuario.getPerfis().add(PerfilUsuario.FUNCIONARIO);
            usuario.getNivelAcesso().add(UserRoles.ROLE_VENDEDOR);
            repositorioUsuario.save(usuario);
            return new ResponseEntity<>(usuario, HttpStatus.OK);
        }
    }

    @PostMapping("/cadastrar-gerente")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Usuario> cadastrarGerente(@RequestBody Usuario usuario) {
        if (usuario == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } else {
            usuario.getPerfis().add(PerfilUsuario.FUNCIONARIO);
            usuario.getNivelAcesso().add(UserRoles.ROLE_GERENTE);
            repositorioUsuario.save(usuario);
            return new ResponseEntity<>(usuario, HttpStatus.OK);
        }
    }

    @PutMapping("/atualizar/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Usuario> atualizarUsuario(@PathVariable long id, @RequestBody Usuario usuario) {
        Usuario logado = getUsuarioLogado();
        Optional<Usuario> usuarioAlvo = repositorioUsuario.findById(id);

        if (usuarioAlvo.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        if (!isAdminOuGerente(logado) && !logado.getId().equals(id)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        Usuario usuarioSelecionado = usuarioAlvo.get();
        if (usuario.getNome() != null) usuarioSelecionado.setNome(usuario.getNome());
        if (usuario.getNomeSocial() != null) usuarioSelecionado.setNomeSocial(usuario.getNomeSocial());

        repositorioUsuario.save(usuarioSelecionado);
        return new ResponseEntity<>(usuarioSelecionado, HttpStatus.OK);
    }

    @DeleteMapping("/excluir/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<String> deletarUsuario(@PathVariable long id) {
        Usuario logado = getUsuarioLogado();
        Optional<Usuario> usuarioAlvo = repositorioUsuario.findById(id);

        if (usuarioAlvo.isEmpty()) {
            return new ResponseEntity<>("Usuário não encontrado", HttpStatus.NOT_FOUND);
        }

        Usuario usuarioParaDeletar = usuarioAlvo.get();

        if (logado.getNivelAcesso().contains(UserRoles.ROLE_GERENTE) &&
                !logado.getNivelAcesso().contains(UserRoles.ROLE_ADMIN)) {
            if (usuarioParaDeletar.getNivelAcesso().contains(UserRoles.ROLE_ADMIN)) {
                return new ResponseEntity<>("Gerente não pode excluir Administrador", HttpStatus.FORBIDDEN);
            }
        }

        service.removeUserFromSale(usuarioParaDeletar.getId());
        service.removeUserFromVehicle(usuarioParaDeletar.getId());
        repositorioUsuario.delete(usuarioParaDeletar);
        return new ResponseEntity<>("Usuário deletado com sucesso!", HttpStatus.OK);
    }
}