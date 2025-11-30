package com.autobots.automanager.controle;

import com.autobots.automanager.entitades.CredencialCodigoBarra;
import com.autobots.automanager.entitades.CredencialUsuarioSenha;
import com.autobots.automanager.entitades.Usuario;
import com.autobots.automanager.modelos.AdicionadorLinkCredenciaisUsuario;
import com.autobots.automanager.modelos.AdicionadorLinkCredencialCodigoBarras;
import com.autobots.automanager.repositorios.RepositorioCredencialCodigoBarra;
import com.autobots.automanager.repositorios.RepositorioCredencialUsuario;
import com.autobots.automanager.repositorios.RepositorioUsuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/credenciais")
public class CredencialControle {
    @Autowired
    private RepositorioUsuario repositorioUsuario;
    @Autowired
    private RepositorioCredencialUsuario repositorioCredencialUsuario;
    @Autowired
    private AdicionadorLinkCredenciaisUsuario adicionadorLinkCredenciaisUsuario;
    @Autowired
    private RepositorioCredencialCodigoBarra repositorioCredencialCodigoBarra;
    @Autowired
    private AdicionadorLinkCredencialCodigoBarras adicionadorLinkCredencialCodigoBarras;

    @GetMapping("/credenciais-usuarios")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<List<CredencialUsuarioSenha>> buscarCredenciaisUsuarios() {
        List<CredencialUsuarioSenha> credenciais = repositorioCredencialUsuario.findAll();
        if (credenciais.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            adicionadorLinkCredenciaisUsuario.adicionadorLinkGeral(credenciais);
            return new ResponseEntity<>(credenciais, HttpStatus.OK);
        }
    }

    @GetMapping("/credenciais-usuarios/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<CredencialUsuarioSenha> buscarCredenciaisUsuariosId(@PathVariable Long id) {
        Optional<CredencialUsuarioSenha> credencialOpt = repositorioCredencialUsuario.findById(id);
        if (credencialOpt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            CredencialUsuarioSenha credencial = credencialOpt.get();
            adicionadorLinkCredenciaisUsuario.adicionadorLink(credencial);
            return new ResponseEntity<>(credencial, HttpStatus.OK);
        }
    }

    @PostMapping("/cadastrar-credencial/{idUsuario}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<?> cadastrarCredencialUsuario(@PathVariable long idUsuario, @RequestBody CredencialUsuarioSenha credencial) {
        Optional<Usuario> usuarioOpt = repositorioUsuario.findById(idUsuario);
        if (usuarioOpt.isEmpty()) {
            return new ResponseEntity<>("Usuário não encontrado.", HttpStatus.NOT_FOUND);
        }

        List<CredencialUsuarioSenha> todasCredenciais = repositorioCredencialUsuario.findAll();
        boolean existe = todasCredenciais.stream()
                .anyMatch(c -> c.getNomeUsuario().equals(credencial.getNomeUsuario()));

        if (existe) {
            return new ResponseEntity<>("Nome de usuário já existe!", HttpStatus.CONFLICT);
        } else {
            Usuario usuario = usuarioOpt.get();
            credencial.setCriacao(new Date());
            credencial.setUltimoAcesso(new Date());
            credencial.setInativo(false);

            usuario.getCredenciais().add(credencial);
            repositorioUsuario.save(usuario);
            return new ResponseEntity<>("Credenciais cadastradas com sucesso!", HttpStatus.CREATED);
        }
    }

    @PutMapping("/atualizar-credencial/{idCredencial}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<?> atualizarCredencialUsuario(@PathVariable long idCredencial, @RequestBody CredencialUsuarioSenha credencialAtualizada) {
        Optional<CredencialUsuarioSenha> credencialOpt = repositorioCredencialUsuario.findById(idCredencial);

        if (credencialOpt.isEmpty()) {
            return new ResponseEntity<>("Credencial não encontrada!", HttpStatus.NOT_FOUND);
        }

        if (credencialAtualizada == null || credencialAtualizada.getNomeUsuario() == null || credencialAtualizada.getSenha() == null) {
            return new ResponseEntity<>("Dados inválidos para atualização!", HttpStatus.BAD_REQUEST);
        }

        CredencialUsuarioSenha credencialExistente = credencialOpt.get();
        credencialExistente.setNomeUsuario(credencialAtualizada.getNomeUsuario());
        credencialExistente.setSenha(credencialAtualizada.getSenha());

        repositorioCredencialUsuario.save(credencialExistente);
        return new ResponseEntity<>("Credencial atualizada com sucesso!", HttpStatus.OK);
    }

