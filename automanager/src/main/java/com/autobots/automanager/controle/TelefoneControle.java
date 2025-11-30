package com.autobots.automanager.controle;

import com.autobots.automanager.entitades.Telefone;
import com.autobots.automanager.modelos.AdicionadorLinkTelefone;
import com.autobots.automanager.repositorios.RepositorioTelefone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/telefones")
public class TelefoneControle {
    @Autowired
    private RepositorioTelefone repositorioTelefone;
    @Autowired
    private AdicionadorLinkTelefone adicionadorLinkTelefone;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<List<Telefone>> listarTelefones() {
        List<Telefone> telefones = repositorioTelefone.findAll();
        if (telefones.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            adicionadorLinkTelefone.adicionadorLinkGeral(telefones);
            return new ResponseEntity<>(telefones, HttpStatus.OK);
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENDEDOR')")
    public ResponseEntity<Telefone> buscarTelefonePorId(@PathVariable long id) {
        Optional<Telefone> telefoneOpt = repositorioTelefone.findById(id);
        if (telefoneOpt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            Telefone telefone = telefoneOpt.get();
            adicionadorLinkTelefone.adicionadorLink(telefone);
            return new ResponseEntity<>(telefone, HttpStatus.OK);
        }
    }

    @PostMapping("/cadastrar")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENDEDOR')")
    public ResponseEntity<String> cadastrarTelefone(@RequestBody Telefone telefone) {
        if (telefone != null) {
            repositorioTelefone.save(telefone);
            return new ResponseEntity<>("Telefone cadastrado com sucesso!", HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>("Não foi possível realizar o cadastro!", HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/atualizar/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENDEDOR')")
    public ResponseEntity<String> editarTelefone(@PathVariable long id, @RequestBody Telefone telefone) {
        Optional<Telefone> telefoneBancoOpt = repositorioTelefone.findById(id);

        if (telefoneBancoOpt.isPresent()) {
            Telefone telefoneBanco = telefoneBancoOpt.get();
            telefoneBanco.setDdd(telefone.getDdd());
            telefoneBanco.setNumero(telefone.getNumero());
            repositorioTelefone.save(telefoneBanco);
            return new ResponseEntity<>("Telefone atualizado com sucesso!", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Nenhum telefone encontrado no banco!", HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/excluir/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<String> excluirTelefone(@PathVariable long id) {
        if (repositorioTelefone.existsById(id)) {
            repositorioTelefone.deleteById(id);
            return new ResponseEntity<>("Telefone deletado com sucesso!", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Não foi possível encontrar o telefone", HttpStatus.NOT_FOUND);
        }
    }
}