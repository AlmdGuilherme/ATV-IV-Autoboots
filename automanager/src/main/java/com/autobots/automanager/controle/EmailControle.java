package com.autobots.automanager.controle;

import com.autobots.automanager.entitades.Email;
import com.autobots.automanager.modelos.AdicionadorLinkEmail;
import com.autobots.automanager.repositorios.RepositorioEmail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/emails")
public class EmailControle {
    @Autowired
    private RepositorioEmail repositorioEmail;
    @Autowired
    private AdicionadorLinkEmail adicionadorLinkEmail;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<List<Email>> listarEmails() {
        List<Email> emails = repositorioEmail.findAll();
        if (emails.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            adicionadorLinkEmail.adicionadorLinkGeral(emails);
            return new ResponseEntity<>(emails, HttpStatus.OK);
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENDEDOR')")
    public ResponseEntity<Email> findEmailById(@PathVariable Long id) {
        Optional<Email> emailAlvo = repositorioEmail.findById(id);
        if (emailAlvo.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            Email email = emailAlvo.get();
            adicionadorLinkEmail.adicionadorLink(email);
            return new ResponseEntity<>(email, HttpStatus.OK);
        }
    }

    @PostMapping("/cadastrar")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENDEDOR')")
    public ResponseEntity<Email> cadastrarEmail(@RequestBody Email email) {
        repositorioEmail.save(email);
        return new ResponseEntity<>(email, HttpStatus.CREATED);
    }

    @PutMapping("/atualizar")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENDEDOR')")
    public ResponseEntity<String> atualizarEmail(@RequestBody Email emailAtualizado) {
        if (emailAtualizado.getId() == null) {
            return new ResponseEntity<>("ID do email é obrigatório", HttpStatus.BAD_REQUEST);
        }

        Optional<Email> emailBancoOpt = repositorioEmail.findById(emailAtualizado.getId());

        if (emailBancoOpt.isPresent()) {
            Email emailBanco = emailBancoOpt.get();
            emailBanco.setEndereco(emailAtualizado.getEndereco());
            repositorioEmail.save(emailBanco);
            return new ResponseEntity<>("Email atualizado com sucesso", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Email não encontrado", HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/excluir/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<String> deletarEmail(@PathVariable long id) {
        if (repositorioEmail.existsById(id)) {
            repositorioEmail.deleteById(id);
            return new ResponseEntity<>("Email deletado com sucesso", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Email não encontrado", HttpStatus.NOT_FOUND);
        }
    }
}