    @DeleteMapping("/deletar-credencial/{idCredencial}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deletarCredencialUsuario(@PathVariable long idCredencial) {
        Optional<CredencialUsuarioSenha> credencialOpt = repositorioCredencialUsuario.findById(idCredencial);
        if (credencialOpt.isEmpty()) {
            return new ResponseEntity<>("Credencial não encontrada!", HttpStatus.NOT_FOUND);
        } else {
            repositorioCredencialUsuario.delete(credencialOpt.get());
            return new ResponseEntity<>("Credencial deletada com sucesso!", HttpStatus.OK);
        }
    }

    @GetMapping("/credenciais-codigo-barras")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<List<CredencialCodigoBarra>> buscarCredenciaisCodigoBarra() {
        List<CredencialCodigoBarra> credencialCodigoBarras = repositorioCredencialCodigoBarra.findAll();
        if (credencialCodigoBarras.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            adicionadorLinkCredencialCodigoBarras.adicionadorLinkGeral(credencialCodigoBarras);
            return new ResponseEntity<>(credencialCodigoBarras, HttpStatus.OK);
        }
    }

    @GetMapping("/credenciais-codigo-barras/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<CredencialCodigoBarra> buscarCredenciaisCodigoBarraId(@PathVariable Long id) {
        Optional<CredencialCodigoBarra> credencialOpt = repositorioCredencialCodigoBarra.findById(id);
        if (credencialOpt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            CredencialCodigoBarra credencial = credencialOpt.get();
            adicionadorLinkCredencialCodigoBarras.adicionadorLink(credencial);
            return new ResponseEntity<>(credencial, HttpStatus.OK);
        }
    }

    @PostMapping("/cadastrar-credencial-codigo/{idUsuario}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<?> cadastrarCredencialCodigoBarra(@PathVariable long idUsuario, @RequestBody CredencialCodigoBarra credencial) {
        Optional<Usuario> usuarioOpt = repositorioUsuario.findById(idUsuario);
        if (usuarioOpt.isEmpty()) {
            return new ResponseEntity<>("Usuário não encontrado.", HttpStatus.NOT_FOUND);
        }

        List<CredencialCodigoBarra> todasCredenciais = repositorioCredencialCodigoBarra.findAll();
        boolean existe = todasCredenciais.stream()
                .anyMatch(c -> c.getCodigo() == credencial.getCodigo());

        if (existe) {
            return new ResponseEntity<>("Código de barras já existe!", HttpStatus.CONFLICT);
        } else {
            Usuario usuario = usuarioOpt.get();
            credencial.setCriacao(new Date());
            credencial.setUltimoAcesso(new Date());
            credencial.setInativo(false);

            usuario.getCredenciais().add(credencial);
            repositorioUsuario.save(usuario);
            return new ResponseEntity<>("Credenciais cadastradas com sucesso!", HttpStatus.CREATED);
        }
    }

    @PutMapping("/atualizar-credencial-codigo/{idCredencial}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<?> atualizarCredencialCodigoBarra(@PathVariable long idCredencial, @RequestBody CredencialCodigoBarra credencialAtualizada) {
        Optional<CredencialCodigoBarra> credencialOpt = repositorioCredencialCodigoBarra.findById(idCredencial);

        if (credencialOpt.isEmpty()) {
            return new ResponseEntity<>("Credencial não encontrada!", HttpStatus.NOT_FOUND);
        }

        if (credencialAtualizada != null && credencialAtualizada.getCodigo() != 0) {
            CredencialCodigoBarra credencialExistente = credencialOpt.get();
            credencialExistente.setCodigo(credencialAtualizada.getCodigo());
            repositorioCredencialCodigoBarra.save(credencialExistente);
            return new ResponseEntity<>("Credencial atualizada com sucesso!", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Dados inválidos para atualização!", HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/deletar-credencial-codigo/{idCredencial}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deletarCredencialCodigoBarra(@PathVariable long idCredencial) {
        Optional<CredencialCodigoBarra> credencialOpt = repositorioCredencialCodigoBarra.findById(idCredencial);
        if (credencialOpt.isEmpty()) {
            return new ResponseEntity<>("Credencial não encontrada!", HttpStatus.NOT_FOUND);
        } else {
            repositorioCredencialCodigoBarra.delete(credencialOpt.get());
            return new ResponseEntity<>("Credencial deletada com sucesso!", HttpStatus.OK);
        }
    }
}